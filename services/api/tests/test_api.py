import os
import pytest

from fastapi.testclient import TestClient

from app.device_auth import issue_device_token, validate_device_token
from app.main import MAX_API_BODY_BYTES, app, get_provider


class FakeProvider:
    def __init__(self):
        self.stream_calls = 0

    async def chat(self, messages, model=None):
        return {"content": "Risposta di prova", "model": model or "fake-local"}

    async def list_models(self):
        return ["fake-local"]

    async def stream_chat(self, messages, model=None):
        self.stream_calls += 1
        yield {"event": "delta", "text":"Risposta ", "provider": "fake", "model": "fake-local"}
        yield {"event": "delta", "text":"in streaming", "provider": "fake", "model": "fake-local"}
        yield {"event": "done", "provider": "fake", "model": "fake-local"}


@pytest.fixture
def auth_environment(tmp_path, monkeypatch):
    database = str(tmp_path / "auth.sqlite3")
    monkeypatch.setenv("SUPERIA_AUTH_DB", database)
    monkeypatch.setenv("SUPERIA_PAIRING_CODE", "pairing-secret-code-123456")
    monkeypatch.setenv("SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED", "true")
    monkeypatch.setenv("SUPERIA_ADMIN_TOKEN", "admin-secret-token-123456")
    monkeypatch.setenv("SUPERIA_CHAT_IDEMPOTENCY_KEY", "test-idempotency-encryption-secret-keep-stable")
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


def test_stream_endpoint_requires_device_token():
    response = TestClient(app).post(
        "/api/v1/chat/stream",
        json={"messages": [{"role": "user", "content": "Ciao"}]},
    )
    assert response.status_code == 401


def test_stream_endpoint_emits_sse_deltas_and_done(auth_environment):
    _, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    response = client.post(
        "/api/v1/chat/stream",
        json={"messages": [{"role": "user", "content": "Ciao"}]},
    )
    assert response.status_code == 200
    assert response.headers["content-type"].startswith("text/event-stream")
    assert 'event: delta' in response.text
    assert '"text":"Risposta "' in response.text
    assert '"text":"in streaming"' in response.text
    assert 'event: done' in response.text


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



def test_stream_request_id_replays_completed_response_without_second_provider_call(auth_environment):
    _, token_data = auth_environment
    fake = FakeProvider()
    app.dependency_overrides[get_provider] = lambda: fake
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    payload = {
        "request_id": "d6cc17ef-78e6-4eb8-bba5-8a4c164bba6f",
        "messages": [{"role": "user", "content": "Rispondi una volta"}],
    }
    first = client.post("/api/v1/chat/stream", json=payload)
    second = client.post("/api/v1/chat/stream", json=payload)
    assert first.status_code == 200 and second.status_code == 200
    assert "Risposta " in first.text and "Risposta " in second.text
    assert 'event: done' in second.text
    assert fake.stream_calls == 1


def test_stream_request_id_rejects_reuse_with_different_payload(auth_environment):
    _, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    request_id = "6a2eac45-7b6d-4bd8-8d1b-97dc07a1c74e"
    first = client.post("/api/v1/chat/stream", json={
        "request_id": request_id,
        "messages": [{"role": "user", "content": "Prima richiesta"}],
    })
    conflict = client.post("/api/v1/chat/stream", json={
        "request_id": request_id,
        "messages": [{"role": "user", "content": "Contenuto diverso"}],
    })
    assert first.status_code == 200
    assert conflict.status_code == 409
    assert "contenuto diverso" in conflict.json()["detail"]


def test_stream_request_id_is_scoped_to_authenticated_device(auth_environment):
    database, token_data = auth_environment
    from app.device_auth import issue_device_token

    second_device = issue_device_token(database, "second-device")
    payload = {
        "request_id": "9a3e39a6-fbf6-47cc-b5de-f43a719ab6c8",
        "messages": [{"role": "user", "content": "Richiesta per dispositivo"}],
    }
    first_client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    second_client = TestClient(app, headers={"Authorization": "Bearer " + second_device["access_token"]})
    assert first_client.post("/api/v1/chat/stream", json=payload).status_code == 200
    assert second_client.post("/api/v1/chat/stream", json=payload).status_code == 200


def test_stream_request_id_rejects_an_already_processing_duplicate(auth_environment):
    from app.chat_requests import claim_chat_request, request_payload_hash

    database, token_data = auth_environment
    request_id = "ed75b51c-5078-4d0f-b11c-9a5cc0f0df53"
    messages = [{"role": "user", "content": "Richiesta già avviata"}]
    claim = claim_chat_request(
        database,
        token_data["device_id"],
        request_id,
        request_payload_hash(messages, None),
        os.environ["SUPERIA_CHAT_IDEMPOTENCY_KEY"],
    )
    assert claim["state"] == "claimed"
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    response = client.post("/api/v1/chat/stream", json={
        "request_id": request_id,
        "messages": messages,
    })
    assert response.status_code == 409
    assert "già in elaborazione" in response.json()["detail"]


def test_stream_idempotency_response_is_encrypted_in_database(auth_environment):
    import sqlite3

    database, token_data = auth_environment
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    client.post("/api/v1/chat/stream", json={
        "request_id": "a9a2fd12-ec9c-4a1d-b8c8-4f7d3c8f4a1e",
        "messages": [{"role": "user", "content": "Risposta da proteggere"}],
    })
    with sqlite3.connect(database) as connection:
        stored = connection.execute(
            "SELECT response_text FROM chat_requests WHERE request_id = ?",
            ("a9a2fd12-ec9c-4a1d-b8c8-4f7d3c8f4a1e",),
        ).fetchone()[0]
    assert stored
    assert "Risposta " not in stored
    assert "in streaming" not in stored


def test_stream_request_id_fails_closed_without_encryption_secret(auth_environment, monkeypatch):
    _, token_data = auth_environment
    monkeypatch.delenv("SUPERIA_CHAT_IDEMPOTENCY_KEY", raising=False)
    client = TestClient(app, headers={"Authorization": "Bearer " + token_data["access_token"]})
    response = client.post("/api/v1/chat/stream", json={
        "request_id": "bb4fbfa9-2d48-4624-83b2-1a33f22a4edc",
        "messages": [{"role": "user", "content": "Non archiviare in chiaro"}],
    })
    assert response.status_code == 503
    assert "SUPERIA_CHAT_IDEMPOTENCY_KEY" in response.json()["detail"]



def test_stream_emits_heartbeat_while_provider_is_silent(auth_environment, monkeypatch):
    import asyncio
    import app.main as api_module

    class SlowProvider(FakeProvider):
        async def stream_chat(self, messages, model=None):
            self.stream_calls += 1
            await asyncio.sleep(0.04)
            yield {"event": "delta", "text": "Dopo attesa", "provider": "fake", "model": "fake-local"}
            yield {"event": "done", "provider": "fake", "model": "fake-local"}

    _, token_data = auth_environment
    monkeypatch.setattr(api_module, "SSE_HEARTBEAT_SECONDS", 0.01)
    slow = SlowProvider()
    api_module.app.dependency_overrides[api_module.get_provider] = lambda: slow
    try:
        client = TestClient(
            api_module.app,
            headers={"Authorization": "Bearer " + token_data["access_token"]},
        )
        response = client.post(
            "/api/v1/chat/stream",
            json={
                "request_id": "b0b3c01f-4e61-4f0e-bfe8-4db3e1c2d3a4",
                "messages": [{"role": "user", "content": "Attendi il provider"}],
            },
        )
        assert response.status_code == 200
        assert ": keep-alive\\n\\n" in response.text
        assert 'event: delta' in response.text
        assert '"text":"Dopo attesa"' in response.text
        assert 'event: done' in response.text
        assert slow.stream_calls == 1
    finally:
        api_module.app.dependency_overrides[api_module.get_provider] = lambda: FakeProvider()
