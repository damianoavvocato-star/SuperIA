from index import app


def test_vercel_entrypoint_exports_existing_fastapi_app():
    assert app.title == "SuperIA Privata API"
    paths = {route.path for route in app.routes}
    assert "/health" in paths
    assert "/api/v1/chat" in paths
