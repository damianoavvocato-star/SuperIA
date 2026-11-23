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

def test_migration_to_postgres_preserves_tokens_and_provider_state(tmp_path):
    import os
    import uuid
    from urllib.parse import quote

    database_url = os.environ.get("SUPERIA_TEST_POSTGRES_URL")
    if not database_url:
        pytest.skip("PostgreSQL integration service is not configured")

    import psycopg

    schema = "migration_" + uuid.uuid4().hex
    with psycopg.connect(database_url, autocommit=True) as admin:
        admin.execute(f'CREATE SCHEMA "{schema}"')

    # Isolate this integration test from the other PostgreSQL tests.
    separator = "&" if "?" in database_url else "?"
    scoped_url = database_url + separator + "options=" + quote(f"-csearch_path={schema}")

    source = tmp_path / "source-postgres.sqlite3"
    issued = issue_device_token(str(source), "postgres-migration-test")
    with sqlite3.connect(source) as connection:
        connection.execute(
            "CREATE TABLE provider_state (provider_id TEXT PRIMARY KEY, "
            "cooldown_until REAL NOT NULL DEFAULT 0, disabled INTEGER NOT NULL DEFAULT 0)"
        )
        connection.execute(
            "INSERT INTO provider_state(provider_id, cooldown_until, disabled) VALUES (?, ?, ?)",
            ("groq", 1234.5, 1),
        )

    try:
        result = migrate_sqlite_to_database(str(source), scoped_url)
        assert result["device_tokens"] == 1
        assert result["provider_state"] == 1
        assert len(result["device_tokens_sha256"]) == 64
        assert len(result["provider_state_sha256"]) == 64
        assert validate_device_token(scoped_url, issued["access_token"])
    finally:
        with psycopg.connect(database_url, autocommit=True) as admin:
            admin.execute(f'DROP SCHEMA IF EXISTS "{schema}" CASCADE')




def test_migration_rolls_back_all_copied_rows_on_schema_error(tmp_path):
    source = tmp_path / "source-rollback.sqlite3"
    destination = tmp_path / "destination-rollback.sqlite3"
    issue_device_token(str(source), "rollback-source")
    with sqlite3.connect(source) as connection:
        connection.execute(
            "CREATE TABLE provider_state (provider_id TEXT PRIMARY KEY, "
            "cooldown_until REAL NOT NULL DEFAULT 0, disabled INTEGER NOT NULL DEFAULT 0)"
        )
        connection.execute(
            "INSERT INTO provider_state(provider_id, cooldown_until, disabled) VALUES (?, ?, ?)",
            ("groq", 1234.5, 1),
        )
    with sqlite3.connect(destination) as connection:
        # Deliberately incompatible empty table causes a failure after token rows were copied.
        connection.execute("CREATE TABLE provider_state (provider_id TEXT PRIMARY KEY)")

    with pytest.raises(sqlite3.OperationalError):
        migrate_sqlite_to_database(str(source), str(destination))

    with sqlite3.connect(destination) as connection:
        tables = {
            row[0] for row in connection.execute(
                "SELECT name FROM sqlite_master WHERE type = 'table'"
            ).fetchall()
        }
        assert "device_tokens" not in tables
        assert connection.execute("SELECT COUNT(*) FROM provider_state").fetchone()[0] == 0


def test_migration_handles_source_without_provider_state_table(tmp_path):
    source = tmp_path / "source-no-provider-state.sqlite3"
    destination = tmp_path / "destination-no-provider-state.sqlite3"
    issue_device_token(str(source), "no-provider-state")

    result = migrate_sqlite_to_database(str(source), str(destination))
    assert result["device_tokens"] == 1
    assert result["provider_state"] == 0
    assert len(result["device_tokens_sha256"]) == 64
    assert len(result["provider_state_sha256"]) == 64
