import hmac
import os
from functools import lru_cache
from pathlib import Path

from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import HTMLResponse, JSONResponse
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

from app.providers.free_router import FreeOnlyRouter, NoFreeProviderAvailable
from app.schemas import ChatRequest, ChatResponse

MAX_API_BODY_BYTES = 2 * 1024 * 1024


class RequestBodyLimitMiddleware:
    """Hard cap API request bodies, including requests without Content-Length."""

    def __init__(self, app, max_body_bytes: int = MAX_API_BODY_BYTES) -> None:
        self.app = app
        self.max_body_bytes = max_body_bytes

    async def __call__(self, scope, receive, send) -> None:
        if scope["type"] != "http" or not scope.get("path", "").startswith("/api/v1/"):
            await self.app(scope, receive, send)
            return

        headers = dict(scope.get("headers", []))
        raw_length = headers.get(b"content-length")
        if raw_length:
            try:
                if int(raw_length) > self.max_body_bytes:
                    await JSONResponse(
                        status_code=413,
                        content={"detail": "Richiesta troppo grande."},
                    )(scope, receive, send)
                    return
            except ValueError:
                await JSONResponse(
                    status_code=400,
                    content={"detail": "Header Content-Length non valido."},
                )(scope, receive, send)
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
            await JSONResponse(
                status_code=413,
                content={"detail": "Richiesta troppo grande."},
            )(scope, receive, send)


class RequestBodyTooLarge(Exception):
    """Raised when a streamed API request exceeds the configured body limit."""


app = FastAPI(
    title="SuperIA Privata API",
    version="0.3.0",
    description="Router online con vincolo inderogabile free-only e nessun fallback a pagamento.",
)
app.add_middleware(RequestBodyLimitMiddleware, max_body_bytes=MAX_API_BODY_BYTES)


@lru_cache(maxsize=1)
def get_provider() -> FreeOnlyRouter:
    """Reuse router state within a warm serverless instance.

    Vercel may recycle instances at any time: /tmp SQLite is only a local
    best-effort cache, never the source of durable conversation history.
    """
    return FreeOnlyRouter()


_bearer_scheme = HTTPBearer(auto_error=False)


async def require_api_token(
    credentials: HTTPAuthorizationCredentials | None = Depends(_bearer_scheme),
) -> None:
    configured_token = os.environ.get("SUPERIA_API_ACCESS_TOKEN", "").strip()
    if not configured_token:
        raise HTTPException(
            status_code=503,
            detail="Backend non configurato: token di accesso non impostato.",
        )
    supplied_token = credentials.credentials if credentials else ""
    if not supplied_token or not hmac.compare_digest(supplied_token, configured_token):
        raise HTTPException(status_code=401, detail="Token di accesso mancante o non valido.")


@app.get("/", response_class=HTMLResponse, include_in_schema=False)
async def home() -> str:
    return (Path(__file__).parent / "static" / "index.html").read_text(encoding="utf-8")


@app.get("/health", tags=["system"])
async def health() -> dict[str, str]:
    return {"status": "ok", "project": "SuperIA Privata"}


@app.get("/api/v1/capabilities", tags=["system"])
async def capabilities(
    _: None = Depends(require_api_token),
    provider: FreeOnlyRouter = Depends(get_provider),
) -> dict[str, object]:
    configured = await provider.list_models()
    return {
        "project": "SuperIA Privata",
        "version": "0.3.0",
        "mode": "online-free-only",
        "modules": {
            "assistant": "online-free-only",
            "images": "planned",
            "video": "planned",
            "audio": "planned",
            "coding": "planned",
            "research": "planned",
            "agents": "planned",
        },
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
    configured = await provider.list_models()
    return {
        "mode": "free-only",
        "paid_fallback": False,
        "providers": [
            {"id": item, "enabled": any(model.startswith(item + ":") for model in configured)}
            for item in FreeOnlyRouter.PROVIDER_ORDER
        ],
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
