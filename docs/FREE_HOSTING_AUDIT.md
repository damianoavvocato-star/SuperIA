# Audit hosting gratuito per SuperIA Privata

Aggiornato: 21 novembre 2026. Obiettivo inderogabile: backend HTTPS senza carta/metodo di pagamento, senza addebiti, ricariche, upgrade automatici o overage fatturabili. I crediti gratuiti sono ammessi anche quando il provider li esprime in unità monetarie: non devono però richiedere un pagamento reale. Quando una quota gratuita finisce, il servizio deve fermarsi senza addebiti e senza passare a un piano a pagamento.

## Valutazione

| Servizio | Esito | Motivo |
|---|---|---|
| Hugging Face Spaces (Docker/Gradio) | Escluso | Il CPU Basic gratuito non è utilizzabile per Docker/Gradio senza un piano PRO/Team/Enterprise. |
| Koyeb | Escluso | La FAQ documenta verifica con carta e preautorizzazione di 29 USD; non rispetta il requisito senza carta. |
| Render Free | Non approvato | Il piano gratuito esiste e non richiede carta, ma non è ancora verificato un hard-stop completo per tutte le risorse e gli eventuali servizi accessori. Il Postgres gratuito scade dopo 30 giorni. |
| Cloudflare Workers Free + Python Workers | Candidato tecnico, non approvato per produzione | Workers Free è disponibile per impostazione predefinita; 100.000 richieste/giorno, 10 ms CPU per richiesta, 128 MB memoria e 50 subrequests. Il superamento della quota giornaliera produce Error 1027; va verificata e impostata la modalità fail-closed. Il limite CPU può essere troppo stretto per FastAPI e parsing/autenticazione. |
| FastAPI Cloud Hobby | Non approvato | No carta richiesta; 3 app, 0,1 CPU/500 MiB per replica, scale-to-zero, 180 replica-ore, 5 GB outbound e 500 minuti build. La documentazione descrive addebiti Pro per overage, ma non chiarisce con sufficiente precisione il comportamento di blocco quando un account Hobby esaurisce le quote. Non attivare Pro. |
| Vercel Hobby + Python Functions | Candidato prioritario per uso personale, non ancora approvato | FastAPI è supportato direttamente. Hobby è gratuito, limitato a uso personale/non commerciale e, superate le quote, i progetti Hobby vengono messi in pausa invece di addebitare overage. Va ancora verificato che registrazione e utilizzo siano possibili senza carta e senza avviare trial Pro. Il runtime usa `/tmp` temporaneo, quindi SQLite non garantisce persistenza tra istanze/cold start. |
| EthioDeploy Free | Escluso | I termini ufficiali dicono che il servizio è fatturato in Birr etiope (ETB), prevede acquisti e ricariche del wallet e addebita gli overage al saldo quando applicabile. Anche se il piano Free dichiara nessuna carta, non rispetta il criterio economico richiesto. |
| Enzonic Python Free / Deploy | Non approvato | Dichiara risorse Python gratuite senza carta, ma i termini consultati sono specifici per game hosting e non chiariscono le condizioni del prodotto Python Deploy. Mancano quindi conferme legali e tecniche su quote, privacy e hard-stop. |
| Waifly | Escluso | Anche se dichiara hosting gratuito senza carta, i termini prevedono una possibile commissione amministrativa di 1 € per recupero/verifica account: non rispetta il criterio di escludere servizi che introducono importi in euro. |

## Verifiche ufficiali Cloudflare aggiornate al 21 novembre 2026

- Workers Free: 100.000 richieste al giorno, reset a mezzanotte UTC, 10 ms di CPU per richiesta, 128 MB di memoria e 50 subrequests per invocazione.
- Superata la quota giornaliera, Cloudflare restituisce Error 1027. Per le route configurabili esistono modalità fail-open e fail-closed: per un backend privato è obbligatoria la modalità fail-closed, così il traffico non aggira il backend.
- Superato il limite CPU, Cloudflare restituisce Error 1102. La documentazione segnala che autenticazione e parsing di payload possono richiedere 10–20 ms: il limite Free di 10 ms è quindi un rischio concreto.
- FastAPI è supportato in Python Workers tramite `workers.asgi.entrypoint(app)`; questo non prova automaticamente che tutte le dipendenze e il codice attuale di SuperIA siano compatibili.
- La documentazione billing indica che un metodo di pagamento è richiesto per acquistare prodotti/servizi Cloudflare. Non aggiungere mai un metodo di pagamento né passare al piano Workers Paid.
- Non attivare Workers Paid, D1 Paid, Durable Objects a pagamento, R2 o altri add-on che richiedano un metodo di pagamento.
- Se le quote Free non sono sufficienti, il sistema deve fermarsi: nessun upgrade automatico, nessun prodotto a pagamento e nessun fallback fatturabile.

## Verifiche e limiti degli altri candidati

### Vercel Hobby — candidato prioritario, non ancora approvato
- Vercel documenta il deploy diretto di FastAPI tramite Python Functions; il backend esistente può essere esposto con un entrypoint sottile senza riscrivere le route.
- Il piano Hobby è gratuito e riservato a uso personale/non commerciale. I piani Hobby vengono messi in pausa quando superano le quote gratuite; non è previsto l'acquisto di overage nel piano Hobby.
- Non è ancora stata verificata con un account reale la registrazione senza carta/metodo di pagamento. Non avviare trial Pro e non collegare alcun metodo di pagamento.
- Il runtime Python usa filesystem read-only con spazio temporaneo `/tmp`. Il database SQLite del router può funzionare come cache temporanea, ma non garantisce cooldown persistenti tra istanze o cold start.
- Preparato `services/api/index.py` come entrypoint. Root Directory prevista: `services/api`. Nessun deploy o account creato.
- Il piano Hobby non è idoneo a un utilizzo commerciale o a distribuire SuperIA come servizio a pagamento.

### FastAPI Cloud
- Hobby è dichiarato gratuito e senza carta; 180 replica-ore mensili corrispondono a 7,5 giorni di esecuzione continua per una singola replica, quindi non basta per un servizio sempre acceso.
- Il piano Hobby include anche 5 GB di dati in uscita e 500 minuti di build.
- La documentazione distingue Hobby e Pro e descrive addebiti di overage per Pro. Non è stata trovata una garanzia sufficiente che l'account Hobby blocchi ogni risorsa all'esaurimento; resta non approvato finché questo punto non è verificato.

### EthioDeploy — escluso
- Sebbene il piano Free dichiari nessuna carta, i termini ufficiali specificano che il servizio è fatturato in Birr etiope (ETB), permette ricariche wallet e applica addebiti per overage al saldo quando applicabile.
- La policy non soddisfa il vincolo di non avere servizi che introducano pagamenti/ricariche in valuta; non collegare GitHub e non creare progetti.

### Enzonic — non approvato
- La pagina Python dichiara un server gratuito per account, 512 MB RAM, 50% CPU, 10 GB storage e nessuna carta.
- La pagina Deploy dichiara FastAPI/Flask e risorse gratuite, ma i termini consultati riguardano esplicitamente il game server e non disciplinano con chiarezza il prodotto Python Deploy.
- Non usare finché non sono disponibili termini applicabili al servizio Python, policy privacy, localizzazione dati, limiti e comportamento a quota esaurita. Nessuna credenziale o progetto è stato creato.

### Waifly
- I termini prevedono sospensione per inattività e possibili sospensioni/cancellazioni per superamento risorse.
- La sezione 6 prevede una possibile commissione amministrativa di 1 € per verifica/recupero account: escluso in base al criterio economico richiesto.

## Database persistente: nuova valutazione

Il database di autenticazione non può usare SQLite in /tmp su un hosting serverless: token revocati, scadenze e stato di sicurezza devono sopravvivere ai riavvii ed essere condivisi tra istanze.

| Database | Esito | Evidenza e limiti |
|---|---|---|
| Prisma Postgres Free | Candidato prioritario, non attivato | La pagina ufficiale dichiara 2 GB, 200.000 operazioni/mese, fino a 50 database, nessuna carta e nessuna scadenza. Al limite mensile le query si fermano fino al reset o a un upgrade: comportamento coerente con il fail-closed e senza overage sul piano Free. Non include backup nel piano Free. Richiede migrare lo storage auth/router da SQLite a PostgreSQL e testare la compatibilità. |
| Aiven PostgreSQL Free | Candidato alternativo, non attivato | La pagina ufficiale dichiara 1 GB storage, 1 GB RAM, 1 CPU, nessuna carta e nessun limite temporale; il servizio viene spento dopo inattività e può essere riavviato. È dichiarato senza costi inattesi, ma prima dell'uso va verificato nel pannello che non sia associato alcun metodo di pagamento e che il passaggio a Paid richieda sempre un'azione esplicita. |
| Turso Free | Non approvato finché gli overage non sono esclusi | La pagina ufficiale conferma Free senza carta e 5 GB, ma indica che gli overage per storage/letture/scritture/sync vengono fatturati automaticamente una volta abilitati. Non è quindi un'opzione sicura senza una verifica inequivocabile che gli overage restino disabilitati e non sia possibile abilitarli accidentalmente. |

### Decisione provvisoria sul database
- Preferire Prisma Postgres Free per la prima integrazione: il blocco delle query a 200.000 operazioni/mese è esplicito e non richiede carta; non attivare upgrade o piani a pagamento.
- Il database gratuito non sostituisce un backup: prima di salvare credenziali reali, predisporre un export cifrato e test di ripristino. Non inserire segreti provider nella tabella auth.
- Migrare device_tokens e lo stato router in PostgreSQL con transazioni e indici; verificare che revoca e scadenza funzionino su più istanze.
- Nessun account o database è stato creato. La selezione resta candidata fino a verifica manuale dell'account e implementazione/test del driver.
- Fonti ufficiali: https://www.prisma.io/postgres/free-postgresql-hosting ; https://aiven.io/free-postgresql-database ; https://turso.tech/pricing

## Rotazione dei database gratuiti (policy proposta)

I database gratuiti entrano in un pool di candidati, ma non si effettua un failover cieco tra database indipendenti. I provider non condividono automaticamente le stesse tabelle: cambiare URL senza migrazione può perdere associazioni, revoche e stato. La rotazione è autorizzata solo dopo aver verificato che il database di destinazione contenga una copia coerente e aggiornata.

### Pool candidati
- **Prisma Postgres Free** — primario candidato: 2 GB e 200.000 operazioni al mese; quando il limite viene raggiunto le query si fermano fino al reset mensile o a un upgrade. Nessun backup automatico incluso. Nessun account creato.
- **Aiven PostgreSQL Free** — standby candidato: 1 GB disco, 1 GB RAM, 1 CPU, backup inclusi e nessuna carta per il piano Free; il servizio può essere spento per inattività e richiedere riattivazione. Nessun account creato.
- **Neon Free** — standby candidato da verificare: la documentazione aggiornata indica 1 GB per progetto, 100 progetti e 100 CU-hours mensili per progetto; al superamento delle quote il compute viene sospeso e le operazioni che aumenterebbero lo storage oltre il limite vengono rifiutate. Prima di approvarlo bisogna verificare in modo esplicito registrazione senza carta, assenza di metodo di pagamento e impossibilità di overage/addebiti.
- **Turso** resta escluso dalla rotazione finché non è provato che ogni opzione di overage fatturabile sia disattivata e non attivabile accidentalmente.

### Regole di failover obbligatorie
1. Monitorare in anticipo query/operazioni, spazio, compute e stato del provider; avviso interno a soglie conservative (es. 70% e 85%). Le soglie sono allarmi, non autorizzazioni a superare il piano.
2. Prima di raggiungere il limite, bloccare nuove scritture durante la migrazione; mantenere l'app in modalità protetta e non perdere le richieste in attesa.
3. Esportare il database sorgente, trasferire i dati al candidato, applicare schema/migrazioni e verificare conteggi, vincoli e controlli di integrità. Per token dispositivo verificare in particolare hash, device_id, expires_at e revoked_at.
4. Solo dopo una verifica positiva, aggiornare atomicamente la configurazione del database attivo e verificare pairing, autenticazione e revoca. Non accettare scritture concorrenti su due database indipendenti e non consentire split-brain.
5. Se il provider si blocca prima che la copia sia aggiornata, non tentare un cambio automatico non verificato: sospendere pairing/revoche e tutte le scritture, mantenere i dati locali cifrati nell'app e mostrare un avviso. L'accesso non deve essere concesso sulla base di uno stato di revoca incerto.
6. Non cancellare il database precedente finché la nuova istanza non ha superato i test e non esiste un export cifrato verificato. Non usare account multipli per moltiplicare quote o aggirare i limiti.
7. Se tutti i database gratuiti sono pieni, sospendere le operazioni server che richiedono persistenza e attendere il reset/riattivazione o un intervento manuale. Nessun upgrade, carta, pagamento o provider a consumo.

### Stato implementativo
- Questa è la policy di progettazione e il pool di candidati; la rotazione automatica non è ancora implementata nel backend.
- L'attuale `device_auth.py` e il router provider usano SQLite; non collegare un database PostgreSQL finché non esiste un livello storage astratto testato e una migrazione completa.
- Il database auth deve essere persistente e condiviso tra tutte le istanze. SQLite in `/tmp` su serverless non è idoneo.
- Prima del rilascio: testare failover, rollback, database indisponibile, quota esaurita, token revocato e recupero dopo ripristino. La sicurezza ha priorità sulla continuità apparente.

### Fonti ufficiali database
- Prisma Postgres Free e limiti: https://www.prisma.io/postgres/free-postgresql-hosting ; https://www.prisma.io/docs/postgres/faq
- Aiven Free PostgreSQL: https://aiven.io/free-postgresql-database ; https://aiven.io/docs/products/postgresql/concepts/pg-free-tier
- Neon Free e comportamento dei limiti: https://neon.com/pricing ; https://neon.com/docs/introduction/pro-plan
- Turso pricing: https://turso.tech/pricing
## Gate di approvazione

1. Verifica manuale dell'account: registrazione e uso Free senza carta, PayPal, wallet o altro metodo di pagamento richiesto/collegato.
2. Nessun prodotto Paid, prova gratuita convertibile, abbonamento o servizio a consumo attivo.
3. Prova documentata che ogni quota gratuita si arresti con errore o sospensione e non generi addebiti.
4. Test di sleep/risveglio, persistenza, backup, localizzazione dati e limiti CPU/RAM.
5. Test provider senza crediti, errori 401/402/403/429 e indisponibilità: ogni scenario deve fallire in modo chiuso, conservare la conversazione lato app e non tentare servizi a pagamento.
6. Chiavi provider e token API solo server-side, mai in APK, codice sorgente o variabili pubbliche.

## Decisione

Nessun hosting è stato attivato o pubblicato. Cloudflare Workers Free resta il candidato tecnico principale ma con rischio CPU; FastAPI Cloud Hobby resta sospeso finché non è dimostrato l'hard-stop delle quote. EthioDeploy e Waifly sono esclusi per riferimenti espliciti a valuta/ricariche o commissioni. Enzonic non è approvato perché mancano termini chiari applicabili al suo servizio Python. Nessun account, carta, abbonamento o credenziale è stato creato o inserito.

## Fonti

- Cloudflare Workers pricing: https://developers.cloudflare.com/workers/platform/pricing/
- Cloudflare Workers limits: https://developers.cloudflare.com/workers/platform/limits/
- FastAPI su Python Workers: https://developers.cloudflare.com/workers/languages/python/packages/fastapi/
- Cloudflare billing profile: https://developers.cloudflare.com/billing/get-started/create-billing-profile/
- FastAPI Cloud pricing: https://fastapicloud.com/pricing/
- FastAPI Cloud usage/billing: https://fastapicloud.com/docs/team-management/billing/usage-based-billing/
- EthioDeploy free backend: https://ethiodeploy.com/free-backend-hosting
- EthioDeploy FastAPI guide: https://ethiodeploy.com/deploy/fastapi
- EthioDeploy terms (ETB and wallet overage): https://ethiodeploy.com/terms
- Enzonic Python hosting: https://enzonic.com/python
- Enzonic terms: https://enzonic.com/legal/terms
- Enzonic pricing: https://enzonic.com/pricing
- Waifly free Python hosting: https://waifly.com/guides/free-python-hosting
- Waifly terms: https://waifly.com/cgu
- Hugging Face Spaces: https://huggingface.co/docs/hub/spaces-overview
- Koyeb pricing FAQ: https://www.koyeb.com/docs/faqs/pricing
- Render free services: https://render.com/docs/free
- Vercel FastAPI: https://vercel.com/docs/frameworks/backend/fastapi
- Vercel Hobby plan: https://vercel.com/docs/plans/hobby
- Vercel plan limits and pause behavior: https://vercel.com/docs/plans
- Vercel Python runtime/filesystem: https://vercel.com/docs/functions/runtimes
- Vercel Terms (Hobby personal/non-commercial; fees): https://vercel.com/legal/terms
