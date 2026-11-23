import os
import uuid

import pytest

from app.database import connect_database, is_postgres_url, validate_deployment_database_url


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



@pytest.mark.parametrize("value", ["postgresql://db.example/app", "postgres://db.example/app", "postgresql+psycopg://db.example/app"])
def test_postgres_url_schemes_are_recognized(value):
    assert is_postgres_url(value)
    validate_deployment_database_url(value, "staging", "SUPERIA_DATABASE_URL")


@pytest.mark.parametrize("value", ["", "/tmp/superia.sqlite3", "sqlite:////tmp/superia.sqlite3"])
@pytest.mark.parametrize("environment", ["staging", "production"])
def test_deployed_environments_reject_missing_or_sqlite_database(value, environment):
    with pytest.raises(ValueError, match="PostgreSQL"):
        validate_deployment_database_url(value, environment, "SUPERIA_DATABASE_URL")


def test_development_environment_keeps_local_sqlite_support():
    validate_deployment_database_url("/tmp/superia-dev.sqlite3", "development", "SUPERIA_DATABASE_URL")
