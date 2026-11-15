from pathlib import Path
from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from app.providers.ollama import OllamaProvider, OllamaUnavailable
from app.schemas import ChatRequest, ChatResponse

app = FastAPI(title="SuperIA Privata API", version="0.2.0",
              description="Backend locale e modulare di SuperIA Privata.")

def get_provider() -> OllamaProvider:
    return OllamaProvider()

@app.get("/", response_class=HTMLResponse, include_in_schema=False)
async def home() -> str:
    return (Path(__file__).parent / "static" / "index.html").read_text(encoding="utf-8")

@app.get("/health", tags=["system"])
async def health() -> dict[str, str]:
    return {"status": "ok", "project": "SuperIA Privata"}

@app.get("/api/v1/capabilities", tags=["system"])
async def capabilities() -> dict[str, object]:
    return {
        "project": "SuperIA Privata", "version": "0.2.0",
        "modules": {"assistant": "initial", "images": "planned", "video": "planned",
                    "audio": "planned", "coding": "planned", "research": "planned", "agents": "planned"},
        "external_providers_enabled_by_default": False, "chat_provider": "ollama-local"
    }

@app.get("/api/v1/models", tags=["assistant"])
async def models(provider: OllamaProvider = Depends(get_provider)) -> dict[str, object]:
    try:
        return {"provider": "ollama-local", "models": await provider.list_models()}
    except OllamaUnavailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc

@app.post("/api/v1/chat", response_model=ChatResponse, tags=["assistant"])
async def chat(request: ChatRequest, provider: OllamaProvider = Depends(get_provider)) -> ChatResponse:
    try:
        result = await provider.chat([m.model_dump() for m in request.messages], request.model)
        return ChatResponse(**result)
    except OllamaUnavailable as exc:
        raise HTTPException(status_code=503, detail=str(exc)) from exc
