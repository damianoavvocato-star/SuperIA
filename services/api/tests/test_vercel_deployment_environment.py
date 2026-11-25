import pytest
from fastapi import HTTPException

from app.main import _auth_database_path


def _clear_database_environment(monkeypatch):
    for name in (
        "SUPERIA_ENVIRONMENT",
        "SUPERIA_DATABASE_URL",
        "SUPERIA_AUTH_DB",
        "VERCEL_ENV",
        "VERCEL",
    ):
        monkeypatch.delenv(name, raising=False)


def test_vercel_production_requires_postgres_even_without_custom_environment(monkeypatch):
    _clear_database_environment(monkeypatch)
    monkeypatch.setenv("VERCEL", "1")
    monkeypatch.setenv("SUPERIA_AUTH_DB", "/tmp/superia.sqlite3")

    with pytest.raises(HTTPException) as error:
        _auth_database_path()

    assert error.value.status_code == 503
    assert "PostgreSQL URL" in error.value.detail


def test_vercel_preview_is_treated_as_staging(monkeypatch):
    _clear_database_environment(monkeypatch)
    monkeypatch.setenv("VERCEL", "1")
    monkeypatch.setenv("VERCEL_ENV", "preview")
    monkeypatch.setenv("SUPERIA_AUTH_DB", "/tmp/superia.sqlite3")

    with pytest.raises(HTTPException) as error:
        _auth_database_path()

    assert error.value.status_code == 503
    assert "PostgreSQL URL" in error.value.detail


def test_vercel_production_accepts_configured_postgres_url(monkeypatch):
    _clear_database_environment(monkeypatch)
    monkeypatch.setenv("VERCEL", "1")
    monkeypatch.setenv(
        "SUPERIA_DATABASE_URL",
        "postgresql://superia:secret@example.invalid/superia",
    )

    assert _auth_database_path() == "postgresql://superia:secret@example.invalid/superia"


def test_custom_development_setting_cannot_downgrade_vercel_production(monkeypatch):
    _clear_database_environment(monkeypatch)
    monkeypatch.setenv("VERCEL", "1")
    monkeypatch.setenv("VERCEL_ENV", "production")
    monkeypatch.setenv("SUPERIA_ENVIRONMENT", "development")
    monkeypatch.setenv("SUPERIA_AUTH_DB", "/tmp/superia.sqlite3")

    with pytest.raises(HTTPException) as error:
        _auth_database_path()

    assert error.value.status_code == 503
    assert "PostgreSQL URL" in error.value.detail


def test_custom_development_setting_cannot_downgrade_vercel_preview(monkeypatch):
    _clear_database_environment(monkeypatch)
    monkeypatch.setenv("VERCEL", "1")
    monkeypatch.setenv("VERCEL_ENV", "preview")
    monkeypatch.setenv("SUPERIA_ENVIRONMENT", "development")
    monkeypatch.setenv("SUPERIA_AUTH_DB", "/tmp/superia.sqlite3")

    with pytest.raises(HTTPException) as error:
        _auth_database_path()

    assert error.value.status_code == 503
    assert "PostgreSQL URL" in error.value.detail
