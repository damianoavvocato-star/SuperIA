import os
import uuid

import pytest

from app.database import connect_database


def test_sqlite_connection_uses_named_rows_and_question_placeholders(tmp_path):
    database = str(tmp_path / "shared.sqlite3")
    with connect_database(database) as connection:
        connection.execute("CREATE TABLE sample (id TEXT PRIMARY KEY, value TEXT NOT NULL)")
        connection.execute("INSERT INTO sample (id, value) VALUES (?, ?)", ("one", "ok"))
        connection.commit()
        row = connection.execute("SELECT value FROM sample WHERE id = ?", ("one",)).fetchone()
    assert row["value"] == "ok"


def test_empty_database_configuration_is_rejected():
    with pytest.raises(ValueError):
        connect_database("")


def test_postgres_connection_and_placeholder_adapter():
    database_url = os.environ.get("SUPERIA_TEST_POSTGRES_URL")
    if not database_url:
        pytest.skip("PostgreSQL integration service is not configured")
    table = "superia_adapter_" + uuid.uuid4().hex
    with connect_database(database_url) as connection:
        connection.execute(f"CREATE TABLE {table} (id TEXT PRIMARY KEY, value TEXT NOT NULL)")
        try:
            connection.execute(
                f"INSERT INTO {table} (id, value) VALUES (?, ?)", ("one", "ok")
            )
            connection.commit()
            row = connection.execute(
                f"SELECT value FROM {table} WHERE id = ?", ("one",)
            ).fetchone()
            assert row["value"] == "ok"
        finally:
            connection.execute(f"DROP TABLE IF EXISTS {table}")
            connection.commit()
