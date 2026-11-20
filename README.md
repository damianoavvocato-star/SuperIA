# SuperIA Privata

Assistente AI personale e modulare, con direzione di prodotto **Android + inferenza esclusivamente online + provider gratuiti verificati**. Repository indipendente da **Editore Foto e Video IA** e **Imperium_Italia**.

## Regole inderogabili
- Nessun abbonamento, pagamento, ricarica, carta, acquisto o fallback a pagamento.
- Sono ammessi crediti gratuiti concessi dai fornitori, anche se contabilizzati in valuta o token: quando terminano, il provider viene sospeso fino al reset o si passa a un altro provider gratuito approvato.
- Le chiavi dei provider restano esclusivamente sul backend.
- L'app Android usa il backend SuperIA tramite HTTPS; non contiene modelli LLM locali.
- La cronologia delle nuove conversazioni resta cifrata sul dispositivo. Il contesto necessario viene inviato al backend solo quando l'utente invia una richiesta.

## Stato della migrazione online
- La schermata chat Android usa `OnlineAssistantClient` e invia le richieste a `POST /api/v1/chat` tramite HTTPS.
- Endpoint e token del backend sono configurabili nelle impostazioni; il token è cifrato con Android Keystore.
- È stata rimossa dal progetto Android la pipeline locale LiteRT-LM/Gemma: motore, catalogo, importatore, registry e dipendenza Gradle.
- È stata aggiunta una migrazione eseguita una sola volta che elimina la vecchia cronologia della chat locale al primo avvio della nuova versione. L'archivio cifrato resta disponibile per le conversazioni online successive.
- Il backend non è ancora pubblicato e non è stato completato il pairing sicuro con token individuali. La chat online non può essere dichiarata operativa end-to-end finché hosting, provider e test reali non sono completati.
- Non è stato ancora generato o installato un APK dopo questa rimozione. La compilazione e la CI della nuova revisione devono essere verificate.

## Backend
Prototipo FastAPI in `services/api`, con router free-only, provider disabilitati per default, autenticazione Bearer e stop senza fallback a pagamento. La persistenza dello stato del router in produzione richiede un percorso DB persistente. Nessun provider va attivato prima della verifica di termini, assenza di addebiti/carta, quote e comportamento fail-closed.

## Hosting
La valutazione dei servizi compatibili con il vincolo senza carta e senza addebiti è in [docs/FREE_HOSTING_AUDIT.md](docs/FREE_HOSTING_AUDIT.md). Cloudflare Workers Free resta un candidato tecnico da provare, non un hosting approvato o attivato.

## App Android
Progetto Kotlin/Jetpack Compose in [android/](android/README.md). Include chat online, configurazione backend cifrata, cronologia conversazioni cifrata, importazione documenti, estrazione testo, OCR e profilo dispositivo. La connessione online richiede un backend HTTPS configurato e raggiungibile.

## Roadmap
La roadmap in 16 fasi (0–15) è in [docs/ROADMAP.md](docs/ROADMAP.md); il registro operativo è in [docs/PHASE_TRACKER.md](docs/PHASE_TRACKER.md). La Fase 0 comprende il [catalogo delle 182 IA](docs/AI_CATALOG.md), la [mappatura funzionale](docs/AI_FUNCTIONAL_ANALYSIS.md), l'[audit strutturato](docs/AI_182_AUDIT.json), le [decisioni architetturali](docs/PHASE0_DECISIONS.md), la [matrice delle alternative](docs/AI_ALTERNATIVES_MATRIX.md) e i registri delle licenze/componenti.

## Avvio prototipo backend
Requisiti: Python 3.11+.

```bash
cd services/api
python -m venv .venv
# Windows: .venv\\Scripts\\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements-dev.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

Interfaccia: http://127.0.0.1:8000 — API docs: http://127.0.0.1:8000/docs

## Privacy e sicurezza
La cronologia Android e i testi estratti dai documenti sono cifrati AES-GCM con chiavi non esportabili in Android Keystore. Il backend web prototipo non va usato per dati altamente riservati finché non sono completati hardening, autenticazione e policy di conservazione. Non esporre il server in rete senza revisione di sicurezza e non inserire segreti nel repository.
