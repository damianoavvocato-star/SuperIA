"""Persistent per-device idempotency records for chat requests."""
from __future__ import annotations

import base64
import hashlib
import json
import os
import time
from contextlib import closing

from cryptography.hazmat.primitives.ciphers.aead import AESGCM

from app.database import connect_database

PROCESSING_LEASE_SECONDS = 30 * 60

def _encrypt_response(content: str, secret: str) -> str:
    if not secret:
        raise ValueError("SUPERIA_CHAT_IDEMPOTENCY_KEY is required")
    nonce = os.urandom(12)
    key = hashlib.sha256(secret.encode("utf-8")).digest()
    ciphertext = AESGCM(key).encrypt(nonce, content.encode("utf-8"), None)
    return base64.urlsafe_b64encode(nonce + ciphertext).decode("ascii")


def _decrypt_response(value: str, secret: str) -> str:
    packed = base64.urlsafe_b64decode(value.encode("ascii"))
    nonce, ciphertext = packed[:12], packed[12:]
    key = hashlib.sha256(secret.encode("utf-8")).digest()
    return AESGCM(key).decrypt(nonce, ciphertext, None).decode("utf-8")



def _connect(database_url: str):
    return connect_database(database_url)


def initialize_chat_request_store(database_url: str) -> None:
    with closing(_connect(database_url)) as connection:
        connection.execute(
            """
            CREATE TABLE IF NOT EXISTS chat_requests (
                device_id TEXT NOT NULL,
                request_id TEXT NOT NULL,
                payload_hash TEXT NOT NULL,
                status TEXT NOT NULL,
                response_text TEXT,
                model TEXT,
                provider TEXT,
                created_at BIGINT NOT NULL,
                updated_at BIGINT NOT NULL,
                PRIMARY KEY (device_id, request_id)
            )
            """
        )
        connection.commit()


def request_payload_hash(messages: list[dict], model: str | None) -> str:
    canonical = json.dumps(
        {"messages": messages, "model": model},
        ensure_ascii=False,
        sort_keys=True,
        separators=(",", ":"),
    )
    return hashlib.sha256(canonical.encode("utf-8")).hexdigest()


def claim_chat_request(
    database_url: str,
    device_id: str,
    request_id: str,
    payload_hash: str,
    encryption_secret: str,
) -> dict[str, object]:
    """Atomically claim a request, replay a completed one, or reject a live duplicate."""
    initialize_chat_request_store(database_url)
    now = int(time.time())
    with closing(_connect(database_url)) as connection:
        connection.execute(
            """
            INSERT INTO chat_requests
                (device_id, request_id, payload_hash, status, response_text,
                 model, provider, created_at, updated_at)
            VALUES (?, ?, ?, 'processing', NULL, NULL, NULL, ?, ?)
            ON CONFLICT (device_id, request_id) DO NOTHING
            """,
            (device_id, request_id, payload_hash, now, now),
        )
        row = connection.execute(
            """
            SELECT payload_hash, status, response_text, model, provider, updated_at
            FROM chat_requests WHERE device_id = ? AND request_id = ?
            """,
            (device_id, request_id),
        ).fetchone()

        if row["payload_hash"] != payload_hash:
            connection.commit()
            return {"state": "conflict"}

        if row["status"] == "completed":
            connection.commit()
            return {
                "state": "completed",
                "content": _decrypt_response(row["response_text"] or "", encryption_secret),
                "model": row["model"] or "unknown",
                "provider": row["provider"] or "free-only-router",
            }

        if row["status"] == "processing":
            if now - int(row["updated_at"]) <= PROCESSING_LEASE_SECONDS:
                connection.commit()
                return {"state": "processing"}
            cursor = connection.execute(
                """
                UPDATE chat_requests SET updated_at = ?
                WHERE device_id = ? AND request_id = ? AND status = 'processing'
                  AND updated_at = ?
                """,
                (now, device_id, request_id, int(row["updated_at"])),
            )
            connection.commit()
            return {"state": "claimed" if cursor.rowcount == 1 else "processing"}

        cursor = connection.execute(
            """
            UPDATE chat_requests
            SET status = 'processing', response_text = NULL, model = NULL,
                provider = NULL, updated_at = ?
            WHERE device_id = ? AND request_id = ? AND status = 'failed'
            """,
            (now, device_id, request_id),
        )
        connection.commit()
        return {"state": "claimed" if cursor.rowcount == 1 else "processing"}


def complete_chat_request(
    database_url: str,
    device_id: str,
    request_id: str,
    content: str,
    model: str,
    provider: str,
    encryption_secret: str,
) -> None:
    encrypted_response = _encrypt_response(content, encryption_secret)
    with closing(_connect(database_url)) as connection:
        connection.execute(
            """
            UPDATE chat_requests
            SET status = 'completed', response_text = ?, model = ?,
                provider = ?, updated_at = ?
            WHERE device_id = ? AND request_id = ? AND status = 'processing'
            """,
            (encrypted_response, model, provider, int(time.time()), device_id, request_id),
        )
        connection.commit()


def fail_chat_request(database_url: str, device_id: str, request_id: str) -> None:
    with closing(_connect(database_url)) as connection:
        connection.execute(
            """
            UPDATE chat_requests SET status = 'failed', updated_at = ?
            WHERE device_id = ? AND request_id = ? AND status = 'processing'
            """,
            (int(time.time()), device_id, request_id),
        )
        connection.commit()
