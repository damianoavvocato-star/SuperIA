"""SQLite/PostgreSQL adapter used by the API persistence layer."""
from __future__ import annotations

import sqlite3
from dataclasses import dataclass
from typing import Any


@dataclass
class DatabaseConnection:
    raw: Any
    dialect: str

    def execute(self, statement: str, parameters: tuple[Any, ...] = ()) -> Any:
        if self.dialect == "postgres":
            statement = statement.replace("?", "%s")
        return self.raw.execute(statement, parameters)

    def commit(self) -> None:
        self.raw.commit()

    def close(self) -> None:
        self.raw.close()

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback) -> None:
        self.close()


def connect_database(database_url: str) -> DatabaseConnection:
    """Open SQLite for local/test use or PostgreSQL for configured deployments."""
    value = (database_url or "").strip()
    if not value:
        raise ValueError("Database URL/path is required")
    if value.startswith(("postgresql://", "postgres://", "postgresql+psycopg://")):
        try:
            import psycopg
            from psycopg.rows import dict_row
        except ImportError as exc:  # pragma: no cover - deployment dependency
            raise RuntimeError("PostgreSQL driver psycopg is not installed") from exc
        normalized = value.replace("postgresql+psycopg://", "postgresql://", 1)
        raw = psycopg.connect(normalized, row_factory=dict_row, connect_timeout=5)
        return DatabaseConnection(raw, "postgres")
    if value.startswith("sqlite:///"):
        value = value[len("sqlite:///"):]
        if not value.startswith("/"):
            value = "/" + value
    raw = sqlite3.connect(value, timeout=10)
    raw.row_factory = sqlite3.Row
    return DatabaseConnection(raw, "sqlite")
