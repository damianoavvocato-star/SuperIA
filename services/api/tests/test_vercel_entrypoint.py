from index import app
from app.main import get_provider


def test_vercel_entrypoint_exports_existing_fastapi_app():
    assert app.title == "SuperIA Privata API"
    paths = {route.path for route in app.routes}
    assert "/health" in paths
    assert "/api/v1/chat" in paths


def test_provider_router_is_reused_within_warm_instance(monkeypatch, tmp_path):
    get_provider.cache_clear()
    monkeypatch.setenv("SUPERIA_ROUTER_STATE_DB", str(tmp_path / "router-state.sqlite3"))
    first = get_provider()
    second = get_provider()
    try:
        assert first is second
    finally:
        get_provider.cache_clear()
