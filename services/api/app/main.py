import hmac
import os
from functools import lru_cache
from pathlib import Path

from fastapi import Depends, FastAPI, Header, HTTPException
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from pydantic import BaseModel, Field

from app.device_auth import issue_device_token, revoke_device_token, validate_device_token
from app.providers.free_router import FreeOnlyRouter, NoFreeProviderAvailable
from app.schemas import ChatRequest, ChatResponse

MAX_API_BODY_BYTES = 2 * 1024 * 1024


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
) -> None:
    if not credentials or not credentials.credentials:
        raise HTTPException(status_code=401, detail="Token dispositivo mancante o non valido.")
    if not validate_device_token(_auth_database_path(), credentials.credentials):
        raise HTTPException(status_code=401, detail="Token dispositivo scaduto, revocato o non valido.")


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
