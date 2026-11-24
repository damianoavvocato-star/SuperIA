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
- Eccezione autorizzata il 24 novembre 2026: sono consentiti esclusivamente APK debug temporanei, generati e installati da GitHub Actions su emulatore per test strumentali delle microfasi 29–30. Vietati firma release, distribuzione e pubblicazione; l'APK definitivo resta bloccato fino al gate finale.

## Stato generale verificato
- Repository: https://github.com/damianoavvocato-star/SuperIA, branch predefinito main.
- Android: client HTTPS, chat, pairing individuale, token cifrato tramite Android Keystore, cronologia cifrata, memoria di progetto, ricostruzione del contesto e funzioni documentali presenti nel codice.
- Backend: prototipo FastAPI, autenticazione per dispositivo, pairing, revoca, endpoint chat e stato provider; non è pubblicato in produzione.
- Provider: router free-only presente, provider disabilitati per default; nessun provider di produzione è stato attivato o verificato su account reale.
- Hosting e database: nessun servizio di produzione creato o collegato; i candidati gratuiti restano da verificare prima della registrazione.
- CI Android: workflow con job JVM testDebugUnitTest e job strumentale su emulatore API 35. Il secondo può assemblare/installare soltanto APK debug temporanei non firmati per prove; non carica né distribuisce APK.
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

Finché anche un solo punto obbligatorio resta aperto, non generare né distribuire l'APK definitivo. Restano consentiti soltanto gli APK debug temporanei in CI già autorizzati per i test strumentali 29–30.

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
- Non generare APK release né distribuire build. Unica eccezione autorizzata il 24 novembre 2026: APK debug temporanei esclusivamente in CI/emulatore per test strumentali delle microfasi 29–30; nessuna installazione su telefoni personali e nessuna pubblicazione.

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
Il client Android non appiattisce più la cronologia in un singolo messaggio user: invia array JSON con ruoli originali, contesto memoria/documenti separato e messaggio utente finale. Stesso comportamento per il retry. Test unitari coprono ruoli, budget totale, memoria/documenti e rifiuto di ruolo non supportato. È stato aggiunto anche il rifiuto esplicito di ruoli inattesi nella cronologia cifrata: nessun ruolo sconosciuto viene convertito silenziosamente in `user`. Backend CI [run 771](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737059): **116 passed, 1 warning**. Android CI [run 259](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119737107): **SUCCESS**, BUILD SUCCESSFUL in 36s. Fase 23 completata senza il precedente limite noto. Nessun APK.


### Micro-fase 23 — validazione ruoli della cronologia persistita — 23 novembre 2026
Il builder Android rifiuta ora ruoli storici diversi da `user` e `assistant`, invece di reinterpretarli silenziosamente come messaggi utente. Test Android [run 261](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45124275214): **SUCCESS**, `testDebugUnitTest`, BUILD SUCCESSFUL. Test backend [run 776](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45124275144): **116 passed, 1 warning**. Il workflow Android continua a eseguire soltanto test unitari, nessun APK.


### Micro-fase 29 — hardening validazione archivio cronologia — 23 novembre 2026
Aggiunta validazione condivisa prima del salvataggio e dopo la decodifica dell'archivio cifrato: sono ammessi soltanto i ruoli persistiti `user` e `assistant`; gli eventuali request ID devono essere UUID validi. I record legacy pending senza request ID restano leggibili. Aggiunti test unitari per ruolo anomalo, UUID malformato, ruoli validi e compatibilità legacy. La fase 29 resta **In attesa di verifica** fino al collaudo instrumented su Android reale/emulatore; il test unitario CI non sostituisce tale prova.


### Micro-fase 29 — unicità identificativi cronologia — 23 novembre 2026
La validazione dell'archivio rifiuta ora anche ID messaggio duplicati, request ID duplicati e messaggi assistant marcati pending. Aggiunti tre test unitari di regressione. Android CI [run 263](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45131163579): **SUCCESS**, `testDebugUnitTest`. Backend CI [run 780](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45131163584): **116 passed, 1 warning**. La fase 29 resta **In attesa di verifica** per il collaudo instrumented su dispositivo/emulatore reale. Nessun APK.

## Verifica persistenza chat e CI — 23 novembre 2026
- Commit `ec03c7d`: corretta l'aspettativa del test ID duplicati (candidato 42 con 42/43/44 occupati produce 45).
- Commit `12fe5d1`: salvataggio preventivo del messaggio pending e del segnaposto assistant anche nel normale invio; rimosso un salvataggio duplicato nel percorso di ripresa.
- Android CI [run 45158387411](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387411): SUCCESS, solo `testDebugUnitTest`, BUILD SUCCESSFUL in 38s.
- Backend CI [run 45158387265](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387265): SUCCESS, **116 passed, 1 warning in 1.57s**.
- La persistenza periodica dei delta resta serializzata e versionata; il test di terminazione forzata, riavvio, sospensione e perdita rete su dispositivo reale non è ancora eseguito. Micro-fasi 26, 28 e 29 restano in attesa di verifica pratica; gate 36 bloccato. Nessun APK.
- Rapporto: [CHAT_STREAM_PERSISTENCE_HARDENING_2026-11-23.md](CHAT_STREAM_PERSISTENCE_HARDENING_2026-11-23.md).


### Verifica successiva — 23 novembre 2026
- Commit `786823c12afee6f4c94c942f4afd1c250f6993bb`: Android unit tests e backend tests **SUCCESS**.
- Commit `827ad1b35e41b00767235dab5a67496c75becad9`: Android run [45161032871](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45161032871) **SUCCESS**; backend run [45161032819](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45161032819) **SUCCESS**.
- Il nuovo test copre collisione ID dopo overflow di `Long.MAX_VALUE`; commento KDoc del persister riallineato alla classe corretta.
- Questi sono test automatici CI; non eseguono instrumented test né provano kill/restart, perdita rete o sospensione su dispositivo. Micro-fasi 26, 28 e 29 restano in attesa di verifica pratica; gate 36 resta bloccato. Nessun APK.


### Verifica persistenza serializzata — commit 790e88d — 23 novembre 2026
- Individuato e corretto un possibile conflitto tra i salvataggi periodici dello streaming e i salvataggi di recupero nei percorsi di errore/cancellazione.
- I percorsi di invio normale e retry creano ora il medesimo `StreamHistoryPersister` prima dello streaming; anche il recupero nei catch passa da `flush()`, così usa mutex e ordinamento per versione condivisi con gli snapshot periodici.
- Il salvataggio iniziale del messaggio utente pending e del segnaposto assistant resta precedente all'apertura della rete.
- Commit: `790e88da53c2e4e9bf649e465f354e85cd7cad0d`.
- Android CI [run 45162213509](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45162213509): **SUCCESS**, job `android-unit-tests` completato.
- Backend CI [run 45162213485](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45162213485): **SUCCESS**, job `test` completato.
- La CI conferma i test automatici del commit; non prova terminazione forzata, riavvio, sospensione o perdita rete su dispositivo reale. Micro-fasi 26, 28 e 29 restano in attesa di verifica pratica; gate 36 resta bloccato. Nessun APK generato.

### Micro-fase 29 — sincronizzazione archivio condivisa tra istanze — 23 novembre 2026
- La sincronizzazione di `ChatHistoryStore` comprende ora il ciclo completo di lettura/scrittura, cifratura AES-GCM e inizializzazione della chiave Android Keystore tramite monitor condiviso `ARCHIVE_WRITE_LOCK`.
- Aggiunti test instrumented per scritture simultanee da due istanze, letture concorrenti durante sostituzione dell'archivio e lettura/scrittura concorrente della memoria di progetto. I task concorrenti propagano gli errori al test chiamante con `Future.get(timeout)`.
- Commit codice: `e811ca6be1f7811a205c393e36165ea4c893a73b`, `92d11e3724b759516241771ea7be0669aa955735`, `878a4323ebe03f109902e0d763e8fd8f955a77f2`.
- Android CI [run 45211341253](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45211341253): **SUCCESS**, solo `testDebugUnitTest`. Backend CI [run 45211341376](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45211341376): **SUCCESS**.
- **La micro-fase 29 resta in attesa di verifica pratica:** i test aggiunti sono sotto `androidTest` e non sono stati eseguiti dalla CI unit; mancano emulator/device, force-stop/reopen, retry con request ID invariato, sospensione e perdita/ripristino rete. Gate 36 resta bloccato. Nessun APK e nessun servizio esterno attivato.
- Rapporto: [CHAT_STREAM_PERSISTENCE_HARDENING_2026-11-23.md](CHAT_STREAM_PERSISTENCE_HARDENING_2026-11-23.md).

### Micro-fase 29 — test prima inizializzazione Keystore concorrente — 23 novembre 2026
Aggiunta al test instrumented `concurrentStoreInstancesSerializeAtomicTranscriptWrites` la rimozione preventiva della chiave Android Keystore dedicata, così due istanze partono senza chiave e tentano il primo salvataggio simultaneamente. Commit test `f459de5fe9a5756b688f4acafa2caab91b810cab`. CI Android [run 45213497131](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213497131) **SUCCESS** (`testDebugUnitTest`) e backend [run 45213496563](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213496563) **SUCCESS**. Il test è instrumented: la CI unit non lo esegue e non equivale a una prova su device. Micro-fase 29 ancora in attesa di verifica pratica; nessun APK.


### Micro-fase 29 — perdita della chiave Keystore — 23 novembre 2026
- Corretto il test di migrazione legacy affinché inizializzi esplicitamente la chiave prima della cifratura di prova.
- Android CI [run 45213885568](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213885568): **SUCCESS**; backend [run 45213885468](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213885468): **SUCCESS**.
- Aggiunto nel commit `76cd14a285d610f8b38ab40703e349890c895d0e` un test instrumented per perdita della chiave: la cronologia deve fallire chiusa e l'archivio cifrato deve restare invariato. CI Android [run 45214332436](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214332436) e backend [run 45214332705](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214332705) avviate, esito da verificare.
- Il test instrumented non è eseguito da `testDebugUnitTest`; serve ancora emulatore/dispositivo. Fase 29 aperta; nessun APK.


### Micro-fase 29 — lettura senza ricreazione della chiave persa — 23 novembre 2026
- Android CI [run 45214332436](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214332436) e backend [run 45214332705](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214332705): **SUCCESS** sul commit `76cd14a`.
- La decifratura usa ora `getExistingKey()`: se la chiave Android Keystore manca, la lettura fallisce esplicitamente e non genera una chiave incompatibile. La creazione resta ammessa soltanto per nuove scritture.
- Test instrumented esteso: verifica che la chiave non venga ricreata e che il file cifrato non venga modificato dopo il fallimento.
- Commit codice `13a2770`, test `bac1cbb`. CI di `bac1cbb`: Android [run 45214619189](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214619189) **SUCCESS** (`testDebugUnitTest`); backend [run 45214619136](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45214619136) **SUCCESS** (`pytest`).
- La fase 29 resta aperta: i test sono instrumented e non sono stati eseguiti su emulatore/dispositivo. Nessun APK.


### Esito CI finale — micro-fase 29, commit bac1cbb — 23 novembre 2026
Android unit CI e backend CI sono entrambe **SUCCESS**. Il workflow Android esegue esclusivamente `gradle --no-daemon testDebugUnitTest`; non genera APK. Il test che manipola la chiave Keystore è instrumented (`androidTest`) e non è eseguito da questa CI. Micro-fase 29 resta in attesa del collaudo su emulatore/dispositivo, incluse perdita chiave, integrità archivio, force-stop/reopen e recupero rete. Gate 36 bloccato; nessun APK o servizio esterno attivato.


### Micro-fase 29 — coerenza fixture test instrumented — 23 novembre 2026
In revisione di `ChatHistoryStoreInstrumentedTest` è stata corretta la fixture `persistsMoreThanOneHundredMessagesAndPendingState`: il messaggio pending finale ora ha ruolo `user`, conforme alla validazione di archivio (pending consentito soltanto per user); gli altri messaggi mantengono l'alternanza dei ruoli. Commit `ed650a4af63e7bb4cb242fee6832e7a11b03327b`. La modifica è in `androidTest`, quindi non viene compilata/eseguita dal task CI unitario `testDebugUnitTest`; nessuna evidenza di run dedicata disponibile al momento. La micro-fase 29 resta aperta per collaudo strumentale reale. Nessun APK.


### Micro-fase 29 — recupero backup AtomicFile — 23 novembre 2026
Individuato e corretto il percorso di lettura: ChatHistoryStore ora chiama AtomicFile.openRead() anche quando il file principale non esiste, così AtomicFile può ripristinare l'unica copia valida dal backup .bak. Aggiunto test instrumented che simula backup-only e verifica contenuto, ripristino del file principale e rimozione del backup. Commit codice 40a9e712ae8efd3c1d7ab971e44ccb95b445daf2; commit test 409fb3703372bfee2d587118a61e013246e0497f. Il test è in androidTest e richiede esecuzione strumentale su device/emulatore; non è coperto da testDebugUnitTest. Micro-fase 29 resta in attesa di verifica pratica. Nessun APK.


### Micro-fase 29 — recupero AtomicFile anche per memoria progetto — 23 novembre 2026
L'audit ha trovato lo stesso caso limite nel caricamento della memoria di progetto: il controllo preliminare di esistenza del file principale poteva impedire il recupero del backup. loadProjectMemory() ora tenta direttamente AtomicFile.openRead() e tratta come memoria vuota soltanto FileNotFoundException; gli errori di decifratura non vengono mascherati. Aggiunto test instrumented backup-only per la memoria oltre a quello per la cronologia. Commit codice 3b68cdec62d0b657f411b050da75b062db0d74bc; test 6af06776deb6ff4dc6d7ac5f9e75fbfd0b231fee. Test non eseguiti da testDebugUnitTest: micro-fase 29 resta in attesa di collaudo Android. Nessun APK.


### Verifica CI — recupero AtomicFile cronologia e memoria — 23 novembre 2026
- Backend CI sul commit `7732dbf040b32fc45c49d8fdddacfc64bce45345`: **SUCCESS** ([run 45216360181](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45216360181)).
- Android CI sul commit `6af06776deb6ff4dc6d7ac5f9e75fbfd0b231fee`: lo step `Run Android unit tests` risulta **SUCCESS** ([run 45216321285](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45216321285)); al momento della verifica il job stava ancora completando gli step post-job. La CI non esegue `androidTest`, quindi non convalida i due nuovi test backup-only.
- Il test Android unitario non genera APK: workflow confermato limitato a `gradle --no-daemon testDebugUnitTest`. Branch presente: solo `main`.
- La micro-fase 29 resta aperta fino all'esecuzione dei test strumentali su emulatore/dispositivo. Nessun APK; nessun backend/provider/servizio esterno attivato.


### Micro-fase 29 — test aggiuntivi di integrità e cancellazione — 23 novembre 2026
- CI del commit `74201a0ad2996f561931b6bab4eb3a1c06b62ad8`: Android unit tests SUCCESS ([45265214148](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265214148)); Backend tests SUCCESS ([45265214131](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265214131)).
- Aggiunti test instrumented per cancellazione anche dei backup AtomicFile, assenza di resurrezione dei dati, memoria corrotta (fail-closed) e chiave Keystore mancante senza ricreazione né perdita dell'archivio.
- Commit test `271e913e78e90aeb51eb21a55171af28f3d986c9`; commit documentazione `6393a5007ae5346ad9444f9eeca0b07e387a1d52`.
- I nuovi test instrumented non sono coperti dal workflow CI ordinario; micro-fase 29 resta aperta in attesa di test su emulatore/dispositivo. Nessun APK.


### Micro-fase 29 — memoria cifrata: corruzione, perdita chiave e cancellazione backup — 23 novembre 2026
Aggiunti test instrumented che verificano: memoria alterata rifiutata; chiave Keystore mancante senza ricreazione e senza modifica dell'archivio cifrato; cancellazione dei backup AtomicFile della cronologia e della memoria senza resurrezione dei dati. Commit test `271e913e78e90aeb51eb21a55171af28f3d986c9`. Android CI [run 45265475942](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265475942) e backend CI [run 45265476027](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265476027) risultano ancora in esecuzione all'aggiornamento. I test instrumented non sono coperti dal workflow `testDebugUnitTest`; micro-fase 29 resta aperta in attesa di emulatore/dispositivo. Nessun APK.


### Micro-fase 29 — segnalazione errori di lettura memoria cifrata — 23 novembre 2026
Il client Android non converte più silenziosamente gli errori di `loadProjectMemory()` in una memoria apparentemente vuota: conserva il risultato del caricamento e inizializza `memoryError` con l'errore effettivo, così corruzione/perdita chiave è distinguibile da memoria vuota. Commit `82ca6606083bb5b656e4935f6eb174fa8ed477fa`. Android CI [run 45265714340](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265714340) e Backend CI [run 45265714497](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265714497) ancora in esecuzione al momento dell'annotazione. I test instrumented restano da eseguire su emulatore/dispositivo; nessun APK.


### Micro-fase 29 — stabilità dell'identificativo nei retry — 23 novembre 2026
Verificato il successo Android+Backend sul commit `82ca6606083bb5b656e4935f6eb174fa8ed477fa` (run [45265714340](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265714340), [45265714497](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265714497)). Introdotta `requestIdForRetry`: riutilizza sempre lo stesso UUID salvato per i retry; genera e rende persistente un UUID soltanto per richieste legacy senza ID. Aggiunti test unitari di riuso ripetuto e generazione legacy. Commit `eccdef8fa7fa2d0a76cd45fb083bb7476d4e691a`; CI finale Android [45265956708](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265956708) e Backend [45265956651](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45265956651) in coda al momento dell'annotazione. Restano necessari test strumentali su dispositivo/emulatore; nessun APK.


### Continuità chat — disconnessione SSE
Corretto il percorso server quando il client interrompe la connessione: la cancellazione asyncio rilascia subito il lease idempotente come `failed`, consentendo un nuovo claim con lo stesso request ID senza attendere i 30 minuti di timeout. Il lease token mantiene il fencing. La funzione di retry `failed` è già coperta da `test_failed_request_can_be_retried_once`; resta da aggiungere/eseguire una prova E2E di disconnessione ASGI e rete reale. Commit `96dc9e943864b328cc0f3dd5ff4d5b8ef68fd939`; Backend CI [45267609593](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45267609593) completata con esito **SUCCESS**.


### Micro-fase 29 — payload esatto conservato per retry idempotente — 23 novembre 2026
- Audit del percorso di ripresa: prima della correzione Android conservava il request ID ma ricostruiva il payload usando memoria/documenti correnti. Una modifica del contesto dopo l'interruzione poteva causare HTTP 409 perché lo stesso ID sarebbe stato riutilizzato con hash diverso.
- `ChatLine` ora può conservare `requestMessages` completi; il payload viene serializzato nell'archivio AES-GCM/AtomicFile insieme alla cronologia. Le richieste nuove salvano il payload costruito prima della rete; i retry usano il payload salvato, senza ricostruirlo. Per le vecchie richieste pendenti prive di payload viene costruito una sola volta e salvato prima del retry.
- Dopo completamento il payload temporaneo viene rimosso dalla riga, mantenendo la cronologia conversazionale ordinaria.
- Validazione aggiunta: payload solo su righe user pendenti, ruoli/contenuti ammessi, limite totale 32.000 caratteri e ultimo messaggio user coerente con il testo pendente.
- Commit codice: store `94fff4ca0e915fce66f85c2e7b1af98a198def8f`; UI `b4192e29b29b4aca2ce579753f0a0d79b9c81838`; test unitari `5b1a139dd3e28c096e889d4e0b55a6332e791438`; test instrumented persistenza `b5df43041238c39893b54cafdd04a6c85e77dca1`.
- Stato test: in attesa della nuova CI Android; il test instrumented di ricreazione archivio ora verifica anche il round-trip del payload, ma non è eseguito da `testDebugUnitTest` e resta da eseguire su emulatore/dispositivo. Nessun test di force-stop reale, nessun APK.
- Gate 36 resta bloccato; nessun backend/provider/database/WAF esterno attivato.


- Rifinitura finale della persistenza payload: al completamento di una richiesta nuova il campo temporaneo `requestMessages` viene azzerato, come già avviene nel percorso di retry. Commit UI `542af37dca194db2bc9ecfd6e12943ef9b9d867c`. Android CI run [45268304090](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268304090) in esecuzione al momento dell'annotazione; Backend CI run [45268304495](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268304495) in esecuzione. Nessun APK.


- Esito della prima CI Android: run [45268304090](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268304090) **FAILURE** in compilazione Kotlin, per due dichiarazioni locali con lo stesso nome `pendingIndex` nel blocco di invio iniziale (non per i test di persistenza). Corretto rinominando la variabile dedicata al salvataggio del payload in `payloadPendingIndex`. Commit fix `da713b3b3c4ddd938e410563815fbf92fc4d8af8`. Nuove CI Android [45268526027](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526027) e Backend [45268526344](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526344) avviate; esito ancora da verificare.


- Verifica finale dopo fix `da713b3b3c4ddd938e410563815fbf92fc4d8af8`: Android unit tests [run 45268526027](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526027) **SUCCESS**; Backend tests [run 45268526344](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526344) **SUCCESS**. Il task Android è esclusivamente `gradle --no-daemon testDebugUnitTest`: nessun APK assemblato. La CI conferma la compilazione Kotlin e i test unitari, non esegue `androidTest`; il round-trip del payload cifrato, il force-stop/reopen e la ripresa rete reale restano da provare su emulatore/dispositivo. Gate 36 ancora bloccato.

### Micro-fase 29 — audit ripresa dopo riapertura e payload immutabile — 23 novembre 2026
- Revisione statica del percorso UI “Richiesta non completata” / “Riprendi richiesta”: la cronologia iniziale viene caricata dallo store cifrato; la ripresa è esplicita e non automatica; il retry riutilizza request ID e payload salvato; il placeholder assistant viene riutilizzato e il primo delta del replay sostituisce il testo parziale precedente.
- Verificato il round-trip request ID + payload nel test strumentale `pendingRequestIdSurvivesStoreRecreationForSafeRetry`, ma il test non è stato eseguito: il workflow ordinario lancia soltanto `testDebugUnitTest`.
- Android unit CI [45268526027](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526027) e Backend CI [45268526344](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526344) SUCCESS. Nessuna prova di force-stop reale, rete reale o dispositivo; nessun APK.
- Rapporto: [ANDROID_RECOVERY_REVIEW_2026-11-23.md](ANDROID_RECOVERY_REVIEW_2026-11-23.md), commit `0f28518a2a1da5b863ab94d5d6c7e96e08163cfc`.
- La micro-fase resta aperta finché non viene eseguita la suite instrumented su emulatore/dispositivo e completato il test E2E con backend/provider autorizzati. Gate 36 resta bloccato.

### Streaming SSE — validazione rigorosa eventi — 23 novembre 2026
- Audit del parser Android: i frammenti `delta` con JSON malformato o campo `text` non stringa venivano ignorati tramite estrazione permissiva, con rischio di accettare una risposta apparentemente completata ma incompleta.
- Il parser ora richiede un oggetto JSON valido per `delta` e `done`; `delta.text` deve essere stringa. Anche l'evento `error` richiede JSON valido e usa un messaggio stringa valido, altrimenti produce un errore esplicito. I frammenti precedenti restano già persistiti dal persister e la richiesta resta pendente per il retry.
- Aggiunti test per delta malformato, campo text non stringa e evento done malformato. Test ancora da eseguire nella CI successiva; non dichiarare il fix validato fino a esito verde.
- Commit parser a248b47c73054781a9c803ea636cacc5fa883be6; commit test 992f6cdee2b12ce8dc8fe36ce0628d3910096dff.
- Confermato che il repository espone solo il branch `main`. Nessun APK; nessuna modifica a provider o servizi esterni. Micro-fase 29 e gate 36 restano aperti/bloccati.

### SSE Android — hardening parser e CI — 23 novembre 2026
- Audit statico conferma: EOF senza `done` genera errore; delta ricevute sono persistite; la UI mantiene la richiesta pendente e il retry riusa request ID/payload congelato. La prima delta del retry sostituisce il parziale, evitando duplicazioni.
- Parser irrigidito: JSON valido richiesto per `delta`, `done`, `error`; `delta.text` deve essere stringa. Test negativi aggiunti.
- Prima CI Android fallita: `org.json` Android è stub non implementato nei test JVM locali. Correzione: aggiunta dipendenza `testImplementation("org.json:json:20250517")` nel commit `20b33283961322f1adae8df2d29aae3cc98ef543`.
- Run 45269768768 (Android) e 45269768629 (backend) erano ancora in corso all'ultimo controllo; non conteggiare la modifica come validata finché Android non è verde. Report: `docs/ANDROID_SSE_STREAM_RELIABILITY_2026-11-23.md`.
- Nessun test rete reale/device; micro-fase 29 resta aperta e gate 36 bloccato. Nessun APK o provisioning esterno.

### SSE multiline framing — 23 novembre 2026
- Parser aggiornato per accumulare più righe `data:` nello stesso evento e dispatch soltanto alla riga vuota; heartbeat ignorati; un evento incompleto a EOF non viene consegnato.
- Aggiunti test per evento JSON multilinea e EOF senza delimitatore; i test precedenti coprono errori JSON, heartbeat, errore dopo delta e assenza di `done`.
- Commit parser `772f711040c1930eaa25d27883903b7122d1b3e8`; test `5edf07e801c326f0eb98d44dcdaa187c479580d2`. CI Android run 45270145822 e backend run 45270145607 ancora in corso all'ultimo controllo.
- Report `docs/SSE_MULTILINE_FRAMING_HARDENING_2026-11-23.md`. Test in memoria, non E2E rete/device. Micro-fase 29 aperta; gate 36 bloccato; nessun APK.

### Correzione fixture SSE — 23 novembre 2026
- Prima CI Android sui test multilinea: 6 test falliti perché `trimIndent()` rimuoveva i delimitatori SSE finali dalle fixture; compilazione Kotlin riuscita. Aggiornate le fixture per terminare esplicitamente gli eventi completi e distinguere EOF su evento completo da EOF su evento incompleto.
- Commit test `547b6bb8e9ed9dd074ef0ba88fdbdfc05ddcafb2`. Android run 45270440049 e backend run 45270440091 ancora in corso all'ultimo controllo; non dichiarare validazione completata.
- Report `docs/SSE_MULTILINE_FRAMING_HARDENING_2026-11-23.md`. Micro-fase 29 resta aperta, gate 36 bloccato, nessun APK.

### Esito CI SSE e test delta parziale — 23 novembre 2026
- Android run 45270440049 e backend run 45270440091: SUCCESS sul commit `547b6bb8e9ed9dd074ef0ba88fdbdfc05ddcafb2`.
- Aggiunto test per conservare il delta precedente se l'evento successivo è malformato; commit `e083de6094fab41afe21fd7c04cf05d3f344af2d`, CI da verificare.
- Verificato staticamente che la cancellazione coroutine chiude la connessione HTTP con `disconnect()`; manca prova su rete reale/device. Micro-fase 29 aperta, gate 36 bloccato, nessun APK.


### SSE Android — CRLF, data vuoti e campi di estensione — 23 novembre 2026
- CI Android [45271269847](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271269847) e Backend [45271269817](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271269817): **SUCCESS** sul commit `0b84d9fa662f847ec3e87a9581afc7ab45df4a70`.
- Parser ora dispatcha anche un campo `data:` esplicitamente vuoto (distinto da evento privo di data); un `done` vuoto viene rifiutato come JSON non valido. Test CRLF e dati vuoti inclusi.
- Aggiunto test per campi SSE `id`, `retry`, campi sconosciuti e tipo evento non gestito, verificando che il delta e il completamento successivi siano comunque elaborati. Commit test `46ebb5a6376bb048c5e4a391a6b7a29056f310ae`; CI Android [45271516710](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516710) e Backend [45271516892](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516892) avviate, esito da verificare.
- Nessun test instrumented/device o rete reale eseguito; micro-fase 29 resta aperta. Gate 36 bloccato. Nessun APK, provisioning o provider attivato.


### Esito CI finale hardening SSE — 23 novembre 2026
- Verificati gli esiti delle run Android [45271516710](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516710) e backend [45271516892](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516892): **SUCCESS** per il test sui campi di estensione SSE.
- Anche le run di controllo documentazione [45271549194](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271549194) e [45271565435](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271565435): **SUCCESS**.
- Hardening parser SSE e relativi test JVM validati in CI; non equivale a test instrumented/device o E2E su rete reale.
- Micro-fase 29 resta aperta; gate 36 bloccato dalle dipendenze esterne e dal collaudo reale. Nessun APK, provider o infrastruttura esterna attivati.
- Report: `docs/SSE_MULTILINE_FRAMING_HARDENING_2026-11-23.md`.

### Integrità archivio retry Android — 23 novembre 2026
- Commit test strumentali 13c3f3e7d5781f1779d324477c71a617f83eaf7e: rifiuto di UUID duplicati tra richieste pendenti, payload associato a messaggio completato e payload non coerente con il messaggio pendente. Il test sul duplicato verifica anche che l'archivio precedente resti intatto.
- I test appartengono ad androidTest e non sono eseguiti da testDebugUnitTest. Non dichiarare questi casi superati finché non girano su emulatore/dispositivo.
- Chiarimento: pendingRequestIdSurvivesStoreRecreationForSafeRetry verifica una nuova istanza dello store nello stesso processo, non un vero force-stop/riavvio del processo Android.
- Micro-fase 29 aperta; prove su processo reale/device e rete ancora necessarie. Gate 36 bloccato; nessun APK.
- Report: docs/ANDROID_RECOVERY_REVIEW_2026-11-23.md.


### CI confermata — integrità archivio retry Android — 23 novembre 2026
- Android unit tests [45272097900](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272097900) e backend tests [45272097827](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272097827): **SUCCESS**.
- Controlli documentazione [45272126971](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272126971) e tracker [45272141250](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272141250): **SUCCESS**.
- La CI Android esegue `testDebugUnitTest`, non `androidTest`: i tre test strumentali aggiunti restano da eseguire su emulatore/dispositivo. Nessun force-stop o riavvio reale verificato.
- Micro-fase 29 resta aperta; gate 36 resta bloccato. Nessun APK, deploy o provisioning esterno.
- Dettagli: `docs/ANDROID_RECOVERY_REVIEW_2026-11-23.md`.


### Revisione test strumentali e vincolo APK — 23 novembre 2026
- Corretto il commento del test di ripristino: nuova istanza dello store nello stesso processo, non simulazione process death. Commit `90fc1150650278b78f5e14fb5516d18cf58a6b26`.
- Audit statico della suite androidTest completato senza individuare una regressione certa da correggere.
- Il workflow Android resta limitato a `testDebugUnitTest`. La task `connectedDebugAndroidTest` richiede build/installazione degli APK debug e test, vietati dal gate attuale che impedisce qualsiasi assemblaggio APK prima del completamento funzionale. Nessuna task di assemblaggio avviata.
- Microfase 29 resta aperta: serve autorizzazione esplicita futura per build strumentale non distribuibile, oltre a force-stop/process death, riavvio, rete interrotta e test su dispositivo fisico. Non dichiarare prove eseguite.
- Report: `docs/ANDROID_RECOVERY_REVIEW_2026-11-23.md`.


## Verifica CI e stato micro-fase 29 — 24 novembre 2026
- Android unit tests [45273970255](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273970255): **SUCCESS**.
- Backend tests [45273970258](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273970258): **SUCCESS**.
- Controllo report/documentazione [45273993198](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273993198): **SUCCESS**.
- Branch `main`, HEAD verificato: `1331a58f87c9abcf423602d1ba34b2d99b89ddc0`.
- Questi esiti non includono `androidTest`: nessuna build/installazione APK, force-stop, process death, emulatore o prova su dispositivo fisico. Il workflow continua a eseguire soltanto `gradle --no-daemon testDebugUnitTest`.
- Micro-fase 29 resta aperta; gate 36 resta bloccato. Nessun servizio esterno o provider attivato.


## Audit aggiuntivo del flusso di ripresa e CI — 24 novembre 2026
- Riesaminati il percorso UI “Riprendi richiesta”, la costruzione del payload immutabile, il riuso del request ID e il `StreamHistoryPersister`: il retry usa il payload cifrato già salvato quando presente, conserva lo stesso UUID e persiste placeholder/risposta parziale prima della conclusione. Il flush finale e quello su errore/cancellazione sono presenti. Nessuna modifica al codice è stata giustificata dall'audit statico.
- Ricontrollati i test JVM `ChatHistoryValidationTest` e `OnlineAssistantClientTest`: coprono validazione ruoli/ID/payload, ID stabile e legacy, limiti messaggi e parser SSE (framing, CRLF, heartbeat, errori, EOF e campi sconosciuti).
- Backend/documentation CI [45288035460](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45288035460) sul commit `d970de1dc97c1375f678c4913be61544250b391d`: **SUCCESS**. Tracker CI [45288042336](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45288042336) sul commit `cc665f9bc5d8aaae278f921c517f8e08d2b7d004`: **SUCCESS**.
- Limite della verifica: si tratta di audit statico e CI; non equivale a `androidTest`, force-stop/process death, test su dispositivo o E2E di rete. Microfase 29 resta aperta e gate 36 bloccato. Nessun APK o servizio esterno attivato.


## Preparazione recovery Android — 24 novembre 2026
- Aggiornato `docs/DEVICE_SMOKE_TEST.md` con una sequenza riproducibile per process death reale, ripresa con lo stesso request ID/payload, Wi-Fi/mobile, sospensione, riavvio, cancellazione e controlli fail-closed.
- Audit statico della suite `ChatHistoryStoreInstrumentedTest`: sono presenti test per cifratura AES-GCM, chiave Keystore mancante, archivio corrotto, AtomicFile backup, cancellazione senza resurrection, migrazione legacy, concorrenza e payload pendente.
- Il test di ricreazione dello store è esplicitamente limitato allo stesso processo e non viene contato come simulazione di process death.
- Nessuna build o installazione APK è stata avviata; `connectedDebugAndroidTest` resta non eseguito perché genera/installa APK e il blocco APK è ancora in vigore.
- Microfase 29 resta **In attesa di verifica**; il protocollo è preparato, ma i test strumentali e le prove su telefono non sono stati eseguiti.


## Audit e correzione archivio documenti — 24 novembre 2026
- Audit statico di `LocalDocumentRepository` ha rilevato che `list()` ignorava gli archivi illeggibili (`mapNotNull`/`runCatching`) e usava una nuova chiave Keystore in lettura. In caso di chiave mancante i documenti potevano sparire dall'interfaccia e restare irrecuperabili con una chiave appena creata.
- Corretto: la lettura usa esclusivamente la chiave Keystore già esistente; un archivio illeggibile interrompe il caricamento con errore esplicito senza cancellare o nascondere i file. Nuovi import non creano una chiave sostitutiva se esistono già archivi.
- Corretto il recupero AtomicFile: `list()` considera anche backup `.enc.bak` e legge tramite `AtomicFile.openRead()`, consentendo il ripristino del backup dopo scrittura interrotta.
- La UI carica l'archivio su dispatcher IO e mostra l'errore di recupero invece di fallire in composizione. La cancellazione verifica che il file appartenga alla directory privata e corrisponda all'ID del documento.
- Aggiunti `LocalDocumentRepositoryInstrumentedTest` per lettura cifrata, backup-only, chiave mancante, archivio corrotto e rifiuto di path esterni. Sono test Android strumentali predisposti, **non eseguiti**: nessun APK di test generato/installato.
- Backend CI [45288625663](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45288625663) sul commit `78f1d9e`: SUCCESS. Android unit CI sui commit successivi e backend CI recenti sono in coda/in corso al momento dell'aggiornamento; vedere [run Android 45288717504](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45288717504) e [run Backend 45288717639](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45288717639).
- Microfase 30 resta **parziale/in attesa di verifica runtime**: i test strumentali e OCR/import/export reali su dispositivo non sono stati eseguiti. Microfase 29 e gate 36 invariati.


### Autorizzazione test APK temporanei e avvio collaudo microfase 29 — 24 novembre 2026
- Su esplicita autorizzazione dell'utente, revocato il divieto limitatamente alla generazione/installazione di APK debug temporanei in CI per i test strumentali. Resta vietata la build release e la distribuzione dell'APK finale prima del gate 36.
- Aggiunta ProcessRestartRecoveryInstrumentedTest: due invocazioni separate verificano scrittura e lettura della cronologia pendente, request UUID, payload immutabile e memoria progetto.
- Aggiornato .github/workflows/android-build.yml: job JVM invariato; aggiunto job con emulatore Android API 35 che esegue i test strumentali di chat/documenti, poi il test di scrittura, adb shell am force-stop e il test di recupero in una nuova invocazione.
- Commit test 3b5c43b4b48df0d3652c0863d7584da5987f4186; commit workflow 22b54c6c2c75c530004d4d80ac0011481949538a.
- Run Android sul workflow aggiornato: [45289452674](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45289452674), in esecuzione al momento dell'aggiornamento. Run JVM separata [45289429271](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45289429271), in esecuzione.
- Fino all'esito delle run non dichiarare superati i test strumentali. La prova su telefono fisico, Wi-Fi/rete mobile e riavvio dispositivo resta distinta e non viene simulata dall'emulatore.


## Aggiornamento CI e documentazione — 24 novembre 2026
- Allineato `docs/DEVICE_SMOKE_TEST.md`: la precedente frase che vietava ogni APK intermedio è stata sostituita con l'autorizzazione circoscritta per APK debug temporanei in CI/emulatore. Commit documentazione: `2328406f798179c70dd8d0df3b5aafc36f1793d9`.
- Run Android [45289452674](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45289452674), tentativo rilanciato: job `android-unit-tests` **SUCCESS**; job `android-instrumented-tests` ancora **IN_PROGRESS** all'ultimo controllo, step emulatore/test in esecuzione. Il recupero log GitHub ha restituito 404 BlobNotFound; non è quindi disponibile il dettaglio dei test in corso.
- Non conteggiare ancora la sequenza instrumented/process-restart come superata. Microfase 29 resta aperta; microfase 30 resta in attesa di verifica runtime. Nessuna prova su telefono fisico è stata eseguita. Nessun APK release, distribuzione, deploy, provider o servizio esterno è stato attivato.
