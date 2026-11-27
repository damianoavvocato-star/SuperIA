# Piano operativo SuperIA — 50 micro-fasi

Aggiornato: 27 novembre 2026 — 22/50 micro-fasi completate; micro-fase 29 validata in CI emulator; gate chat online 36 bloccato  
Ambito: esclusivamente `damianoavvocato-star/SuperIA`. Questo piano scompone le 16 macro-aree della roadmap in 50 unità di lavoro verificabili. Non sostituisce i requisiti vincolanti di README e PHASE_TRACKER: li rende eseguibili passo per passo.

## Regole di esecuzione

- Si lavora su una micro-fase per volta, in ordine, senza dichiararla conclusa finché il relativo criterio di accettazione non è verificato.
- Ogni micro-fase registra: obiettivo, file/area interessata, modifica, test, risultato CI o prova pratica, commit e limiti residui.
- Stati ammessi: **Da iniziare**, **In corso**, **In attesa di verifica**, **Bloccata da dipendenza**, **Completata**. Il codice scritto da solo non basta per lo stato Completata.
- Una micro-fase completata non viene riaperta senza annotare il motivo e la regressione.
- Prima di iniziare una fase si controllano le dipendenze e il branch head di `main`; dopo la modifica si controllano diff, test pertinenti e CI disponibile.
- Non creare rami aggiuntivi per frammentare il lavoro: al controllo del 23 novembre 2026 il repository ha soltanto `main` e nessuna pull request. Usare commit piccoli e leggibili su `main`, salvo futura necessità tecnica documentata.
- **Blocco APK assoluto**: nessun assembleDebug/assembleRelease, firma, upload o distribuzione di APK intermedi. Un'unica build APK finale soltanto dopo la micro-fase 36 e dopo tutte le verifiche obbligatorie della chat online.
- Nessun costo: niente carte, pagamenti, ricariche, overage, account multipli o elusione dei limiti. Crediti gratuiti legittimi ammessi; quando finiscono, stop o rotazione verso altro provider gratuito approvato.
- Inferenza esclusivamente online tramite backend SuperIA HTTPS. Nessun runtime LLM locale e nessuna reintroduzione di Gemma/Ollama.
- SuperIA resta separata da Editore_Foto_Video_IA e Imperium_Italia.

## Quadro di avanzamento

| Gruppo | Micro-fasi | Obiettivo |
|---|---:|---|
| Ricognizione e fondamenta | 01–07 | Inventario, decisioni, repository, documentazione e test di base |
| Sicurezza e infrastruttura online | 08–18 | Identità, pairing, protezioni, hosting, database e segreti |
| Motore chat online | 19–30 | Provider gratuiti, streaming, retry, memoria, documenti e privacy |
| Collaudo e sblocco chat | 31–36 | Deploy, prove reali, sicurezza, telefoni e gate finale chat |
| Espansione funzionale | 37–50 | Ricerca, traduzione, media, coding, produttività, agenti e rilascio |

## Elenco delle 50 micro-fasi

### A. Ricognizione e fondamenta

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 01 | Inventario e riallineamento policy delle 182 IA | Conteggio 182/182 verificato; policy online-only applicata ai 182 record; nessuna voce è dichiarata integrata o verificata; audit di provider, licenze e quote resta alla micro-fase 19 e ai relativi test. | Completata |
| 02 | Consolidamento delle decisioni vincolanti | Un'unica policy corrente conferma Android-only, online-only, gratuità, privacy e separazione dei progetti; registri e documenti legacy sono marcati come storici/superati. | Completata |
| 03 | Audit repository, branch e cronologia | Branch, PR, commit recenti, workflow e file principali sono registrati; incongruenze documentali corrette o marcate storiche; residui legacy documentati senza cancellazioni non verificate. | Completata |
| 04 | Allineamento della documentazione | README, ROADMAP, PHASE_TRACKER e documenti di migrazione non si contraddicono; le note storiche sono chiaramente archiviate. | Completata |
| 05 | Audit del client Android | Configurazione HTTPS, token, storage cifrato, gestione errori e UI chat sono mappati rispetto al codice corrente. | Completata |
| 06 | Baseline dei test Android senza APK | Il workflow esegue solo testDebugUnitTest; il risultato CI del commit corrente è acquisito e non contiene task di assemblaggio APK. | Completata |
| 07 | Baseline dei test backend | Suite backend e PostgreSQL CI sono riproducibili; esito, commit, numero test e warning sono registrati. | Completata |

### B. Sicurezza e infrastruttura online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 08 | Validazione centralizzata della configurazione backend | Variabili obbligatorie, valori non validi e configurazioni insicure vengono rifiutati con comportamento fail-closed e test. | Completata |
| 09 | Autenticazione individuale dei dispositivi | Token individuali, hash lato server, scadenza e isolamento tra dispositivi sono coperti da test. | Completata |
| 10 | Pairing sicuro | Codice pairing, emissione token, limiti, scadenza e risposte d'errore sono verificati. | Completata |
| 11 | Revoca e rotazione credenziali dispositivo | Revoca immediata, token scaduti e tentativi successivi sono verificati sia lato server sia Android. | Completata |
| 12 | Rate limiting IP effettivo | Gateway/WAF applica limiti reali prima del pairing pubblico; prove dimostrano blocco, finestra e recupero. Il flag applicativo da solo non vale come prova. | Bloccata da dipendenza esterna |
| 13 | Selezione hosting gratuito | Provider hosting verificato su termini attuali: nessuna carta, pagamento, overage o conversione automatica a piano pagato. | Completata |
| 14 | Pubblicazione backend HTTPS di staging | Backend raggiungibile tramite HTTPS, segreti protetti e deploy ripetibile; health e readiness sono distinti. | Bloccata da dipendenza esterna |
| 15 | Database PostgreSQL persistente | Database condiviso e persistente configurato; nessuno stato di produzione dipende da filesystem temporanei. | Bloccata da dipendenza esterna |
| 16 | Migrazione e verifica dati | Migrazione SQLite/PostgreSQL verificata con conteggi, hash, schema e test di rollback/errore. | Completata |
| 17 | Gestione dei segreti di produzione | Chiavi provider, token amministrativi e chiave idempotenza sono solo nel secret manager; rotazione e assenza segreti nei log testate. | Bloccata da dipendenza esterna |
| 18 | Readiness e diagnostica operativa | Endpoint diagnostici distinguono processo, database, autenticazione e provider senza rivelare segreti. | Completata |

### C. Motore chat online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 19 | Audit provider AI gratuiti | Ogni provider candidato è verificato su termini, quota gratuita reale, carta richiesta, hard-stop, modelli e limiti. | Bloccata da dipendenza esterna |
| 20 | Attivazione controllata del primo provider gratuito | Una sola integrazione è provata con credenziale reale server-side e senza rischio di addebito. | Bloccata da dipendenza esterna |
| 21 | Guard-rail free-only | Configurazione errata, quota finita, 402 e billing non consentito causano stop; nessun fallback pagato è possibile. | Completata |
| 22 | Rotazione tra provider approvati | Cooldown, selezione alternativa, quote e indisponibilità sono testati; nessuna rotazione di account/chiavi per eludere limiti. | Completata |
| 23 | Contratto conversazionale multi-turno | Messaggi, ruoli, limiti, contesto e identificativi richiesta sono coerenti tra Android e backend. | Completata |
| 24 | Streaming SSE lato backend | Eventi delta/done/error, delimitatori, headers, timeout e chiusura stream sono verificati. | Completata |
| 25 | Streaming SSE lato Android | Parsing incrementale, ordine dei delta, completamento, errori e cancellazione sono coperti da test. | Completata |
| 26 | Heartbeat e connessioni lente | Heartbeat, rinnovo lease, timeout proxy e cancellazione provider alla chiusura client sono verificati. | In attesa di verifica pratica (test CI heartbeat/lease superato; proxy e dispositivo fisico restano aperti) |
| 27 | Idempotenza e deduplicazione richieste | Request ID, hash payload, replay cifrato, lease e fencing sono testati su PostgreSQL; limiti exactly-once sono documentati. | Completata in CI; staging reale aperto |
| 28 | Retry e ripresa dopo interruzione | Retry usa lo stesso ID, non duplica risposte già persistite e comunica chiaramente quando deve rigenerare invece di riprendere lo stream. | In attesa di verifica pratica |
| 29 | Continuità della conversazione e memoria | Cronologia persistente, riepiloghi verificabili, recupero contesto e ripresa dopo riavvio funzionano senza imporre una nuova chat. | Completata in CI emulator; telefono fisico separato alla fase 35 |
| 30 | Contesto documentale e privacy dei dati | PDF/DOCX/TXT/CSV, retrieval, citazioni, minimizzazione contesto, log, retention, export e cancellazione sono testati e documentati. | In attesa di verifica |

### D. Collaudo e sblocco della chat online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 31 | Deploy di staging ripetibile | Deploy, migrazioni, restart, rollback e monitoraggio essenziale sono ripetibili senza interventi manuali nascosti. | Bloccata da dipendenza esterna |
| 32 | Test end-to-end backend-provider | Pairing, autenticazione, chat multi-turno, streaming e persistenza sono provati contro provider reale gratuito. | Bloccata da dipendenza esterna |
| 33 | Test quote finite e indisponibilità | Esaurimento quota, provider down, 401/403/429/5xx, rete assente e database non disponibile non producono addebiti o perdita silenziosa. | Completata con test simulati |
| 34 | Test sicurezza e abuso | Rate limit, revoca, accesso incrociato, input oversized, segreti, log e concorrenza superano prove mirate. | In attesa di verifica; WAF reale mancante |
| 35 | Collaudo su telefoni Android reali | Pairing e chat sono provati su dispositivi fisici, con Wi-Fi/mobile, sospensione app, riavvio, rete interrotta e ripresa. | Bloccata da dipendenza esterna |
| 36 | Gate finale chat online | Tutti i punti 1–12 del PHASE_TRACKER sono chiusi con evidenze. Solo dopo questa fase si può autorizzare la singola build APK finale della chat. | Bloccata da dipendenze esterne |

### E. Espansione funzionale dell'applicazione

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 37 | Ricerca web e analisi link | Ricerca su richiesta, estrazione pagine/link, fonti, date e citazioni sono integrate e testate. | Da iniziare |
| 38 | Traduzione e localizzazione | Traduzione testi/documenti e gestione lingua sono integrate con controlli qualità e limiti dichiarati. | Da iniziare |
| 39 | Generazione immagini | Pipeline gratuita approvata o componente legittimo, limiti e risultati sono verificati. | Da iniziare |
| 40 | Modifica immagini e OCR | Editing, rimozione/sostituzione elementi, OCR e gestione file sono integrati e testati. | Da iniziare |
| 41 | Comprensione ed editing video | Import, analisi, taglio, sottotitoli, traduzione e rendering sono verificati su file di prova. | Da iniziare |
| 42 | Trascrizione e analisi audio | Speech-to-text, diarizzazione se supportata e pulizia audio sono integrate con limiti documentati. | Da iniziare |
| 43 | Sintesi vocale e doppiaggio | Text-to-speech, voci e doppiaggio rispettano consensi, diritti e vincoli dei provider. | Da iniziare |
| 44 | Agente programmazione e GitHub | Analisi repository, patch, diff, branch, commit, PR e Actions richiedono permessi minimi e approvazioni. | Da iniziare |
| 45 | Produttività e dati | Documenti, fogli, CSV, formule, grafici, presentazioni ed esportazione sono testati. | Da iniziare |
| 46 | Agenti e automazioni | Workflow multi-step, pianificazione, ripresa, audit e conferme per azioni sensibili sono funzionanti. | Da iniziare |
| 47 | Moduli specialistici e catalogo IA | Le capacità delle 182 IA sono mappate a moduli integrati o alternative legittime, con licenze e stato reale verificati. | Da iniziare |
| 48 | Interfaccia unica e gestione progetti | Chat principale, progetti, moduli, dispositivi e attività sono accessibili senza frammentare la conversazione. | Da iniziare |
| 49 | Prestazioni, accessibilità e compatibilità | Test su telefoni/tablet supportati, consumi, accessibilità, stabilità e limiti hardware sono documentati. | Da iniziare |
| 50 | Collaudo completo e rilascio finale | Tutti i moduli inclusi superano test automatici e pratici; documentazione, privacy, licenze e unico pacchetto finale sono verificati. | Da iniziare |

## Regola pratica per ogni micro-fase

Prima di lavorare:
1. leggere stato e dipendenze della fase;
2. controllare branch `main` e commit corrente;
3. scegliere una sola unità di lavoro.

Dopo aver lavorato:
1. controllare il diff;
2. eseguire soltanto i test pertinenti;
3. attendere e verificare CI quando disponibile;
4. registrare commit e prova;
5. aggiornare lo stato senza anticipare il completamento;
6. passare alla micro-fase successiva soltanto se quella corrente è completata o se il blocco è esterno e chiaramente documentato.

## Stato al momento della suddivisione — 23 novembre 2026

- Branch presente: `main` soltanto; nessuna pull request trovata.
- Ultimi commit comprendono hardening SSE, heartbeat, idempotenza/fencing e test client Android.
- Backend CI run 577: 60 test superati, 1 warning, nessun fallimento, sul commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`. Questo non è un test end-to-end reale.
- I test Android aggiunti più di recente non risultano ancora verificati da una run CI consultabile; non marcarli come superati.
- Backend pubblico, database di produzione, provider gratuito reale, rate limiting edge e test su telefoni reali restano dipendenze aperte.
- Nessun APK intermedio deve essere prodotto.


### Evidenza micro-fase 01 — 23 novembre 2026

- Conteggio JSON verificato: 182/182 record, numerazione completa da 1 a 182.
- Tutti i 182 record sono stati riallineati alla policy di inferenza online tramite backend SuperIA; strumenti locali ammessi solo per attività non-LLM.
- 16 riferimenti candidati a inferenza LLM locale sono stati sostituiti con il percorso provider online approvato.
- Matrice alternative e analisi funzionale marcate come documenti storici dove proponevano local-first; registro componenti marca Ollama e llama.cpp come superati/non integrabili per inferenza.
- Non è stata dichiarata alcuna verifica individuale di prezzi, termini, quote, licenze, disponibilità o compatibilità: tali controlli restano esplicitamente aperti e saranno eseguiti sulle alternative/provider effettivamente selezionati.
- Verifica strutturale post-commit: nessun candidato nei 182 record conserva riferimenti a inferenza LLM locale; nessun record è stato marcato come integrato.


### Evidenza micro-fase 02 — 23 novembre 2026

- README e PHASE_TRACKER restano le fonti operative correnti; ROADMAP, decisioni Fase 0 e architettura Android ora marcano esplicitamente le proposte di inferenza LLM locale come storiche e superate.
- Il registro modelli locali è stato declassato a riferimento storico: candidati, runtime e decisioni non autorizzano download o integrazioni.
- La policy corrente è coerente: client Android, inferenza AI esclusivamente online via backend HTTPS, provider gratuiti verificati, nessun fallback locale LLM o a pagamento; elaborazioni locali non-LLM consentite solo previa verifica.
- Verifica documentale completata; non equivale a deploy o collaudo runtime.


### Evidenza micro-fase 03 — 23 novembre 2026

Audit statico registrato in [docs/REPOSITORY_AUDIT_2026-11-23.md](REPOSITORY_AUDIT_2026-11-23.md). Repository pubblico non archiviato, default branch `main`, un solo branch e zero PR. Workflow Android esegue solo `testDebugUnitTest`; backend CI run 605 SUCCESS sul commit precedente all'audit; web-preview deploya soltanto `preview/`. README aggiornati al percorso SSE; guida backend token condiviso, architettura e privacy del prototipo marcate storiche; registro componenti Android riallineato. Residui `services/api/app/static/index.html`, `providers/ollama.py` e route legacy `/api/v1/chat` sono stati documentati e non rimossi: richiedono una micro-fase dedicata di pulizia e test.


### Esito micro-fase 04 — 23 novembre 2026
Documentazione principale allineata alla policy online-only, al piano operativo da 50 micro-fasi e allo stato reale della chat SSE. README, ROADMAP, PHASE0_DECISIONS e PHASE_TRACKER riportano la prevalenza del piano corrente; le indicazioni obsolete nel verbale storico sono esplicitamente superate. ONLINE_BACKEND_IMPLEMENTATION resta marcato come storico e rimanda alla documentazione corrente. Verificati i contenuti aggiornati via GitHub dopo i commit. Nessun codice runtime modificato; nessun APK generato.


### Esito micro-fase 05 — audit client Android — 23 novembre 2026
Audit statico dei sorgenti Android registrato in [ANDROID_CLIENT_AUDIT_2026-11-23.md](ANDROID_CLIENT_AUDIT_2026-11-23.md). Verificati HTTPS e rifiuto URL pericolosi, pairing individuale, token cifrato, SSE, UUID request, cancellazione connessione, cronologia/memoria cifrate, prompt bounded e ripresa manuale. Rilevato il metodo legacy non-streaming `send()` ancora presente; non rimosso senza verifica riferimenti e test. L'audit non equivale a test su telefono reale.

### Esito micro-fase 06 — baseline Android — 23 novembre 2026
Workflow verificato: JDK 17, Gradle 8.11.1, comando unico `gradle --no-daemon testDebugUnitTest`; nessun task assemble/sign/upload APK. Sono presenti test unitari client, ma non è stata acquisita una run CI Android verificabile sul commit corrente. La run Android 222 è stata acquisita e verificata SUCCESS; non ha eseguito assemblaggio APK.

### Esito micro-fase 07 — baseline backend — 23 novembre 2026
Verificata GitHub Actions run 577, ID `45089445756`, commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`: job PostgreSQL completato SUCCESS; log pytest: **60 passed, 1 warning in 1.31s**. Tutti gli step del job, inclusi avvio PostgreSQL e suite `services/api/tests`, sono conclusi con successo. Questa evidenza non certifica deploy, provider reale o E2E Android.


### Verifica CI Android — micro-fase 06 — 23 novembre 2026
GitHub Actions [run 222](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45091390966), ID `45091390966`, commit `739f585b168b24bc817ab0cbcc49babc799ad392`: job `android-unit-tests` SUCCESS. Log: `gradle --no-daemon testDebugUnitTest`, task `:app:testDebugUnitTest`, **BUILD SUCCESSFUL in 28s**. I passaggi job confermano JDK 17 e Gradle setup. Non compaiono task assemble/sign/upload APK. La run verifica la suite sul commit indicato, non necessariamente l'HEAD documentale successivo.


### Esito micro-fase 08 — validazione configurazione backend — 23 novembre 2026
Aggiunta `validate_provider_environment` nel router e chiamata all'inizializzazione. Rifiuta modalità free-only non valida, provider parziali, attestazioni non affirmative, modello fuori allowlist, OpenRouter senza suffisso `:free` e Cloudflare senza account ID. Test dedicati. Evidenza [Backend tests run 634](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092312609): SUCCESS, **68 passed, 1 warning in 1.14s**, PostgreSQL effimero. Rapporto: [BACKEND_CONFIG_VALIDATION_2026-11-23.md](BACKEND_CONFIG_VALIDATION_2026-11-23.md). La verifica delle condizioni reali dei provider resta micro-fase 19.


### Esito micro-fase 09 — autenticazione dispositivi — 23 novembre 2026
Audit di `device_auth.py` e test: token casuale, SHA-256 server-side, UUID per dispositivo, TTL 90 giorni, scadenza, revoca e isolamento. Aggiunti test per expiry e revoca selettiva. Backend CI [run 638](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092450472): SUCCESS, **70 passed, 1 warning in 1.08s**, PostgreSQL effimero. Rapporto [DEVICE_AUTH_VALIDATION_2026-11-23.md](DEVICE_AUTH_VALIDATION_2026-11-23.md).

### Micro-fase 10 — pairing sicuro — in corso
Test aggiunti per segreto pairing mancante, validazione dei limiti input e contenuto della risposta. CI backend sul commit `a0f08ad51a890ebdd0992fe65d7ae281d2ef6462` in attesa di completamento. Il flag edge resta solo un gate di configurazione e non sostituisce il rate limiting reale della micro-fase 12.


### Esito micro-fase 10 — pairing sicuro — 23 novembre 2026
Verificati gate edge fail-closed, segreto server-side con confronto constant-time, limiti Pydantic (pairing code 16–256, device name 1–80), token individuale e risposta senza codice pairing. Aggiunti test per segreto mancante, input fuori range e contenuto risposta. Backend CI [run 639](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092525104): SUCCESS, **76 passed, 1 warning in 1.26s**, PostgreSQL effimero. Rapporto [PAIRING_SECURITY_VALIDATION_2026-11-23.md](PAIRING_SECURITY_VALIDATION_2026-11-23.md). Il rate limiting IP effettivo resta aperto alla fase 12.


### Micro-fase 11 — revoca e rotazione token — in corso
Implementati endpoint autenticati `POST /api/v1/device/rotate` e `POST /api/v1/device/revoke-self`; la rotazione sostituisce il token mantenendo il device ID e usa compare-and-swap sul token hash corrente per impedire doppie rotazioni concorrenti. Android dispone di rinnovo e revoca remota nella schermata Impostazioni. L'endpoint associato è ora fissato separatamente dal campo di inserimento: le richieste autenticate non devono inviare il Bearer token a un endpoint modificato dall'utente. Test backend e Android in esecuzione; fase non completata finché entrambi i workflow finali non risultano SUCCESS.


### Esito micro-fase 11 — revoca e rotazione — 23 novembre 2026
Backend: endpoint authenticated rotate/self-revoke; rotazione token compare-and-swap, mantiene device ID e invalida il token precedente; test su scadenza, revoca, concorrenza e autenticazione. Android: metodi rotate/revoke e controlli UI; endpoint associato fissato separatamente dal campo modificabile per evitare inoltro del Bearer token a un host cambiato. Backend CI [run 661](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093088810): **83 passed, 1 warning**, SUCCESS. Android CI [run 229](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092957056): SUCCESS, BUILD SUCCESSFUL in 36s, test-only. Rapporto [DEVICE_TOKEN_LIFECYCLE_2026-11-23.md](DEVICE_TOKEN_LIFECYCLE_2026-11-23.md). Test fisico resta alla fase 35.


### Esito micro-fase 12 — rate limiting gateway — bloccata — 23 novembre 2026
Verificata documentazione ufficiale Cloudflare WAF Free: una regola, path/IP, finestra e mitigazione minime di 10 secondi. Preparata configurazione proposta in [EDGE_RATE_LIMIT_PREPARATION_2026-11-23.md](EDGE_RATE_LIMIT_PREPARATION_2026-11-23.md). Non esiste dominio/zone autorizzato né backend pubblico; Cloudflare avverte inoltre che i contatori possono avere ritardo e lasciare passare richieste eccedenti. Il flag edge resta false. Serve dominio/zone e prova esterna; pairing pubblico resta disabilitato.

### Esito micro-fase 13 — selezione hosting — completata per staging — 23 novembre 2026
Selezionati Render Free Web Service (staging only) + Aiven PostgreSQL Free (database di staging), con limiti e hard-stop documentati in [FREE_STAGING_HOST_SELECTION_2026-11-23.md](FREE_STAGING_HOST_SELECTION_2026-11-23.md). EthioDeploy escluso per overage fatturabile; Render Postgres escluso per scadenza a 30 giorni. Nessun account creato o servizio attivato. Non è una selezione per produzione.

### Esito micro-fase 14 — deploy HTTPS staging — bloccata da autorizzazione esterna — 23 novembre 2026
Preparato `render.yaml` con piano Free, regione Frankfurt, rootDir `services/api`, health check `/health`, autoDeploy false, provider AI disabilitati e pairing edge false. Segreti dichiarati solo `sync: false`. Guida: [RENDER_STAGING_DEPLOY_GUIDE.md](RENDER_STAGING_DEPLOY_GUIDE.md). Non sono stati creati account, installate GitHub App, provisionati DB o servizi né eseguito deploy: occorre consenso esplicito dell'utente. Stato resta bloccato.


### Esito micro-fase 15 — database persistente — bloccata da provisioning esterno
Implementata guardia fail-closed: staging/production richiedono PostgreSQL per auth e stato router; SQLite e `/tmp` restano solo development/test. Test automatici superati. Nessun database esterno è stato creato: serve consenso per Aiven e provisioning. Rapporto [DATABASE_PERSISTENCE_AND_MIGRATION_2026-11-23.md](DATABASE_PERSISTENCE_AND_MIGRATION_2026-11-23.md).

### Esito micro-fase 16 — migrazione dati — completata
Testata migrazione SQLite/PostgreSQL con conteggi, digest SHA-256, rifiuto destinazione non vuota, errore schema, rollback integrale e assenza tabella provider_state. Backend CI [run 680](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093532739): **99 passed, 1 warning in 1.57s**, PostgreSQL effimero. Nessun dato utente migrato.

### Micro-fase 17 — gestione segreti — in corso
Esteso `.gitignore` a file ambiente, chiavi e directory secrets; aggiunto `services/api/.env.example` con placeholder vuoti e provider disabilitati; aggiunto test che impedisce la divulgazione delle API key nelle diagnostiche. Ricerca repository per pattern comuni di chiavi non ha restituito match. CI finale in attesa; il secret manager di produzione resta da configurare dopo il deploy.


### Esito micro-fase 17 — igiene segreti — bloccata per secret manager produzione
Esteso `.gitignore`, aggiunto template `services/api/.env.example` con soli placeholder e provider disabilitati, test redazione API key nelle diagnostiche. Code search per pattern comuni di chiavi e token non ha trovato match. Backend CI [run 683](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093635048): **100 passed, 1 warning**, SUCCESS. Secret manager e rotazione produzione richiedono deploy/account esterno, non ancora autorizzati. Rapporto [SECRET_MANAGEMENT_BASELINE_2026-11-23.md](SECRET_MANAGEMENT_BASELINE_2026-11-23.md).


### Esito micro-fase 18 — readiness — completata
Aggiunto `GET /api/v1/readiness` protetto da token amministrativo; distingue processo, database, schema device auth e configurazione provider senza esporre URL, nomi provider o segreti. `/health` resta solo process health. Backend CI [run 691](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093774332): **102 passed, 1 warning in 1.19s**, SUCCESS. Rapporto [READINESS_DIAGNOSTICS_2026-11-23.md](READINESS_DIAGNOSTICS_2026-11-23.md). Deploy reale resta necessario per verificare servizi esterni.


### Esito micro-fase 19 — audit provider — bloccata per account-level verification
Completato audit pubblico dei 5 candidati: Groq e Cloudflare condizionati, OpenRouter non approvato per chat privata, Gemini non approvato per training sui prompt Free, SambaNova escluso per payment method e acquisto crediti. Aggiunta attestazione `DATA_POLICY_APPROVED` e rimosso SambaNova dal router. Nessun provider attivo. Rapporto [AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md](AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md). Serve verifica no-card, hard-stop e ZDR nel pannello account.

### Esito micro-fase 20 — attivazione provider — bloccata da consenso esterno
Non creati account né API key; non autorizzato il collegamento a provider. Non attivare provider senza consenso e verifica account-level. Nessun test reale di inferenza.

### Esito micro-fase 21 — guard-rail free-only — completata
Router richiede `SUPERIA_FREE_ONLY`, attestazioni economiche e `DATA_POLICY_APPROVED`, modello in allowlist; OpenRouter solo `:free`; Cloudflare account ID obbligatorio; SambaNova rifiutato. 429 cooldown/failover solo tra provider free approvati; 401/402/403 disabilita; 408/5xx/rete cooldown; mai switch dopo primo token; nessun fallback locale/pagato. Backend CI [run 705](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45094107321): **104 passed, 1 warning**, SUCCESS. Rapporto [PROVIDER_AUDIT_AND_FREE_GUARDS_2026-11-23.md](PROVIDER_AUDIT_AND_FREE_GUARDS_2026-11-23.md).


### Esito micro-fasi 22–28 — motore chat online — 23 novembre 2026
22 completata: failover solo tra provider free approvati, cooldown e stop. 23 completata: contratto API 1–60 messaggi, ruoli validi, 32k per messaggio; Android limita anche il prompt totale con memoria/documenti/cronologia. Limite noto: Android invia il contesto come un singolo messaggio user con ruoli esplicitati nel testo. 24 completata: rimossi route non-streaming, root web prototype, static index, Ollama backend e client send; solo SSE resta esposto. 25 completata: parser SSE testabile e copertura delta/done/error/heartbeat/troncamento/escape JSON. 26 in attesa: heartbeat e rinnovo lease testati, ma disconnessione su telefono/rete reale manca. 27 completata: dedupe, replay cifrato, 409, lease/fencing verificati. 28 in attesa: request ID e pending persistono, ma test app kill/restart reale manca. Backend CI [run 738](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45094905446): **109 passed, 1 warning**; Android CI [run 246](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45095009850): SUCCESS, BUILD SUCCESSFUL in 39s. Rapporto [ONLINE_CHAT_ENGINE_AUDIT_2026-11-23.md](ONLINE_CHAT_ENGINE_AUDIT_2026-11-23.md).


### Esito micro-fasi 29–35 — continuità, documenti e collaudo — 23 novembre 2026
29 in attesa: cronologia AES-GCM/Keystore, AtomicFile, pending ID, memoria editabile/cancellabile e export TXT implementati; instrumented test presenti ma non eseguiti su device. 30 in attesa: documenti locali cifrati, BM25, fonti, contesto 6k, prompt 32k, export TXT e delete implementati; OCR/storage reali da verificare. 31 bloccata da account/deploy esterno. 32 bloccata da provider reale e credenziale. 33 completata a livello simulato: 401/402/403/408/429/5xx/rete, DB, cooldown/failover e hard-stop; Backend CI [run 750](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45095467059): **110 passed, 1 warning**. 34 in attesa: token, revoca, rotazione, isolamento device, input e segreti testati; WAF rate limit non disponibile. 35 bloccata fino a backend/provider e telefono reale. Rapporto [CONVERSATION_AND_DOCUMENTS_VALIDATION_2026-11-23.md](CONVERSATION_AND_DOCUMENTS_VALIDATION_2026-11-23.md).


### Gate micro-fase 36 — NON SUPERATO — 23 novembre 2026
Il gate resta bloccato: backend HTTPS pubblico, PostgreSQL persistente, WAF/IP rate limit, provider free reale approvato, secret manager e test E2E su telefono reale non sono disponibili. Non creare APK. Rapporto completo [ONLINE_CHAT_FINAL_GATE_2026-11-23.md](ONLINE_CHAT_FINAL_GATE_2026-11-23.md).


### Allineamento stato piano e tracker — 23 novembre 2026
Riconciliate le righe della tabella con le evidenze dettagliate in PHASE_TRACKER e nei rapporti: micro-fase 18 completata (CI 102 passed); 22–25 completate; 26 e 28 in attesa di prova pratica; 27 completata in CI con staging reale ancora aperto; 33 completata con test simulati; 34 in attesa per il WAF mancante. Il totale verificato resta 21/50 completate. Il gate 36 non è superato. Questa riconciliazione documentale non cambia il codice né autorizza deploy, pairing pubblico o APK.


### Aggiornamento micro-fase 34 — regressione pairing anti-spoofing — 23 novembre 2026
Aggiunto test parametrico: flag edge vuoto/false/0/yes/enabled, header proxy falsificati (`X-Forwarded-For`, `X-Real-IP`, `Forwarded`) e guardia che impedisce emissione token; verifica 503 e assenza del pairing code. Backend CI [run 45119185491](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119185491): **115 passed, 1 warning in 1.52s**. La fase 34 resta in attesa perché manca ancora rate limiting condiviso WAF/gateway reale; fase 12 bloccata e flag pairing edge resta false. Rapporto [SECURITY_PAIRING_ANTI_SPOOFING_TEST_2026-11-23.md](SECURITY_PAIRING_ANTI_SPOOFING_TEST_2026-11-23.md).


### Aggiornamento micro-fase 23 — ruoli multi-turno nativi — 23 novembre 2026
Rimosso il limite noto del precedente prompt appiattito: Android ora invia messaggi strutturati user/assistant, contesto memoria/documenti separato ed etichettato, nuovo messaggio finale e retry con gli stessi ruoli. Validazione client: ruoli, 1–60 messaggi e 32.000 caratteri totali. Backend CI [run 771](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737059): **116 passed, 1 warning**; Android CI [run 259](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737107): **SUCCESS**, testDebugUnitTest. Nessun APK.


### Aggiornamento 24 novembre 2026 — diagnostica crash instrumentation (micro-fasi 29–30)
La prima esecuzione reale su emulatore API 35 ha compilato e installato gli APK debug temporanei autorizzati per i test, ma il runner instrumentation è terminato prima di scoprire/eseguire test: report JUnit con 0 test, UTP `INSTRUMENTATION_FAILED`, messaggio `Process crashed`. Il job Android JVM è SUCCESS; il job strumentale è FAILURE. Non attribuire il crash ai test di cronologia/documenti senza stack trace. Artifact report: run [45428615380](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45428615380), artifact ID `14939369301`. Commit successivo `c6a7bba04e9a49a3b58045690f2cb27bc93d02e1` aggiunge acquisizione di logcat e stato instrumentation in caso di errore; la nuova run deve fornire la causa. Micro-fasi 29–30 restano in attesa di prova valida; il collaudo su dispositivo fisico è separato e ancora mancante.


### Diagnosi e correzione esecuzione emulatore — 24 novembre 2026
La run [45483033138](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45483033138) è fallita prima dell'avvio dei test strumentali: l'emulatore è partito, ma lo script shell multilinea ha definito una funzione in una singola invocazione e le righe successive sono state eseguite in shell separate. Errore osservato: `Syntax error: end of file unexpected (expecting "}")`. Corretto in commit `a3302498c9b460f3a334ab0d24e4e157c980d701`, rimuovendo la funzione e usando comandi autosufficienti per ciascuna invocazione Gradle con raccolta diagnostica inline. Nessuna prova strumentale è ancora valida; micro-fasi 29–30 aperte e gate 36 invariato.


### Aggiornamento micro-fase 29 e test Android multi-chat — 27 novembre 2026
La micro-fase 29 è **Completata per la verifica automatica CI emulator**: Android API 34 ha eseguito 38/38 test strumentali senza errori o skip; le prove distinte di scrittura dello stato pendente e recupero dopo `adb shell am force-stop` hanno entrambe restituito `OK (1 test)`. Android unit test e build temporanea dei soli APK di test sono SUCCESS; backend run `46058183407` SUCCESS. Run Android `46058183460`: https://github.com/damianoavvocato-star/SuperIA/actions/runs/46058183460. La fase 30 resta in attesa per copertura funzionale completa dei formati/OCR e privacy documentale; la prova su telefono fisico resta nella fase 35. Nessun APK è stato consegnato o installato sul telefono, nessun deploy/provider è stato attivato.


### Aggiornamento micro-fase 26 — heartbeat e rinnovo lease SSE — 27 novembre 2026
Il commit `c066ed8faf2bd6ab06d6a02fc224bf87a14360ee` aggiunge un test backend con provider lento: verifica emissione `: keep-alive`, successivi eventi `delta`/`done` e rinnovo ripetuto della lease sulla stessa richiesta. GitHub Actions Backend tests run [46075657157](https://github.com/damianoavvocato-star/SuperIA/actions/runs/46075657157): **success**. La micro-fase 26 resta in attesa di verifica pratica: mancano test attraverso il proxy/hosting reale e su dispositivo Android fisico, inclusi disconnessione e cancellazione effettiva del provider. Nessun deploy o APK autorizzato.

### Aggiornamento micro-fase 26 — cancellazione stream e retry immediato — 27 novembre 2026
Il commit `a676fe67fe0bdfcca5ee542221160cae7b9e86d2` aggiunge un test backend end-to-end del generatore SSE: il client interrompe il consumo, il task provider riceve la cancellazione, la lease viene marcata fallita e lo stesso request ID può essere reclamato immediatamente con un nuovo fencing token. GitHub Actions Backend tests run [46075950792](https://github.com/damianoavvocato-star/SuperIA/actions/runs/46075950792): **success**. Questo chiude la verifica simulata backend di heartbeat, lease e cancellazione; la micro-fase resta in attesa per proxy/hosting reale, disconnessione effettiva Android e prova su dispositivo fisico. Nessun deploy o APK autorizzato.


### Aggiornamento micro-fase 26 — hook di cancellazione HTTP Android — 27 novembre 2026
Il commit `f900f2e5bc885c6484372f2305324609fb3870b0` isola la registrazione della disconnessione di `HttpURLConnection` alla cancellazione della coroutine. Il commit `dfe75ac17b358ec3d5ef1266709bdce9751a7b02` aggiunge test unitari per verificare che la cancellazione invochi `disconnect()` e che il completamento normale non lo invochi. Android unit tests GitHub Actions run [46076365329](https://github.com/damianoavvocato-star/SuperIA/actions/runs/46076365329): **success**; Backend tests run [46076365260](https://github.com/damianoavvocato-star/SuperIA/actions/runs/46076365260): **success**. Questa è una verifica dell'hook di cancellazione, non una simulazione di rete lenta né una prova di interruzione di una lettura SSE reale: restano aperte la prova con server/proxy di staging e la verifica su dispositivo fisico. Micro-fase 26 non chiusa; nessun deploy o APK autorizzato.
