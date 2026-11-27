import asyncio

import httpx
import pytest

from app.providers.free_router import FreeOnlyRouter, NoFreeProviderAvailable


def free_env(provider_id="groq", model="openai/gpt-oss-120b"):
    prefix = f"SUPERIA_PROVIDER_{provider_id.upper()}_"
    return {
        "SUPERIA_FREE_ONLY": "true",
        prefix + "APPROVED_FREE": "true",
        prefix + "NO_PAYMENT_METHOD": "true",
        prefix + "HARD_STOP": "true",
        prefix + "NO_FIAT_BILLING": "true",
        prefix + "DATA_POLICY_APPROVED": "true",
        prefix + "API_KEY": "test-secret",
        prefix + "MODEL": model,
        prefix + "FREE_MODEL_IDS": model,
    }


def test_router_is_disabled_by_default(tmp_path):
    async def run():
        router = FreeOnlyRouter(environ={}, state_db_path=str(tmp_path / 'router-state.sqlite3'))
        assert await router.list_models() == []
        with pytest.raises(NoFreeProviderAvailable):
            await router.chat([{"role": "user", "content": "Ciao"}])
    asyncio.run(run())


def test_openrouter_rejects_paid_model(tmp_path):
    async def run():
        env = free_env("openrouter", "some-model")
        with pytest.raises(ValueError, match="OpenRouter model must end with ':free'"):
            FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / 'router-state.sqlite3'))
    asyncio.run(run())


def test_router_fails_over_on_rate_limit_without_paid_fallback(tmp_path):
    async def run():
        env = free_env("groq")
        env.update(free_env("openrouter", "example/free-model:free"))

        def handler(request: httpx.Request) -> httpx.Response:
            if request.url.host == "api.groq.com":
                return httpx.Response(429, json={"error": "rate limit"})
            return httpx.Response(
                200,
                json={"choices": [{"message": {"content": "Risposta gratuita"}}]},
            )

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        router = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=str(tmp_path / 'router-state.sqlite3'))
        result = await router.chat([{"role": "user", "content": "Ciao"}])
        assert result["content"] == "Risposta gratuita"
        assert result["provider"] == "openrouter"
        assert result["model"].endswith(":free")
    asyncio.run(run())


def test_payment_method_and_no_fiat_billing_flags_are_mandatory(tmp_path):
    async def run():
        env = free_env("groq")
        env["SUPERIA_PROVIDER_GROQ_NO_PAYMENT_METHOD"] = "false"
        with pytest.raises(ValueError, match="NO_PAYMENT_METHOD"):
            FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / 'router-state-2.sqlite3'))
        env = free_env("groq")
        env["SUPERIA_PROVIDER_GROQ_NO_FIAT_BILLING"] = "false"
        with pytest.raises(ValueError, match="NO_FIAT_BILLING"):
            FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "router-state-3.sqlite3"))
    asyncio.run(run())


def test_cooldown_survives_router_recreation(tmp_path):
    async def run():
        env = free_env("groq")
        env.update(free_env("openrouter", "example/free-model:free"))
        state_db = str(tmp_path / "router-state.sqlite3")
        calls = []

        def handler(request: httpx.Request) -> httpx.Response:
            calls.append(request.url.host)
            if request.url.host == "api.groq.com":
                return httpx.Response(429, headers={"Retry-After": "120"})
            return httpx.Response(
                200,
                json={"choices": [{"message": {"content": "Risposta gratuita"}}]},
            )

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        first = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        result = await first.chat([{"role": "user", "content": "Ciao"}])
        assert result["provider"] == "openrouter"
        assert "api.groq.com" in calls

        calls.clear()
        second = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        result = await second.chat([{"role": "user", "content": "Ancora ciao"}])
        assert result["provider"] == "openrouter"
        assert "api.groq.com" not in calls
    asyncio.run(run())


@pytest.mark.parametrize("status_code", [401, 402, 403])
def test_auth_or_billing_error_persistently_disables_provider(tmp_path, status_code):
    async def run():
        env = free_env("groq")
        state_db = str(tmp_path / f"router-state-{status_code}.sqlite3")
        calls = []

        def handler(request: httpx.Request) -> httpx.Response:
            calls.append(request.url.host)
            return httpx.Response(status_code, json={"error": "provider rejected request"})

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        first = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        with pytest.raises(NoFreeProviderAvailable):
            await first.chat([{"role": "user", "content": "Ciao"}])

        second = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        with pytest.raises(NoFreeProviderAvailable):
            await second.chat([{"role": "user", "content": "Ancora ciao"}])

        assert calls == ["api.groq.com"]

    asyncio.run(run())



def test_requested_model_is_honored_without_silent_fallback(tmp_path):
    async def run():
        env = free_env("groq")
        env.update(free_env("openrouter", "example/free-model:free"))

        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(
                200,
                json={"choices": [{"message": {"content": request.url.host}}]},
            )

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        router = FreeOnlyRouter(
            environ=env,
            client_factory=client_factory,
            state_db_path=str(tmp_path / "router-state.sqlite3"),
        )
        result = await router.chat(
            [{"role": "user", "content": "Ciao"}],
            model="openrouter:example/free-model:free",
        )
        assert result["provider"] == "openrouter"
        with pytest.raises(NoFreeProviderAvailable):
            await router.chat(
                [{"role": "user", "content": "Ciao"}],
                model="provider:model-not-configured",
            )

    asyncio.run(run())


def test_postgres_provider_state_round_trip():
    import os
    import uuid

    database_url = os.environ.get("SUPERIA_TEST_POSTGRES_URL")
    if not database_url:
        pytest.skip("PostgreSQL integration service is not configured")

    router = FreeOnlyRouter(
        environ={},
        state_db_path=database_url,
    )
    provider_id = "integration-" + uuid.uuid4().hex
    router._set_cooldown(provider_id, 90)
    cooldown_until, disabled = router._state(provider_id)
    assert cooldown_until > 0
    assert disabled is False

    router._disable(provider_id)
    cooldown_until, disabled = router._state(provider_id)
    assert disabled is True

def test_parallel_provider_state_updates_are_persisted(tmp_path):
    from concurrent.futures import ThreadPoolExecutor

    router = FreeOnlyRouter(
        environ={},
        state_db_path=str(tmp_path / "router-parallel.sqlite3"),
    )
    provider_id = "parallel-provider"

    with ThreadPoolExecutor(max_workers=8) as pool:
        list(pool.map(
            lambda seconds: router._set_cooldown(provider_id, seconds),
            [15, 30, 45, 60, 75, 90, 105, 120],
        ))

    cooldown_until, disabled = router._state(provider_id)
    assert cooldown_until > 0
    assert disabled is False


def test_configured_postgres_outage_fails_closed_without_sqlite_fallback(monkeypatch):
    import pytest
    import app.providers.free_router as free_router_module

    def unavailable_database(_database_url):
        raise RuntimeError("PostgreSQL unavailable")

    monkeypatch.setattr(free_router_module, "connect_database", unavailable_database)
    with pytest.raises(RuntimeError, match="PostgreSQL unavailable"):
        FreeOnlyRouter(
            environ={"SUPERIA_DATABASE_URL": "postgresql://unavailable.invalid/superia"},
        )

def test_provider_recovers_after_temporary_service_outage(tmp_path, monkeypatch):
    import app.providers.free_router as free_router_module

    now = [1_000.0]
    monkeypatch.setattr(free_router_module.time, "time", lambda: now[0])
    env = free_env("groq")
    calls = []

    def handler(request: httpx.Request) -> httpx.Response:
        calls.append(request.url.host)
        if len(calls) == 1:
            return httpx.Response(503, json={"error": "temporarily unavailable"})
        return httpx.Response(
            200,
            json={"choices": [{"message": {"content": "Servizio ripristinato"}}]},
        )

    transport = httpx.MockTransport(handler)

    def client_factory(**kwargs):
        return httpx.AsyncClient(transport=transport, **kwargs)

    router = FreeOnlyRouter(
        environ=env,
        client_factory=client_factory,
        state_db_path=str(tmp_path / "router-recovery.sqlite3"),
    )

    async def run():
        with pytest.raises(NoFreeProviderAvailable):
            await router.chat([{"role": "user", "content": "Prima richiesta"}])

        # A retry during cooldown must not contact the unavailable provider.
        with pytest.raises(NoFreeProviderAvailable):
            await router.chat([{"role": "user", "content": "Retry anticipato"}])
        assert len(calls) == 1

        # Once the bounded cooldown expires, the same free provider can recover.
        now[0] += 16
        result = await router.chat([{"role": "user", "content": "Riprovo"}])
        assert result["content"] == "Servizio ripristinato"
        assert result["provider"] == "groq"
        assert len(calls) == 2

    asyncio.run(run())


def test_provider_status_distinguishes_unconfigured_ready_cooldown_and_disabled(tmp_path):
    env = free_env("groq")
    env.update(free_env("openrouter", "example/free-model:free"))
    state_db = str(tmp_path / "provider-status.sqlite3")
    router = FreeOnlyRouter(environ=env, state_db_path=state_db)

    router._set_cooldown("groq", 120)
    router._disable("openrouter")
    status = {item["id"]: item for item in router.provider_status()}

    assert status["gemini"]["state"] == "not_configured"
    assert status["gemini"]["available"] is False
    assert status["groq"]["state"] == "cooldown"
    assert status["groq"]["available"] is False
    assert status["groq"]["cooldown_seconds"] > 0
    assert status["openrouter"]["state"] == "disabled"
    assert status["openrouter"]["available"] is False


def test_provider_status_marks_approved_provider_ready(tmp_path):
    router = FreeOnlyRouter(
        environ=free_env("groq"),
        state_db_path=str(tmp_path / "provider-ready.sqlite3"),
    )
    status = {item["id"]: item for item in router.provider_status()}
    assert status["groq"]["enabled"] is True
    assert status["groq"]["available"] is True
    assert status["groq"]["state"] == "ready"




def test_stream_chat_parses_deltas_and_fails_over_before_first_token(tmp_path):
    async def run():
        env = free_env("groq")
        env.update(free_env("openrouter", "example/free-model:free"))

        def handler(request: httpx.Request) -> httpx.Response:
            assert request.read()
            if request.url.host == "api.groq.com":
                return httpx.Response(429, headers={"Retry-After": "1"})
            body = (
                'data: {"choices":[{"delta":{"content":"Ciao "}}]}\n\n'
                'data: {"choices":[{"delta":{"content":"mondo"}}]}\n\n'
                'data: [DONE]\n\n'
            )
            return httpx.Response(200, headers={"content-type": "text/event-stream"}, content=body)

        transport = httpx.MockTransport(handler)
        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        router = FreeOnlyRouter(
            environ=env,
            client_factory=client_factory,
            state_db_path=str(tmp_path / "router-stream.sqlite3"),
        )
        events = [item async for item in router.stream_chat([{"role": "user", "content": "Ciao"}])]
        assert [item["text"] for item in events if item["event"] == "delta"] == ["Ciao ", "mondo"]
        assert events[-1]["event"] == "done"
        assert events[-1]["provider"] == "openrouter"
    asyncio.run(run())


def test_stream_chat_rejects_unapproved_or_unavailable_models(tmp_path):
    async def run():
        router = FreeOnlyRouter(environ={}, state_db_path=str(tmp_path / "router-stream-empty.sqlite3"))
        with pytest.raises(NoFreeProviderAvailable):
            _ = [item async for item in router.stream_chat([{"role": "user", "content": "Ciao"}])]
    asyncio.run(run())



def test_stream_chat_never_switches_provider_after_emitting_partial_answer(tmp_path):
    class BrokenAfterFirstToken(httpx.AsyncByteStream):
        async def __aiter__(self):
            yield b'data: {"choices":[{"delta":{"content":"Risposta parziale"}}]}\n\n'
            raise httpx.ReadError("connection dropped after first token")

        async def aclose(self):
            return None

    async def run():
        env = free_env("groq")
        env.update(free_env("openrouter", "example/free-model:free"))
        calls = []

        def handler(request: httpx.Request) -> httpx.Response:
            calls.append(request.url.host)
            if request.url.host == "api.groq.com":
                return httpx.Response(
                    200,
                    headers={"content-type": "text/event-stream"},
                    stream=BrokenAfterFirstToken(),
                )
            return httpx.Response(
                200,
                headers={"content-type": "text/event-stream"},
                content=b'data: {"choices":[{"delta":{"content":"NON DEVE APPARIRE"}}]}\n\ndata: [DONE]\n\n',
            )

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        router = FreeOnlyRouter(
            environ=env,
            client_factory=client_factory,
            state_db_path=str(tmp_path / "router-stream-partial.sqlite3"),
        )
        received = []
        with pytest.raises(RuntimeError, match="Flusso interrotto dopo l'inizio"):
            async for item in router.stream_chat([{"role": "user", "content": "Ciao"}]):
                received.append(item)
        assert [item["text"] for item in received] == ["Risposta parziale"]
        assert calls == ["api.groq.com"]

    asyncio.run(run())


def test_provider_config_rejects_invalid_global_free_only_value(tmp_path):
    with pytest.raises(ValueError, match="SUPERIA_FREE_ONLY"):
        FreeOnlyRouter(
            environ={"SUPERIA_FREE_ONLY": "yes"},
            state_db_path=str(tmp_path / "invalid-global.sqlite3"),
        )


def test_provider_config_rejects_credentials_without_global_free_only(tmp_path):
    env = free_env("groq")
    env.pop("SUPERIA_FREE_ONLY")
    with pytest.raises(ValueError, match="requires SUPERIA_FREE_ONLY=true"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "missing-global.sqlite3"))


def test_provider_config_rejects_missing_free_attestation(tmp_path):
    env = free_env("groq")
    env["SUPERIA_PROVIDER_GROQ_HARD_STOP"] = "false"
    with pytest.raises(ValueError, match="HARD_STOP"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "missing-attestation.sqlite3"))


def test_provider_config_rejects_partial_credentials(tmp_path):
    env = free_env("groq")
    env.pop("SUPERIA_PROVIDER_GROQ_API_KEY")
    with pytest.raises(ValueError, match="API_KEY, MODEL and FREE_MODEL_IDS"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "partial-provider.sqlite3"))


def test_provider_config_rejects_model_not_in_free_allowlist(tmp_path):
    env = free_env("groq")
    env["SUPERIA_PROVIDER_GROQ_FREE_MODEL_IDS"] = "another-model"
    with pytest.raises(ValueError, match="included in FREE_MODEL_IDS"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "model-not-allowed.sqlite3"))


def test_provider_config_rejects_cloudflare_without_account_id(tmp_path):
    env = free_env("cloudflare", "@cf/meta/llama")
    with pytest.raises(ValueError, match="ACCOUNT_ID is required"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "cloudflare-account.sqlite3"))


def test_provider_config_accepts_complete_free_provider_attestations(tmp_path):
    router = FreeOnlyRouter(
        environ=free_env("groq"),
        state_db_path=str(tmp_path / "valid-provider.sqlite3"),
    )
    assert asyncio.run(router.list_models()) == ["groq:openai/gpt-oss-120b"]



def test_deployed_router_rejects_temporary_database_paths():
    with pytest.raises(ValueError, match="PostgreSQL"):
        FreeOnlyRouter(
            environ={
                "SUPERIA_ENVIRONMENT": "production",
                "SUPERIA_DATABASE_URL": "",
                "SUPERIA_AUTH_DB": "/tmp/auth.sqlite3",
            },
            state_db_path="/tmp/router.sqlite3",
        )


def test_deployed_router_rejects_sqlite_router_state_even_with_postgres_auth_db():
    with pytest.raises(ValueError, match="PostgreSQL"):
        FreeOnlyRouter(
            environ={
                "SUPERIA_ENVIRONMENT": "staging",
                "SUPERIA_DATABASE_URL": "postgresql://user:pass@db.example/superia",
                "SUPERIA_ROUTER_STATE_DB": "/tmp/router.sqlite3",
            },
        )



def test_provider_diagnostics_never_return_api_key(tmp_path):
    env = free_env("groq")
    router = FreeOnlyRouter(
        environ=env,
        state_db_path=str(tmp_path / "secret-redaction.sqlite3"),
    )
    diagnostics = repr(router.provider_status())
    models = repr(asyncio.run(router.list_models()))
    assert "test-secret" not in diagnostics
    assert "test-secret" not in models
    assert "groq" in diagnostics



def test_provider_config_rejects_missing_data_policy_approval(tmp_path):
    env = free_env("groq")
    env.pop("SUPERIA_PROVIDER_GROQ_DATA_POLICY_APPROVED")
    with pytest.raises(ValueError, match="DATA_POLICY_APPROVED"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "data-policy.sqlite3"))



def test_sambanova_is_rejected_as_payment_method_required(tmp_path):
    env = {
        "SUPERIA_FREE_ONLY": "true",
        "SUPERIA_PROVIDER_SAMBANOVA_API_KEY": "must-not-be-used",
    }
    with pytest.raises(ValueError, match="SambaNova is excluded"):
        FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "sambanova-excluded.sqlite3"))


def test_vercel_runtime_rejects_sqlite_router_state_despite_development_override():
    env = {
        "VERCEL": "1",
        "VERCEL_ENV": "production",
        "SUPERIA_ENVIRONMENT": "development",
        "SUPERIA_DATABASE_URL": "postgresql://user:pass@db.example/superia",
        "SUPERIA_ROUTER_STATE_DB": "/tmp/router.sqlite3",
    }
    with pytest.raises(ValueError, match="PostgreSQL"):
        FreeOnlyRouter(environ=env)


@pytest.mark.parametrize("vercel_env", ["preview", "production"])
def test_vercel_runtime_requires_postgres_before_defaulting_router_state(vercel_env):
    env = {
        "VERCEL": "1",
        "VERCEL_ENV": vercel_env,
        "SUPERIA_ENVIRONMENT": "development",
    }
    with pytest.raises(ValueError, match="PostgreSQL"):
        FreeOnlyRouter(environ=env)



@pytest.mark.parametrize("streaming", [False, True])
def test_cloudflare_daily_free_quota_exhaustion_disables_provider_until_manual_review(tmp_path, streaming):
    async def run():
        env = free_env("cloudflare", "@cf/zai-org/glm-4.7-flash")
        env["SUPERIA_PROVIDER_CLOUDFLARE_ACCOUNT_ID"] = "test-account"
        calls = []

        def handler(request: httpx.Request) -> httpx.Response:
            calls.append(request.url.host)
            return httpx.Response(
                429,
                json={
                    "success": False,
                    "errors": [{
                        "code": 3036,
                        "message": "You have used up your daily free allocation of 10,000 neurons.",
                    }],
                },
            )

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        router = FreeOnlyRouter(
            environ=env,
            client_factory=client_factory,
            state_db_path=str(tmp_path / f"cloudflare-quota-{streaming}.sqlite3"),
        )
        messages = [{"role": "user", "content": "Ciao"}]
        with pytest.raises(NoFreeProviderAvailable, match="quota gratuita giornaliera esaurita"):
            if streaming:
                _ = [item async for item in router.stream_chat(messages)]
            else:
                await router.chat(messages)

        status = {item["id"]: item for item in router.provider_status()}
        assert status["cloudflare"]["state"] == "disabled"
        assert status["cloudflare"]["available"] is False

        # A second request must not contact Cloudflare again, even after router recreation.
        second = FreeOnlyRouter(
            environ=env,
            client_factory=client_factory,
            state_db_path=str(tmp_path / f"cloudflare-quota-{streaming}.sqlite3"),
        )
        with pytest.raises(NoFreeProviderAvailable):
            if streaming:
                _ = [item async for item in second.stream_chat(messages)]
            else:
                await second.chat(messages)
        assert calls == ["api.cloudflare.com"]

    asyncio.run(run())
