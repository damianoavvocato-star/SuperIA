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

def test_concurrent_validation_then_committed_revocation_is_fail_closed(tmp_path):
    from concurrent.futures import ThreadPoolExecutor

    database = str(tmp_path / "auth-concurrent.sqlite3")
    issued = issue_device_token(database, "parallel-phone")

    # Validations completed before revocation may succeed. Once revocation commits,
    # every subsequent validation must reject the token.
    with ThreadPoolExecutor(max_workers=8) as pool:
        results = list(pool.map(
            lambda _: validate_device_token(database, issued["access_token"]),
            range(32),
        ))
    assert all(results)

    assert revoke_device_token(database, issued["device_id"])
    with ThreadPoolExecutor(max_workers=8) as pool:
        after_revoke = list(pool.map(
            lambda _: validate_device_token(database, issued["access_token"]),
            range(32),
        ))
    assert not any(after_revoke)

def test_postgres_auth_database_outage_does_not_fall_back_to_sqlite(monkeypatch):
    import pytest
    import app.device_auth as device_auth

    def unavailable(_database_url):
        raise OSError("PostgreSQL unavailable")

    monkeypatch.setattr(device_auth, "connect_database", unavailable)
    with pytest.raises(OSError, match="PostgreSQL unavailable"):
        device_auth.validate_device_token(
            "postgresql://unreachable.invalid/superia", "any-token"
        )



def test_device_token_expires_at_configured_ttl(tmp_path, monkeypatch):
    import app.device_auth as device_auth

    now = [1_800_000_000]
    monkeypatch.setattr(device_auth.time, "time", lambda: now[0])
    database = str(tmp_path / "auth-expiry.sqlite3")
    issued = issue_device_token(database, "expiry-phone")

    assert issued["expires_at"] == now[0] + device_auth.DEVICE_TOKEN_TTL_SECONDS
    assert validate_device_token(database, issued["access_token"])

    now[0] = issued["expires_at"]
    assert not validate_device_token(database, issued["access_token"])


def test_revoking_one_device_does_not_revoke_another(tmp_path):
    from app.device_auth import device_id_for_token

    database = str(tmp_path / "auth-isolation.sqlite3")
    first = issue_device_token(database, "first-phone")
    second = issue_device_token(database, "second-phone")

    assert device_id_for_token(database, first["access_token"]) == first["device_id"]
    assert device_id_for_token(database, second["access_token"]) == second["device_id"]
    assert revoke_device_token(database, first["device_id"])

    assert device_id_for_token(database, first["access_token"]) is None
    assert device_id_for_token(database, second["access_token"]) == second["device_id"]
    assert validate_device_token(database, second["access_token"])



def test_rotating_device_token_invalidates_previous_token_and_keeps_device_id(tmp_path):
    from app.device_auth import device_id_for_token, rotate_device_token

    database = str(tmp_path / "auth-rotation.sqlite3")
    issued = issue_device_token(database, "rotation-phone")
    rotated = rotate_device_token(database, issued["device_id"])

    assert rotated is not None
    assert rotated["device_id"] == issued["device_id"]
    assert rotated["access_token"] != issued["access_token"]
    assert not validate_device_token(database, issued["access_token"])
    assert validate_device_token(database, rotated["access_token"])
    assert device_id_for_token(database, rotated["access_token"]) == issued["device_id"]


def test_expired_or_revoked_device_token_cannot_be_rotated(tmp_path, monkeypatch):
    import app.device_auth as device_auth

    now = [1_900_000_000]
    monkeypatch.setattr(device_auth.time, "time", lambda: now[0])
    database = str(tmp_path / "auth-rotation-expired.sqlite3")
    issued = issue_device_token(database, "expired-phone")
    now[0] = issued["expires_at"]
    assert device_auth.rotate_device_token(database, issued["device_id"]) is None

    now[0] += 1
    active = issue_device_token(database, "revoked-phone")
    assert revoke_device_token(database, active["device_id"])
    assert device_auth.rotate_device_token(database, active["device_id"]) is None
