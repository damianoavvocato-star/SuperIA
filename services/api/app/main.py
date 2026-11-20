import hmac
import os
from pathlib import Path
from fastapi import Depends, FastAPI, HTTPException
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer
from fastapi.responses import HTMLResponse

from app.providers.free_router import FreeOnlyRouter, NoFreeProviderAvailable
from app.schemas import ChatRequest, ChatResponse

app = FastAPI(
    title="SuperIA Privata API",
    version="0.3.0",
    description="Router online con vincolo inderogabile free-only e nessun fallback a pagamento.",
)


def get_provider() -> FreeOnlyRouter:
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
