# Registro operativo delle fasi — SuperIA

Aggiornato: 23 novembre 2026
Ambito: esclusivamente il repository SuperIA. Questo registro separa codice presente, test CI, collaudo reale e rilascio.

## Regole inderogabili
- SuperIA deve restare gratuita: niente pagamenti, carte, ricariche, acquisti, overage fatturabili o fallback a pagamento.
- I crediti gratuiti dei provider sono ammessi anche se espressi in valuta o token; quando terminano si passa a un provider gratuito approvato oppure si interrompe il servizio senza addebiti.
- Non creare account multipli né ruotare chiavi per aggirare i termini dei fornitori.
- Le chiavi dei provider restano esclusivamente sul backend.
- Inferenza AI esclusivamente online. Gemma/LiteRT-LM e il runtime locale sono stati rimossi dal progetto Android.
- Cronologia e memoria di progetto restano cifrate localmente; al backend si invia soltanto il contesto necessario.
- SuperIA resta indipendente da Editore Foto e Video IA e Imperium_Italia.
- BLOCCO APK: non generare, assemblare, firmare o pubblicare APK intermedi. L'APK definitivo sarà autorizzato soltanto dopo il completamento e la verifica end-to-end di tutte le funzioni necessarie alla chat online.

## Stato generale verificato
- Repository: https://github.com/damianoavvocato-star/SuperIA, branch predefinito main.
- Android: client HTTPS, chat, pairing individuale, token cifrato tramite Android Keystore, cronologia cifrata, memoria di progetto, ricostruzione del contesto e funzioni documentali presenti nel codice.
- Backend: prototipo FastAPI, autenticazione per dispositivo, pairing, revoca, endpoint chat e stato provider; non è pubblicato in produzione.
- Provider: router free-only presente, provider disabilitati per default; nessun provider di produzione è stato attivato o verificato su account reale.
- Hosting e database: nessun servizio di produzione creato o collegato; i candidati gratuiti restano da verificare prima della registrazione.
- CI Android: workflow modificato per eseguire soltanto gradle --no-daemon testDebugUnitTest. La modifica rimuove assemblaggio debug/release, firma, upload APK e test strumentali che richiedono un APK.
- Non dichiarare un esito CI successivo a tale modifica senza una run verificata. Nessun APK va prodotto durante il completamento della chat online.

## Stato delle 16 fasi

| Fase | Ambito | Stato attuale | Lavoro residuo / criterio di chiusura |
|---|---|---|---|
| 0 | Inventario e progettazione | Documentazione presente, audit incompleto | Verificare individualmente le 182 IA: termini, licenze, prezzi, disponibilità, API e alternative; il catalogo non equivale a integrazione. |
| 1 | Fondamenta Android e connessioni | Parziale | Completare configurazione, pairing/connessione reali, gestione dispositivi e test su hardware. Il pairing backend richiede rate limiting IP effettivo sul gateway/WAF. |
| 2 | Motore AI e chat | Parziale, online-only | Completare e collaudare conversazione multi-turno, streaming, errori, retry, failover, quota esaurita, continuità del contesto e ripresa dopo riavvio. |
| 3 | Documenti e conoscenza | Primo incremento presente | Test pratici PDF/DOCX/TXT/CSV, OCR, archivio cifrato, recupero, citazioni e limiti; migliorare retrieval dove necessario. |
| 4 | Ricerca, navigazione e traduzione | Non integrata nell'app | Ricerca web esplicita, analisi link, estrazione contenuti, traduzione e citazioni. |
| 5 | Immagini e grafica | Non implementata | Pipeline generazione/modifica, OCR immagini e verifica licenze/costi. |
| 6 | Video | Non implementata | Import, montaggio, sottotitoli, trascrizione, traduzione e rendering. |
| 7 | Audio, voce e musica | Non implementata | STT/TTS, pulizia, doppiaggio e gestione dei diritti/consensi. |
| 8 | Programmazione e GitHub | Non integrata come funzione prodotto | Agente coding controllato, diff, branch, commit, issue/PR e test con approvazioni. |
| 9 | Produttività e dati | Non implementata | Documenti, fogli, presentazioni, CSV, formule, grafici e database. |
| 10 | Agenti e automazioni | Non implementata | Workflow multi-step, pianificazione, ripresa, audit e conferme per azioni sensibili. |
| 11 | Aree professionali | Non implementata | Moduli specialistici con fonti, limiti e verifiche specifiche. |
| 12 | Ecosistema modelli/componenti | Registry documentale | Gestione operativa di componenti, versioni, dipendenze, licenze e compatibilità. |
| 13 | App unica e interfacce | UI Android di base | Completare interfaccia unificata, conversazione persistente, progetti, accessibilità e gestione dispositivi. |
| 14 | Privacy e sicurezza | Protezioni parziali | Threat model, rate limiting, retention, export/cancellazione, isolamento, minimizzazione log e test di sicurezza. |
| 15 | Collaudo e distribuzione | Non chiusa; APK bloccato | Test unitari/integrati, prove su telefoni reali, sicurezza, prestazioni e verifica online end-to-end. Solo dopo autorizzare l'unica build APK definitiva. |

## Chat online: checklist obbligatoria prima dell'APK
1. Backend HTTPS pubblicato su hosting gratuito verificato, senza carta, pagamenti o overage.
2. Database persistente configurato e verificato; niente stato di produzione su filesystem temporanei come /tmp.
3. Rate limiting IP reale sul gateway/WAF prima di abilitare il pairing pubblico.
4. Pairing individuale, token cifrato sul dispositivo, hash lato server, scadenza e revoca testati.
5. Almeno un provider AI gratuito verificato su account reale; chiavi solo server-side.
6. Rotazione tra provider gratuiti con cooldown, hard-stop e nessun fallback a pagamento.
7. Streaming della risposta, timeout, errori HTTP, retry e ripresa delle richieste interrotte verificati.
8. Conversazione multi-turno con cronologia, memoria e documenti; contesto pertinente senza inviare tutta la cronologia.
9. Quota esaurita, provider indisponibile, rete assente, riavvio backend e database testati.
10. Privacy: minimizzazione dati/log, retention, revoca dispositivo, export/cancellazione e segreti verificati.
11. Test end-to-end completati su backend e dispositivi Android reali.
12. Documentazione aggiornata e tutti i gate superati.

Finché anche un solo punto obbligatorio resta aperto, non generare APK.

## Sicurezza del backend
- SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=true è soltanto un gate di configurazione: non implementa il rate limiting. Non abilitarlo in produzione finché la protezione effettiva non è configurata e provata sul gateway/WAF.
- /health verifica la disponibilità del processo, non quella dei provider o la piena operatività.
- Il backend non deve esporre chiavi, token o dettagli sensibili di configurazione.
- PostgreSQL persistente è necessario per lo stato condiviso di produzione; SQLite su /tmp è ammesso soltanto per test/sviluppo.
- Non attivare provider o servizi hosting finché gratuità, assenza di carta, assenza di billing automatico e hard-stop non sono verificati.

## Criteri di aggiornamento
- Registrare commit, run CI, test report e dispositivo per ogni verifica.
- Distinguere sempre codice presente, test CI superato, test reale superato e funzione pronta.
- Non attribuire percentuali globali arbitrarie alle fasi.
- Non dichiarare un servizio online operativo finché non è pubblicato e testato.
- Non creare APK intermedi: il blocco è vincolante fino alla chiusura della checklist chat online.

## Aggiornamento 21 novembre 2026 — streaming chat
- Backend: endpoint SSE autenticato `/api/v1/chat/stream` aggiunto.
- Router: parsing dei delta OpenAI-compatible; failover limitato alla fase precedente al primo token; niente cambio silenzioso dopo output parziale.
- Android: client SSE e aggiornamento progressivo della risposta nella chat unica; salvataggio del testo parziale in caso di errore.
- Test: aggiunti test backend e Android. Esito CI ancora non verificato; test end-to-end con provider reale, cancellazione di rete e ripresa completa restano aperti.
- Questa funzione è quindi **implementata a livello iniziale, non ancora validata**. Non sblocca la generazione dell'APK.

- Ulteriore protezione Android: la cancellazione della coroutine chiude la connessione HTTP sottostante; aggiunto test backend che impedisce il failover dopo l'emissione di un frammento.

- Il comando di ripresa conserva nella cronologia il testo parziale già mostrato, ma lo esclude dal contesto della nuova generazione per evitare che venga ripetuto come risposta completa.


- Persistenza durante lo streaming: Android salva snapshot cifrati della conversazione a intervalli limitati (circa 900 ms), con scritture serializzate e controllo versione per impedire che uno snapshot più vecchio sovrascriva uno più recente. Al termine viene forzato un salvataggio completo; in caso di errore/cancellazione resta attivo il salvataggio di recupero già esistente.
- Limite noto: il server non dispone ancora di un registro persistente di idempotenza per richiesta. Un retry dopo perdita della connessione può quindi avviare una nuova generazione, non riprendere quella precedente. Non dichiarare garantita l'esecuzione exactly-once finché request ID, stato e risposta non sono registrati atomicamente nel database persistente condiviso.


## Aggiornamento — identificativi e deduplicazione richieste chat
- Ogni nuovo messaggio Android riceve un UUID stabile, conservato insieme al messaggio nell'archivio cifrato. Il retry riutilizza lo stesso UUID; se recupera una vecchia richiesta senza ID, assegna e salva l'ID prima di inviare.
- Il backend associa ogni richiesta all'ID dispositivo autenticato e salva hash del payload, stato e risposta completata nel database condiviso configurato. La stessa coppia dispositivo/ID con payload differente viene rifiutata (HTTP 409); una richiesta già in elaborazione viene rifiutata (HTTP 409); una richiesta completata viene riprodotta dal registro senza invocare nuovamente il provider.
- Gli errori prima del completamento rendono la richiesta riprovabile; i record in elaborazione hanno una lease di 30 minuti e il recupero della lease usa un aggiornamento condizionale atomico.
- Test backend aggiunti per replay senza seconda chiamata al provider, conflitto di payload e isolamento tra dispositivi. Esito CI non ancora verificato.
- Limite distribuito: questa protezione evita la duplicazione dopo che la risposta completata è stata registrata e blocca i duplicati durante l'elaborazione. Non può garantire exactly-once nel caso estremo in cui il provider abbia già generato la risposta ma il processo server si interrompa prima di registrarla nel database. La ripresa dello stream non riparte dal token interrotto: ritenta la richiesta con lo stesso ID.
- Il registro usa lo stesso database configurato per l'autenticazione. Per produzione deve essere PostgreSQL persistente e condiviso tra tutte le istanze; SQLite locale resta soltanto per sviluppo/test.

- Privacy del registro richieste: le risposte salvate per il replay sono cifrate lato applicazione con AES-256-GCM. Il backend richiede il segreto stabile `SUPERIA_CHAT_IDEMPOTENCY_KEY`; deve essere generato casualmente, custodito solo nel secret manager del backend e condiviso tra le istanze. Non inserirlo nell'app Android, nel repository o nei log. Se manca, l'endpoint con request ID fallisce chiuso con HTTP 503. La rotazione della chiave richiede una procedura di ricifratura dei record esistenti, altrimenti il replay dei record precedenti non sarà decifrabile.

- È stato aggiunto anche un test d'integrazione PostgreSQL che verifica claim, rifiuto del duplicato in elaborazione, completamento e replay della risposta decifrata; il workflow backend dispone già di un servizio PostgreSQL effimero. Il risultato della nuova run CI non è ancora verificabile da qui.


### Aggiornamento — fencing delle richieste chat
- Ogni claim nuovo, recupero di lease scaduta o retry di richiesta fallita assegna un token di proprietà distinto.
- Le operazioni di rinnovo, completamento e fallimento richiedono il token corrente; un worker precedente non può modificare lo stato dopo che un altro worker ha recuperato la richiesta.
- Migrazione compatibile del registro esistente: aggiunta della colonna lease_token sia per SQLite sia per PostgreSQL, senza cancellare risposte cifrate.
- Corretto il primo inserimento: se l'INSERT crea il record, claim_chat_request restituisce immediatamente claimed. Il duplicato simultaneo resta processing.
- Test aggiunti per proprietario precedente, token nuovo, completamento protetto e migrazione di un database SQLite preesistente.
- Stato: codice e test aggiunti al repository; esecuzione CI e verifica PostgreSQL reale ancora non confermate. Non dichiarare la funzione validata end-to-end.


### Verifica CI — 23 novembre 2026
- GitHub Actions Backend tests, run **577**, commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`: **success**.
- Esito: **60 test superati**, 1 warning, nessun test fallito.
- La run comprende il test PostgreSQL del registro richieste e i test backend di streaming, deduplicazione, recupero lease e fencing token.
- Il precedente run 576 aveva 59 test superati e un test router fallito per newline letterali errati nel fixture SSE; fixture corretto e run 577 superata.
- Questa è una verifica CI backend, non un collaudo end-to-end con provider reale o dispositivo Android. Il blocco APK resta attivo.

### Verifica Android — ripresa dopo chiusura/riapertura
- Aggiunti due test strumentali per l'archivio cifrato: il primo ricrea ChatHistoryStore e verifica che una richiesta pendente conservi lo stesso requestId; il secondo verifica la compatibilità con una vecchia richiesta pendente senza requestId e la sua successiva migrazione.
- Commit test: `978c82fe5b4f9f252abc462bd2925e4f5a7d162e`.
- Stato: test aggiunti, **non eseguiti**. Il workflow corrente esegue test unitari Android e non avvia test strumentali sul dispositivo/emulatore. Serve un successivo collaudo Android dedicato, senza produrre o distribuire APK intermedi.
- La ripresa manuale resta necessaria dopo la riapertura; l'app non riparte automaticamente a generare per evitare invii inattesi e duplicazioni.
- Nessun APK generato.

### Miglioramento ripresa rete e diagnostica errori HTTP — 23 novembre 2026
- Il client Android valida localmente che il requestId dello streaming sia un UUID prima di aprire la connessione, evitando richieste che il backend rifiuterebbe.
- Aggiunta diagnostica specifica per HTTP 401 (riassociazione), 409 (richiesta già in elaborazione/conflitto), 429 (attendere il limite) e 5xx/503 (indisponibilità temporanea e provider gratuiti non pronti).
- L'interfaccia mantiene la richiesta pendente e il testo parziale in cronologia cifrata in caso di errore; il recupero continua a usare lo stesso requestId.
- Aggiunto test unitario per rifiutare un requestId malformato prima di aprire la rete.
- Commit codice: `d59a7f462766c71b2899e2e9da2af6378128fdda`; commit test: `8ca78906102ae74b4612d1d7e1cc5ea51a789ff1`.
- Stato: modifiche e test aggiunti; CI Android non verificata per questi commit. Non è stato eseguito un test end-to-end con rete intermittente e provider reale.
- Nessun APK generato o distribuito.


## Aggiornamento 23 novembre 2026 — heartbeat SSE
- Backend: lo stream chat invia commenti SSE `: keep-alive` ogni 15 secondi mentre il provider non produce nuovi eventi; i commenti non diventano delta e non modificano la risposta visualizzata.
- Durante l'attesa viene rinnovata anche la lease della richiesta idempotente, evitando che una generazione lenta venga recuperata da un secondo worker solo perché non ha ancora prodotto token.
- La lettura asincrona del provider resta pendente tra gli heartbeat; alla chiusura/cancellazione dello stream il task pendente viene cancellato e l'iteratore viene chiuso.
- Aggiunto test backend con provider simulato lento: verifica heartbeat, delta finale, evento done e singola invocazione del provider.
- Esito CI da verificare; il test non è dichiarato superato finché GitHub Actions non termina con successo.
- Nessun APK generato. Hosting, provider reale, PostgreSQL di produzione e collaudo su telefono restano non configurati/non verificati.

### Fase 1 — hardening test client Android (23 novembre 2026)
- Estesa la suite OnlineAssistantClientTest con verifiche pre-rete per lo streaming: rifiuto di endpoint HTTPS contenenti query string, credenziali nell'URL o frammenti, e rifiuto di prompt vuoti.
- Commit test: 045a619b092d93b4da8f132ae3f6479b0f45cc96.
- La modifica al percorso android/** attiva il workflow Android unit tests su push a main; l'esito deve essere controllato prima di considerare verificata la suite.
- La Fase 1 resta aperta: pairing e connessione su backend HTTPS pubblicato, rate limiting effettivo sul gateway/WAF e prova su dispositivo fisico non sono ancora eseguibili perché l'infrastruttura di produzione non è stata configurata. Nessun APK creato.


## Piano dettagliato in 50 micro-fasi — 23 novembre 2026

Il lavoro operativo viene ora suddiviso in 50 micro-fasi numerate, ognuna con criterio di accettazione, stato iniziale e dipendenze. Consultare [docs/MASTER_MICRO_PHASE_PLAN.md](MASTER_MICRO_PHASE_PLAN.md). Le 16 fasi macro restano la mappa generale; le 50 micro-fasi sono l'unità di esecuzione e verifica. Si procede una micro-fase alla volta. Il gate chat online è la micro-fase 36; il blocco APK resta attivo fino al suo superamento e alla verifica completa dei requisiti.

Audit repository del 23 novembre 2026: è presente soltanto il branch `main`; la ricerca pull request non ha restituito PR nel repository. Non sono stati creati rami aggiuntivi.


## Micro-fase 01 completata — 23 novembre 2026

Inventario verificato: 182 record numerati da 1 a 182. La policy online-only è stata applicata all'audit strutturato; 16 riferimenti a inferenza LLM locale sono stati sostituiti e i documenti/matrice storici sono stati marcati come superati dove in conflitto. Il registro componenti marca Ollama e llama.cpp come non integrabili per inferenza. Questo completamento è documentale e strutturale: non certifica termini, prezzi, quote, licenze, provider o hardware dei servizi nominati. L'audit dei provider effettivamente candidati resta nella micro-fase 19.

Commit di riferimento: audit `c3815b8`, matrice `585a2c9`, analisi `08cdd38`, registro `cea44df`.


## Micro-fase 02 completata — 23 novembre 2026

Consolidata la policy online-only: le proposte local-first/local LLM presenti in ROADMAP, decisioni Fase 0 e architettura Android sono marcate come storiche; il registro modelli locali è disattivato come fonte di candidati per l'inferenza corrente. Nessun modello locale deve essere scaricato o integrato. Le elaborazioni non-LLM locali restano valutabili. Verifica documentale, non runtime.


## Micro-fase 03 completata — 23 novembre 2026

Audit statico repository/branch/cronologia/workflow/documenti registrato in [REPOSITORY_AUDIT_2026-11-23.md](REPOSITORY_AUDIT_2026-11-23.md). Branch unico `main`, zero PR. README allineati a SSE; documentazione legacy su token condiviso e prototipo web marcata storica; registry Android online-only. I residui legacy web/Ollama e route non-streaming sono documentati ma non cancellati senza micro-fase specifica e test. Backend CI run 605 SUCCESS sul commit precedente a questo audit; non è CI Android né E2E.


## Allineamento al piano 50 micro-fasi — 23 novembre 2026
Il presente tracker contiene anche una vista macro storica delle 16 fasi. Per la sequenza operativa corrente e lo stato delle singole unità fa fede [MASTER_MICRO_PHASE_PLAN.md](MASTER_MICRO_PHASE_PLAN.md). Stato micro-fasi: 01 Completata; 02 Completata; 03 Completata; 04 Completata; 05–50 secondo gli stati registrati nel piano. La dicitura nelle sezioni macro che riporta streaming o client come mancanti va letta come fotografia storica: il client Android usa POST /api/v1/chat/stream e il backend ha il primo percorso SSE, ma la verifica E2E con provider reale resta aperta.

Ultima evidenza CI backend registrata nell'audit del 23 novembre: run 605 SUCCESS sul commit d168e0b2660fedf6a4fd5a3b3c6923e2fcdcee72. Questo esito riguarda la suite backend PostgreSQL e non dimostra un deploy pubblico, un provider reale o un test Android. Nessun APK è stato generato.


### Esito micro-fase 04 — 23 novembre 2026
- README, ROADMAP, PHASE0_DECISIONS e PHASE_TRACKER aggiornati con la prevalenza del piano da 50 micro-fasi e lo stato reale della chat online.
- Le decisioni obsolete del verbale Fase 0 sono esplicitamente archiviate come storiche; la guida backend con token condiviso resta non operativa.
- I README Android e principale sono stati ricontrollati: pairing individuale, SSE, cifratura, limiti di rilascio e blocco APK sono coerenti.
- Verifica: riletti i file aggiornati da GitHub; nessun test runtime necessario per modifiche solo documentali.
- Limite: la coerenza documentale non certifica hosting, provider o chat E2E.


## Gruppo 01–07 — avanzamento autonomo (23 novembre 2026)

### Micro-fase 05 — Completata
Audit statico del client Android: [ANDROID_CLIENT_AUDIT_2026-11-23.md](ANDROID_CLIENT_AUDIT_2026-11-23.md). Verificati HTTPS, pairing, token cifrato, SSE, request ID, cancellazione, storage cifrato, memoria e limiti prompt. Resta rilevato il metodo legacy non-streaming `send()`, da rimuovere solo dopo ricerca riferimenti e test mirati. Nessun APK.

### Micro-fase 06 — Completata
GitHub Actions Android unit tests, run 222 (ID `45091390966`), commit `739f585b168b24bc817ab0cbcc49babc799ad392`: SUCCESS. Log: `gradle --no-daemon testDebugUnitTest`, task `:app:testDebugUnitTest`, BUILD SUCCESSFUL in 28s. Job esegue checkout, JDK 17, Gradle e test unitari; nessun task assemble/sign/upload APK. Evidenza CI del commit indicato; non è un test su dispositivo fisico.

### Micro-fase 07 — Completata
GitHub Actions Backend tests, run 577 (ID `45089445756`), commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`: job PostgreSQL SUCCESS; log pytest **60 passed, 1 warning in 1.31s**. Tutti gli step del job hanno conclusione success. Questa è baseline backend automatica, non test E2E con provider reale.

- Micro-fase 06 chiusa con evidenza run Android 222, job SUCCESS, comando unit-test only; nessun APK prodotto.


## Micro-fase 08 — Completata (23 novembre 2026)
Aggiunta validazione fail-closed della configurazione provider in `services/api/app/providers/free_router.py`; test per modalità globale, attestazioni, credenziali parziali, allowlist, OpenRouter e Cloudflare. Rapporto: [BACKEND_CONFIG_VALIDATION_2026-11-23.md](BACKEND_CONFIG_VALIDATION_2026-11-23.md). GitHub Actions Backend tests run 634, commit `a526d3fb69af368a16fe9a3120b7000eed6822e7`: **SUCCESS, 68 passed, 1 warning** con PostgreSQL. Le condizioni contrattuali/account reali non sono certificate dal codice e restano da verificare nella fase provider 19. Nessun APK.


## Micro-fase 09 — Completata (23 novembre 2026)
Verificati token casuali per dispositivo, hash SHA-256 lato server, UUID, TTL 90 giorni, scadenza, revoca selettiva e isolamento tra dispositivi. Test aggiunti e CI backend run 638 SUCCESS: **70 passed, 1 warning** con PostgreSQL. Rapporto: [DEVICE_AUTH_VALIDATION_2026-11-23.md](DEVICE_AUTH_VALIDATION_2026-11-23.md).

## Micro-fase 10 — In corso (23 novembre 2026)
Aggiunti test pairing per segreto server mancante, input fuori range e risposta contenente esclusivamente credenziali individuali. CI run successiva in attesa. Il flag `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED` resta un gate e non prova il rate limiting effettivo; la protezione edge sarà verificata nella micro-fase 12. Nessun APK.


## Micro-fase 10 — Completata (23 novembre 2026)
Pairing server-side verificato: codice segreto con `hmac.compare_digest`, gate fail-closed, vincoli input, token individuale e risposta minimale. Test aggiunti. Backend CI run 639 SUCCESS: **76 passed, 1 warning**. Rapporto: [PAIRING_SECURITY_VALIDATION_2026-11-23.md](PAIRING_SECURITY_VALIDATION_2026-11-23.md). Attenzione: il flag edge non implementa il rate limiting IP; la fase 12 resta obbligatoria prima dell'esposizione pubblica.


## Micro-fase 11 — In corso (23 novembre 2026)
Aggiunti rinnovo token individuale (nuovo token, stesso device ID, vecchio token invalidato) e revoca self-service autenticata. Rotazione protetta da compare-and-swap sul token hash attuale per evitare gare tra richieste concorrenti. Android aggiunge pulsanti per rinnovare e revocare sul server; l'endpoint associato è mantenuto separato dal campo modificabile, così il Bearer token non viene inoltrato a un host digitato successivamente. Test backend e Android in esecuzione. Non segnare completata finché le run finali non sono SUCCESS. Nessun APK.


## Micro-fase 11 — Completata (23 novembre 2026)
Aggiunti rinnovo token compare-and-swap (stesso device ID, token precedente invalidato) e revoca self-service autenticata. Android integra rinnovo e revoca remota; l'endpoint associato è fissato per non inoltrare il Bearer token a un host modificato. CI backend run 661 SUCCESS: **83 passed, 1 warning**; Android run 229 SUCCESS, testDebugUnitTest, BUILD SUCCESSFUL in 36s. Rapporto: [DEVICE_TOKEN_LIFECYCLE_2026-11-23.md](DEVICE_TOKEN_LIFECYCLE_2026-11-23.md). Test su dispositivo reale ancora da svolgere alla fase 35. Nessun APK.


## Micro-fase 12 — Bloccata da dipendenza esterna (23 novembre 2026)
Preparata la regola candidata Cloudflare WAF Free: path `/api/v1/device/pair`, conteggio IP, 5 richieste/10s, block per 10s. Nessuna zone/dominio autorizzato né deploy pubblico; la documentazione Cloudflare segnala ritardo dei contatori. Il flag edge resta false. Rapporto: [EDGE_RATE_LIMIT_PREPARATION_2026-11-23.md](EDGE_RATE_LIMIT_PREPARATION_2026-11-23.md).

## Micro-fase 13 — Completata (selezione staging, non provisioning)
Selezionati Render Free Web Service (staging only) e Aiven PostgreSQL Free (test data only). EthioDeploy escluso per overage wallet; Render Postgres escluso per scadenza 30 giorni. Limiti e fonti: [FREE_STAGING_HOST_SELECTION_2026-11-23.md](FREE_STAGING_HOST_SELECTION_2026-11-23.md). Nessun account o servizio creato.

## Micro-fase 14 — Bloccata da autorizzazione esterna
Preparato `render.yaml` con piano Free, Frankfurt, autoDeploy false, health check, provider AI e pairing disabilitati; segreti solo variabili non sincronizzate nel repository. Guida: [RENDER_STAGING_DEPLOY_GUIDE.md](RENDER_STAGING_DEPLOY_GUIDE.md). Per collegare account Render/Aiven e autorizzare GitHub App serve consenso esplicito. Nessun deploy eseguito.


## Micro-fase 15 — Bloccata da provisioning esterno
Il codice ora rifiuta SQLite e filesystem temporanei in staging/production e richiede PostgreSQL condiviso per auth e router state. CI run 678: **95 passed, 1 warning**. Database esterno non creato; consenso e provisioning Aiven ancora necessari.

## Micro-fase 16 — Completata
Migrazione SQLite/PostgreSQL verificata con conteggi, digest SHA-256, destinazione non vuota, schema errato, rollback completo e sorgente senza provider_state. CI run 680: **99 passed, 1 warning** con PostgreSQL. Rapporto: [DATABASE_PERSISTENCE_AND_MIGRATION_2026-11-23.md](DATABASE_PERSISTENCE_AND_MIGRATION_2026-11-23.md).

## Micro-fase 17 — In corso
Esteso `.gitignore` per .env e segreti; aggiunto template `services/api/.env.example` senza credenziali reali e con provider disabilitati; test di redazione delle API key nelle diagnostiche. Ricerca pattern segreti noti senza match. CI finale da verificare; secret manager reale richiede deploy/consenso esterno.


## Micro-fase 17 — Bloccata da dipendenza esterna (23 novembre 2026)
Completata igiene repository: `.gitignore` esteso, template `services/api/.env.example` senza segreti, provider disabilitati, test che impedisce API key nelle diagnostiche; code search pattern segreti senza match. CI run 683: **100 passed, 1 warning**. Secret manager e rotazione produzione non configurabili senza deploy autorizzato. Rapporto: [SECRET_MANAGEMENT_BASELINE_2026-11-23.md](SECRET_MANAGEMENT_BASELINE_2026-11-23.md).


## Micro-fase 18 — Completata (23 novembre 2026)
Aggiunto `GET /api/v1/readiness` admin-only con stati sintetici process/database/device-auth/provider e nessun segreto in risposta. `/health` resta process-only. CI backend run 691: **102 passed, 1 warning**. Rapporto: [READINESS_DIAGNOSTICS_2026-11-23.md](READINESS_DIAGNOSTICS_2026-11-23.md).
Test di regressione aggiuntivo: richieste readiness senza token o con token amministrativo errato devono restituire 401 senza esporre il segreto. Backend CI run 760 sul commit `53c9a18b1f9c7f833e453c8b30560b28d4a6be40`: **SUCCESS, 116 passed, 1 warning in 1.49s**.


## Micro-fase 19 — Bloccata da verifica account-level
Audit pubblico provider aggiornato: Groq/Cloudflare candidati condizionati; OpenRouter e Gemini non approvati per chat privata; SambaNova escluso. Aggiunta attestazione `DATA_POLICY_APPROVED`, SambaNova rimosso dal router. Nessun provider abilitato; account no-card/ZDR da verificare con consenso. Rapporto: [AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md](AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md).

## Micro-fase 20 — Bloccata da consenso esterno
Nessun account provider o API key creati. Attivazione del primo provider reale richiede consenso e verifica account no-card/hard-stop/ZDR. Nessuna inferenza reale eseguita.

## Micro-fase 21 — Completata
Guard-rail free-only verificati: allowlist, attestazioni economiche + data policy, esclusione SambaNova, cooldown/failover 429, disabilitazione 401/402/403, cooldown 408/5xx/rete, nessun cambio provider dopo primo token, nessun fallback locale o pagato. CI backend run 705: **104 passed, 1 warning**. Rapporto: [PROVIDER_AUDIT_AND_FREE_GUARDS_2026-11-23.md](PROVIDER_AUDIT_AND_FREE_GUARDS_2026-11-23.md).


## Micro-fasi 22–28 — aggiornamento 23 novembre 2026
- 22 Completata: failover/cooldown solo tra provider free approvati, nessun fallback locale o pagato.
- 23 Completata: schema ruoli/limiti verificato; Android invia ora messaggi strutturati user/assistant separati, contesto memoria/documenti etichettato e nuovo messaggio finale. Retry conserva i ruoli. Limite client: 1–60 messaggi, 32.000 caratteri totali.
- 24 Completata: rimosse route non-streaming, root web prototype, static index, Ollama backend e metodo client send; solo SSE.
- 25 Completata: parser SSE Android testato per delta, heartbeat, done, error, troncamento ed escape JSON.
- 26 In attesa di verifica: heartbeat e lease implementati; test disconnessione reale telefono/rete ancora mancante.
- 27 Completata: idempotenza, replay cifrato, conflitto payload, lease e fencing testati su PostgreSQL CI.
- 28 In attesa di verifica: ID e pending persistono, retry usa lo stesso ID; manca test E2E kill/restart app su dispositivo.

Evidenze: backend CI run 738 **109 passed, 1 warning**; Android CI run 246 SUCCESS, BUILD SUCCESSFUL in 39s. Rapporto: [ONLINE_CHAT_ENGINE_AUDIT_2026-11-23.md](ONLINE_CHAT_ENGINE_AUDIT_2026-11-23.md).


## Micro-fasi 29–35 — aggiornamento 23 novembre 2026
- 29 In attesa: cronologia cifrata, AtomicFile, pending ID, memoria editabile/cancellabile e export TXT; instrumented tests Android presenti ma non eseguiti.
- 30 In attesa: documenti locali cifrati, BM25, fonti, contesto limitato, export testo e cancellazione; OCR e storage reali da provare.
- 31 Bloccata: account/deploy staging non autorizzati.
- 32 Bloccata: nessun provider reale o API key attivato.
- 33 Completata con test simulati: status provider, quote, rete, DB e stop no-paid; CI run 750 **110 passed, 1 warning**.
- 34 In attesa: revoca, rotazione, isolamento cross-device, input oversized, segreti e concorrenza testati; rate limit WAF mancante.
- 35 Bloccata: test fisico Android richiede backend/provider e telefono.

Rapporto: [CONVERSATION_AND_DOCUMENTS_VALIDATION_2026-11-23.md](CONVERSATION_AND_DOCUMENTS_VALIDATION_2026-11-23.md).


## Micro-fase 36 — Gate finale chat online — BLOCCATO (23 novembre 2026)
Non soddisfatti: backend HTTPS pubblico, database PostgreSQL persistente, WAF IP rate limit reale, provider free account-level, secret manager e test E2E su telefono reale. Nessun APK autorizzato o generato. Fasi 37–50 in attesa del gate. Rapporto: [ONLINE_CHAT_FINAL_GATE_2026-11-23.md](ONLINE_CHAT_FINAL_GATE_2026-11-23.md).

## Riepilogo stato piano — 23 novembre 2026
21/50 micro-fasi completate (42%); 10 bloccate da dipendenze esterne; 5 in attesa di verifica pratica; 14 non iniziate (fasi 37–50). Le fasi completate sono supportate da test/documenti/commit indicati nei rapporti; nessun completamento è attribuito a deploy o provider reali non eseguiti.


## Aggiornamento micro-fase 34 — 23 novembre 2026
Aggiunto test di regressione pairing: valori non confermati del flag edge, header proxy falsificati, nessuna emissione token, risposta 503 senza segreto. Backend CI run 45119185491: **115 passed, 1 warning in 1.52s**, SUCCESS. La fase 34 resta in attesa della protezione WAF/gateway reale (micro-fase 12); il gate edge non va abilitato. Rapporto: [SECURITY_PAIRING_ANTI_SPOOFING_TEST_2026-11-23.md](SECURITY_PAIRING_ANTI_SPOOFING_TEST_2026-11-23.md).


### Micro-fase 23 — aggiornamento contratto multi-turno — 23 novembre 2026
Il client Android non appiattisce più la cronologia in un singolo messaggio user: invia array JSON con ruoli originali, contesto memoria/documenti separato e messaggio utente finale. Stesso comportamento per il retry. Test unitari coprono ruoli, budget totale, memoria/documenti e rifiuto di ruolo non supportato. Backend CI [run 771](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737059): **116 passed, 1 warning**. Android CI [run 259](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737107): **SUCCESS**, BUILD SUCCESSFUL in 36s. Fase 23 completata senza il precedente limite noto. Nessun APK.
