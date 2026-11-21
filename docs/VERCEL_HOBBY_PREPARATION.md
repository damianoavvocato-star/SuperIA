# Preparazione Vercel Hobby per SuperIA Privata

## Stato
Preparazione del codice soltanto. **Nessun account creato, nessun progetto importato e nessun deploy eseguito.** Vercel resta candidato non approvato finché non viene verificata la registrazione senza carta e l'uso del solo piano Hobby.

## Requisiti hosting
- Vercel documenta il supporto diretto a FastAPI con Python Functions.
- Hobby è gratuito ma destinato esclusivamente a progetti personali/non commerciali.
- I progetti Hobby vengono messi in pausa al superamento delle quote gratuite; non è possibile acquistare overage restando su Hobby.
- Il runtime Python usa filesystem read-only e /tmp temporaneo: nessun database SQLite locale va considerato persistente.
- Prima dell'attivazione verificare registrazione senza carta, nessun trial Pro, nessun metodo di pagamento collegato e sospensione senza addebiti al superamento delle quote. Se una di queste condizioni non è confermata, non attivare.

## Sicurezza backend: associazione dispositivi
Il backend usa token casuali individuali per dispositivo, non un unico token condiviso:
- POST /api/v1/device/pair associa un dispositivo usando SUPERIA_PAIRING_CODE e restituisce un token Bearer casuale valido 90 giorni.
- Il database conserva solo l'hash SHA-256 del token, non il token in chiaro.
- POST /api/v1/device/revoke revoca un dispositivo usando SUPERIA_ADMIN_TOKEN e il suo device_id.
- Tutte le route API protette verificano token attivo, non scaduto e non revocato.
- Se mancano configurazioni o archivio auth, l'accesso fallisce in modo chiuso (503); non viene usato il vecchio token condiviso.
- I token sono individuali e revocabili; il codice pairing e il token amministratore devono essere segreti casuali forti e restare esclusivamente nelle variabili d'ambiente server-side.
- La connessione deve essere HTTPS. Non inserire pairing code, token amministratore o token dispositivo nel repository, nei log o nell'APK.

### Variabili server-side richieste
- SUPERIA_DATABASE_URL: URL PostgreSQL condiviso da usare in produzione per autenticazione e stato router (preferito se presente). La credenziale va conservata solo come segreto server-side.
- SUPERIA_AUTH_DB: percorso SQLite legacy per sviluppo/test o migrazione; non usare un file locale/\`/tmp\` come archivio auth in produzione.
- SUPERIA_PAIRING_CODE: segreto di associazione iniziale, lungo e casuale.
- SUPERIA_ADMIN_TOKEN: segreto amministratore distinto, lungo e casuale, necessario per revocare i dispositivi.
- SUPERIA_API_ACCESS_TOKEN non è più usato: il vecchio token condiviso è stato rimosso dall'autenticazione.

### Limite importante per Vercel
Il backend ora dispone di un adapter comune SQLite/PostgreSQL (psycopg 3). \`SUPERIA_DATABASE_URL\` abilita PostgreSQL per auth e stato router; in assenza resta il percorso SQLite legacy. Il filesystem /tmp di Vercel è effimero e non condiviso tra istanze: non configurare SQLite su /tmp in produzione, perché revoche e cooldown potrebbero divergere dopo cold start o tra istanze. La migrazione automatica dei dati SQLite esistenti e il failover/rotazione tra database non sono ancora implementati. Prima del deploy serve un PostgreSQL persistente e condiviso, con piano gratuito verificato senza carta e senza overage; in mancanza, non pubblicare il backend. La conversazione resta salvata cifrata nell'app, non in questo database.

La route di pairing richiede inoltre una protezione anti-abuso a livello gateway/WAF (limite richieste per IP). La limitazione in-process non è sufficiente in un ambiente serverless multi-istanza: non esporre pubblicamente il pairing finché non è configurato questo controllo.

## Limite richieste
Le API rifiutano body superiori a 2 MiB, inclusi i body in streaming senza Content-Length. Il limite è una protezione aggiuntiva, non sostituisce rate limiting, WAF o protezione DDoS del provider hosting.

## Passi prima di un deployment
1. Verificare account Vercel Hobby senza carta e senza trial/upgrade.
2. Verificare uso personale/non commerciale.
3. Trovare e approvare uno storage persistente condiviso senza carta e senza pagamenti/overage.
4. Configurare i tre segreti server-side e lo storage auth persistente; non usare /tmp.
5. Configurare rate limit per IP su pairing e revoca.
6. Eseguire test di pairing, token scaduto, token revocato, database indisponibile, 401/403/429 e provider senza crediti.
7. La schermata Android ora richiede URL HTTPS e codice di associazione, chiama il nuovo endpoint di pairing e salva token e ID dispositivo cifrati tramite Android Keystore. La compatibilità va confermata con build e test Android automatici.
8. Non collegare l'APK di produzione finché tutti i gate non sono superati.

## Preparazione tecnica
- services/api/index.py esporta l'app FastAPI esistente.
- Root Directory Vercel prevista: services/api.
- Il router provider viene riutilizzato nella stessa istanza calda; cooldown e cache router restano comunque best-effort in ambiente serverless.
- Nessun account, deploy, segreto, credenziale provider o metodo di pagamento è stato creato o inserito.

## Fonti ufficiali
- FastAPI su Vercel: https://vercel.com/docs/frameworks/backend/fastapi
- Runtime Python: https://vercel.com/docs/functions/runtimes/python
- Piano Hobby: https://vercel.com/docs/plans/hobby
- Quote e pausa: https://vercel.com/docs/plans
- Limiti Functions: https://vercel.com/docs/functions/limitations
- Termini: https://vercel.com/legal/terms

## Decisione
Vercel resta candidato tecnico, non approvato per produzione. Mancano ancora verifica senza carta, storage persistente condiviso e rate limiting gateway. Nessun deploy è stato eseguito.

## Database persistente gratuito: pool candidati e rotazione

La persistenza dell'autenticazione non può usare SQLite su `/tmp`: il filesystem serverless è effimero e non condiviso. Il backend non deve essere pubblicato finché il database auth non è persistente, condiviso tra istanze e testato.

### Candidati da verificare (nessuno è attivo)
- **Prisma Postgres Free** — prima scelta da verificare: il piano Free dichiara 2 GB e 200.000 operazioni mensili; al raggiungimento della quota le query si fermano. Non include backup automatici. Verificare nuovamente registrazione senza carta e assenza di billing prima dell'uso.
- **Aiven PostgreSQL Free** — alternativa: 1 GB disco, backup inclusi e piano senza carta; il servizio può sospendersi per inattività e richiedere riattivazione.
- **Neon Free** — ulteriore alternativa, subordinata alla verifica esplicita di account senza carta e di hard-stop senza overage/addebiti.
- **Turso** — escluso finché non è dimostrato che gli overage fatturabili siano disabilitati e non attivabili accidentalmente.

Questi sono candidati documentali, non account, database o integrazioni già configurati. Nessun account è stato creato e nessuna credenziale è stata inserita.

### Regole per passaggio a un database alternativo
1. Monitorare quote e inviare avvisi preventivi (es. 70% e 85%); non attendere l'esaurimento.
2. Sospendere temporaneamente le scritture, esportare la sorgente e importare la destinazione.
3. Applicare schema e migrazioni; verificare conteggi, integrità e soprattutto hash token, ID dispositivo, scadenze e revoche.
4. Cambiare configurazione solo dopo verifica completa, in modo atomico; poi testare pairing, autenticazione e revoca.
5. Se la sorgente è già bloccata e la copia non è aggiornata, non fare failover cieco: sospendere pairing, revoche e scritture finché lo stato non è verificato.
6. Conservare sorgente ed export cifrato fino al completamento dei test di ripristino. Non creare account multipli per moltiplicare le quote.
7. Se tutti i candidati gratuiti sono indisponibili, fermare le operazioni server che richiedono persistenza. Nessun upgrade, carta, acquisto, overage o fallback a pagamento.

### Stato tecnico e gate mancanti
- `device_auth.py` e il router provider usano l'adapter SQLite/PostgreSQL; la destinazione PostgreSQL è selezionata tramite `SUPERIA_DATABASE_URL`.
- La CI avvia PostgreSQL 16 e verifica connessione, conversione placeholder, lettura righe nominate e ciclo di vita dei token dispositivo sul database reale di test.
- Restano da aggiungere test specifici per concorrenza, revoca simultanea su più istanze, quota esaurita e indisponibilità prolungata del database.
- Lo stato del router in `/tmp` è best-effort e non condiviso; non va usato come stato autorevole in produzione.
- La protezione rate-limit/WAF per pairing e revoca resta obbligatoria prima dell'esposizione pubblica.

La policy di rotazione è documentata, ma la rotazione automatica non è implementata. Non distribuire il backend finché questi gate non sono superati.

Fonti ufficiali: https://www.prisma.io/postgres/free-postgresql-hosting ; https://www.prisma.io/docs/postgres/faq ; https://aiven.io/free-postgresql-database ; https://aiven.io/docs/products/postgresql/concepts/pg-free-tier ; https://neon.com/pricing ; https://neon.com/docs/introduction/pro-plan ; https://turso.tech/pricing


## Stato dell'adapter database (implementazione iniziale)
- `services/api/app/database.py` introduce un adapter DB-API con SQLite per sviluppo/test e PostgreSQL tramite psycopg 3.
- `device_auth.py` e il router provider usano l'adapter; il router può condividere `SUPERIA_DATABASE_URL` per conservare cooldown e provider disabilitati tra istanze.
- La dipendenza `psycopg[binary]` è inclusa nelle requirements backend.
- I test automatici coprono l'adapter SQLite e il ciclo di vita dei token; non sostituiscono un test d'integrazione con un vero server PostgreSQL.
- È stato aggiunto `services/api/app/migration.py`: migrazione manuale SQLite → database condiviso, che richiede API fermata, sorgente in sola lettura e destinazione vuota; copia token e stato router e verifica i conteggi. Non è stata eseguita alcuna migrazione reale. Prima di cambiare database attivo bisogna provare la procedura su un PostgreSQL di test, verificare token/revoche e collaudare il rollback.
- Per eseguire la migrazione manuale, con API arrestata e backup cifrato verificato, impostare `SUPERIA_MIGRATION_SOURCE_SQLITE` e `SUPERIA_DATABASE_URL`, quindi eseguire `PYTHONPATH=services/api python -m app.migration`. La destinazione deve essere vuota. Non eseguire su dati reali senza prima una prova completa e un piano di ripristino.
- La rotazione automatica tra provider DB non è attiva. Non configurare un database reale né pubblicare il backend finché test PostgreSQL, migrazione, rate limit e controlli economici non sono superati.


## Aggiornamento verifica PostgreSQL (21 novembre 2026)
- La pipeline `.github/workflows/backend-tests.yml` avvia un servizio PostgreSQL 16 temporaneo per i test.
- `test_database.py` prova la connessione PostgreSQL reale, la conversione dei placeholder e le righe nominate.
- `test_device_auth.py` verifica emissione, validazione e revoca di token su PostgreSQL.
- I workflow CI associati all'introduzione dei test PostgreSQL sono terminati con esito positivo; il database usato è un servizio effimero di test GitHub Actions, non un account/database esterno.
- Nessun provider hosting o database gratuito è stato attivato. Nessuna migrazione reale, pubblicazione o configurazione di credenziali è stata eseguita.


### Correzione e test dello stato router PostgreSQL
Durante l'estensione dei test è emersa una differenza reale tra i driver: psycopg restituisce righe nominate (mapping), mentre SQLite consente anche l'accesso posizionale. Il router leggeva lo stato provider per indice e avrebbe potuto fallire con PostgreSQL. La lettura è stata corretta usando i nomi delle colonne e aggiunto un test d'integrazione che verifica cooldown e disabilitazione persistenti su PostgreSQL 16. CI superata nei workflow `44889299678` e `44889307686`.


### Verifica integrità dati nella migrazione
La migrazione manuale ora ordina le righe per chiave primaria, confronta i conteggi e calcola impronte SHA-256 canoniche separate per `device_tokens` e `provider_state`. Se anche un solo valore copiato differisce, la transazione viene annullata; le impronte vengono restituite nel riepilogo dell'operazione. I test verificano la presenza delle impronte. Questa procedura è ancora da collaudare su un database reale destinazione prima di migrare dati effettivi.
