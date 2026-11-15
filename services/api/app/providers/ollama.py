import os
import httpx

class OllamaUnavailable(RuntimeError):
    """Ollama locale non raggiungibile o risposta non valida."""

class OllamaProvider:
    def __init__(self) -> None:
        self.base_url = os.getenv("OLLAMA_BASE_URL", "http://127.0.0.1:11434").rstrip("/")
        self.default_model = os.getenv("OLLAMA_MODEL", "llama3.2:3b")

    async def list_models(self) -> list[str]:
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                response = await client.get(f"{self.base_url}/api/tags")
                response.raise_for_status()
                data = response.json()
            return sorted(m["name"] for m in data.get("models", [])
                          if isinstance(m, dict) and isinstance(m.get("name"), str))
        except (httpx.HTTPError, ValueError, KeyError) as exc:
            raise OllamaUnavailable("Ollama non raggiungibile: avvia il servizio e controlla OLLAMA_BASE_URL.") from exc

    async def chat(self, messages: list[dict[str, str]], model: str | None = None) -> dict[str, str]:
        selected = model or self.default_model
        try:
            async with httpx.AsyncClient(timeout=180.0) as client:
                response = await client.post(
                    f"{self.base_url}/api/chat",
                    json={"model": selected, "messages": messages, "stream": False},
                )
                response.raise_for_status()
                data = response.json()
            content = data.get("message", {}).get("content")
            if not isinstance(content, str) or not content.strip():
                raise OllamaUnavailable("Il modello locale ha restituito una risposta vuota.")
            return {"content": content, "model": selected}
        except OllamaUnavailable:
            raise
        except (httpx.HTTPError, ValueError, AttributeError) as exc:
            raise OllamaUnavailable("Richiesta a Ollama non riuscita: verifica servizio e modello installato.") from exc
