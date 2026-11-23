import asyncio
import hmac
import json
import os
from functools import lru_cache
from pathlib import Path

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.responses import HTMLResponse, JSONResponse, StreamingResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, Field

from app.device_auth import device_id_for_token, issue_device_token, revoke_device_token, rotate_device_token
from app.providers.free_router import FreeOnlyRouter, NoFreeProviderAvailable
from app.chat_requests import claim_chat_request, complete_chat_request, fail_chat_request, refresh_chat_request_lease, request_payload_hash
from app.schemas import ChatRequest, ChatResponse

MAX_API_BODY_BYTES = 2 * 1024 * 1024
SSE_HEARTBEAT_SECONDS = 15


class RequestBodyTooLarge(Exception):
    """Raised when a streamed API request exceeds the configured body limit."""


class RequestBodyLimitMiddleware:
    """Hard cap API request bodies, including requests without Content-Length."""

    def __init__(self, app, max_body_bytes: int = MAX_API_BODY_BYTES) -> None:
        self.app = app
        self.max_body_bytes = max_body_bytes

    async def __call__(self, scope, receive, send) -> None:
        if scope["type"] != "http" or not scope.get("path", "").startswith("/api/v1/"):
            await self.app(scope, receive, send)
            return
        raw_length = dict(scope.get("headers", [])).get(b"content-length")
        if raw_length:
            try:
                if int(raw_length) > self.max_body_bytes:
                    await JSONResponse(status_code=413, content={"detail": "Richiesta troppo grande."})(scope, receive, send)
                    return
            except ValueError:
                await JSONResponse(status_code=400, content={"detail": "Header Content-Length non valido."})(scope, receive, send)
                return
        received_bytes = 0

        async def limited_receive():
            nonlocal received_bytes
            message = await receive()
            if message["type"] == "http.request":
                received_bytes += len(message.get("body", b""))
                if received_bytes > self.max_body_bytes:
                    raise RequestBodyTooLarge
            return message

        try:
            await self.app(scope, limited_receive, send)
        except RequestBodyTooLarge:
            await JSONResponse(status_code=413, content={"detail": "Richiesta troppo grande."})(scope, receive, send)


class PairDeviceRequest(BaseModel):
    pairing_code: str = Field(min_length=16, max_length=256)
    device_name: str = Field(min_length=1, max_length=80)


class RevokeDeviceRequest(BaseModel):
    device_id: str = Field(min_length=36, max_length=36)


app = FastAPI(
    title="SuperIA API",
    version="0.4.0",
    description="Router online free-only con associazione dispositivi e token revocabili.",
)
app.add_middleware(RequestBodyLimitMiddleware, max_body_bytes=MAX_API_BODY_BYTES)


@lru_cache(maxsize=1)
def get_provider() -> FreeOnlyRouter:
    """Reuse router state within a warm serverless instance."""
    return FreeOnlyRouter()


_bearer_scheme = HTTPBearer(auto_error=False)


def _required_secret(name: str) -> str:
    value = os.environ.get(name, "").strip()
    if not value:
        raise HTTPException(status_code=503, detail=f"Configurazione server mancante: {name}.")
    return value


def _auth_database_path() -> str:
    # Shared PostgreSQL URL for production; legacy SQLite path remains for local use.
    return os.environ.get("SUPERIA_DATABASE_URL", "").strip() or _required_secret("SUPERIA_AUTH_DB")


async def require_api_token(
    credentials: HTTPAuthorizationCredentials | None = Depends(_bearer_scheme),
) -> str:
    if not credentials or not credentials.credentials:
        raise HTTPException(status_code=401, detail="Token dispositivo mancante o non valido.")
    device_id = device_id_for_token(_auth_database_path(), credentials.credentials)
    if not device_id:
        raise HTTPException(status_code=401, detail="Token dispositivo scaduto, revocato o non valido.")
    return device_id


async def require_admin_token(
    credentials: HTTPAuthorizationCredentials | None = Depends(_bearer_scheme),
) -> None:
    expected = _required_secret("SUPERIA_ADMIN_TOKEN")
    supplied = credentials.credentials if credentials else ""
    if not supplied or not hmac.compare_digest(supplied, expected):
        raise HTTPException(status_code=401, detail="Credenziale amministratore non valida.")


@app.get("/", response_class=HTMLResponse, include_in_schema=False)
async def home() -> str:
    return (Path(__file__).parent / "static" / "index.html").read_text(encoding="utf-8")


@app.get("/health", tags=["system"])
async def health() -> dict[str, str]:
    return {"status": "ok", "project": "SuperIA"}


@app.post("/api/v1/device/pair", tags=["devices"])
async def pair_device(request: PairDeviceRequest) -> dict[str, object]:
    # Do not expose pairing until a shared gateway/WAF rate limit is explicitly confirmed.
    edge_limit = os.environ.get("SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED", "").strip().lower()
    if edge_limit != "true":
        raise HTTPException(status_code=503, detail="Associazione temporaneamente disabilitata: protezione anti-abuso non confermata.")
    expected = _required_secret("SUPERIA_PAIRING_CODE")
    if not hmac.compare_digest(request.pairing_code, expected):
        raise HTTPException(status_code=401, detail="Codice di associazione non valido.")
    return issue_device_token(_auth_database_path(), request.device_name)


@app.post("/api/v1/device/revoke", tags=["devices"])
async def revoke_device(
    request: RevokeDeviceRequest,
    _: None = Depends(require_admin_token),
) -> dict[str, bool]:
    revoked = revoke_device_token(_auth_database_path(), request.device_id)
    if not revoked:
        raise HTTPException(status_code=404, detail="Dispositivo non trovato o già revocato.")
    return {"revoked": True}


@app.post("/api/v1/device/rotate", tags=["devices"])
async def rotate_current_device_token(
    credentials: HTTPAuthorizationCredentials | None = Depends(_bearer_scheme),
    device_id: str = Depends(require_api_token),
) -> dict[str, object]:
    current_token = credentials.credentials if credentials else ""
    rotated = rotate_device_token(_auth_database_path(), device_id, current_token)
    if not rotated:
        raise HTTPException(status_code=401, detail="Token dispositivo scaduto o non più attivo; associa nuovamente il dispositivo.")
    return rotated


@app.post("/api/v1/device/revoke-self", tags=["devices"])
async def revoke_current_device_token(
    device_id: str = Depends(require_api_token),
) -> dict[str, bool]:
    revoked = revoke_device_token(_auth_database_path(), device_id)
    if not revoked:
        raise HTTPException(status_code=401, detail="Dispositivo già revocato o non più attivo.")
    return {"revoked": True}


@app.get("/api/v1/capabilities", tags=["system"])
async def capabilities(
    _: None = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> dict[str, object]:
    configured = await provider.list_models()
    return {
        "project": "SuperIA",
        "version": "0.4.0",
        "mode": "online-free-only",
        "modules": {"assistant": "online-free-only", "images": "planned", "video": "planned", "audio": "planned", "coding": "planned", "research": "planned", "agents": "planned"},
        "external_providers_enabled_by_default": False,
        "paid_fallback": False,
        "payment_methods_allowed": False,
        "configured_free_providers": len(configured),
    }


@app.get("/api/v1/providers", tags=["system"])
async def providers(
    _: None = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> dict[str, object]:
    return {
        "mode": "free-only",
        "paid_fallback": False,
        "providers": provider.provider_status(),
    }


@app.get("/api/v1/models", tags=["assistant"])
async def models(
    _: None = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> dict[str, object]:
    return {"provider": "free-only-router", "models": await provider.list_models()}


@app.post("/api/v1/chat/stream", tags=["assistant"])
async def chat_stream(
    request: ChatRequest,
    device_id: str = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> StreamingResponse:
    database_url = _auth_database_path()
    request_id = str(request.request_id) if request.request_id else None
    if request_id:
        idempotency_secret = _required_secret("SUPERIA_CHAT_IDEMPOTENCY_KEY")
        if len(idempotency_secret) < 32:
            raise HTTPException(status_code=503, detail="SUPERIA_CHAT_IDEMPOTENCY_KEY deve contenere almeno 32 caratteri.")
        messages = [message.model_dump() for message in request.messages]
        claim = claim_chat_request(
            database_url,
            device_id,
            request_id,
            request_payload_hash(messages, request.model),
            idempotency_secret,
        )
        if claim["state"] == "conflict":
            raise HTTPException(status_code=409, detail="Questo ID richiesta è già associato a un contenuto diverso.")
        if claim["state"] == "processing":
            raise HTTPException(status_code=409, detail="Questa richiesta è già in elaborazione.")
        lease_token = str(claim["lease_token"]) if claim["state"] == "claimed" else None
        if claim["state"] == "completed":
            async def replay_events():
                replay_payload = json.dumps({"text": claim["content"], "provider": claim["provider"], "model": claim["model"]}, ensure_ascii=False)
                done_payload = json.dumps({"provider": claim["provider"], "model": claim["model"]}, ensure_ascii=False)
                yield f"event: delta\ndata: {replay_payload}\n\n"
                yield f"event: done\ndata: {done_payload}\n\n"
            return StreamingResponse(
                replay_events(),
                media_type="text/event-stream",
                headers={"Cache-Control": "no-cache, no-transform", "X-Accel-Buffering": "no"},
            )

    async def events():
        accumulated = []
        final_model = request.model or "unknown"
        final_provider = "free-only-router"
        completed = False
        try:
            stream_iterator = provider.stream_chat(
                [message.model_dump() for message in request.messages],
                request.model,
            ).__aiter__()
            pending_next = asyncio.create_task(stream_iterator.__anext__())
            try:
                while True:
                    finished, _ = await asyncio.wait(
                        {pending_next},
                        timeout=SSE_HEARTBEAT_SECONDS,
                    )
                    if not finished:
                        if request_id and not refresh_chat_request_lease(
                            database_url, device_id, request_id, lease_token or ""
                        ):
                            raise RuntimeError("Lease della richiesta non più attivo.")
                        # SSE comment: keeps proxies alive without becoming a chat event.
                        yield ": keep-alive\n\n"
                        continue
                    try:
                        item = pending_next.result()
                    except StopAsyncIteration:
                        break
                    event = item.get("event", "delta")
                    if event == "delta":
                        accumulated.append(str(item.get("text", "")))
                        if request_id and not refresh_chat_request_lease(database_url, device_id, request_id, lease_token or ""):
                            raise RuntimeError("Lease della richiesta non più attivo.")
                    if item.get("model"):
                        final_model = str(item["model"])
                    if item.get("provider"):
                        final_provider = str(item["provider"])
                    if event == "done":
                        if request_id:
                            saved = complete_chat_request(
                                database_url, device_id, request_id,
                                "".join(accumulated), final_model, final_provider,
                                idempotency_secret,
                                lease_token or "",
                            )
                            if not saved:
                                raise RuntimeError("La richiesta non appartiene più a questo tentativo.")
                        completed = True
                    payload = json.dumps(item, ensure_ascii=False, separators=(",", ":"))
                    yield f"event: {event}\ndata: {payload}\n\n"
                    pending_next = asyncio.create_task(stream_iterator.__anext__())
            finally:
                if not pending_next.done():
                    pending_next.cancel()
                    try:
                        await pending_next
                    except (asyncio.CancelledError, StopAsyncIteration):
                        pass
                close_iterator = getattr(stream_iterator, "aclose", None)
                if close_iterator is not None:
                    await close_iterator()
            if not completed:
                raise RuntimeError("Il provider ha chiuso lo stream senza evento done.")
        except NoFreeProviderAvailable as exc:
            if request_id:
                fail_chat_request(database_url, device_id, request_id, lease_token or '')
            payload = json.dumps({"message": str(exc), "retryable": True}, ensure_ascii=False)
            yield f"event: error\ndata: {payload}\n\n"
        except Exception as exc:
            if request_id and not completed:
                fail_chat_request(database_url, device_id, request_id, lease_token or '')
            payload = json.dumps({
                "message": "Il flusso si è interrotto. Il testo già ricevuto resta visibile; non è stato effettuato un fallback dopo l'inizio della risposta.",
                "retryable": True,
                "error_type": type(exc).__name__,
            }, ensure_ascii=False)
            yield f"event: error\ndata: {payload}\n\n"

    return StreamingResponse(
        events(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache, no-transform",
            "X-Accel-Buffering": "no",
            "Connection": "keep-alive",
        },
    )

@app.post("/api/v1/chat", response_model=ChatResponse, tags=["assistant"])
async def chat(
    request: ChatRequest,
    _: None = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> ChatResponse:
    try:
        result = await provider.chat([message.model_dump() for message in request.messages], request.model)
        return ChatResponse(**result)
    except NoFreeProviderAvailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
