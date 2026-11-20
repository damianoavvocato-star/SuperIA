import os
import pytest
from fastapi.testclient import TestClient
from app.main import app, get_provider

class FakeProvider:
    async def chat(self, messages, model=None):
        return {"content": "Risposta di prova", "model": model or "fake-local"}
    async def list_models(self):
        return ["fake-local"]

app.dependency_overrides[get_provider] = lambda: FakeProvider()
os.environ["SUPERIA_API_ACCESS_TOKEN"] = "test-access-token"
client = TestClient(app, headers={"Authorization": "Bearer test-access-token"})

def test_health():
    r = client.get("/health")
    assert r.status_code == 200 and r.json()["status"] == "ok"

def test_capabilities_keep_cloud_disabled():
    r = client.get("/api/v1/capabilities")
    assert r.status_code == 200
    assert r.json()["external_providers_enabled_by_default"] is False

def test_models():
    r = client.get("/api/v1/models")
    assert r.status_code == 200 and r.json()["models"] == ["fake-local"]

def test_chat():
    r = client.post("/api/v1/chat", json={"messages": [{"role": "user", "content": "Ciao"}]})
    assert r.status_code == 200 and r.json()["content"] == "Risposta di prova"

def test_chat_rejects_empty_messages():
    r = client.post("/api/v1/chat", json={"messages": []})
    assert r.status_code == 422

def test_chat_rejects_unknown_role():
    r = client.post("/api/v1/chat", json={"messages": [{"role": "tool", "content": "Ciao"}]})
    assert r.status_code == 422

def test_chat_rejects_oversized_message():
    r = client.post("/api/v1/chat", json={"messages": [{"role": "user", "content": "x" * 32001}]})
    assert r.status_code == 422

def test_home_page():
    r = client.get("/")
    assert r.status_code == 200 and "SuperIA Privata" in r.text
    assert "localStorage" in r.text
    assert "Nuova conversazione" in r.text
    assert "Memoria personale locale" in r.text
    assert "MEMORY_KEY" in r.text


def test_api_routes_reject_missing_or_invalid_token(monkeypatch):
    monkeypatch.setenv("SUPERIA_API_ACCESS_TOKEN", "expected-secret")
    no_token_client = TestClient(app)
    bad_token_client = TestClient(app, headers={"Authorization": "Bearer wrong-secret"})
    assert no_token_client.get("/api/v1/models").status_code == 401
    assert bad_token_client.get("/api/v1/models").status_code == 401


def test_api_routes_fail_closed_when_server_token_is_unset(monkeypatch):
    monkeypatch.delenv("SUPERIA_API_ACCESS_TOKEN", raising=False)
    no_token_client = TestClient(app, headers={"Authorization": "Bearer test-access-token"})
    assert no_token_client.get("/api/v1/models").status_code == 503
