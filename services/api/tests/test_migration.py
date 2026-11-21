import sqlite3

import pytest

from app.migration import migrate_sqlite_to_database
from app.device_auth import issue_device_token, validate_device_token


def test_migration_copies_tokens_and_provider_state(tmp_path):
    source = tmp_path / "source.sqlite3"
    destination = tmp_path / "destination.sqlite3"

    issued = issue_device_token(str(source), "migration-test")
    connection = sqlite3.connect(source)
    connection.execute(
        "CREATE TABLE provider_state (provider_id TEXT PRIMARY KEY, cooldown_until REAL NOT NULL DEFAULT 0, disabled INTEGER NOT NULL DEFAULT 0)"
    )
    connection.execute(
        "INSERT INTO provider_state(provider_id, cooldown_until, disabled) VALUES (?, ?, ?)",
        ("groq", 1234.5, 1),
    )
    connection.commit()
    connection.close()

    result = migrate_sqlite_to_database(str(source), str(destination))
    assert result["device_tokens"] == 1
    assert result["provider_state"] == 1
    assert len(result["device_tokens_sha256"]) == 64
    assert len(result["provider_state_sha256"]) == 64
    assert validate_device_token(str(destination), issued["access_token"])


def test_migration_refuses_non_empty_destination(tmp_path):
    source = tmp_path / "source.sqlite3"
    destination = tmp_path / "destination.sqlite3"
    issue_device_token(str(source), "source")
    issue_device_token(str(destination), "existing")

    with pytest.raises(ValueError, match="not empty"):
        migrate_sqlite_to_database(str(source), str(destination))


def test_migration_requires_device_tokens_table(tmp_path):
    source = tmp_path / "source.sqlite3"
    sqlite3.connect(source).close()
    with pytest.raises(ValueError, match="device_tokens"):
        migrate_sqlite_to_database(str(source), str(tmp_path / "destination.sqlite3"))
