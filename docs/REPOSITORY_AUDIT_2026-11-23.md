# Audit repository SuperIA — 23 novembre 2026

## Ambito e metodo
Verifica del repository pubblico `damianoavvocato-star/SuperIA`, branch predefinito `main`, commit HEAD al momento dell'audit `d168e0b2660fedf6a4fd5a3b3c6923e2fcdcee72`. Controllati metadati repository, branch, pull request, cronologia recente, albero directory principale, workflow GitHub Actions, configurazione Gradle, dipendenze Python, README, documenti architetturali e punti di ricerca nel codice. È un audit statico GitHub: non equivale a checkout locale, esecuzione test o penetration test.

## Struttura rilevata
- `.github/workflows/`: `android-build.yml`, `backend-tests.yml`, `web-preview.yml`.
- `android/`: app Kotlin/Jetpack Compose, build Gradle e sorgenti `main`, `test`, `androidTest`.
- `services/api/`: backend FastAPI, provider, static web page e test.
- `config/`: registri componenti e modelli.
- `docs/`: roadmap, audit provider/hosting, privacy, architettura, tracker e piano micro-fasi.
- `preview/index.html`: anteprima web statica separata dal backend.

## Repository e cronologia
- Repository pubblico, non archiviato, default branch `main`.
- Branch trovati: soltanto `main`.
- Pull request trovate: 0.
- HEAD verificato durante audit: `d168e0b2660fedf6a4fd5a3b3c6923e2fcdcee72`.
- Nessun ramo separato da unire o sincronizzare; la cronologia recente contiene commit incrementali su backend, Android e documentazione.

## Workflow
1. **Android unit tests**: push su `main` limitato ai percorsi `android/**` e al workflow; PR e workflow_dispatch; JDK 17, Gradle 8.11.1; esegue esclusivamente `gradle --no-daemon testDebugUnitTest`. Non esegue assemble, firma o upload APK.
2. **Backend tests**: push/PR su `main` e `develop`; PostgreSQL 16 service; Python 3.11; installa requirements-dev ed esegue `PYTHONPATH=services/api pytest -q services/api/tests`.
3. **Web preview**: deploy GitHub Pages quando cambia `preview/**` o il workflow; pubblica solo la directory `preview`. Non è deploy del backend FastAPI né dell'app Android.

### CI verificata
- Backend run 605: SUCCESS, commit `d168e0b...`, 23 novembre 2026. Run: https://github.com/damianoavvocato-star/SuperIA/actions/runs/45091156407.
- Il combined commit status restituisce `pending` con zero status checks registrati: non è prova di fallimento né di successo generale.
- Per i commit documentali recenti non è partita una run Android, perché il workflow Android è filtrato sui percorsi Android. Nessun esito Android nuovo è quindi attestato da questo audit.
- Nessun APK è stato generato da queste attività.

## Rilievi e correzioni documentali
1. README principale e README Android descrivevano l'endpoint non-streaming `/api/v1/chat` come percorso principale, mentre il client Android usa lo streaming `/api/v1/chat/stream`. I README sono stati riallineati.
2. `docs/ONLINE_BACKEND_IMPLEMENTATION.md` descriveva il vecchio token condiviso `SUPERIA_API_ACCESS_TOKEN`; la versione corrente usa pairing individuale, token device, hash server-side e revoca. Il documento è marcato come storico e rimanda alla preparazione aggiornata.
3. `docs/ARCHITECTURE.md` e `docs/PRIVACY.md` descrivevano l'originario prototipo web locale/Ollama/localStorage. Sono marcati come documenti storici, non specifiche della app Android corrente.
4. `config/android-components.registry.json` conteneva policy local-first e runtime LLM on-device; policy e stati runtime sono stati riallineati online-only.
5. Ricerca codice ha individuato `services/api/app/static/index.html` e `services/api/app/providers/ollama.py` come residui del prototipo web locale. Il backend corrente espone ancora la route root che serve la pagina statica e conserva l'endpoint legacy `/api/v1/chat`. Non sono stati rimossi in questo audit per evitare una cancellazione funzionale senza prima una micro-fase dedicata di analisi dipendenze e test. Non considerarli il client ufficiale o un fallback locale ammesso.
6. I riferimenti storici a LiteRT-LM/Gemma in documenti di archivio restano conservati quando esplicitamente marcati come superati.

## Configurazione e dipendenze
- Android: namespace/applicationId `it.superiaprivata.app`, compile/target SDK 35, min SDK 26, JDK target 17, AGP 8.9.2, Kotlin 2.2.0.
- Gradle workflow usa Gradle 8.11.1. Il progetto non contiene wrapper Gradle nel root Android, perciò CI usa l'installazione Gradle del runner.
- Backend requirements: FastAPI, Uvicorn, HTTPX, psycopg binary, cryptography; pytest per sviluppo.
- Il backend mantiene dipendenze e file del vecchio provider Ollama; il router di produzione corrente è FreeOnlyRouter e i provider gratuiti restano disabilitati per default.
- Nessun backend di produzione, database di produzione, rate limiting edge o provider reale è configurato.

## Confini e decisioni
- Inferenza AI corrente: solo backend HTTPS online e provider gratuiti verificati.
- Nessuna carta, pagamento, ricarica, overage o fallback a pagamento.
- Elaborazioni locali non-LLM possono essere valutate separatamente; nessun runtime LLM locale.
- Non creare APK intermedi; il workflow Android resta test-only.
- Non unire o copiare contenuti da Editore_Foto_Video_IA o Imperium_Italia.

## Esito micro-fase 03
Completata la ricognizione statica di repository, branch, PR, cronologia, directory principali, workflow e dipendenze. Le incongruenze documentali individuate sono state corrette o marcate come storiche. I residui web/Ollama sono documentati come legacy e rinviati a una fase di pulizia dedicata; non si dichiara che siano già stati rimossi o che la loro eliminazione sia stata testata.
