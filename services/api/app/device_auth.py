import hashlib
import secrets
import sqlite3
import time
import uuid
from contextlib import closing
from pathlib import Path

DEVICE_TOKEN_TTL_SECONDS = 90 * 24 * 60 * 60


def _connect(database_path: str) -> sqlite3.Connection:
    path = Path(database_path)
    path.parent.mkdir(parents=True, exist_ok=True)
    connection = sqlite3.connect(str(path), timeout=10)
    connection.row_factory = sqlite3.Row
    return connection


def initialize_auth_store(database_path: str) -> None:
    with closing(_connect(database_path)) as connection:
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS device_tokens (
                token_hash TEXT PRIMARY KEY,
                device_id TEXT NOT NULL UNIQUE,
                device_name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                expires_at INTEGER NOT NULL,
                revoked_at INTEGER
            )
            """
        )
        connection.commit()


def issue_device_token(database_path: str, device_name: str) -> dict[str, object]:
    initialize_auth_store(database_path)
    token = secrets.token_urlsafe(32)
    device_id = str(uuid.uuid4())
    now = int(time.time())
    expires_at = now + DEVICE_TOKEN_TTL_SECONDS
    token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
    with closing(_connect(database_path)) as connection:
        connection.execute(
            """
            INSERT INTO device_tokens
                (token_hash, device_id, device_name, created_at, expires_at, revoked_at)
            VALUES (?, ?, ?, ?, ?, NULL)
            """,
            (token_hash, device_id, device_name.strip(), now, expires_at),
        )
        connection.commit()
    return {
        "access_token": token,
        "device_id": device_id,
        "expires_at": expires_at,
        "token_type": "Bearer",
    }


def validate_device_token(database_path: str, token: str) -> bool:
    if not token:
        return False
    initialize_auth_store(database_path)
    token_hash = hashlib.sha256(token.encode("utf-8")).hexdigest()
    now = int(time.time())
    with closing(_connect(database_path)) as connection:
        row = connection.execute(
            "SELECT expires_at, revoked_at FROM device_tokens WHERE token_hash = ?",
            (token_hash,),
        ).fetchone()
    return bool(row and row["revoked_at"] is None and row["expires_at"] > now)


def revoke_device_token(database_path: str, device_id: str) -> bool:
    initialize_auth_store(database_path)
    with closing(_connect(database_path)) as connection:
        cursor = connection.execute(
            """
            UPDATE device_tokens SET revoked_at = ?
            WHERE device_id = ? AND revoked_at IS NULL
            """,
            (int(time.time()), device_id),
        )
        connection.commit()
        return cursor.rowcount == 1
