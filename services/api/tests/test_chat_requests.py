import os
import uuid

import pytest

from app.chat_requests import (
    claim_chat_request,
    complete_chat_request,
    request_payload_hash,
)


def test_postgres_chat_request_claim_and_encrypted_replay():
    database_url = os.environ.get("SUPERIA_TEST_POSTGRES_URL")
    if not database_url:
        pytest.skip("PostgreSQL integration service is not configured")

    device_id = str(uuid.uuid4())
    request_id = str(uuid.uuid4())
    secret = "postgres-test-idempotency-secret-at-least-32-chars"
    messages = [{"role": "user", "content": "Test PostgreSQL replay"}]
    payload_hash = request_payload_hash(messages, None)

    first = claim_chat_request(database_url, device_id, request_id, payload_hash, secret)
    assert first["state"] == "claimed"

    duplicate = claim_chat_request(database_url, device_id, request_id, payload_hash, secret)
    assert duplicate["state"] == "processing"

    complete_chat_request(
        database_url,
        device_id,
        request_id,
        "Risposta PostgreSQL cifrata",
        "test-model",
        "test-provider",
        secret,
    )

    replay = claim_chat_request(database_url, device_id, request_id, payload_hash, secret)
    assert replay == {
        "state": "completed",
        "content": "Risposta PostgreSQL cifrata",
        "model": "test-model",
        "provider": "test-provider",
    }
