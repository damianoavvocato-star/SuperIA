# Fase 2 — Motore AI centrale e assistente online

Aggiornato: 21 novembre 2026

## Decisione tecnica attuale
L'inferenza AI di SuperIA è esclusivamente online tramite backend HTTPS. Gemma 3 1B, LiteRT-LM e il runtime LLM locale sono stati rimossi dal progetto Android e non fanno parte della roadmap attiva. Le note più vecchie in questo documento che descrivono inferenza locale, importazione Gemma o benchmark LiteRT-LM sono superate e conservate soltanto come storico.

## Funzioni già presenti nel codice
- Chat nativa Kotlin/Jetpack Compose.
- Client HTTPS OnlineAssistantClient per POST /api/v1/chat/stream (SSE).
- Pairing individuale tramite POST /api/v1/device/pair.
- Token dispositivo e device ID conservati cifrati tramite Android Keystore; codice di pairing non persistito.
- Backend: token individuali memorizzati come hash, scadenza 90 giorni e revoca.
- Cronologia chat cifrata localmente e memoria di progetto modificabile.
- Ricostruzione del prompt con cronologia pertinente, memoria e contesto documentale.
- Salvataggio della richiesta pendente e possibilità di riprenderla dopo un errore.
- Controllo autenticato dello stato del backend e dei provider disponibili.
- Router backend free-only con provider disabilitati per default e fail-closed.

## Lavoro ancora necessario
1. Pubblicare backend HTTPS su hosting gratuito verificato senza carta, pagamenti, overage o conversione automatica.
2. Configurare database PostgreSQL persistente e verificare riavvio, integrità, esportazione e ripristino.
3. Configurare rate limiting IP reale sul gateway/WAF prima di abilitare pairing pubblico; il flag di conferma nel backend non è un rate limiter.
4. Verificare e attivare almeno un provider AI gratuito reale; conservare le chiavi esclusivamente sul server.
5. Completare streaming delle risposte, timeout, gestione errori, retry sicuro e ripresa delle richieste.
6. Verificare failover tra provider gratuiti, cooldown, quota esaurita e hard-stop senza alcun fallback a pagamento.
7. Collaudare conversazione multi-turno, cronologia, memoria, documenti e contesto oltre i limiti del singolo prompt.
8. Testare pairing, autenticazione, revoca, rete assente, riavvio backend/database e indisponibilità dei provider.
9. Eseguire collaudo end-to-end su dispositivi Android reali e registrare risultati.

## CI e regola APK
Il workflow Android corrente deve eseguire soltanto gradle --no-daemon testDebugUnitTest. Non deve eseguire assembleDebug, assembleRelease, firma, upload APK o test strumentali che richiedono un APK. Non dichiarare esiti CI non verificati dopo la modifica del workflow.

Su richiesta dell'utente è vietato generare APK intermedi. La build APK definitiva sarà autorizzata una sola volta dopo il completamento e la verifica end-to-end di tutte le funzioni necessarie alla chat online.

## Criteri di chiusura della Fase 2
La fase si chiude soltanto quando backend, database, pairing, provider gratuiti, streaming, failover, gestione quote/errori, continuità conversazionale, privacy e prove su dispositivi reali risultano verificati. La presenza del codice o il superamento dei soli test unitari non basta.

## Riferimenti
- Registro generale e checklist: docs/PHASE_TRACKER.md.
- Architettura: docs/ANDROID_ONLY_ARCHITECTURE.md.
- Migrazione online-only: docs/ONLINE_ONLY_MIGRATION_PLAN.md.
- Test pratici: docs/DEVICE_SMOKE_TEST.md.