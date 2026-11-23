import os
import uuid

import pytest

from app.chat_requests import (
    claim_chat_request,
    complete_chat_request,
    request_payload_hash,
    fail_chat_request,
    refresh_chat_request_lease,
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



def test_active_stream_refreshes_its_processing_lease(tmp_path, monkeypatch):
    import app.chat_requests as store

    database = str(tmp_path / "lease.sqlite3")
    secret = "lease-test-secret-that-is-at-least-32-characters"
    messages = [{"role": "user", "content": "Stream lungo"}]
    payload_hash = request_payload_hash(messages, None)
    clock = [1_000_000]
    monkeypatch.setattr(store.time, "time", lambda: clock[0])

    first = claim_chat_request(database, "device-1", "request-1", payload_hash, secret)
    assert first["state"] == "claimed"

    clock[0] += 29 * 60
    assert refresh_chat_request_lease(database, "device-1", "request-1") is True

    clock[0] += 2 * 60
    duplicate = claim_chat_request(database, "device-1", "request-1", payload_hash, secret)
    assert duplicate["state"] == "processing"


def test_stale_processing_lease_can_be_recovered(tmp_path, monkeypatch):
    import app.chat_requests as store

    database = str(tmp_path / "stale.sqlite3")
    secret = "stale-test-secret-that-is-at-least-32-characters"
    messages = [{"role": "user", "content": "Recupero stream"}]
    payload_hash = request_payload_hash(messages, None)
    clock = [2_000_000]
    monkeypatch.setattr(store.time, "time", lambda: clock[0])

    first = claim_chat_request(database, "device-1", "request-2", payload_hash, secret)
    assert first["state"] == "claimed"

    clock[0] += store.PROCESSING_LEASE_SECONDS + 1
    recovered = claim_chat_request(database, "device-1", "request-2", payload_hash, secret)
    assert recovered["state"] == "claimed"

    duplicate = claim_chat_request(database, "device-1", "request-2", payload_hash, secret)
    assert duplicate["state"] == "processing"


def test_failed_request_can_be_retried_once(tmp_path):
    database = str(tmp_path / "failed.sqlite3")
    secret = "failed-test-secret-that-is-at-least-32-characters"
    messages = [{"role": "user", "content": "Riprova"}]
    payload_hash = request_payload_hash(messages, None)

    assert claim_chat_request(database, "device-1", "request-3", payload_hash, secret)["state"] == "claimed"
    fail_chat_request(database, "device-1", "request-3")

    retry = claim_chat_request(database, "device-1", "request-3", payload_hash, secret)
    assert retry["state"] == "claimed"
    concurrent_retry = claim_chat_request(database, "device-1", "request-3", payload_hash, secret)
    assert concurrent_retry["state"] == "processing"
