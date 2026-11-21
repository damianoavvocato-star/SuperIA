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
