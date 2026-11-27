# Registro operativo delle fasi — SuperIA

Aggiornato: 26 novembre 2026
Ambito: esclusivamente il repository SuperIA. Questo registro separa codice presente, test CI, collaudo reale e rilascio.

## Verifica emulatore Android — 26 novembre 2026
- Run `45796356330` sul commit `2dea1b02`: test unitari Android superati e APK debug temporanei compilati; test strumentali non avviati.
- Diagnosi dai log: il workflow forzava TCG/software emulation tramite `ANDROID_I_WANT_MY_TCG=yes`; l'emulatore ha impiegato circa 479 secondi per il boot e il servizio `settings` ha restituito `Broken pipe`, causando exit code 224 prima dell'esecuzione dello script di test.
- Correzione workflow: rimosso il forcing TCG e impostata accelerazione automatica (`-accel auto`) con opzioni headless/no-snapshot/SwiftShader. La correzione deve essere convalidata da una nuova esecuzione CI; non equivale a test strumentali superati.

## Seconda diagnosi emulatore Android — 26 novembre 2026
- Run `45800116557` sul commit `dd6fa40b`: test unitari Android superati, backend superato, build APK debug temporanea superata; test strumentali non avviati.
- Log: l'action `reactivecircus/android-emulator-runner` disabilita l'accelerazione hardware e aggiunge `-accel off`; l'emulatore termina con `Software CPU emulation (-accel off) is not supported` perché il runner non espone accelerazione hardware.
- Correzione successiva: abilita esplicitamente TCG tramite `ANDROID_I_WANT_MY_TCG=yes` e aumenta il boot timeout a 1200 secondi. Il precedente tentativo TCG ha raggiunto `sys.boot_completed=1` dopo circa 479 secondi ma ha poi avuto `Broken pipe` sul servizio Settings; questa soluzione resta sperimentale e deve essere verificata.

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
- CI Android: workflow ordinario limitato ai test JVM rapidi; la suite strumentale su emulatore è disattivata nei push/PR e può essere avviata solo manualmente con `run_instrumented=true`. Il job APK temporanei è stato rimosso.
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


### Correzione controllo process-death CI — 24 novembre 2026
- Corretto il controllo successivo a `adb shell am force-stop`: ora verifica la presenza di un PID numerico con `grep`, evitando il parsing CRLF fragile della precedente pipeline `tr`.
- Commit: `96c867ef84ec120eeccde3794de5121bcdc37265`.
- Backend CI [45403971830](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45403971830): **SUCCESS**.
- Backend CI [45403894403](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45403894403): **SUCCESS**.
- Android CI [45404458992](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45404458992): **IN_PROGRESS** al controllo; entrambi i job erano in preparazione Gradle, test strumentali non ancora iniziati.
- La precedente run Android [45289452674](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45289452674) risulta ancora bloccata nello step emulator/test; log non disponibili (404 BlobNotFound). Non conteggiare come successo.
- Microfase 29 aperta; microfase 30 in attesa di verifica runtime; nessun test su telefono fisico, release APK o servizio esterno.


### Aggiornamento 24 novembre 2026 — prima esecuzione test strumentali 29–30
- Commit CI esaminato: `783637a9b2877bcf3ebe94b31d8aad507d1a6744`.
- Android JVM: job `android-unit-tests` **SUCCESS**.
- Emulator API 35: job `android-instrumented-tests` **FAILURE**. Il report XML indica **0 test eseguiti** e `Instrumentation run failed due to Process crashed`; non è quindi un fallimento di asserzioni e non prova né smentisce il comportamento delle classi sotto test.
- Diagnostica caricata come artifact GitHub Actions `android-instrumented-test-reports` (ID `14939369301`). Il log UTP conferma installazione dei due APK debug temporanei e crash del processo instrumentation; non contiene lo stack trace nativo/Java necessario per attribuire la causa.
- Commit `c6a7bba04e9a49a3b58045690f2cb27bc93d02e1`: il workflow ora raccoglie logcat, stato instrumentation e lista dei runner installati in caso di crash e li allega all'artifact. In attesa della nuova run per identificare la causa.
- Micro-fasi 29 e 30 **restano aperte**: nessun test strumentale è ancora stato eseguito con esito verificabile. Nessun test su telefono fisico, nessuna build release o distribuzione.


### Correzione workflow emulatore — 24 novembre 2026
- Esaminati i log completi della run Android [45483033138](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45483033138): il runner emulatore si avvia correttamente, ma lo script si interrompe prima di Gradle con errore shell `Syntax error: end of file unexpected (expecting "}")` sulla definizione di funzione `run_instrumented_tests()`.
- Causa identificata: l'azione emulator-runner esegue le righe dello script tramite invocazioni shell separate; la definizione multilinea della funzione non persiste tra le invocazioni. Nessun test strumentale è stato eseguito in questa run; l'artifact contiene solo la lista instrumentation vuota.
- Corretto il workflow eliminando la funzione e rendendo ogni chiamata Gradle e la relativa raccolta diagnostica un singolo comando shell. Il controllo force-stop resta separato.
- Commit fix: `a3302498c9b460f3a334ab0d24e4e157c980d701`. Nuova run automatica da verificare.
- Android JVM: **SUCCESS**; backend: **SUCCESS**. Micro-fasi 29 e 30 restano aperte: nessuna prova strumentale completata. Nessun test su telefono fisico, nessuna build release/distribuzione e nessun servizio esterno attivato.


## Aggiornamento hosting — 24 novembre 2026
- Verificata la struttura effettiva del backend: `services/api/index.py` esporta già l'istanza FastAPI `app`; `services/api/requirements.txt` dichiara le dipendenze runtime e il test `test_vercel_entrypoint.py` controlla l'entrypoint.
- Le istruzioni ufficiali Vercel confermano che `index.py` è un entrypoint FastAPI riconosciuto; con Root Directory `services/api` non serve creare un wrapper aggiuntivo o un `vercel.json` catch-all.
- Aggiunta checklist di configurazione Preview, variabili server-side, test health/readiness, SSE, PostgreSQL e firewall nel documento `docs/VERCEL_HOBBY_PREPARATION.md`.
- Confermato da Prisma che Free prevede 200.000 operazioni/mese, 2 GB e nessuna carta; non include backup automatici e al limite le query si fermano. Non è stato creato alcun database.
- Vercel Hobby è gratuito per uso personale ma non è stata verificata la registrazione senza carta per l'account/paese dell'utente. Questo resta un passaggio manuale obbligatorio; non fare trial Pro, non inserire carta e non effettuare deploy senza verifica.
- Nessun account, progetto Vercel, database, segreto, provider AI o deployment è stato creato/attivato. Backend online e chat Android restano non operativi.
- Microfasi backend/staging non chiuse: necessarie registrazione Hobby senza carta, import Preview, PostgreSQL, segreti, firewall rate-limit pubblicato e testato, SSE end-to-end e provider gratuito reale approvato.



### Correzione preventiva ambiente Vercel — 25 novembre 2026
- Rilevamento automatico del runtime tramite `VERCEL=1` e `VERCEL_ENV`: Production viene trattato come production, Preview come staging, anche senza `SUPERIA_ENVIRONMENT` personalizzata.
- In entrambi gli ambienti il backend rifiuta SQLite e richiede `SUPERIA_DATABASE_URL` PostgreSQL; la presenza di `SUPERIA_AUTH_DB` non consente più il fallback a storage effimero.
- In locale, fuori da Vercel, resta invariata la modalità development per SQLite.
- Commit codice: `739a9a3ae5fb16e71b96eb6192ff574525b57f80`; commit test: `9b2d19065e3a0a7fbafe00d4567cbb074437d579`; commit documentazione: `20de12c2f4c74849139f48413b5d570cd96c863d`.
- Aggiunti test per Vercel Production senza configurazione custom, Preview e PostgreSQL configurato. Esito CI non ancora verificato.
- Nessun account, database, segreto, provider o deployment è stato creato. Il pairing resta disabilitato finché non è confermato il rate limit gateway effettivo.


### Rafforzamento fail-closed Vercel — 25 novembre 2026
- Revisione del controllo precedente: la variabile di piattaforma `VERCEL=1` ora prevale sempre su `SUPERIA_ENVIRONMENT`, così un valore custom `development` non può riabilitare SQLite in Preview/Production.
- Aggiunti test specifici per downgrade tentato sia in Production sia in Preview.
- Commit logica: `b290eb875dfb633acbb67393590fd76924c3fba0`; commit test: `c30e5ff5c076ba66b5d0a8412ddfc2185d446156`.
- Stato CI non verificabile tramite i controlli disponibili al momento; non dichiarare i test superati finché GitHub Actions non mostra un'esecuzione completata.


### Isolamento dei test Vercel — 25 novembre 2026
- Il fixture helper dei test rimuove anche `VERCEL` e `VERCEL_ENV` prima di impostare i valori specifici di ciascun test, evitando dipendenze dall'ambiente del runner.
- Commit: `2c7882ada509b686174cbfcacf2ef7bb20e23902`. Esito CI non ancora disponibile tramite i controlli consultabili.


### Hardening stato router su Vercel — 25 novembre 2026
- Audit ha rilevato che `FreeOnlyRouter` determinava l'ambiente solo da `SUPERIA_ENVIRONMENT`: una variabile custom `development` avrebbe potuto consentire `SUPERIA_ROUTER_STATE_DB=/tmp/...` anche su Vercel.
- Corretto: `VERCEL=1` prevale sempre; Preview è staging e Production è production. PostgreSQL è richiesto anche per lo stato del router; nessun fallback SQLite temporaneo in quei runtime.
- Test aggiunti: Preview/Production senza PostgreSQL e Production con override development ma stato router SQLite vengono rifiutati.
- Codice: `ca5a7e9c6b1a575e370ba3c332a72358e441a725`; test: `e44d38af7535778664ab615e943b7ad819d3077e`. CI da verificare.


### Verifica backend — idempotenza concorrente e hardening SSE — 25 novembre 2026
- Run Backend tests [45650079235](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45650079235): **SUCCESS**. La suite completa ha verificato il claim concorrente: due chiamate simultanee con lo stesso dispositivo/ID producono un solo lease `claimed` e un risultato `processing`.
- Correzione del test introdotta nel commit `e3cea74238fb2b11e250f45175f2fc0092033406`; non sono stati modificati i vincoli di produzione.
- Hardening SSE: commit codice `0446d0dcdfc2c2f009fd1fb8ac465e25994a8c83`; commit test `b137a8601bfd4b1cd394a485719253900876e7ea`.
- Il backend ora accetta soltanto i nomi evento `delta` e `done`; un evento provider non previsto viene trattato come errore retryable. Dopo `done` interrompe il consumo dello stream e chiude l'iteratore, senza elaborare eventi successivi.
- Run Backend tests [45687252959](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45687252959): **SUCCESS**, inclusi i test contro event injection e dati successivi a `done`.
- Questi risultati validano la suite backend in CI, non il provider AI reale, il deploy HTTPS, il database persistente di produzione, il pairing protetto da WAF o l'esperienza Android end-to-end.
- Micro-fase 36 resta **bloccata** dai prerequisiti esterni già elencati. Microfasi Android 29–30 restano aperte per le verifiche strumentali/device non completate. Nessun APK release, provider, hosting o database esterno attivato.



### Correzione fixture SSE e verifica cancellazione stream — 25 novembre 2026
- Il test anti-iniezione SSE è stato corretto per includere un vero carattere di nuova riga nel valore evento; Backend tests [45687491112](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45687491112): **SUCCESS**, suite completa superata.
- Precisazione sull'idempotenza: `test_stream_request_id_rejects_an_already_processing_duplicate` prepara un claim già in stato `processing` e verifica il rifiuto HTTP 409; separatamente, `test_concurrent_claims_only_grant_one_lease` usa `Barrier` e `ThreadPoolExecutor` per avviare due claim simultanei e verifica un solo lease `claimed` e l'altro `processing`. Il test di race reale è presente nel commit `e3cea74238fb2b11e250f45175f2fc0092033406` e nella suite Backend tests [45650079235](https://github.com/damianoavvocato-star/SuperIA): **SUCCESS**.
- Aggiunto `test_cancelled_chat_stream_releases_lease_and_closes_provider`: consuma un delta, simula la cancellazione del client durante l'attesa del provider e verifica chiusura dell'iteratore e rilascio del lease allo stato `failed`. Commit test: `e82ec2fb5e5b727c9f97f2bbe8ddda976b79acb8`; commit di chiarimento asserzioni: `8ae3322f7a5a91c927b487a661cd4062d6132aab`.
- La verifica CI del nuovo test di cancellazione è in attesa di un riscontro GitHub Actions consultabile; non dichiarare il test superato fino al completamento del workflow.
- I test backend in CI non dimostrano ancora pairing pubblico, provider AI reale, PostgreSQL di produzione, deploy HTTPS o chat Android end-to-end. Gate 36 resta bloccato dai prerequisiti esterni; microfasi Android 29–30 restano aperte per i test strumentali non completati. Nessun APK release, servizio esterno, database o provider è stato attivato.

- Estensione successiva del test di cancellazione: dopo aver verificato `failed` e chiusura dell'iteratore, il test prova a reclamare immediatamente lo stesso `request_id` con lo stesso payload e verifica un nuovo lease. Commit: `5c228d2a392335b7d45fcfeb8662dd9d300e2d88`. Il wrapper GitHub disponibile non espone l'elenco completo dei workflow push; lo stato CI di questo commit non è stato quindi confermato e resta da verificare dalla pagina Actions.


## Nuova funzionalità approvata — chat multiple, progetti separati e cancellazione completa — 25 novembre 2026
- L'utente ha approvato la possibilità di creare più chat/progetti indipendenti, passare da un progetto all'altro e conservare separatamente cronologia, memoria, documenti e file.
- Requisito di eliminazione: cancellare definitivamente i dati della chat e gli output attribuiti ad essa, incluse copie private dell'app e copie esportate nella Galleria quando Android consente la rimozione.
- Specifica dettagliata aggiunta in `docs/MULTI_CHAT_PROJECTS_AND_DELETION_PLAN.md` (commit `0a2d38de32cf2cdc701d81e4dd920d09939eb7bc`).
- Vincoli Android: le copie in MediaStore create da SuperIA saranno tracciate per chat; se il sistema richiede consenso, la cancellazione attende il dialogo Android. Non si eliminano file preesistenti/importati o copie esterne non più controllabili; gli esiti parziali devono essere espliciti.
- Audit iniziale: l'archivio corrente `ChatHistoryStore` conserva una sola cronologia cifrata e una memoria progetto globale; `LocalDocumentRepository` conserva documenti in un archivio privato condiviso. Non esiste ancora un'associazione per chat ID sufficiente per cancellare selettivamente.
- Stato: **specifica approvata, implementazione da avviare**. Prossimo passo: audit completo delle dipendenze di persistenza/UI, poi modello multi-chat cifrato con migrazione legacy atomica. Nessun test nuovo eseguito, nessun APK, deploy, provider o servizio esterno attivato.


### Audit iniziale multi-chat e cancellazione — 25 novembre 2026
- Verificati staticamente `ChatHistoryStore.kt`, `AssistantChatPanel.kt`, `LocalDocumentRepository.kt` e `MainActivity.kt`.
- Confermato: una sola cronologia cifrata `conversation_history.enc`, una sola memoria globale `project_memory.enc`, documenti condivisi in `filesDir/documents`, UI senza elenco conversazioni né ID chat.
- Il pulsante attuale `Svuota` elimina soltanto la cronologia globale, non è ancora una cancellazione completa chat/progetto.
- L'import documenti conserva una copia cifrata globale e la UI la rende consultabile nelle chat successive; non attribuire retroattivamente tali file a una nuova chat né cancellarli automaticamente.
- L'esportazione TXT usa un URI scelto dall'utente e non è attualmente tracciata; non dichiarare che SuperIA possa cancellare copie esterne già esportate.
- Audit dettagliato: `docs/MULTI_CHAT_INITIAL_AUDIT_2026-11-25.md` (commit `0d1b7036856f204ed3bec9a0782940c67ec3ed94`).
- Prossimo step implementativo: progettare/aggiungere archivio cifrato multi-chat con migrazione legacy atomica, preservando memoria e richieste pendenti. Nessuna modifica runtime né test sono stati eseguiti in questo audit.
# Registro operativo delle fasi — SuperIA

Aggiornato: 25 novembre 2026
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


### Correzione controllo process-death CI — 24 novembre 2026
- Corretto il controllo successivo a `adb shell am force-stop`: ora verifica la presenza di un PID numerico con `grep`, evitando il parsing CRLF fragile della precedente pipeline `tr`.
- Commit: `96c867ef84ec120eeccde3794de5121bcdc37265`.
- Backend CI [45403971830](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45403971830): **SUCCESS**.
- Backend CI [45403894403](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45403894403): **SUCCESS**.
- Android CI [45404458992](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45404458992): **IN_PROGRESS** al controllo; entrambi i job erano in preparazione Gradle, test strumentali non ancora iniziati.
- La precedente run Android [45289452674](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45289452674) risulta ancora bloccata nello step emulator/test; log non disponibili (404 BlobNotFound). Non conteggiare come successo.
- Microfase 29 aperta; microfase 30 in attesa di verifica runtime; nessun test su telefono fisico, release APK o servizio esterno.


### Aggiornamento 24 novembre 2026 — prima esecuzione test strumentali 29–30
- Commit CI esaminato: `783637a9b2877bcf3ebe94b31d8aad507d1a6744`.
- Android JVM: job `android-unit-tests` **SUCCESS**.
- Emulator API 35: job `android-instrumented-tests` **FAILURE**. Il report XML indica **0 test eseguiti** e `Instrumentation run failed due to Process crashed`; non è quindi un fallimento di asserzioni e non prova né smentisce il comportamento delle classi sotto test.
- Diagnostica caricata come artifact GitHub Actions `android-instrumented-test-reports` (ID `14939369301`). Il log UTP conferma installazione dei due APK debug temporanei e crash del processo instrumentation; non contiene lo stack trace nativo/Java necessario per attribuire la causa.
- Commit `c6a7bba04e9a49a3b58045690f2cb27bc93d02e1`: il workflow ora raccoglie logcat, stato instrumentation e lista dei runner installati in caso di crash e li allega all'artifact. In attesa della nuova run per identificare la causa.
- Micro-fasi 29 e 30 **restano aperte**: nessun test strumentale è ancora stato eseguito con esito verificabile. Nessun test su telefono fisico, nessuna build release o distribuzione.


### Correzione workflow emulatore — 24 novembre 2026
- Esaminati i log completi della run Android [45483033138](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45483033138): il runner emulatore si avvia correttamente, ma lo script si interrompe prima di Gradle con errore shell `Syntax error: end of file unexpected (expecting "}")` sulla definizione di funzione `run_instrumented_tests()`.
- Causa identificata: l'azione emulator-runner esegue le righe dello script tramite invocazioni shell separate; la definizione multilinea della funzione non persiste tra le invocazioni. Nessun test strumentale è stato eseguito in questa run; l'artifact contiene solo la lista instrumentation vuota.
- Corretto il workflow eliminando la funzione e rendendo ogni chiamata Gradle e la relativa raccolta diagnostica un singolo comando shell. Il controllo force-stop resta separato.
- Commit fix: `a3302498c9b460f3a334ab0d24e4e157c980d701`. Nuova run automatica da verificare.
- Android JVM: **SUCCESS**; backend: **SUCCESS**. Micro-fasi 29 e 30 restano aperte: nessuna prova strumentale completata. Nessun test su telefono fisico, nessuna build release/distribuzione e nessun servizio esterno attivato.


## Aggiornamento hosting — 24 novembre 2026
- Verificata la struttura effettiva del backend: `services/api/index.py` esporta già l'istanza FastAPI `app`; `services/api/requirements.txt` dichiara le dipendenze runtime e il test `test_vercel_entrypoint.py` controlla l'entrypoint.
- Le istruzioni ufficiali Vercel confermano che `index.py` è un entrypoint FastAPI riconosciuto; con Root Directory `services/api` non serve creare un wrapper aggiuntivo o un `vercel.json` catch-all.
- Aggiunta checklist di configurazione Preview, variabili server-side, test health/readiness, SSE, PostgreSQL e firewall nel documento `docs/VERCEL_HOBBY_PREPARATION.md`.
- Confermato da Prisma che Free prevede 200.000 operazioni/mese, 2 GB e nessuna carta; non include backup automatici e al limite le query si fermano. Non è stato creato alcun database.
- Vercel Hobby è gratuito per uso personale ma non è stata verificata la registrazione senza carta per l'account/paese dell'utente. Questo resta un passaggio manuale obbligatorio; non fare trial Pro, non inserire carta e non effettuare deploy senza verifica.
- Nessun account, progetto Vercel, database, segreto, provider AI o deployment è stato creato/attivato. Backend online e chat Android restano non operativi.
- Microfasi backend/staging non chiuse: necessarie registrazione Hobby senza carta, import Preview, PostgreSQL, segreti, firewall rate-limit pubblicato e testato, SSE end-to-end e provider gratuito reale approvato.



### Correzione preventiva ambiente Vercel — 25 novembre 2026
- Rilevamento automatico del runtime tramite `VERCEL=1` e `VERCEL_ENV`: Production viene trattato come production, Preview come staging, anche senza `SUPERIA_ENVIRONMENT` personalizzata.
- In entrambi gli ambienti il backend rifiuta SQLite e richiede `SUPERIA_DATABASE_URL` PostgreSQL; la presenza di `SUPERIA_AUTH_DB` non consente più il fallback a storage effimero.
- In locale, fuori da Vercel, resta invariata la modalità development per SQLite.
- Commit codice: `739a9a3ae5fb16e71b96eb6192ff574525b57f80`; commit test: `9b2d19065e3a0a7fbafe00d4567cbb074437d579`; commit documentazione: `20de12c2f4c74849139f48413b5d570cd96c863d`.
- Aggiunti test per Vercel Production senza configurazione custom, Preview e PostgreSQL configurato. Esito CI non ancora verificato.
- Nessun account, database, segreto, provider o deployment è stato creato. Il pairing resta disabilitato finché non è confermato il rate limit gateway effettivo.


### Rafforzamento fail-closed Vercel — 25 novembre 2026
- Revisione del controllo precedente: la variabile di piattaforma `VERCEL=1` ora prevale sempre su `SUPERIA_ENVIRONMENT`, così un valore custom `development` non può riabilitare SQLite in Preview/Production.
- Aggiunti test specifici per downgrade tentato sia in Production sia in Preview.
- Commit logica: `b290eb875dfb633acbb67393590fd76924c3fba0`; commit test: `c30e5ff5c076ba66b5d0a8412ddfc2185d446156`.
- Stato CI non verificabile tramite i controlli disponibili al momento; non dichiarare i test superati finché GitHub Actions non mostra un'esecuzione completata.


### Isolamento dei test Vercel — 25 novembre 2026
- Il fixture helper dei test rimuove anche `VERCEL` e `VERCEL_ENV` prima di impostare i valori specifici di ciascun test, evitando dipendenze dall'ambiente del runner.
- Commit: `2c7882ada509b686174cbfcacf2ef7bb20e23902`. Esito CI non ancora disponibile tramite i controlli consultabili.


### Hardening stato router su Vercel — 25 novembre 2026
- Audit ha rilevato che `FreeOnlyRouter` determinava l'ambiente solo da `SUPERIA_ENVIRONMENT`: una variabile custom `development` avrebbe potuto consentire `SUPERIA_ROUTER_STATE_DB=/tmp/...` anche su Vercel.
- Corretto: `VERCEL=1` prevale sempre; Preview è staging e Production è production. PostgreSQL è richiesto anche per lo stato del router; nessun fallback SQLite temporaneo in quei runtime.
- Test aggiunti: Preview/Production senza PostgreSQL e Production con override development ma stato router SQLite vengono rifiutati.
- Codice: `ca5a7e9c6b1a575e370ba3c332a72358e441a725`; test: `e44d38af7535778664ab615e943b7ad819d3077e`. CI da verificare.


### Verifica backend — idempotenza concorrente e hardening SSE — 25 novembre 2026
- Run Backend tests [45650079235](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45650079235): **SUCCESS**. La suite completa ha verificato il claim concorrente: due chiamate simultanee con lo stesso dispositivo/ID producono un solo lease `claimed` e un risultato `processing`.
- Correzione del test introdotta nel commit `e3cea74238fb2b11e250f45175f2fc0092033406`; non sono stati modificati i vincoli di produzione.
- Hardening SSE: commit codice `0446d0dcdfc2c2f009fd1fb8ac465e25994a8c83`; commit test `b137a8601bfd4b1cd394a485719253900876e7ea`.
- Il backend ora accetta soltanto i nomi evento `delta` e `done`; un evento provider non previsto viene trattato come errore retryable. Dopo `done` interrompe il consumo dello stream e chiude l'iteratore, senza elaborare eventi successivi.
- Run Backend tests [45687252959](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45687252959): **SUCCESS**, inclusi i test contro event injection e dati successivi a `done`.
- Questi risultati validano la suite backend in CI, non il provider AI reale, il deploy HTTPS, il database persistente di produzione, il pairing protetto da WAF o l'esperienza Android end-to-end.
- Micro-fase 36 resta **bloccata** dai prerequisiti esterni già elencati. Microfasi Android 29–30 restano aperte per le verifiche strumentali/device non completate. Nessun APK release, provider, hosting o database esterno attivato.



### Correzione fixture SSE e verifica cancellazione stream — 25 novembre 2026
- Il test anti-iniezione SSE è stato corretto per includere un vero carattere di nuova riga nel valore evento; Backend tests [45687491112](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45687491112): **SUCCESS**, suite completa superata.
- Precisazione sull'idempotenza: `test_stream_request_id_rejects_an_already_processing_duplicate` prepara un claim già in stato `processing` e verifica il rifiuto HTTP 409; separatamente, `test_concurrent_claims_only_grant_one_lease` usa `Barrier` e `ThreadPoolExecutor` per avviare due claim simultanei e verifica un solo lease `claimed` e l'altro `processing`. Il test di race reale è presente nel commit `e3cea74238fb2b11e250f45175f2fc0092033406` e nella suite Backend tests [45650079235](https://github.com/damianoavvocato-star/SuperIA): **SUCCESS**.
- Aggiunto `test_cancelled_chat_stream_releases_lease_and_closes_provider`: consuma un delta, simula la cancellazione del client durante l'attesa del provider e verifica chiusura dell'iteratore e rilascio del lease allo stato `failed`. Commit test: `e82ec2fb5e5b727c9f97f2bbe8ddda976b79acb8`; commit di chiarimento asserzioni: `8ae3322f7a5a91c927b487a661cd4062d6132aab`.
- La verifica CI del nuovo test di cancellazione è in attesa di un riscontro GitHub Actions consultabile; non dichiarare il test superato fino al completamento del workflow.
- I test backend in CI non dimostrano ancora pairing pubblico, provider AI reale, PostgreSQL di produzione, deploy HTTPS o chat Android end-to-end. Gate 36 resta bloccato dai prerequisiti esterni; microfasi Android 29–30 restano aperte per i test strumentali non completati. Nessun APK release, servizio esterno, database o provider è stato attivato.

- Estensione successiva del test di cancellazione: dopo aver verificato `failed` e chiusura dell'iteratore, il test prova a reclamare immediatamente lo stesso `request_id` con lo stesso payload e verifica un nuovo lease. Commit: `5c228d2a392335b7d45fcfeb8662dd9d300e2d88`. Il wrapper GitHub disponibile non espone l'elenco completo dei workflow push; lo stato CI di questo commit non è stato quindi confermato e resta da verificare dalla pagina Actions.


## Nuova funzionalità approvata — chat multiple, progetti separati e cancellazione completa — 25 novembre 2026
- L'utente ha approvato la possibilità di creare più chat/progetti indipendenti, passare da un progetto all'altro e conservare separatamente cronologia, memoria, documenti e file.
- Requisito di eliminazione: cancellare definitivamente i dati della chat e gli output attribuiti ad essa, incluse copie private dell'app e copie esportate nella Galleria quando Android consente la rimozione.
- Specifica dettagliata aggiunta in `docs/MULTI_CHAT_PROJECTS_AND_DELETION_PLAN.md` (commit `0a2d38de32cf2cdc701d81e4dd920d09939eb7bc`).
- Vincoli Android: le copie in MediaStore create da SuperIA saranno tracciate per chat; se il sistema richiede consenso, la cancellazione attende il dialogo Android. Non si eliminano file preesistenti/importati o copie esterne non più controllabili; gli esiti parziali devono essere espliciti.
- Audit iniziale: l'archivio corrente `ChatHistoryStore` conserva una sola cronologia cifrata e una memoria progetto globale; `LocalDocumentRepository` conserva documenti in un archivio privato condiviso. Non esiste ancora un'associazione per chat ID sufficiente per cancellare selettivamente.
- Stato: **specifica approvata, implementazione da avviare**. Prossimo passo: audit completo delle dipendenze di persistenza/UI, poi modello multi-chat cifrato con migrazione legacy atomica. Nessun test nuovo eseguito, nessun APK, deploy, provider o servizio esterno attivato.


### Audit iniziale multi-chat e cancellazione — 25 novembre 2026
- Verificati staticamente `ChatHistoryStore.kt`, `AssistantChatPanel.kt`, `LocalDocumentRepository.kt` e `MainActivity.kt`.
- Confermato: una sola cronologia cifrata `conversation_history.enc`, una sola memoria globale `project_memory.enc`, documenti condivisi in `filesDir/documents`, UI senza elenco conversazioni né ID chat.
- Il pulsante attuale `Svuota` elimina soltanto la cronologia globale, non è ancora una cancellazione completa chat/progetto.
- L'import documenti conserva una copia cifrata globale e la UI la rende consultabile nelle chat successive; non attribuire retroattivamente tali file a una nuova chat né cancellarli automaticamente.
- L'esportazione TXT usa un URI scelto dall'utente e non è attualmente tracciata; non dichiarare che SuperIA possa cancellare copie esterne già esportate.
- Audit dettagliato: `docs/MULTI_CHAT_INITIAL_AUDIT_2026-11-25.md` (commit `0d1b7036856f204ed3bec9a0782940c67ec3ed94`).
- Prossimo step implementativo: progettare/aggiungere archivio cifrato multi-chat con migrazione legacy atomica, preservando memoria e richieste pendenti. Nessuna modifica runtime né test sono stati eseguiti in questo audit.


### Primo incremento implementativo — indice chat/progetti cifrato — 25 novembre 2026
- Aggiunto `ChatWorkspaceStore.kt`: indice locale cifrato AES-GCM con chiave dedicata Android Keystore e scrittura AtomicFile; supporta creazione di chat/progetti con UUID, elenco, rinomina e archiviazione/ripristino.
- Aggiunti test strumentali per persistenza tra istanze, ID distinti, isolamento rinomina/archiviazione, cifratura a riposo, rifiuto indice corrotto e input non valido.
- Commit codice: `004b9cb2fbc331c3ef5c56e5c8c366515da2d1bf`; commit test: `1d155cfbdaac328129258bfc083f1c8d0c3ce730`.
- **Limite esplicito:** il registro è soltanto il primo livello dati. Non è ancora collegato alla UI, alla cronologia/memoria per chat o ai documenti. La vecchia cronologia `conversation_history.enc` e la memoria `project_memory.enc` restano intatte e non vengono migrate o cancellate. Non esporre ancora la cancellazione chat: prima occorre implementare archivi separati e cancellazione coordinata per ID.
- Test strumentali aggiunti ma non ancora eseguiti/confermati in CI; nessun APK generato, nessun test su telefono, nessun servizio esterno attivato.


### Architettura isolamento cronologia/memoria — 25 novembre 2026
- Tracciato il flusso reale in `AssistantChatPanel`: store singolo, stato Compose caricato una volta, memoria globale, persister streaming e retry legati allo store iniziale; il repository documenti è condiviso.
- Definita migrazione conservativa: workspace legacy stabile, copia byte-per-byte degli archivi già cifrati con la chiave esistente, verifica destinazioni e marker durevole prima di trattare i vecchi percorsi come obsoleti. Nessuna cancellazione dei vecchi archivi nella prima migrazione.
- Specificati i requisiti per coroutine/stream: ID workspace immutabile per ogni richiesta; navigazione bloccata durante generazione o job sempre vincolato al workspace d'origine; mai cambiare il target di scrittura di un retry pendente.
- Definito isolamento futuro documenti sotto directory per workspace e cancellazione coordinata prima della rimozione dall'indice.
- Documento: `docs/MULTI_CHAT_DATA_ISOLATION_ARCHITECTURE.md`, commit `2e255fa9d89037103efbdd5333f5950588ba0f5c`.
- Nessuna modifica runtime in questo incremento; nessuna migrazione eseguita, nessun dato legacy toccato, nessun test o APK dichiarato. Il registro cifrato e i test strumentali del precedente incremento restano in attesa di CI verificabile.


### Isolamento archivio cronologia e memoria per workspace — 25 novembre 2026
- `ChatHistoryStore(context, workspaceId)` ora supporta percorsi AtomicFile e SharedPreferences distinti per UUID workspace; l'uso senza ID mantiene i nomi e il comportamento legacy esistenti.
- La cifratura AES-GCM continua a usare la chiave non esportabile Android Keystore già esistente: gli archivi dei workspace sono separati ma restano cifrati a riposo.
- `clearLegacyLocalHistoryOnce()` rifiuta esplicitamente qualsiasi store associato a un workspace, impedendo che la pulizia del vecchio archivio venga invocata accidentalmente da una nuova chat.
- Codice: commit `38f03b20ca5c0ddca52f8defb57b77f2b1d2789b`.
- Test strumentali aggiunti per verificare isolamento di due cronologie e due memorie, persistenza dopo ricreazione dello store, preservazione dell'archivio legacy e blocco della pulizia legacy da workspace: commit `8b3309bde33950c6f25f0e418cc51f1b6049ff97`.
- **Test non ancora eseguiti/confermati**. Nessuna migrazione automatica è stata eseguita; i dati legacy restano nei percorsi originali. La UI non usa ancora il nuovo costruttore e non espone ancora chat/progetti multipli.
- Prossimo passo: migrazione legacy idempotente e verificata, poi isolamento documenti e collegamento UI. Nessun APK, deploy, provider o servizio esterno attivato.


### Migrazione non distruttiva del workspace legacy — 25 novembre 2026
- Aggiunto workspace stabile `00000000-0000-4000-8000-000000000001` per la conversazione preesistente.
- Migrazione copia byte-per-byte gli archivi AES-GCM di cronologia e memoria e verifica la lettura della destinazione; conserva intatti i file sorgente e interrompe se rileva contenuti di destinazione differenti.
- Marker di completamento durevole, migrazione ripetibile e chiamata integrata prima del caricamento della chat.
- Rimosso dal percorso di avvio il richiamo a `clearLegacyLocalHistoryOnce()`: non deve cancellare la cronologia esistente durante l'avvio multi-chat.
- Test strumentale aggiunto per verificare contenuti, payload pendenti `requestId`/`requestMessages`, uguaglianza byte-per-byte e retry della migrazione.
- Codice: `4a072312`, `452fe5ff`, `29728fa`, `9447cdaf`; test: `fec715e4`.
- Test CI ancora da verificare. Nessun APK rilasciato; nessun dato sorgente cancellato.


### Directory documenti isolate per workspace — 25 novembre 2026
- `LocalDocumentRepository` accetta un UUID workspace e usa `filesDir/documents/<UUID>/`; il costruttore senza ID conserva esattamente il percorso legacy.
- Cifratura AES-GCM, chiave Keystore, AtomicFile e controlli dimensione restano invariati. I documenti esistenti non vengono spostati né duplicati.
- Test strumentale aggiunto per isolamento tra due workspace e archivio legacy.
- Codice: `77dc9701`; test: `d5568d7c`. CI Android in attesa.
- La UI continua a usare il repository legacy fino al collegamento della selezione chat. Nessuna cancellazione documenti o media eseguita.



### Verifica CI e correzione compilazione — 25 novembre 2026
- Il workflow Android sul commit `fec715e4` ha rilevato un errore Kotlin: il nuovo ID legacy era stato inserito in un secondo `companion object`. Corretto spostando la costante nel companion già esistente: commit `5154181e1e757b7f5be2132201d9f2cfe83adb58`.
- Il precedente run Android `45691128529` è fallito per crash del processo instrumentation/emulatore prima dell'esecuzione dei test (`INSTRUMENTATION_FAILED`, `Process crashed`); non è un esito di test funzionali superati né una prova di bug applicativo. Run: https://github.com/damianoavvocato-star/SuperIA/actions/runs/45691128529
- I nuovi workflow Android sul commit `5154181e` sono in coda; compilazione e test della migrazione e dell'isolamento documenti restano non confermati fino alla conclusione.

- Correzione successiva: il companion del `ChatWorkspaceStore` è privato e il test Android non poteva accedere alla costante. L'ID stabile è ora una costante top-level `internal`, usata anche dai test: commit `7479c4e4`, `0ac4e4f7`, `ba2ba00b`.
- Il run Android `45693657335` ha confermato il problema di visibilità nel target `compileDebugAndroidTestKotlin`; non è ancora una verifica dei test di migrazione. La correzione è in attesa del nuovo workflow.


 
### Primo collegamento UI multi-chat — 25 novembre 2026
- `AssistantChatPanel` ora carica la migrazione legacy prima di costruire lo store attivo e usa `ChatHistoryStore(context, selectedWorkspaceId)`; il workspace storico è selezionato inizialmente.
- Aggiunti comandi «Nuova chat» e «Progetto», selettore delle chat/progetti non archiviati, rinomina e archiviazione. Il cambio workspace è disabilitato durante generazione o importazione; la selezione azzera il contesto allegato temporaneo.
- Cronologia e memoria si ricaricano dallo store del workspace selezionato; il repository documenti viene chiamato con lo stesso UUID. Nuovi documenti vengono salvati nella directory dedicata.
- Aggiornati i testi UI per non promettere più che gli allegati siano condivisi tra chat.
- Commit UI `035b3dbd`; testi `daa60f10`; commenti store `cb41ead9`.
- Android CI sul commit UI iniziale era in corso al momento dell'aggiornamento; le modifiche successive hanno generato nuove run. Non dichiarare compilazione o test superati finché non è disponibile il risultato sul commit aggiornato.
- Limiti noti: archiviate non ancora consultabili/ripristinabili dalla UI; eliminazione definitiva con pulizia coordinata dei documenti/media non ancora implementata. Nessun dato viene cancellato automaticamente.



### Navigazione archivio chat — 25 novembre 2026
- Aggiunti filtro «Archiviate» e azione «Ripristina»; il ripristino riporta la chat nell'elenco attivo senza ricreare o spostare i dati.
- Commit UI archivio: `422f5f05`.
- La run Android `45694203120` sul commit `daa60f10` ha completato con successo il job `android-unit-tests`; i job strumentali risultavano ancora in corso. Questo esito non copre le modifiche successive né equivale a test su telefono fisico.
- Run Android per il commit più recente `422f5f05`: `https://github.com/damianoavvocato-star/SuperIA/actions/runs/45694345545` (in coda al momento della verifica).
- Restano da implementare eliminazione definitiva coordinata e pulizia dei media generati. Nessun contenuto è stato eliminato.

- Correzione di isolamento UI: le liste Compose di messaggi e memoria sono ora chiave anche per l'identità dello store attivo, non soltanto per il contenuto caricato. Questo evita che due chat entrambe vuote riutilizzino accidentalmente lo stesso stato Compose. Commit `69c0d817`.
- Nuova CI Android sul commit `69c0d817`: `https://github.com/damianoavvocato-star/SuperIA/actions/runs/45694421403` in coda al momento della verifica.

### Cancellazione definitiva per chat/progetto — primo incremento
- Aggiunto coordinatore fail-closed ChatWorkspaceDeletion: preflight cronologia, memoria e documenti; pulizia UUID-scoped; rimozione dell'indice come ultimo passo; workspace legacy protetto; creazione di chat vuota alternativa se si elimina l'ultima attiva.
- Aggiunto pulsante «Elimina» con conferma esplicita nella UI. Il comando è disabilitato durante generazione/importazione/salvataggio memoria.
- Pulizia cronologia/memoria include AtomicFile e .bak, e preferenze private del solo workspace. Pulizia documenti è confinata alla cartella UUID; file sconosciuti bloccano la cancellazione.
- Test strumentali aggiunti: eliminazione di A preservando B e legacy, blocco eliminazione legacy, verifica backup, blocco su file sconosciuti.
- Audit media: nessuna generazione/salvataggio MediaStore nativa rilevata; Renderforest è un servizio web esterno; esportazioni CreateDocument sono esterne e non vengono rimosse. La Galleria non viene modificata.
- Commit principali: 4448a09c, 26a7058f, 93e69650, 467c3328, 7f255d87, 439e71a6, a4fae5c3, c5f26c98.
- CI Android più recente al momento della nota: 45695040036 (f29c14de) in coda; nessuna prova strumentale conclusa per questo incremento. Non distribuire APK sulla base del solo codice committato.

- Ulteriore correzione di usabilità: la selezione del workspace attivo viene ora salvata nelle preferenze locali e ripristinata all'avvio; se il workspace non esiste più, la UI ripiega su uno spazio valido. Commit b12d14e8.
- CI Android sul commit codice/test b12d14e8 in attesa di esecuzione: verificare la run più recente prima di considerare conclusa la fase.

- Isolato anche il contesto scelto da Documenti: la MainActivity lo indicizza per workspace e AssistantChatPanel lo accetta soltanto quando l'ID proprietario coincide con quello selezionato. Il contesto in memoria viene rimosso alla cancellazione del workspace. Commit UI: b29e3f1f, 4ad45d02, a4e3f495, 2302fb76, e0c2d07d.
- La conversazione legacy è eliminabile dopo che il marker di migrazione è verificato; le copie storiche globali vengono eliminate insieme alla chat, mentre la raccolta Documenti generale/OCR rimane separata e non attribuita al workspace. La migrazione non ricrea una conversazione legacy già eliminata. Commit: 0d78b5c3, 4e85c811, 10f92d2a, 8f5121dc, 37a4bca7, e7207601, f894155e, 31fd2633.
- Run Android per il commit di test più recente 31fd2633: https://github.com/damianoavvocato-star/SuperIA/actions/runs/45696080470 (in coda al momento dell'ultimo controllo). Alcune run precedenti hanno completato i job unitari e di build APK debug, ma il job strumentale emulator è rimasto in corso; nessuna validazione completa dell'incremento è ancora dichiarata.

### Ultime correzioni di sicurezza e isolamento — 25 novembre 2026
- La cancellazione della conversazione legacy è consentita soltanto dopo marker di migrazione verificato; rimuove anche le copie sorgente storiche e le preferenze legacy, senza ricreare la chat al successivo avvio. Il flusso è idempotente anche se la cronologia legacy sorgente è più vecchia della conversazione attuale.
- Il contesto documentale temporaneo scelto dal pannello Documenti è indicizzato per ID workspace; la chat lo usa solo se il proprietario coincide con il workspace attivo e la cancellazione rimuove la relativa voce in memoria.
- La raccolta generale Documenti e il dizionario OCR restano globali e non attribuiti a una chat; non vengono cancellati eliminando un progetto. File esportati dall'utente in altre app e contenuti di servizi esterni (es. Renderforest) non sono controllabili da SuperIA.
- Commit recenti di correzione: 5e35d939, f8faa195, 2bb86be9; documentazione architettura aggiornata in c7a6e6b6.
- Run Android sul commit più recente con modifiche Android: https://github.com/damianoavvocato-star/SuperIA/actions/runs/45696319701 (in coda). Run precedenti hanno completato unit test e build temporanea APK in alcuni commit, ma i job emulator strumentali sono rimasti in corso; l'incremento attuale non è ancora validato completamente.


### Verifica CI Android aggiornata — 26 novembre 2026
- Controllato il run Android `45696319701` sul commit `2bb86be98171a70a11a1acf7ac257598422ba818`.
- `android-unit-tests`: **success**. Compilazione Kotlin app e test Android è arrivata a termine; il job unitario è superato.
- `android-physical-device-test-apks`: **success** nella sola generazione degli APK temporanei di test. Non è stato avviato alcun rilascio né distribuzione; nessun nuovo APK è stato consegnato o installato sul telefono.
- `android-instrumented-tests`: **failure infrastrutturale/emulatore**. Gradle ha compilato app e test, ma il runner ha avviato 0 test e ha terminato con `Instrumentation run failed due to Process crashed`; non è quindi un fallimento attribuibile a un test funzionale specifico, né una validazione positiva dell'incremento.
- Workflow backend sul commit documentale `ff4cf6278b9fe6202ff1a1ae6ae3cb491c1902dc`: **success** (`45696378360`).
- Stato: codice multi-chat presente su `main`; unit test e compilazione superati; suite strumentale emulator ancora non verificata. Non dichiarare completata la verifica end-to-end né la cancellazione reale sul dispositivo. Prossimo passo: isolare il crash dell'instrumentation runner e ripetere la suite senza modificare dati utente o attivare servizi esterni.


### Rifinitura recupero dopo cancellazione parziale — 26 novembre 2026
- In caso di errore durante la cancellazione, la UI rilegge ora l'indice workspace prima di mostrare l'errore. Se il coordinatore aveva già creato una chat di fallback prima di interrompersi, questa compare subito nell'elenco e l'utente può riprovare senza riavviare l'app.
- Modifica su `main`: commit `2dea1b02f2ba68c944394ae6c4249b979f61cf30`.
- Verifica Android della modifica appena inviata: in attesa della nuova run CI. La suite emulator precedente resta non verificata per crash del processo instrumentation prima dell'avvio dei test.


### Diagnosi terzo tentativo emulatore Android — 26 novembre 2026
- Run Android `45802841443` sul commit `f2e6e73e1675aeffa051c2862d8cc7b889aab8ff`: `android-unit-tests` **success**, `android-physical-device-test-apks` **success** (solo APK debug temporanei), `android-instrumented-tests` **failure**.
- Il report XML conferma `tests=0`, `failures=0` e `Instrumentation run failed due to Process crashed`: nessun test strumentale è stato eseguito.
- Analisi dell'artefatto diagnostico `15109107059`: il logcat registra `SIGABRT` nel thread `bt_stack_manage` del processo `com.android.bluetooth`; il device UTP inoltre segnala permesso negato per `/sdcard/Android/media/it.superiaprivata.app/additional_test_output`. Non vi è evidenza sufficiente per attribuire il crash al codice SuperIA.
- Correzione CI su `main`, commit `b1d0f02c3d962ae80b4a68b4048f83bb073d2871`: immagine emulatore ridotta da API 35 Google APIs a API 34 AOSP default per isolare i crash dei servizi Google/Bluetooth nell'emulazione TCG. La correzione è sperimentale; richiede nuova CI.
- Backend run `45802841352`: **success**. Nessun APK distribuito/installato e nessun test su telefono fisico.


### Verifica strumentale Android 14 AOSP — 26 novembre 2026
- Run Android `45811087356` sul commit `b1d0f02c3d962ae80b4a68b4048f83bb073d2871`: `android-unit-tests` **success**, compilazione e APK temporanei di test **success**; backend run `45811087165` **success**.
- La suite strumentale è partita ed ha eseguito **38 test**: 34 superati e 4 falliti. Non è stato un crash dell'emulatore: i test sono stati eseguiti.
- L'analisi dei fallimenti evidenzia un test di concorrenza che supera il timeout di 30 secondi su TCG; il test condivideva inoltre l'archivio legacy globale con gli altri test, quindi un worker lento poteva contaminare i test successivi. Correzione: archivio workspace UUID isolato, carico concorrente ridotto e timeout più realistico.
- Correzione UI: l'importazione documentale conserva l'ID del workspace che ha aperto il selettore; la navigazione tra workspace è bloccata mentre il selettore è aperto o l'importazione è in corso. Questo evita di salvare il documento nel progetto sbagliato.
- Nessun APK è stato consegnato o installato sul telefono; nessun test fisico o rilascio eseguito.


### Verifica test strumentali e correzione script CI — 26 novembre 2026
- Run Android `45827701798` sul commit `a51bb61268d3de997a59f4ac50879f9a1baabd14`: unit test **success**; build degli APK debug/test temporanei **success**; backend run `45827701660` **success**.
- La suite emulator Android 14 AOSP ha eseguito **38/38 test: 0 falliti, 0 saltati**. Il test separato `writePendingStateForProcessRestart` ha completato **1/1** senza errori.
- Il job complessivo è tuttavia terminato **failure dopo i test**: lo shell step di verifica post-`force-stop` ha restituito `Syntax error: end of file unexpected (expecting "fi")`. Il log mostra che i test erano già terminati correttamente; non si tratta di un test applicativo fallito.
- Correzione in corso su `main`: rimosso il blocco condizionale shell ridondante dopo `adb shell am force-stop`. Il test successivo di recovery verifica direttamente il recupero del processo/stato; il comando force-stop rimane.
- Nessun APK consegnato/installato, nessun test su telefono fisico, nessun rilascio o attivazione backend/provider.


### Diagnosi e correzione verifica process-restart — 27 novembre 2026
- Run Android `45837855804` sul commit `9dc9e73679b7a872bcfef7c91c4a8398e33e64d6`: unit test **success**; build APK temporanei **success**; backend run `45837855673` **success**.
- Test strumentali emulator API 34: suite principale **38/38 passati**; `writePendingStateForProcessRestart` **1/1 passato**; `verifyPendingStateAfterProcessRestart` **1/1 fallito** con archivio vuoto dopo force-stop. Il job è quindi correttamente fallito; il controllo shell non era più il problema.
- Il workflow eseguiva la verifica finale richiamando una seconda volta `connectedDebugAndroidTest`, che può reinstallare i pacchetti di test/app e interferire con la persistenza tra le due invocazioni. Modificato il flusso: dopo il force-stop del solo package app, la verifica viene lanciata direttamente con `adb shell am instrument`, senza un nuovo task Gradle/installazione; output controllato esplicitamente per `OK (1 test)` e `FAILURES!!!`.
- La nuova esecuzione deve confermare la persistenza reale tra processi; non considerare ancora superato il test fino al suo esito.
- Nessun APK consegnato/installato sul telefono, nessun test fisico, nessun rilascio o attivazione backend/provider.

### Verifica CI process-restart — 27 novembre 2026 (run 45954167172)
- Sul commit `bff3fedc4f6024363841e218766cb7ea0d18e9bf`: unit test Android **success**; build degli APK debug/test temporanei **success**; backend run `45954167131` **success**.
- Suite strumentale principale **38/38 passati**; `writePendingStateForProcessRestart` **1/1 passato**.
- La verifica diretta finale non ha raggiunto il test: `adb shell am instrument` ha restituito `INSTRUMENTATION_FAILED`. L'artefatto mostra che `pm list instrumentation` iniziale era vuoto; il task Gradle precedente non aveva lasciato installato il package runner per la chiamata diretta. Non è un fallimento dell'asserzione di recupero: il test finale non è stato eseguito.
- Correzione su `main`, commit `1f5067bf0d7134dcfbf5173d43ad9da4db5f31f1`: reinstallazione esplicita del solo APK `app-debug-androidTest.apk` con `adb install -r` (senza rimuovere i dati dell'app), controllo che il runner risulti registrato tramite `pm list instrumentation`, poi force-stop dell'app e verifica diretta.
- Nuova CI necessaria per convalidare la correzione. Nessun APK consegnato/installato sul telefono, nessun test fisico, nessun deploy o provider attivato.


### Diagnosi e correzione installazione runner process-restart — 27 novembre 2026
- Run Android `46017581505` sul commit `1f5067bf0d7134dcfbf5173d43ad9da4db5f31f1`: unit test **success**; build APK temporanei **success**; backend run `46017581474` **success**.
- Il report conferma la suite principale eseguita e il test writer `writePendingStateForProcessRestart` **1/1 passato**. La verifica finale non è stata eseguita: dopo l'installazione del solo APK androidTest il runner compare in `pm list instrumentation`, ma `am instrument` restituisce `INSTRUMENTATION_FAILED`. Non è un errore dell'asserzione di recovery.
- Correzione CI su `main`: installare esplicitamente entrambi gli APK (app e androidTest) una sola volta con `adb install -r`, quindi eseguire direttamente writer, force-stop e verifier con `adb shell am instrument`. Si elimina la seconda invocazione Gradle e si impedisce che Gradle disinstalli/reinstalli i pacchetti tra le due prove. Il nuovo run dovrà verificare entrambi i risultati `OK (1 test)`.
- Nessun APK consegnato/installato sul telefono fisico; nessun deploy o provider attivato.


### Correzione controllo shell test process-restart — 27 novembre 2026
- Run Android `46020798071` sul commit `d21008c1e59fb0d7d15d71706621449baf9e0164`: suite emulator principale **38/38 superati** (XML: 0 failure, 0 error, 0 skipped); test writer `writePendingStateForProcessRestart` **1/1 superato**.
- Il workflow si è interrotto dopo il writer, prima del force-stop e della verifica finale, per un errore sintattico shell: l'action esegue ogni riga dello script separatamente e non supporta blocchi `if/then/fi` distribuiti su più righe.
- Correzione su `main`: controlli writer e verifier riscritti come singole righe shell con `test`, `grep` e blocco di diagnostica inline. Il test di recovery non è ancora convalidato; attendere il prossimo run.
- Android unit test, APK temporanei di test e backend **success**. Nessun APK consegnato/installato sul telefono fisico, nessun rilascio o servizio esterno attivato.


### Correzione finale isolamento shell process-restart — 27 novembre 2026
- Analizzato l'artefatto del run Android `46032584074`: report Gradle **38/38 test passati**, 0 fallimenti e 0 saltati; il test writer `writePendingStateForProcessRestart` **1/1 passato**.
- Causa esatta del job failure: l'emulator-runner esegue ogni riga dello script in un processo shell separato. Le variabili `write_status` e `verification_status` venivano assegnate su righe precedenti e risultavano vuote nel controllo successivo (`test: Illegal number`). Il verifier non era stato avviato.
- Correzione su `main`, commit `af0e2a6343895c5071896fd122d413d816543118`: ogni invocazione `adb shell am instrument`, cattura exit status, stampa output e controllo `OK (1 test)`/assenza `FAILURES!!!` sono ora nello stesso comando shell. Il force-stop resta tra writer e verifier.
- Nuovi run avviati: Android `46058183460`, backend `46058183407`. Entrambi erano in coda al momento dell'aggiornamento. Non dichiarare ancora superata la verifica di process restart finché il verifier non restituisce `OK (1 test)`.
- Nessun APK consegnato/installato sul telefono fisico, nessun test fisico, nessun rilascio o attivazione backend/provider.


### Verifica completa CI Android e recupero dopo arresto processo — 27 novembre 2026
- Run Android `46058183460` sul commit applicativo `af0e2a6343895c5071896fd122d413d816543118`: tutti e tre i job **success** (`android-unit-tests`, `android-instrumented-tests`, `android-physical-device-test-apks`).
- Report XML emulator API 34: **38 test, 0 failures, 0 errors, 0 skipped**.
- Diagnostica `process-restart-write.txt`: `OK (1 test)`; `process-restart-verification.txt`: `OK (1 test)`. La verifica finale è stata effettivamente eseguita dopo `adb shell am force-stop it.superiaprivata.app`, senza nuova installazione Gradle tra writer e verifier.
- Run backend `46058183407`: **success**; suite pytest backend superata.
- La causa precedente è risolta: le variabili shell venivano perse tra righe eseguite in shell separate; cattura status e controllo output sono stati accorpati nello stesso comando.
- Artefatto report emulator: `15227843683`; artefatto APK debug/test temporanei: `15227755914` (non installati sul telefono fisico, non distribuiti e con scadenza breve).
- Stato: test automatici Android unitari, strumentali e backend superati per questo commit. La prova non sostituisce test sul telefono fisico né valida l'invio chat con backend reale/provider. Nessun deploy, provisioning, APK release o attivazione provider effettuati.


### Rifinitura selezione workspace dopo archiviazione — 27 novembre 2026
- Audit della UI multi-chat: creazione chat/progetto, selezione, rinomina, archiviazione, filtro archiviate/ripristino ed eliminazione sono presenti nello stesso pannello, senza richiedere schermate separate.
- Corretto il caso limite in cui l'utente archivia l'ultima chat attiva rimasta (possibile dopo la rimozione del workspace legacy): la UI crea automaticamente una nuova chat vuota e la seleziona, evitando una schermata senza workspace attivo.
- Commit su `main`: `adbc2197c19020a0c6bd887bb60a454e5b870d3c`.
- CI avviata: Android run `46060796600`, backend run `46060796611`; entrambi ancora in esecuzione al momento della nota. Non dichiarare la modifica validata fino al completamento dei job.
- La modifica non tocca archivi delle altre chat e non cambia la policy di cancellazione. Nessun APK consegnato/installato sul telefono fisico, nessun deploy o provider attivato.

## Metodo di sviluppo aggiornato — priorità chat online
- Su richiesta dell'utente, le verifiche strumentali lente su emulatore sono rinviate al collaudo finale della chat online.
- Il workflow Android su push/PR esegue soltanto i test JVM rapidi. La suite emulator/process-restart è disponibile esclusivamente tramite avvio manuale con input `run_instrumented=true`; non viene eseguita automaticamente.
- Rimosso il job che assemblava e caricava APK debug temporanei a ogni modifica. Nessun APK viene generato durante lo sviluppo ordinario.
- Modifica workflow: commit `0e51309855c2f2dcbd9444ccf38a8d67d03f02f7`.
- Diagnosi operativa: Android dispone già del client HTTPS/SSE e del pairing individuale. La chat reale resta bloccata da backend non pubblicato/configurato e assenza di provider gratuiti attivi con verifiche account-level di no-card, hard-stop e privacy. Non aggirare i guard-rail né inventare un endpoint o credenziali.
- Hosting/database e provider richiedono ancora approvazione e configurazione esplicita; nessun account, servizio, chiave o pagamento è stato creato/attivato. Prossimo lavoro: preparare l'integrazione e rendere trasparente lo stato di connessione, poi eseguire un unico collaudo end-to-end quando l'infrastruttura autorizzata sarà disponibile.

## Aggiornamento provider prioritario — 27 novembre 2026
- Verificate le fonti ufficiali Cloudflare Workers AI aggiornate a novembre 2026. Workers Free dispone di 10.000 Neurons/giorno e le richieste oltre quota falliscono; il passaggio a Workers Paid comporta costi ed è escluso.
- Zero retention e no-training sono documentati per inferenza standard stateless; log, cache e funzionalità stateful restano esclusi dal progetto.
- Candidati da verificare a livello account: `@cf/zai-org/glm-4.7-flash` e `@cf/google/gemma-4-26b-a4b-it`; nessuno dei due è stato attivato. Non assumere che la presenza nel catalogo equivalga a conferma no-card/account Free.
- Rapporto aggiornato: [AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md](AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md), commit `efcf2a3b7f9f785b2d34e2f563b78595f621bcaf`.
- Fonti ufficiali: https://developers.cloudflare.com/workers-ai/platform/pricing/ ; https://developers.cloudflare.com/workers-ai/platform/data-usage/ ; https://developers.cloudflare.com/workers-ai/models/glm-4.7-flash/ ; https://developers.cloudflare.com/workers-ai/models/gemma-4-26b-a4b-it/ ; https://developers.cloudflare.com/changelog/post/2026-11-05-planned-model-deprecations/.
- Blocco ancora aperto: verifica no-card, hard-stop effettivo e creazione autorizzata del token; nessun account o segreto creato, nessun servizio attivato.

## Diagnostica connessione Android — 27 novembre 2026
- Aggiunto in Impostazioni il pulsante `Verifica indirizzo backend`, utilizzabile prima del pairing. Richiama `GET /health` via HTTPS e mostra se il processo backend risponde; il messaggio chiarisce che questo controllo non certifica database, pairing o provider IA.
- Il pulsante evita di inserire un codice pairing quando l'endpoint è scritto male o il servizio non è raggiungibile.
- Commit Android: `d205ae3a9d04fe44e370ac908db6000595fc96cd`.
- Non è stato eseguito alcun test su backend remoto, perché non esiste ancora un endpoint di staging autorizzato. La suite emulator è rinviata; i test JVM rapidi sono il solo controllo automatico ordinario.


## Hard-stop quota gratuita Cloudflare — 27 novembre 2026
- Verificato il codice reale del router: prima della correzione ogni HTTP 429 applicava solo un cooldown temporaneo. Per Cloudflare questo non distingueva l'esaurimento definitivo della quota giornaliera gratuita dagli altri limiti temporanei.
- Corretto `services/api/app/providers/free_router.py`: riconosce l'errore Cloudflare `3036` (quota gratuita giornaliera di 10.000 Neurons esaurita) sia nel flusso SSE sia nella richiesta non-streaming; disabilita persistentemente il provider nel database di stato, invece di riprovare ogni minuto. Nessun fallback verso piani/modelli a pagamento; un eventuale altro provider viene considerato solo se è configurato e attestato separatamente come gratuito.
- Aggiunti test automatici per entrambe le modalità: provider disabilitato dopo errore 3036, stato persistente dopo ricreazione del router e nessuna seconda chiamata al provider.
- Commit router: `9dc1d8f92e68dcbda92e14be12b89bbe9c6d5056`.
- Commit test: `b00f6b68e42f9b70c34afbdee4a9c04e66843fe6`.
- CI backend avviata sul commit test: run `46061807173` (queued al momento dell'aggiornamento). Il run precedente sul solo router `46061796374` era ancora in corso. Non dichiarare i test superati finché il run sul commit test non termina con successo.
- Nessun provider attivato, token creato, account Cloudflare collegato, backend pubblicato o costo autorizzato. La configurazione rimane disabilitata per impostazione predefinita.


### Allowlist rigida modelli Cloudflare Workers Free — 27 novembre 2026
- Rafforzata la configurazione Cloudflare: il router accetta esclusivamente i due modelli identificati nella documentazione ufficiale come disponibili sul piano Workers Free: `@cf/zai-org/glm-4.7-flash` e `@cf/google/gemma-4-26b-a4b-it`.
- La lista configurabile `SUPERIA_PROVIDER_CLOUDFLARE_FREE_MODEL_IDS` non può più autorizzare da sola un modello fuori da questa allowlist; il modello scelto deve essere presente sia nella lista esplicita sia nell'allowlist del codice.
- Aggiunti test per entrambi i modelli ammessi e per il rifiuto di un modello Cloudflare che richiede piano/crediti a pagamento.
- Commit router: `75725acf0d62868961ff9642634957d84760ecb7`.
- Commit test: `4e171aa58a340ba6deca5f01c1a7777f84d9120b`.
- Il test automatico precedente sul commit `b00f6b6` era ancora in esecuzione al momento della verifica; il nuovo commit test avvia un ulteriore run. Stato CI da confermare prima di dichiarare i test superati.
- Fonti ufficiali modello e piano gratuito: https://developers.cloudflare.com/changelog/post/2026-07-28-models-require-workers-paid/ e https://developers.cloudflare.com/workers-ai/platform/pricing/ .


## Readiness backend più rigorosa — 27 novembre 2026
- Audit del codice ha individuato un falso positivo: `/api/v1/readiness` restituiva `ready` anche con database e autenticazione funzionanti ma nessun provider AI configurato. La chat non avrebbe comunque potuto generare risposte.
- Corretto: ora `ready` richiede database operativo, schema autenticazione presente e almeno un provider gratuito configurato e validato dal router. In assenza di provider, restituisce HTTP 503 e stato `degraded`. Il campo `provider_configuration` resta esplicito.
- Test aggiornato per il caso senza provider e aggiunto test con provider Cloudflare configurato tramite credenziali fittizie: verifica che il readiness sia positivo senza esporre chiavi o account ID.
- Commit backend: `e58a587916dbce007f0e45b0be21d20feda27bc7`.
- Commit test: `746ff0bc9d98e46c200576f63dbf15691f65833a`.
- Run backend sul commit test: `46062054725`, in coda/in esecuzione al momento della registrazione. Non dichiarare superati questi nuovi test fino al completamento con successo.
- Il readiness verifica la configurazione, non la raggiungibilità effettiva del provider né la quota residua; il primo messaggio reale resta il test end-to-end decisivo.


## Aggiornamento audit Multi-Provider — 27 novembre 2026
- Esteso l'audit dei provider oltre Cloudflare/Groq: valutati NVIDIA Build, Mistral, Cohere, SambaNova, Gemini, OpenRouter, Z.ai e SiliconFlow.
- Decisione: non aggiungere provider alla rotazione privata sulla sola base di un badge 'free'. NVIDIA Build trial dichiara registrazione input/output e uso per migliorare i servizi; Mistral Labs non offre opt-out/ZDR e ZDR è documentato per pay-as-you-go; Cohere resta trial/prototipazione senza evidenza sufficiente di garanzie privacy e idoneità produzione; SambaNova richiede metodo di pagamento/acquisto crediti; Gemini/OpenRouter non approvati per chat privata per le ragioni già documentate.
- Pool candidati per la chat privata: Cloudflare Workers AI e GroqCloud, entrambi ancora subordinati a verifica account-level no-card, hard-stop e policy dati. Nessun provider attivo, nessuna chiave/account creati.
- Z.ai e SiliconFlow restano in valutazione: servono prove ufficiali sufficienti su piano gratuito permanente, no-card, hard-stop e trattamento prompt prima di qualsiasi integrazione.
- Documento dettagliato: docs/AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md.
- Fonti pubbliche: https://build.nvidia.com/fastino/gliner2-pii ; https://docs.mistral.ai/inference/model-lifecycle ; https://docs.mistral.ai/admin/monitor-comply/zero-data-retention ; https://docs.cohere.com/docs/rate-limits ; https://cloud.sambanova.ai/plans ; https://ai.google.dev/gemini-api/docs/pricing.
- Questa è una verifica documentale; non è un test account-level né un'integrazione runtime.


## Decisione utente sui provider gratuiti — 27 novembre 2026
- L'utente accetta che i provider gratuiti possano utilizzare prompt/risposte per migliorare i propri prodotti. Tale uso dei dati non è più motivo automatico di esclusione; le condizioni restano da comunicare.
- Priorità aggiornata: zero costi e nessuna possibilità di addebito; API Free realmente utilizzabile; hard-stop prima di qualsiasi piano a pagamento; compatibilità e stabilità; informativa sull'uso dei dati.
- Gemini Free e OpenRouter modelli `:free` possono essere considerati nel pool; il router già supporta Gemini/OpenRouter/Groq/Cloudflare. OpenRouter resta strettamente limitato a ID `:free`; non acquistare mai crediti per aumentare il limite.
- Cohere Trial API è candidato aggiuntivo (trial gratuito limitato; adapter API/streaming da implementare e testare). Mistral API non è attualmente gratuito: la documentazione dichiara che Free mode non include API access e che API richiede billing activation/pay-as-you-go. SambaNova resta escluso finché richiede pagamento.
- Audit dettagliato e fonti ufficiali aggiornate in `docs/AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md`.
- Non sono stati creati account, API key, metodi di pagamento, crediti o servizi. Nessun provider è attivo fino a configurazione autorizzata.


## Ricognizione estesa provider AI — 27 novembre 2026
- Ricerca web ampliata a provider globali e regionali: Gemini, Groq, Cloudflare, OpenRouter, Cohere, Z.AI, SenseNova, SEA-LION, Synap/Linkrra, Novita AI/Apodex, SiliconFlow, SambaNova, Cerebras, NVIDIA, Mistral, DeepSeek, Hugging Face, GitHub Models, OpenAI, Anthropic, Together, Fireworks, Replicate, Nebius e altri.
- Evidenza di API gratuite pubbliche trovata per Gemini Free, Groq Free, Cloudflare Workers AI Free, OpenRouter modelli `:free`, Cohere Trial, Z.AI Flash, SenseNova Token Plan beta, SEA-LION POC, Synap free tier; Novita espone Apodex 1.1 Mini a prezzo Free ma quota/account/hard-stop non sono ancora dimostrati.
- Distinzione mantenuta tra gratuità ricorrente, quota trial, prezzo modello $0, crediti promozionali e accesso web/chat: non sono equivalenti.
- Mistral API Free mode non include API access; GitHub Models è ritirato dal 30 luglio 2026; Cerebras è emerso come trial a crediti con metodo di pagamento verificato; DeepSeek e altri servizi non hanno dimostrato una quota ricorrente gratuita no-card; SiliconFlow e SambaNova richiedono ulteriore verifica ufficiale.
- Graduatoria: Cloudflare/Groq prioritari; Gemini/OpenRouter :free già supportati; Cohere richiede adapter; Z.AI/SenseNova da verificare; SEA-LION/Synap sperimentali; Novita da verificare prima di qualsiasi integrazione.
- Nessun account, chiave, credito, metodo di pagamento, deploy o provider attivato. Nessun test API reale con credenziali.
- Rapporto completo: `docs/AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md`.
- Fonti ufficiali principali: https://ai.google.dev/gemini-api/docs/pricing ; https://console.groq.com/docs/rate-limits ; https://developers.cloudflare.com/workers-ai/platform/pricing/ ; https://openrouter.ai/collections/free-models/ ; https://docs.cohere.com/v2/docs/rate-limits ; https://docs.mistral.ai/admin/billing-usage/usage-limits/ ; https://docs.github.com/en/github-models ; https://novita.ai/pricing ; https://sea-lion.ai/try-sea-lion/ ; https://sensetime.com/en/faq ; https://synap.linkrra.com/free .
- Stato: audit documentale esteso registrato; verifiche account-level, termini per regione e test compatibilità restano aperti. Nessuna integrazione runtime autorizzata automaticamente.


## Cohere adapter implementation — 27 November 2026
- Added Cohere to `services/api/app/providers/free_router.py` provider order and environment validation.
- Added Cohere v2 endpoint `https://api.cohere.com/v2/chat` with Bearer authentication.
- Added non-streaming parser for `message.content[]` text blocks and streaming SSE parser for `delta.message.content.text` events.
- Preserved free-only attestation requirements and fail-closed behavior: 401/402/403 persistently disables the provider; 429 enters cooldown; no paid fallback.
- Added tests for Cohere non-streaming, SSE streaming, 402 hard-disable/no retry, and mandatory no-payment attestation in `services/api/tests/test_free_router.py`.
- Official docs: https://docs.cohere.com/v2/reference/chat-stream ; https://docs.cohere.com/v2/docs/chat-api ; https://docs.cohere.com/v2/docs/rate-limits
- Cohere official trial keys: free but limited; 1,000 calls/month and 20 req/min for listed Chat models. Account-level no-card, model eligibility and hard-stop still require verification.
- No account/key created, no provider activated, no live API call. Tests are added but have not yet been executed/CI verified.


## Z.AI adapter implementation — 27 November 2026
- Official pricing page confirms input and output prices of Free for `glm-4.7-flash`, `glm-4.5-flash`, and `glm-4.6v-flash`: https://docs.z.ai/guides/overview/pricing
- Added Z.AI OpenAI-compatible adapter using `https://api.z.ai/api/paas/v4/chat/completions`.
- Added hard-coded allowlist for the three official zero-price model IDs; paid and non-listed models are rejected before HTTP calls.
- Added tests for all three allowed model IDs, rejection of paid model IDs, normalized chat response and persistent disable on HTTP 402 / insufficient balance.
- Z.AI API developer terms say End User Content is not used to improve services unless explicitly agreed; disclose applicable terms. Public docs do not settle no-card requirement or public rate limits for every account/region.
- No account/key created, no provider activated, no live request. Account-level no-card and quota hard-stop verification remains mandatory.


### Synap free-tier adapter — 27 novembre 2026
- Aggiunto `synap` al router backend con endpoint OpenAI-compatible `https://synap402.com/v1/chat/completions`.
- Allowlist rigida: solo `qwen3-coder-30b`; ogni altro modello viene rifiutato in fase di configurazione.
- Chat non-streaming e SSE standard normalizzati nel formato SuperIA.
- HTTP 402 disabilita il provider; per sicurezza anche HTTP 429 lo disabilita persistentemente, senza retry automatico. Nessun fallback verso crediti/modelli a pagamento.
- Le cinque attestazioni free-only restano obbligatorie. Nessun account o token creato e nessun provider attivato.
- Test aggiunti per allowlist, chat, streaming, 402/429, persistenza della disattivazione e assenza di seconda chiamata.
- Fonte provider: https://synap.linkrra.com/free.html ; privacy: https://synap.linkrra.com/privacy.html ; termini: https://synap.linkrra.com/terms.html.
- Commit codice: `2770c7c11f0a3b0b20df2aae2cca64ecde726b90`; commit test: `a66c7297b1724abd5af007b3b28d572a2305318c`.
- CI backend sul commit test da confermare. L'adapter resta non configurato finché non viene verificato il singolo account Free/no-card.


### Esito suite backend Synap — 27 novembre 2026
- Il primo run test sul commit `a66c7297` ha rilevato un difetto esclusivamente nel fixture SSE: il testo di test conteneva escape letterali anziché newline. Nessun errore nella logica dell'adapter; 158 test erano già passati.
- Fixture corretto nel commit `5959c4664f8e1615a1bbcb19f8d2107eefb13ba3`.
- Run GitHub Actions `46063864065`: **success**; pytest **159 passed, 1 warning**, in 1,80 s.
- Il risultato convalida i test mock del router e dell'adapter; non è una chiamata API reale e non attiva Synap. Restano necessari verifica account Free/no-card e test end-to-end dopo autorizzazione di credenziali e backend.


### SambaNova Cloud Free Tier — adapter aggiunto, 27 novembre 2026
- Verificata la documentazione ufficiale: il Free Tier si applica senza metodo di pagamento collegato; Developer Tier solo con metodo collegato.
- Limiti pubblicati per ciascun modello Free Tier di produzione: 20 RPM, 20 RPD, 200.000 TPD.
- Allowlist in codice limitata a `DeepSeek-V3.1`, `Meta-Llama-3.3-70B-Instruct`, `gpt-oss-120b`. Esclusi i modelli Preview, perché non raccomandati per produzione.
- Endpoint OpenAI-compatible `https://api.sambanova.ai/v1/chat/completions`, Bearer key, supporto streaming SSE.
- HTTP 402 disabilita il provider; HTTP 429 disabilita persistentemente il provider per evitare retry automatici oltre la quota gratuita.
- Restano obbligatorie tutte le attestazioni free-only. Nessun account o token creato; nessun provider attivato.
- Privacy documentata: Customer Content trattato per erogare il servizio; query log e dati d'uso non contenenti Customer Content possono essere usati per migliorare i servizi. Disclosure necessaria nell'app.
- Fonti ufficiali: https://docs.sambanova.ai/docs/en/models/rate-limits ; https://cloud.sambanova.ai/dashboard ; https://sambanova.ai/cloud-end-user-license-agreement.
- CI backend sul commit dei test da confermare.


### Chiusura verifica SambaNova e audit candidati — 27 novembre 2026
- Suite backend sul commit `72f0249f4968e44ce0272b34a5c569fbb93335ae`: **172 passed, 1 warning**, GitHub Actions run `46064211110` concluso `success`.
- I fallimenti precedenti sui commit intermedi erano dovuti a un test obsoleto che ancora si aspettava l'esclusione di SambaNova; aggiornato il test per richiedere invece tutte le attestazioni account Free. Nessun errore residuo nella suite sul commit di test corretto.
- Provider esclusi o non integrati dopo verifica: Aion Labs (carta richiesta per attivare API key), Typhoon (ricerca/prototipazione e produzione indirizzata al partner a pagamento; termini consentono training sugli input), Regolo (solo trial gratuito di 30 giorni poi PAYG), Novita (prezzi/referral credit, non quota ricorrente), LLM7 (no downstream gateway senza autorizzazione scritta), NVIDIA NIM hosted (limiti/hard-stop/produzione non sufficientemente verificati).
- Documentazione completa nell'audit `docs/AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md`.
- SambaNova resta spento fino a verifica account individuale e inserimento manuale delle attestazioni; nessun account/API key creati. La CI sul successivo commit documentale è indipendente dai test codice già superati.


### Verifica provider SenseNova e riconciliazione audit — 27 novembre 2026
- Verificata la pagina ufficiale Token Plan: piano Free pubblico in beta, 1.500 chiamate per modello ogni 5 ore; la pagina consultata elenca SenseNova 6.7 Flash-Lite e U1 Fast. La FAQ ufficiale conferma che occorre registrazione e creazione di API key.
- Una comunicazione ufficiale successiva promuove U1.5 Lite, mentre la documentazione API pubblica reperita descrive altre generazioni di modelli/endpoint. Non è ancora dimostrata l'idoneità dell'account dall'Italia, l'assenza di carta/metodo di pagamento né il blocco economico al termine della beta.
- Decisione: SenseNova resta candidato sperimentale, non integrato e non attivato; nessun account, token o test API reale creato.
- Rettificato l'audit provider: le precedenti righe che escludevano SambaNova per carta/acquisto crediti erano obsolete. L'adapter SambaNova è presente e i test sono superati; il provider resta disattivato fino alla verifica account-level e alle attestazioni free-only.
- CI backend: commit test `72f0249f4968e44ce0272b34a5c569fbb93335ae` — 172 passed, 1 warning; workflow sul commit documentale `2777188867f335c59c3ae0f6a6f2762593144548` — run `46064312749` success.
- Fonti: https://www.sensenova.ai/token-plan ; https://sensetime.com/en/faq ; https://sensetime.com/en/news/sensenova-u1-5-lite-token-plan-20260911-1741.
