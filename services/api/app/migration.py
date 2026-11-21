"""One-shot, operator-run SQLite -> shared database migration.

Run only with the API stopped and a verified backup. The destination must be
empty; this prevents accidental merges or overwriting a live auth database.
"""
from __future__ import annotations

import hashlib
import json
import os
import sqlite3
from pathlib import Path
from app.database import connect_database


def _rows_digest(rows: list[tuple[object, ...]]) -> str:
    canonical = json.dumps(rows, ensure_ascii=False, separators=(",", ":"), default=str)
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()


def migrate_sqlite_to_database(source_path: str, destination_url: str) -> dict[str, int | str]:
    source_file = Path(source_path).expanduser().resolve()
    if not source_file.is_file():
        raise FileNotFoundError("Source SQLite database does not exist")
    if not destination_url.strip():
        raise ValueError("Destination database URL is required")

    source = sqlite3.connect(f"file:{source_file.as_posix()}?mode=ro", uri=True)
    source.row_factory = sqlite3.Row
    destination = connect_database(destination_url)
    try:
        source_tables = {
            row["name"]
            for row in source.execute(
                "SELECT name FROM sqlite_master WHERE type = 'table'"
            ).fetchall()
        }
        if "device_tokens" not in source_tables:
            raise ValueError("Source database has no device_tokens table")

        destination.execute("BEGIN")
        destination.execute(
            """
            CREATE TABLE IF NOT EXISTS device_tokens (
                token_hash TEXT PRIMARY KEY,
                device_id TEXT NOT NULL UNIQUE,
                device_name TEXT NOT NULL,
                created_at BIGINT NOT NULL,
                expires_at BIGINT NOT NULL,
                revoked_at BIGINT
            )
            """
        )
        destination.execute(
            """
            CREATE TABLE IF NOT EXISTS provider_state (
                provider_id TEXT PRIMARY KEY,
                cooldown_until REAL NOT NULL DEFAULT 0,
                disabled INTEGER NOT NULL DEFAULT 0
            )
            """
        )

        for table in ("device_tokens", "provider_state"):
            existing = destination.execute(f"SELECT COUNT(*) AS count FROM {table}").fetchone()
            if int(existing["count"]) != 0:
                raise ValueError(f"Destination table {table} is not empty")

        token_columns = (
            "token_hash", "device_id", "device_name", "created_at", "expires_at", "revoked_at"
        )
        token_rows = source.execute(
            """
            SELECT token_hash, device_id, device_name, created_at, expires_at, revoked_at
            FROM device_tokens
            ORDER BY token_hash
            """
        ).fetchall()
        source_token_values = [tuple(row[column] for column in token_columns) for row in token_rows]
        for row in token_rows:
            destination.execute(
                """
                INSERT INTO device_tokens
                    (token_hash, device_id, device_name, created_at, expires_at, revoked_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                tuple(row),
            )

        provider_count = 0
        if "provider_state" in source_tables:
            provider_rows = source.execute(
                "SELECT provider_id, cooldown_until, disabled FROM provider_state ORDER BY provider_id"
            ).fetchall()
            source_provider_values = [
                (row["provider_id"], row["cooldown_until"], row["disabled"])
                for row in provider_rows
            ]
            for row in provider_rows:
                destination.execute(
                    """
                    INSERT INTO provider_state (provider_id, cooldown_until, disabled)
                    VALUES (?, ?, ?)
                    """,
                    tuple(row),
                )
            provider_count = len(provider_rows)
        else:
            source_provider_values = []

        copied_token_rows = destination.execute(
            """
            SELECT token_hash, device_id, device_name, created_at, expires_at, revoked_at
            FROM device_tokens ORDER BY token_hash
            """
        ).fetchall()
        copied_provider_rows = destination.execute(
            """
            SELECT provider_id, cooldown_until, disabled
            FROM provider_state ORDER BY provider_id
            """
        ).fetchall()
        copied_token_values = [
            tuple(row[column] for column in token_columns) for row in copied_token_rows
        ]
        copied_provider_values = [
            (row["provider_id"], row["cooldown_until"], row["disabled"])
            for row in copied_provider_rows
        ]
        result = {
            "device_tokens": len(copied_token_values),
            "provider_state": len(copied_provider_values),
        }
        if result["device_tokens"] != len(source_token_values):
            raise RuntimeError("Post-migration device token row count does not match source")
        if result["provider_state"] != provider_count:
            raise RuntimeError("Post-migration provider state row count does not match source")
        token_digest = _rows_digest(source_token_values)
        if token_digest != _rows_digest(copied_token_values):
            raise RuntimeError("Post-migration device token integrity check failed")
        provider_digest = _rows_digest(source_provider_values)
        if provider_digest != _rows_digest(copied_provider_values):
            raise RuntimeError("Post-migration provider state integrity check failed")
        result["device_tokens_sha256"] = token_digest
        result["provider_state_sha256"] = provider_digest
        destination.commit()
        return result
    except Exception:
        destination.raw.rollback()
        raise
    finally:
        destination.close()
        source.close()


if __name__ == "__main__":
    source_path = os.environ.get("SUPERIA_MIGRATION_SOURCE_SQLITE", "").strip()
    destination_url = os.environ.get("SUPERIA_DATABASE_URL", "").strip()
    if not source_path or not destination_url:
        raise SystemExit(
            "Set SUPERIA_MIGRATION_SOURCE_SQLITE and SUPERIA_DATABASE_URL; "
            "stop the API and back up the source first."
        )
    print(migrate_sqlite_to_database(source_path, destination_url))
