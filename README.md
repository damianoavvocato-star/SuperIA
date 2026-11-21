# SuperIA

Assistente AI personale e modulare, con client Android nativo, inferenza esclusivamente online e selezione di provider gratuiti verificati. Repository indipendente da **Editore Foto e Video IA** e **Imperium_Italia**.

## Regole inderogabili
- Nessun abbonamento, pagamento, ricarica, carta, acquisto, overage fatturabile o fallback a pagamento.
- Sono ammessi crediti gratuiti concessi dai fornitori, anche se contabilizzati in valuta o token. Quando terminano, il provider viene sospeso fino al reset o si passa a un altro provider gratuito approvato.
- Non creare account multipli o ruotare chiavi per aggirare i termini dei fornitori.
- Le chiavi dei provider restano esclusivamente sul backend.
- L'app Android usa il backend SuperIA tramite HTTPS; non contiene modelli LLM locali.
- La cronologia delle conversazioni resta cifrata sul dispositivo; viene inviato al backend solo il contesto necessario alla richiesta.

## Stato Android e CI — 21 novembre 2026
- La chat Android usa `OnlineAssistantClient` e invia richieste a `POST /api/v1/chat` tramite HTTPS.
- Il pairing individuale è implementato: il client invia il codice di associazione a `POST /api/v1/device/pair`, riceve token e identificativo dispositivo; il token e il device ID sono conservati cifrati tramite Android Keystore. Il codice di pairing non viene persistito.
- I token dispositivo sono individuali, memorizzati come hash sul backend, revocabili e con scadenza di 90 giorni.
- La pipeline Android sul commit `9ce190cd87ed8c0114a5213ebbb534929e066c1c`, run [44925187326](https://github.com/damianoavvocato-star/SuperIA/actions/runs/44925187326), è terminata **SUCCESS al secondo tentativo**: test unitari, build debug, APK firmato, upload artefatto e test strumentali Android Keystore su emulatore.
- Artefatto installabile `superia-privata-installable`, ID `14716277709`, circa 31,2 MB, disponibile fino al 19 febbraio 2027 tramite la pagina della run.
- La pipeline backend associata (run `44925187325`) è **SUCCESS**.
- La CI non sostituisce il collaudo su telefoni reali e non prova che il servizio online sia già operativo.

## Stato backend e pubblicazione
Il backend FastAPI è implementato e testato, ma **non è ancora pubblicato**. Non sono stati creati account hosting/database, configurati segreti di produzione o attivati provider. Il pairing richiede inoltre rate limiting IP effettivo a livello gateway/WAF prima dell'esposizione pubblica: il flag di conferma presente nel codice è un gate operativo, non un rate limiter.
La persistenza di produzione richiede un database esterno persistente; non usare SQLite su filesystem temporanei come `/tmp`. La rotazione tra database/provider è una procedura documentata, non un failover automatico già operativo.

## Hosting e database
- L'audit hosting è in [docs/FREE_HOSTING_AUDIT.md](docs/FREE_HOSTING_AUDIT.md).
- Cloudflare Workers Free e Vercel Hobby restano candidati tecnici non approvati; non attivare piani a pagamento o metodi di pagamento.
- Prisma Postgres Free, Aiven PostgreSQL Free e Neon Free sono candidati database da verificare prima di qualsiasi registrazione. Nessun database è stato creato o collegato.
- La regola è fail-closed: se quote, autenticazione, persistenza o provider non sono disponibili, fermare il servizio senza addebiti.

## Rimozione Gemma
La pipeline locale LiteRT-LM/Gemma è stata rimossa dal progetto Android, inclusi motore, catalogo, importatore e dipendenza Gradle. La migrazione dell'app aggiornata elimina la vecchia cronologia locale della chat una sola volta; non elimina la memoria di progetto né l'archivio documenti.

## Backend
Prototipo FastAPI in `services/api`, con router free-only, provider disabilitati per default, autenticazione Bearer, pairing individuale e stop senza fallback a pagamento. Nessun provider va attivato prima della verifica di termini, assenza di carta/addebiti, quote e comportamento fail-closed.

## App Android
Progetto Kotlin/Jetpack Compose in [android/](android/README.md). Include chat online, configurazione backend cifrata, cronologia conversazioni cifrata, importazione documenti, estrazione testo, OCR e profilo dispositivo. La connessione online richiede un backend HTTPS pubblico e configurato.

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
La cronologia Android e i testi estratti dai documenti sono cifrati AES-GCM con chiavi non esportabili in Android Keystore. Il backend non va usato per dati altamente riservati finché hosting pubblico, hardening, rate limiting, autenticazione e policy di conservazione non sono configurati e verificati.
