# SuperIA Privata

Assistente AI personale, modulare e con priorità all'esecuzione locale. Questo repository è indipendente da **Editore Foto e Video IA**.

## Obiettivo
Riordinare in un'unica piattaforma le capacità delle 182 IA individuate nella ricognizione iniziale, usando alternative open source/gratuite e rispettando licenze e requisiti. Non si clonano codice, pesi o servizi proprietari.

## Vincoli del progetto
- Uso personale e riservato; repository e dati separati da Editore Foto e Video IA.
- Nessun abbonamento o API a pagamento obbligatori.
- Modelli locali e funzionamento offline dove possibile; Internet solo per funzioni che lo richiedono.
- Destinazione PC e Android.
- Moduli progressivi, privacy e controllo dell'utente.

## Stato attuale
- Fondamenta: backend FastAPI e documentazione iniziale.
- Assistente: Ollama locale, API chat/modelli, interfaccia web, cronologia ed esportazione.
- Memoria: nota personale locale nel browser, aggiunta al contesto chat; non addestra il modello.
- I test backend GitHub Actions recenti risultano superati. Il collaudo end-to-end su Ollama e dispositivi PC/Android è ancora da fare.
- Immagini, video, audio, coding avanzato, agenti e APK non sono ancora implementati.

## Roadmap completa
La pianificazione aggiornata in 16 fasi (0–15) è in [docs/ROADMAP.md](docs/ROADMAP.md). La Fase 0 comprende il [catalogo delle IA](docs/AI_CATALOG.md), la [matrice preliminare delle alternative gratuite/locali](docs/AI_ALTERNATIVES_MATRIX.md), la [mappatura funzionale delle 182 IA](docs/AI_FUNCTIONAL_ANALYSIS.md) il [registro tecnico delle licenze](docs/COMPONENTS_LICENSE_REGISTER.md) e il [registro macchina dei componenti](config/components.registry.json). L'inventario nominativo originale ora contiene 182 nomi su 182; resta da analizzare ogni sistema e validare alternative, licenze, costi e requisiti.

## Avvio sviluppo
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
Senza Ollama, le richieste AI restituiscono un errore esplicito. Nessun fallback cloud.

## Privacy
Cronologia e memoria personale sono conservate nel localStorage del browser sul dispositivo corrente e non sono cifrate dall'app. Non inserire dati altamente riservati su dispositivi condivisi. Dettagli: [docs/PRIVACY.md](docs/PRIVACY.md).

Non esporre il server in rete senza autenticazione e revisione di sicurezza. Non inserire segreti nel repository.
