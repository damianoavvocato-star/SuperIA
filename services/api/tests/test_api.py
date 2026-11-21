import os
import pytest

from fastapi.testclient import TestClient

from app.device_auth import issue_device_token, validate_device_token
from app.main import MAX_API_BODY_BYTES, app, get_provider


class FakeProvider:
    async def chat(self, messages, model=None):
        return {"content": "Risposta di prova", "model": model or "fake-local"}

    async def list_models(self):
        return ["fake-local"]


@pytest.fixture
def auth_environment(tmp_path, monkeypatch):
    database = str(tmp_path / "auth.sqlite3")
    monkeypatch.setenv("SUPERIA_AUTH_DB", database)
    monkeypatch.setenv("SUPERIA_PAIRING_CODE", "pairing-secret-code-123456")
    monkeypatch.setenv("SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED", "true")
    monkeypatch.setenv("SUPERIA_ADMIN_TOKEN", "admin-secret-token-123456")
    app.dependency_overrides[get_provider] = lambda: FakeProvider()
    token_data = issue_device_token(database, "test-device")
    yield database, token_data
    app.dependency_overrides.pop(get_provider, None)


def test_health():
    response = TestClient(app).get("/health")
    assert response.status_code == 200 and response.json()["status"] == "ok"


def test_device_pairing_issues_unique_token(auth_environment):
    database, _ = auth_environment
    client = TestClient(app)
    response = client.post("/api/v1/device/pair", json={
        "pairing_code": "pairing-secret-code-123456",
        "device_name": "Realme",
    })
    assert response.status_code == 200
    token = response.json()["access_token"]
    assert validate_device_token(database, token)
    assert response.json()["device_id"]


def test_device_pairing_rejects_wrong_code(auth_environment):
    response = TestClient(app).post("/api/v1/device/pair", json={
        "pairing_code": "incorrect-pairing-code",
        "device_name": "Unknown",
    })
    assert response.status_code == 401


def test_device_token_can_be_revoked(auth_environment):
    database, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer admin-secret-token-123456"})
    response = client.post("/api/v1/device/revoke", json={"device_id": token_data["device_id"]})
    assert response.status_code == 200 and response.json()["revoked"] is True
    assert not validate_device_token(database, token_data["access_token"])
    protected = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    assert protected.get("/api/v1/models").status_code == 401


def test_revoke_requires_admin_token(auth_environment):
    response = TestClient(app).post("/api/v1/device/revoke", json={"device_id": "00000000-0000-0000-0000-000000000000"})
    assert response.status_code == 401


def test_capabilities_keep_cloud_disabled(auth_environment):
    database, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    response = client.get("/api/v1/capabilities")
    assert response.status_code == 200
    assert response.json()["external_providers_enabled_by_default"] is False
    assert response.json()["paid_fallback"] is False


def test_models_and_chat_require_device_token(auth_environment):
    _, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    assert client.get("/api/v1/models").status_code == 200
    response = client.post("/api/v1/chat", json={"messages": [{"role": "user", "content": "Ciao"}]})
    assert response.status_code == 200 and response.json()["content"] == "Risposta di prova"


def test_api_rejects_missing_or_invalid_device_token(auth_environment):
    TestClient(app)
    assert TestClient(app).get("/api/v1/models").status_code == 401
    assert TestClient(app, headers={"Authorization": "Bearer wrong-secret"}).get("/api/v1/models").status_code == 401


def test_api_fails_closed_when_auth_database_is_not_configured(monkeypatch):
    monkeypatch.delenv("SUPERIA_AUTH_DB", raising=False)
    monkeypatch.setenv("SUPERIA_PAIRING_CODE", "pairing-secret-code-123456")
    response = TestClient(app).post("/api/v1/device/pair", json={
        "pairing_code": "pairing-secret-code-123456",
        "device_name": "Test",
    })
    assert response.status_code == 503


def test_api_rejects_body_over_hard_limit(auth_environment):
    _, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    response = client.post(
        "/api/v1/chat",
        content=b"x" * (MAX_API_BODY_BYTES + 1),
        headers={"Content-Type": "application/json"},
    )
    assert response.status_code == 413


def test_home_page():
    response = TestClient(app).get("/")
    assert response.status_code == 200 and "SuperIA" in response.text
    assert "localStorage" in response.text
    assert "Nuova conversazione" in response.text
    assert "Memoria personale locale" in response.text
    assert "MEMORY_KEY" in response.text


def test_pairing_fails_closed_without_confirmed_edge_rate_limit(auth_environment, monkeypatch):
    monkeypatch.delenv("SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED", raising=False)
    response = TestClient(app).post("/api/v1/device/pair", json={
        "pairing_code": "pairing-secret-code-123456",
        "device_name": "Unprotected",
    })
    assert response.status_code == 503

def test_chat_returns_service_unavailable_when_all_free_providers_are_exhausted(auth_environment):
    from app.providers.free_router import NoFreeProviderAvailable

    class UnavailableProvider:
        async def chat(self, messages, model=None):
            raise NoFreeProviderAvailable(
                "Nessun provider gratuito disponibile. La conversazione resta salvata; riprova più tardi."
            )

        async def list_models(self):
            return []

    app.dependency_overrides[get_provider] = lambda: UnavailableProvider()
    try:
        _, token_data = auth_environment
        client = TestClient(
            app,
            headers={"Authorization": "Bearer " + token_data["access_token"]},
        )
        response = client.post(
            "/api/v1/chat",
            json={"messages": [{"role": "user", "content": "Riprendi la mia richiesta"}]},
        )

        assert response.status_code == 503
        assert "Nessun provider gratuito disponibile" in response.json()["detail"]
        assert "riprova più tardi" in response.json()["detail"]
    finally:
        app.dependency_overrides[get_provider] = lambda: FakeProvider()


def test_provider_endpoint_reports_safe_operational_state(auth_environment, tmp_path):
    from app.providers.free_router import FreeOnlyRouter

    _, token_data = auth_environment
    env = {
        "SUPERIA_FREE_ONLY": "true",
        "SUPERIA_PROVIDER_GROQ_APPROVED_FREE": "true",
        "SUPERIA_PROVIDER_GROQ_NO_PAYMENT_METHOD": "true",
        "SUPERIA_PROVIDER_GROQ_HARD_STOP": "true",
        "SUPERIA_PROVIDER_GROQ_NO_FIAT_BILLING": "true",
        "SUPERIA_PROVIDER_GROQ_API_KEY": "must-never-be-returned",
        "SUPERIA_PROVIDER_GROQ_MODEL": "openai/gpt-oss-120b",
        "SUPERIA_PROVIDER_GROQ_FREE_MODEL_IDS": "openai/gpt-oss-120b",
    }
    router = FreeOnlyRouter(
        environ=env,
        state_db_path=str(tmp_path / "provider-status.sqlite3"),
    )
    app.dependency_overrides[get_provider] = lambda: router
    try:
        client = TestClient(
            app,
            headers={"Authorization": "Bearer " + token_data["access_token"]},
        )
        response = client.get("/api/v1/providers")
        assert response.status_code == 200
        payload = response.json()
        groq = next(item for item in payload["providers"] if item["id"] == "groq")
        assert groq["enabled"] is True
        assert groq["available"] is True
        assert groq["state"] == "ready"
        assert "must-never-be-returned" not in response.text
        assert payload["paid_fallback"] is False
    finally:
        app.dependency_overrides[get_provider] = lambda: FakeProvider()

