# Migrazione SuperIA a online-only

Aggiornato: 21 novembre 2026

## Decisione vincolante

SuperIA esegue l'inferenza AI esclusivamente online. Il runtime locale Gemma/LiteRT-LM è stato rimosso dal progetto Android e non deve essere reintrodotto come motore alternativo. La chat usa il backend SuperIA via HTTPS. Cronologia e memoria di progetto restano cifrate sul dispositivo; viene inviato al backend solo il contesto necessario.

## Regole economiche inderogabili
- Sono ammessi crediti gratuiti interni al servizio (crediti, token, punti o unità), anche se assegnati una tantum o periodicamente e anche se il fornitore li contabilizza in valuta.
- Sono esclusi depositi, ricariche, acquisti, addebiti, carte/metodi di pagamento, overage fatturabili e conversioni automatiche a piani pagati.
- Non creare account multipli né ruotare chiavi per aggirare quote o condizioni.
- Quando la quota gratuita termina, il router passa al prossimo provider gratuito già verificato; se tutti sono esauriti, interrompe la generazione e conserva la conversazione senza spendere denaro.
- Ogni provider resta disabilitato finché non sono verificati account, modello gratuito esatto, termini, privacy, disponibilità in Italia, assenza di carta e hard-stop a quota zero.

## Stato tecnico verificato nel repository
- Il client Android OnlineAssistantClient invia richieste HTTPS al backend e la UI prepara il prompt con cronologia pertinente, memoria di progetto e contesto documentale.
- Il pairing individuale è implementato: il codice di associazione viene inviato a POST /api/v1/device/pair; token e ID dispositivo sono conservati cifrati tramite Android Keystore. Il codice di pairing non viene persistito.
- Il backend FastAPI include autenticazione Bearer per dispositivo, token individuali memorizzati come hash, scadenza, revoca, endpoint chat e stato operativo dei provider.
- Il router free-only include Gemini, Groq, OpenRouter (solo modelli con suffisso :free), Cloudflare Workers AI e SambaNova; provider disabilitati per default.
- Sono presenti test unitari per router, autenticazione, API e database; tali test non sostituiscono un collaudo end-to-end con provider e hosting reali.
- Il backend non è pubblicato e non è collegato a un database di produzione.
- Non risultano account hosting/database creati, segreti di produzione configurati o provider AI attivati.
- Il router supporta PostgreSQL tramite SUPERIA_DATABASE_URL; SQLite e il percorso /tmp sono soltanto fallback di sviluppo/test e non devono essere usati per stato persistente in produzione.
- Il flag SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=true è un gate di conferma operativa, non un rate limiter: prima dell'esposizione pubblica serve una regola IP reale sul gateway/WAF.
- La CI Android è stata modificata per eseguire esclusivamente gradle --no-daemon testDebugUnitTest; non deve assemblare né caricare APK.

## Lavoro ancora necessario
1. Hosting e database: scegliere servizi gratuiti verificati senza carta, pagamenti, overage o conversioni automatiche; creare e configurare solo dopo l'audit dei termini. Usare un database persistente e verificare esportazione e ripristino.
2. Hardening: rate limiting IP reale per pairing e limiti per dispositivo, protezione segreti, minimizzazione dei log, retention, revoca e gestione incidenti.
3. Provider: verificare per ogni provider modello/API esatti, termini, disponibilità in Italia, quote, assenza di metodo di pagamento e comportamento hard-stop; attivare solo quelli approvati.
4. Router: verificare rotazione su quota/429, cooldown, errori temporanei, provider disabilitati e persistenza dello stato dopo riavvio; nessun fallback a pagamento.
5. Chat Android: completare streaming delle risposte, timeout, error handling, retry idempotente dove applicabile, ripresa dopo chiusura app, stato della richiesta e UX per indisponibilità/quote esaurite.
6. Contesto: collaudare conversazione multi-turno, memoria di progetto, cronologia cifrata, retrieval documentale e limiti del prompt; non inviare tutta la cronologia senza necessità.
7. Test end-to-end: pairing, autenticazione, invio/ricezione, provider failover, quota zero, rete assente, riavvio backend/database, revoca dispositivo, privacy e cancellazione.
8. Dispositivi reali: verificare app e chat online su dispositivi Android reali, registrando errori, tempi, stabilità e consumo rete/batteria.
9. Gate APK: solo dopo che tutti i punti precedenti sono completati e documentati, autorizzare una singola build APK definitiva. Nessun APK intermedio.

## Nota su strumenti e piattaforme
Lovable ed Emergent possono restare strumenti di sviluppo con crediti gratuiti, ma non sono automaticamente endpoint API di inferenza. Non conteggiarli come provider chat finché non esiste un'API ufficiale compatibile e le relative condizioni sono state verificate. La lista in docs/FREE_PROVIDER_BILLING_AUDIT.md è un elenco di candidati, non un elenco di provider già attivi.

## Avanzamento streaming — 21 novembre 2026
È stato aggiunto un endpoint SSE autenticato e il router free-only inoltra i delta del provider. Il failover resta consentito soltanto prima del primo token: se il flusso si interrompe dopo aver mostrato testo, SuperIA conserva il parziale e segnala l'errore senza concatenare una risposta di un altro provider. Il client Android aggiorna il messaggio Compose progressivamente e salva il contenuto parziale. Sono stati aggiunti test mirati, ma la CI non ha ancora fornito un risultato verificabile e manca il collaudo end-to-end con provider gratuito reale. La fase resta aperta.

Il client Android collega la cancellazione della coroutine alla chiusura della connessione HTTP per non lasciare letture SSE appese. È stato aggiunto anche un test router che simula la caduta della connessione dopo il primo token e verifica che il secondo provider non venga contattato.


## Deduplicazione e recupero richieste — incremento implementato
- Android crea un UUID per ogni messaggio e lo conserva nell'archivio chat cifrato; il pulsante di ripresa riutilizza lo stesso identificativo.
- Il backend registra la richiesta per dispositivo, verifica l'hash del contenuto e impedisce che lo stesso ID venga riutilizzato con un payload differente.
- Se la richiesta è già in elaborazione restituisce HTTP 409; se è completata riproduce la risposta salvata senza richiamare il provider; se è fallita permette un nuovo tentativo.
- Le risposte archiviate per il replay sono cifrate con AES-256-GCM. È obbligatorio configurare il segreto server-side stabile `SUPERIA_CHAT_IDEMPOTENCY_KEY`, uguale su tutte le istanze e mai distribuito al client. La sua rotazione richiede ricifratura dei record.
- Sono stati aggiunti test backend per replay, conflitto di payload, isolamento per dispositivo, duplicato in elaborazione e cifratura nel database. CI e collaudo reale restano da verificare.
- La garanzia non è exactly-once assoluta: un arresto del server dopo la generazione del provider ma prima della registrazione atomica della risposta può richiedere una nuova generazione. Il retry non riprende il flusso dal token esatto.
