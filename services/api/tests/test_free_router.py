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
        router = FreeOnlyRouter(environ=env)
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
