import os
import time
from dataclasses import dataclass
from typing import Callable

import httpx


class NoFreeProviderAvailable(RuntimeError):
    """No verified zero-cost provider is currently available."""


@dataclass(frozen=True)
class ProviderConfig:
    provider_id: str
    endpoint: str
    api_key: str
    model: str
    headers: dict[str, str]


class FreeOnlyRouter:
    """
    Fail-closed router. A provider is eligible only when the operator has
    explicitly attested that its account is free-only, has no payment method,
    and hard-stops at quota exhaustion. No paid model is selected automatically.
    """

    PROVIDER_ORDER = ("gemini", "groq", "openrouter", "cloudflare", "sambanova")

    def __init__(
        self,
        environ: dict[str, str] | None = None,
        client_factory: Callable[..., httpx.AsyncClient] = httpx.AsyncClient,
    ) -> None:
        self.environ = os.environ if environ is None else environ
        self.client_factory = client_factory
        self._cooldowns: dict[str, float] = {}
        self._disabled: set[str] = set()

    def _provider(self, provider_id: str) -> ProviderConfig | None:
        env = self.environ
        prefix = f"SUPERIA_PROVIDER_{provider_id.upper()}_"
        if env.get("SUPERIA_FREE_ONLY", "").lower() != "true":
            return None
        if env.get(prefix + "APPROVED_FREE", "").lower() != "true":
            return None
        if env.get(prefix + "NO_PAYMENT_METHOD", "").lower() != "true":
            return None
        if env.get(prefix + "HARD_STOP", "").lower() != "true":
            return None
        if env.get(prefix + "NO_FIAT_BILLING", "").lower() != "true":
            return None

        api_key = env.get(prefix + "API_KEY", "").strip()
        model = env.get(prefix + "MODEL", "").strip()
        if not api_key or not model:
            return None

        allowed_models = {
            item.strip()
            for item in env.get(prefix + "FREE_MODEL_IDS", "").split(",")
            if item.strip()
        }
        if model not in allowed_models:
            return None
        if provider_id == "openrouter" and not model.endswith(":free"):
            return None

        if provider_id == "gemini":
            endpoint = "https://generativelanguage.googleapis.com/v1beta/openai/chat/completions"
            headers = {"Authorization": f"Bearer {api_key}"}
        elif provider_id == "groq":
            endpoint = "https://api.groq.com/openai/v1/chat/completions"
            headers = {"Authorization": f"Bearer {api_key}"}
        elif provider_id == "openrouter":
            endpoint = "https://openrouter.ai/api/v1/chat/completions"
            headers = {"Authorization": f"Bearer {api_key}"}
        elif provider_id == "cloudflare":
            account_id = env.get(prefix + "ACCOUNT_ID", "").strip()
            if not account_id:
                return None
            endpoint = f"https://api.cloudflare.com/client/v4/accounts/{account_id}/ai/v1/chat/completions"
            headers = {"Authorization": f"Bearer {api_key}"}
        elif provider_id == "sambanova":
            endpoint = "https://api.sambanova.ai/v1/chat/completions"
            headers = {"Authorization": f"Bearer {api_key}"}
        else:
            return None

        return ProviderConfig(provider_id, endpoint, api_key, model, headers)

    def configured_providers(self) -> list[ProviderConfig]:
        return [
            config
            for provider_id in self.PROVIDER_ORDER
            if (config := self._provider(provider_id)) is not None
        ]

    async def list_models(self) -> list[str]:
        return [f"{p.provider_id}:{p.model}" for p in self.configured_providers()]

    async def chat(
        self,
        messages: list[dict[str, str]],
        model: str | None = None,
    ) -> dict[str, str]:
        providers = self.configured_providers()
        if not providers:
            raise NoFreeProviderAvailable(
                "Nessun provider gratuito verificato è configurato. La richiesta è stata conservata; riprova più tardi."
            )

        last_error = "Nessun provider gratuito disponibile."
        now = time.monotonic()
        attempted = False
        for provider in providers:
            if provider.provider_id in self._disabled:
                continue
            if self._cooldowns.get(provider.provider_id, 0.0) > now:
                continue
            attempted = True
            try:
                async with self.client_factory(timeout=httpx.Timeout(45.0, connect=10.0)) as client:
                    response = await client.post(
                        provider.endpoint,
                        headers={**provider.headers, "Content-Type": "application/json"},
                        json={
                            "model": provider.model,
                            "messages": messages,
                            "stream": False,
                        },
                    )
                if response.status_code == 429:
                    retry_after = response.headers.get("Retry-After", "60")
                    try:
                        cooldown = float(retry_after)
                    except ValueError:
                        cooldown = 60.0
                    self._cooldowns[provider.provider_id] = (
                        time.monotonic() + max(1.0, min(cooldown, 3600.0))
                    )
                    last_error = f"{provider.provider_id}: quota o limite temporaneo raggiunto."
                    continue
                if response.status_code in (401, 402, 403):
                    self._disabled.add(provider.provider_id)
                    last_error = (
                        f"{provider.provider_id}: credenziali, billing o permessi non compatibili "
                        "con il profilo gratuito; provider disabilitato."
                    )
                    continue
                if response.status_code == 408 or response.status_code >= 500:
                    self._cooldowns[provider.provider_id] = time.monotonic() + 15.0
                    last_error = f"{provider.provider_id}: servizio temporaneamente non disponibile."
                    continue
                response.raise_for_status()
                payload = response.json()
                content = payload.get("choices", [{}])[0].get("message", {}).get("content")
                if not isinstance(content, str) or not content.strip():
                    last_error = f"{provider.provider_id}: risposta vuota o formato non riconosciuto."
                    continue
                return {"content": content, "model": provider.model, "provider": provider.provider_id}
            except (httpx.HTTPError, ValueError, KeyError, IndexError, TypeError) as exc:
                self._cooldowns[provider.provider_id] = time.monotonic() + 15.0
                last_error = f"{provider.provider_id}: errore di rete o risposta non valida ({type(exc).__name__})."
                continue

        if not attempted:
            last_error = "Tutti i provider configurati sono in cooldown o disabilitati."
        raise NoFreeProviderAvailable(
            "Nessun provider gratuito disponibile. La conversazione resta salvata; " + last_error
        )
