# SuperIA Privata

Assistente AI personale, modulare e con priorità all'esecuzione locale. Questo repository è indipendente da **Editore Foto e Video IA**.

## Stato
- Fase 1: fondamenta e architettura documentate.
- Fase 2: backend chat FastAPI, integrazione locale Ollama, interfaccia web, selezione modello, cronologia conversazioni locale, esportazione e cancellazione.
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

## Cronologia e privacy
Le conversazioni sono conservate nel localStorage del browser sul dispositivo corrente; il backend non le persiste. Puoi creare, selezionare, eliminare, svuotare ed esportare le chat. Il localStorage non è cifrato dall'app: evita dati altamente riservati su dispositivi condivisi. Dettagli: [docs/PRIVACY.md](docs/PRIVACY.md).

Non esporre il server in rete senza autenticazione e revisione di sicurezza. Non inserire segreti nel repository.

Roadmap: [docs/ROADMAP.md](docs/ROADMAP.md).
