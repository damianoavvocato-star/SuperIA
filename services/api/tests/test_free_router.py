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
        router = FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / 'router-state.sqlite3'))
        assert await router.list_models() == []
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
        router = FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / 'router-state-2.sqlite3'))
        assert await router.list_models() == []
        env = free_env("groq")
        env["SUPERIA_PROVIDER_GROQ_NO_FIAT_BILLING"] = "false"
        router = FreeOnlyRouter(environ=env, state_db_path=str(tmp_path / "router-state-3.sqlite3"))
        assert await router.list_models() == []
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


def test_auth_or_billing_error_persistently_disables_provider(tmp_path):
    async def run():
        env = free_env("groq")
        state_db = str(tmp_path / "router-state.sqlite3")

        def handler(request: httpx.Request) -> httpx.Response:
            return httpx.Response(402, json={"error": "payment required"})

        transport = httpx.MockTransport(handler)

        def client_factory(**kwargs):
            return httpx.AsyncClient(transport=transport, **kwargs)

        first = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        with pytest.raises(NoFreeProviderAvailable):
            await first.chat([{"role": "user", "content": "Ciao"}])

        second = FreeOnlyRouter(environ=env, client_factory=client_factory, state_db_path=state_db)
        with pytest.raises(NoFreeProviderAvailable):
            await second.chat([{"role": "user", "content": "Ancora ciao"}])
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

