import os
import time
from contextlib import closing
from pathlib import Path
from dataclasses import dataclass
from typing import Callable

import httpx

from app.database import connect_database


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
        state_db_path: str | None = None,
    ) -> None:
        self.environ = os.environ if environ is None else environ
        self.client_factory = client_factory
        self.state_db_path = state_db_path or self.environ.get(
            "SUPERIA_ROUTER_STATE_DB",
            self.environ.get("SUPERIA_DATABASE_URL", "/tmp/superia-router-state.sqlite3"),
        )
        self._init_state()

    def _connect_state(self):
        if not self.state_db_path.startswith(("postgresql://", "postgres://", "postgresql+psycopg://")):
            db_path = Path(self.state_db_path)
            db_path.parent.mkdir(parents=True, exist_ok=True)
        connection = connect_database(self.state_db_path)
        connection.execute(
            "CREATE TABLE IF NOT EXISTS provider_state ("
            "provider_id TEXT PRIMARY KEY, cooldown_until REAL NOT NULL DEFAULT 0, "
            "disabled INTEGER NOT NULL DEFAULT 0)"
        )
        connection.commit()
        return connection

    def _init_state(self) -> None:
        with closing(self._connect_state()):
            pass

    def _state(self, provider_id: str) -> tuple[float, bool]:
        with closing(self._connect_state()) as connection:
            row = connection.execute(
                "SELECT cooldown_until, disabled FROM provider_state WHERE provider_id = ?",
                (provider_id,),
            ).fetchone()
        return (
            (float(row["cooldown_until"]), bool(row["disabled"]))
            if row
            else (0.0, False)
        )

    def _set_cooldown(self, provider_id: str, seconds: float) -> None:
        until = time.time() + max(1.0, min(seconds, 3600.0))
        with closing(self._connect_state()) as connection:
            connection.execute(
                "INSERT INTO provider_state(provider_id, cooldown_until, disabled) "
                "VALUES (?, ?, 0) ON CONFLICT(provider_id) DO UPDATE SET "
                "cooldown_until = excluded.cooldown_until",
                (provider_id, until),
            )
            connection.commit()

    def _disable(self, provider_id: str) -> None:
        with closing(self._connect_state()) as connection:
            connection.execute(
                "INSERT INTO provider_state(provider_id, cooldown_until, disabled) "
                "VALUES (?, 0, 1) ON CONFLICT(provider_id) DO UPDATE SET disabled = 1",
                (provider_id,),
            )
            connection.commit()

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

    def provider_status(self) -> list[dict[str, object]]:
        """Return safe operational state without exposing provider credentials."""
        now = time.time()
        statuses: list[dict[str, object]] = []
        for provider_id in self.PROVIDER_ORDER:
            configured = self._provider(provider_id) is not None
            if not configured:
                statuses.append({
                    "id": provider_id,
                    "enabled": False,
                    "available": False,
                    "state": "not_configured",
                    "cooldown_seconds": 0,
                })
                continue
            cooldown_until, disabled = self._state(provider_id)
            remaining = max(0, int(cooldown_until - now + 0.999))
            if disabled:
                state = "disabled"
            elif remaining > 0:
                state = "cooldown"
            else:
                state = "ready"
            statuses.append({
                "id": provider_id,
                "enabled": True,
                "available": state == "ready",
                "state": state,
                "cooldown_seconds": remaining,
            })
        return statuses

    async def list_models(self) -> list[str]:
        return [f"{p.provider_id}:{p.model}" for p in self.configured_providers()]

    async def stream_chat(self, messages: list[dict[str, str]], model: str | None = None):
        """Yield text deltas; fail over only before the first user-visible token."""
        import json

        providers = self.configured_providers()
        if model is not None:
            providers = [
                provider for provider in providers
                if f"{provider.provider_id}:{provider.model}" == model
            ]
            if not providers:
                raise NoFreeProviderAvailable(
                    "Il modello richiesto non è tra quelli gratuiti attualmente configurati."
                )
        if not providers:
            raise NoFreeProviderAvailable(
                "Nessun provider gratuito verificato è configurato. La richiesta è stata conservata; riprova più tardi."
            )

        last_error = "Nessun provider gratuito disponibile."
        attempted = False
        for provider in providers:
            cooldown_until, disabled = self._state(provider.provider_id)
            if disabled or cooldown_until > time.time():
                continue
            attempted = True
            emitted = False
            try:
                timeout = httpx.Timeout(90.0, connect=10.0, read=90.0)
                async with self.client_factory(timeout=timeout) as client:
                    async with client.stream(
                        "POST",
                        provider.endpoint,
                        headers={**provider.headers, "Content-Type": "application/json", "Accept": "text/event-stream"},
                        json={"model": provider.model, "messages": messages, "stream": True},
                    ) as response:
                        if response.status_code == 429:
                            retry_after = response.headers.get("Retry-After", "60")
                            try:
                                cooldown = float(retry_after)
                            except ValueError:
                                cooldown = 60.0
                            self._set_cooldown(provider.provider_id, cooldown)
                            last_error = f"{provider.provider_id}: quota o limite temporaneo raggiunto."
                            continue
                        if response.status_code in (401, 402, 403):
                            self._disable(provider.provider_id)
                            last_error = f"{provider.provider_id}: credenziali, billing o permessi non compatibili con il profilo gratuito; provider disabilitato."
                            continue
                        if response.status_code == 408 or response.status_code >= 500:
                            self._set_cooldown(provider.provider_id, 15.0)
                            last_error = f"{provider.provider_id}: servizio temporaneamente non disponibile."
                            continue
                        if response.status_code < 200 or response.status_code >= 300:
                            last_error = f"{provider.provider_id}: errore HTTP {response.status_code}."
                            continue

                        async for line in response.aiter_lines():
                            if not line.startswith("data:"):
                                continue
                            data = line[5:].strip()
                            if not data:
                                continue
                            if data == "[DONE]":
                                break
                            try:
                                payload = json.loads(data)
                                choices = payload.get("choices") or []
                                delta = choices[0].get("delta", {}) if choices else {}
                                piece = delta.get("content")
                                if isinstance(piece, list):
                                    piece = "".join(
                                        item.get("text", "") for item in piece
                                        if isinstance(item, dict) and isinstance(item.get("text", ""), str)
                                    )
                            except (ValueError, TypeError, IndexError, AttributeError):
                                continue
                            if isinstance(piece, str) and piece:
                                emitted = True
                                yield {"event": "delta", "text": piece, "provider": provider.provider_id, "model": provider.model}

                if emitted:
                    yield {"event": "done", "provider": provider.provider_id, "model": provider.model}
                    return
                last_error = f"{provider.provider_id}: il flusso non ha prodotto testo."
            except (httpx.HTTPError, OSError) as exc:
                self._set_cooldown(provider.provider_id, 15.0)
                last_error = f"{provider.provider_id}: errore di rete ({type(exc).__name__})."
                if emitted:
                    raise RuntimeError("Flusso interrotto dopo l'inizio della risposta; riprova solo dopo aver verificato il testo già ricevuto.") from exc
                continue

        if not attempted:
            last_error = "Tutti i provider configurati sono in cooldown o disabilitati."
        raise NoFreeProviderAvailable(
            "Nessun provider gratuito disponibile. La conversazione resta salvata; " + last_error
        )

    async def chat(
        self,
        messages: list[dict[str, str]],
        model: str | None = None,
    ) -> dict[str, str]:
        providers = self.configured_providers()
        if model is not None:
            providers = [
                provider
                for provider in providers
                if f"{provider.provider_id}:{provider.model}" == model
            ]
            if not providers:
                raise NoFreeProviderAvailable(
                    "Il modello richiesto non è tra quelli gratuiti attualmente configurati."
                )
        if not providers:
            raise NoFreeProviderAvailable(
                "Nessun provider gratuito verificato è configurato. La richiesta è stata conservata; riprova più tardi."
            )

        last_error = "Nessun provider gratuito disponibile."
        attempted = False
        for provider in providers:
            cooldown_until, disabled = self._state(provider.provider_id)
            if disabled or cooldown_until > time.time():
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
                    self._set_cooldown(provider.provider_id, cooldown)
                    last_error = f"{provider.provider_id}: quota o limite temporaneo raggiunto."
                    continue
                if response.status_code in (401, 402, 403):
                    self._disable(provider.provider_id)
                    last_error = (
                        f"{provider.provider_id}: credenziali, billing o permessi non compatibili "
                        "con il profilo gratuito; provider disabilitato."
                    )
                    continue
                if response.status_code == 408 or response.status_code >= 500:
                    self._set_cooldown(provider.provider_id, 15.0)
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
                self._set_cooldown(provider.provider_id, 15.0)
                last_error = f"{provider.provider_id}: errore di rete o risposta non valida ({type(exc).__name__})."
                continue

        if not attempted:
            last_error = "Tutti i provider configurati sono in cooldown o disabilitati."
        raise NoFreeProviderAvailable(
            "Nessun provider gratuito disponibile. La conversazione resta salvata; " + last_error
        )
