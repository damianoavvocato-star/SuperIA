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
        str(first["lease_token"]),
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
    assert refresh_chat_request_lease(database, "device-1", "request-1", str(first["lease_token"])) is True

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

    first = claim_chat_request(database, "device-1", "request-3", payload_hash, secret)
    assert first["state"] == "claimed"
    assert fail_chat_request(database, "device-1", "request-3", str(first["lease_token"])) is True

    retry = claim_chat_request(database, "device-1", "request-3", payload_hash, secret)
    assert retry["state"] == "claimed"
    concurrent_retry = claim_chat_request(database, "device-1", "request-3", payload_hash, secret)
    assert concurrent_retry["state"] == "processing"



def test_old_lease_owner_cannot_refresh_fail_or_complete_after_recovery(tmp_path, monkeypatch):
    import app.chat_requests as store

    database = str(tmp_path / "fencing.sqlite3")
    secret = "fencing-test-secret-that-is-at-least-32-characters"
    messages = [{"role": "user", "content": "Proteggi il proprietario"}]
    payload_hash = request_payload_hash(messages, None)
    clock = [3_000_000]
    monkeypatch.setattr(store.time, "time", lambda: clock[0])

    old_claim = claim_chat_request(database, "device-1", "request-fence", payload_hash, secret)
    assert old_claim["state"] == "claimed"
    old_token = str(old_claim["lease_token"])

    clock[0] += store.PROCESSING_LEASE_SECONDS + 1
    new_claim = claim_chat_request(database, "device-1", "request-fence", payload_hash, secret)
    assert new_claim["state"] == "claimed"
    new_token = str(new_claim["lease_token"])
    assert new_token != old_token

    assert refresh_chat_request_lease(database, "device-1", "request-fence", old_token) is False
    assert fail_chat_request(database, "device-1", "request-fence", old_token) is False
    assert complete_chat_request(
        database, "device-1", "request-fence", "Risposta vecchia",
        "old-model", "old-provider", secret, old_token,
    ) is False

    assert complete_chat_request(
        database, "device-1", "request-fence", "Risposta nuova",
        "new-model", "new-provider", secret, new_token,
    ) is True

    replay = claim_chat_request(database, "device-1", "request-fence", payload_hash, secret)
    assert replay["state"] == "completed"
    assert replay["content"] == "Risposta nuova"


def test_existing_chat_request_table_is_migrated_with_lease_token(tmp_path):
    import sqlite3

    database = str(tmp_path / "legacy.sqlite3")
    with sqlite3.connect(database) as connection:
        connection.execute("""
            CREATE TABLE chat_requests (
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
        """)

    messages = [{"role": "user", "content": "Compatibilità database"}]
    claim = claim_chat_request(
        database, "device-1", "legacy-request",
        request_payload_hash(messages, None),
        "legacy-migration-secret-32-chars-minimum",
    )
    assert claim["state"] == "claimed"
    assert claim["lease_token"]



def test_idempotency_records_are_isolated_between_devices(tmp_path):
    database = str(tmp_path / "cross-device.sqlite3")
    secret = "cross-device-secret-that-is-at-least-32-characters"
    request_id = "shared-request-id"
    messages = [{"role": "user", "content": "Richiesta privata"}]
    payload_hash = request_payload_hash(messages, None)

    first = claim_chat_request(database, "device-A", request_id, payload_hash, secret)
    assert first["state"] == "claimed"
    assert complete_chat_request(
        database, "device-A", request_id, "Risposta A", "model-a", "provider-a",
        secret, str(first["lease_token"]),
    )

    second = claim_chat_request(database, "device-B", request_id, payload_hash, secret)
    assert second["state"] == "claimed"
    assert complete_chat_request(
        database, "device-B", request_id, "Risposta B", "model-b", "provider-b",
        secret, str(second["lease_token"]),
    )

    replay_a = claim_chat_request(database, "device-A", request_id, payload_hash, secret)
    replay_b = claim_chat_request(database, "device-B", request_id, payload_hash, secret)
    assert replay_a["content"] == "Risposta A"
    assert replay_b["content"] == "Risposta B"
