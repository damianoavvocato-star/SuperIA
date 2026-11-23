# SuperIA

Assistente AI personale e modulare, con client Android nativo e inferenza esclusivamente online tramite provider gratuiti verificati. Repository indipendente da **Editore Foto e Video IA** e **Imperium_Italia**.

## Regole inderogabili
- Nessun abbonamento, pagamento, ricarica, carta, acquisto, overage fatturabile o fallback a pagamento.
- Sono ammessi crediti gratuiti concessi dai fornitori, anche se contabilizzati in valuta o token. Quando terminano, il provider viene sospeso fino al reset o si passa a un altro provider gratuito approvato.
- Non creare account multipli o ruotare chiavi per aggirare i termini dei fornitori.
- Le chiavi dei provider restano esclusivamente sul backend.
- L'app Android usa il backend SuperIA tramite HTTPS; non contiene modelli LLM locali.
- La cronologia delle conversazioni resta cifrata sul dispositivo; viene inviato al backend solo il contesto necessario alla richiesta.

## Stato attuale — 21 novembre 2026

### Android
- Chat Compose collegata al client OnlineAssistantClient per richieste HTTPS in streaming SSE a POST /api/v1/chat/stream; il vecchio endpoint non-streaming è legacy.
- Pairing individuale tramite POST /api/v1/device/pair; token e ID dispositivo conservati cifrati tramite Android Keystore. Il codice pairing non viene persistito.
- Token individuali memorizzati come hash lato server, revocabili e con scadenza di 90 giorni.
- Cronologia cifrata localmente, memoria di progetto, ricostruzione del contesto e gestione delle richieste pendenti con possibilità di ripresa.
- Importazione PDF, DOCX, TXT e CSV, estrazione testo, archivio cifrato, ricerca lessicale e OCR PDF tramite Tesseract se è disponibile il dizionario italiano.
- La continuità del contesto è un primo incremento implementato, ma va ancora collaudata end-to-end con backend e provider online reali.

### Backend e provider
- Prototipo FastAPI in services/api, con autenticazione Bearer per dispositivo, pairing, revoca, endpoint chat e stato operativo dei provider.
- Router free-only per Gemini, Groq, OpenRouter, Cloudflare Workers AI e SambaNova; i provider sono disabilitati per default e richiedono approvazioni e credenziali esplicite.
- Nessun backend pubblico, database di produzione, segreto di produzione o provider AI è attualmente attivato.
- Il flag di conferma pairing non sostituisce un rate limiter: occorre configurare e verificare il rate limiting IP effettivo sul gateway/WAF prima dell'esposizione pubblica.
- In produzione serve un database persistente. Non usare SQLite su filesystem temporanei come /tmp.

## Blocco APK — vincolante
Su richiesta dell'utente, non generare, assemblare, firmare o pubblicare APK intermedi. Il workflow Android deve eseguire soltanto test unitari, senza assembleDebug, assembleRelease o upload di artefatti APK. L'APK definitivo sarà prodotto una sola volta, dopo che tutte le funzioni necessarie alla chat online saranno implementate e verificate end-to-end.

## Gratuità e hosting
- L'audit hosting è in docs/FREE_HOSTING_AUDIT.md.
- Prisma Postgres Free, Aiven PostgreSQL Free e Neon Free sono candidati da verificare prima di qualsiasi registrazione; nessun database è stato creato o collegato.
- Non collegare carte o attivare piani con overage o conversione automatica a pagamento.
- Quando le quote gratuite terminano, fermare il servizio o passare a un altro provider gratuito approvato, senza addebiti.

## Roadmap e verifiche
La roadmap in 16 fasi (0–15) è in docs/ROADMAP.md; il registro operativo aggiornato e la checklist per il completamento della chat online sono in docs/PHASE_TRACKER.md. La migrazione online-only è descritta in docs/ONLINE_ONLY_MIGRATION_PLAN.md.

## Avvio prototipo backend
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

## Privacy e sicurezza
La cronologia Android e i testi estratti dai documenti sono cifrati AES-GCM con chiavi non esportabili in Android Keystore. Il backend non va usato per dati altamente riservati finché hosting pubblico, hardening, rate limiting, autenticazione, retention e procedure di cancellazione non sono configurati e verificati.

### Aggiornamento tecnico — streaming chat (21 novembre 2026)
- Implementato il primo percorso SSE autenticato `POST /api/v1/chat/stream`.
- Il router gratuito legge i delta OpenAI-compatible e può passare al provider gratuito successivo soltanto prima del primo token; dopo l'inizio della risposta non concatena un secondo modello e segnala l'interruzione.
- Il client Android legge gli eventi SSE e aggiorna progressivamente il messaggio nella chat Compose; il testo parziale viene salvato in caso di errore/cancellazione.
- Aggiunti test backend per delta, failover pre-token e autenticazione SSE e test Android per le validazioni del nuovo client.
- Stato: implementazione iniziale presente, ma CI non ha ancora restituito esiti verificabili e il test end-to-end con provider reale non è stato eseguito. Non considerare la chat online pronta per produzione.

- Il client Android interrompe anche la connessione HTTP sottostante quando la coroutine viene cancellata, evitando di lasciare una lettura SSE bloccata fino al timeout.


### Richieste chat: retry e deduplicazione
L'app assegna un identificativo stabile a ogni messaggio e lo conserva nella cronologia cifrata. Il backend registra l'esito per dispositivo, rifiuta ID riutilizzati con contenuti diversi e riproduce le risposte già completate senza richiamare il provider. Le risposte memorizzate per il replay sono cifrate AES-GCM; il backend richiede il segreto stabile `SUPERIA_CHAT_IDEMPOTENCY_KEY`, solo server-side. Questa protezione non equivale a exactly-once assoluto in caso di arresto del server tra la generazione del provider e la registrazione della risposta.


### Aggiornamento tecnico — protezione dei tentativi concorrenti
- Ogni acquisizione o recupero di una richiesta riceve un token di proprietà casuale (fencing token); rinnovo lease, errore e completamento sono accettati soltanto dal proprietario corrente.
- Un vecchio stream che riprende dopo il recupero della lease non può più aggiornare, fallire o completare la richiesta appartenente al nuovo tentativo.
- Il database esistente viene aggiornato aggiungendo la colonna lease_token senza eliminare le risposte cifrate già registrate.
- Corretto anche il primo claim: l'inserimento riuscito restituisce immediatamente lo stato claimed e il relativo token, anziché essere scambiato per un duplicato già in elaborazione.
- Aggiunti test per fencing token, recupero lease, retry fallito e migrazione SQLite. Esecuzione CI ancora da verificare; nessun test è dichiarato superato.


### Verifica backend — 23 novembre 2026
GitHub Actions Backend tests, run 577, commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`: **60 test superati**, 1 warning, nessun test fallito. La verifica include PostgreSQL effimero, streaming SSE, deduplicazione e fencing delle richieste. Non sostituisce il collaudo end-to-end con provider reale e app Android; nessun APK viene generato in questa fase.


## Piano operativo aggiornato — 23 novembre 2026
Il piano esecutivo corrente è [docs/MASTER_MICRO_PHASE_PLAN.md](docs/MASTER_MICRO_PHASE_PLAN.md), articolato in 50 micro-fasi. Il [registro operativo](docs/PHASE_TRACKER.md) contiene i gate obbligatori e prevale sulle descrizioni sintetiche o storiche delle roadmap precedenti. Le fasi 01–04 sono completate; la fase 05 è il prossimo audit operativo. La presenza del codice non equivale a disponibilità online: hosting, database persistente, rate limiting edge e provider reale restano da configurare e verificare. Nessun APK intermedio.
