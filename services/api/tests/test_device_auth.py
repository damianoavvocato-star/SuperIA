from app.device_auth import issue_device_token, revoke_device_token, validate_device_token


def test_sqlite_device_token_lifecycle(tmp_path):
    database = str(tmp_path / "auth.sqlite3")
    issued = issue_device_token(database, "test-phone")
    assert validate_device_token(database, issued["access_token"])
    assert revoke_device_token(database, issued["device_id"])
    assert not validate_device_token(database, issued["access_token"])
    assert not revoke_device_token(database, issued["device_id"])


def test_token_storage_keeps_only_hash(tmp_path):
    import sqlite3

    database = str(tmp_path / "auth.sqlite3")
    issued = issue_device_token(database, "test-phone")
    connection = sqlite3.connect(database)
    try:
        stored = connection.execute(
            "SELECT token_hash FROM device_tokens WHERE device_id = ?",
            (issued["device_id"],),
        ).fetchone()[0]
    finally:
        connection.close()
    assert stored != issued["access_token"]
    assert len(stored) == 64


def test_postgres_device_token_lifecycle():
    import os

    import pytest

    database_url = os.environ.get("SUPERIA_TEST_POSTGRES_URL")
    if not database_url:
        pytest.skip("PostgreSQL integration service is not configured")

    issued = issue_device_token(database_url, "postgres-test-phone")
    try:
        assert validate_device_token(database_url, issued["access_token"])
        assert revoke_device_token(database_url, issued["device_id"])
        assert not validate_device_token(database_url, issued["access_token"])
    finally:
        # Revocation is idempotent; ensure no active test credential remains.
        revoke_device_token(database_url, issued["device_id"])
