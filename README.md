# SuperIA Privata

Assistente AI personale, modulare e con priorità all'esecuzione locale. Questo repository è indipendente da **Editore Foto e Video IA**.

## Stato
- Fase 1: fondamenta e architettura documentate.
- Fase 2: prima chat API, integrazione locale Ollama e interfaccia web iniziale.
- Le funzioni non implementate restano esplicitamente pianificate.

## Avvio
Requisiti: Python 3.11+.

```bash
cd services/api
python -m venv .venv
# Windows: .venv\Scripts\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements-dev.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

Interfaccia: http://127.0.0.1:8000 — API docs: http://127.0.0.1:8000/docs

## Modello locale
Installa Ollama, avvialo e scarica un modello compatibile, ad esempio `ollama pull llama3.2:3b`.
Variabili: `OLLAMA_MODEL` (predefinito `llama3.2:3b`) e `OLLAMA_BASE_URL` (predefinito `http://127.0.0.1:11434`).
Senza Ollama, stato e interfaccia funzionano; le richieste AI restituiscono un errore esplicito. Nessun fallback cloud.

## Privacy
Avvio documentato solo su localhost. La chat non viene salvata dal backend. Non esporre in rete senza autenticazione e revisione di sicurezza. Non inserire segreti nel repository.

Roadmap: [docs/ROADMAP.md](docs/ROADMAP.md).
