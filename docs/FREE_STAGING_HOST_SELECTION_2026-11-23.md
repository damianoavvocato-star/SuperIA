# Selezione hosting gratuito per staging — micro-fase 13

Data verifica: 23 novembre 2026  
Ambito: solo staging tecnico; nessun account creato, nessun servizio attivato e nessun dato personale caricato.

## Selezione proposta

### Backend: Render Free Web Service — staging only
Motivi:
- Render documenta web service Python gratuiti, 512 MB RAM e 750 ore istanza per workspace/mese.
- Il servizio dorme dopo 15 minuti senza traffico e può richiedere circa un minuto per riavviarsi.
- Il filesystem è effimero: nessun dato o database deve risiedere sul disco locale.
- Se non è presente un metodo di pagamento, al raggiungimento dei limiti di banda/build il servizio viene sospeso o i nuovi build vengono fermati; se si aggiunge un metodo di pagamento, alcuni consumi eccedenti possono essere fatturati.
- Render dichiara che le istanze Free sono per test/hobby e non per applicazioni di produzione. Per SuperIA è quindi candidato esclusivamente per staging e test di collegamento.

### Database: Aiven PostgreSQL Free — staging/test data
Motivi:
- Aiven dichiara iscrizione senza carta e tier gratuito senza scadenza.
- Limiti: 1 CPU, 1 GB RAM, 1 GB disco, massimo 20 connessioni, nessun connection pooling e nessuno SLA 99,99%.
- Il servizio può essere spento dopo inattività; l'avviso precede lo spegnimento e può essere riattivato.
- Nel tier Free non si può scegliere cloud o regione: non usare per dati personali reali finché la collocazione e le condizioni privacy non sono state chiarite.
- Upgrade a piani a pagamento richiede l'aggiunta di un metodo di pagamento; non aggiungere carte né attivare upgrade.

## Architettura di staging proposta
- Render esegue FastAPI con HTTPS gestito e URL `onrender.com`.
- Aiven fornisce PostgreSQL persistente; `SUPERIA_DATABASE_URL` è configurato solo nel pannello come segreto, mai nel repository.
- `SUPERIA_ROUTER_STATE_DB` deve usare lo stesso PostgreSQL, evitando filesystem temporanei.
- `SUPERIA_FREE_ONLY=false` e `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=false` inizialmente; nessun provider AI reale e pairing disabilitato finché la fase 12 non è superata.
- Health check su `/health`; readiness autenticata separata.
- Nessun APK o segreto nel file di configurazione.

## Fornitori esclusi o non approvati
- **EthioDeploy**: non selezionato. Pur dichiarando un backend e Postgres gratuiti senza carta, i termini prevedono overage addebitato al wallet e sospensione in caso di overage non saldato; ciò non rispetta il requisito SuperIA di hard-stop senza debiti o overage. Inoltre non offre SLA.
- **Enzonic**: non approvato. La pagina Python dichiara risorse gratuite, ma i termini pubblicati esaminati regolano principalmente hosting game server; non è stato verificato che le medesime condizioni di no-overage, privacy e retention si applichino al servizio Python. La privacy policy descrive inoltre partner pubblicitari e processor di pagamento.
- **Render Postgres Free**: escluso come database persistente perché scade dopo 30 giorni e non ha backup.
- **Neon**: non selezionato in questa fase perché il modello commerciale usage-based dei piani a pagamento e la gestione esatta dell'arresto oltre quota Free richiedono una verifica ulteriore prima di approvarlo per un backend che non deve mai produrre addebiti.

## Condizioni prima dell'uso
1. Consenso esplicito dell'utente per autorizzare le app GitHub e creare gli account/servizi Render e Aiven.
2. Verifica manuale nel pannello che il piano selezionato sia esattamente Free e non sia presente alcun metodo di pagamento.
3. Verifica dei limiti e dell'hard-stop attuali prima del deploy.
4. Nessun dato personale reale in staging; solo dati sintetici.
5. Non promuovere questo assetto a produzione: il tier Render Free è dichiarato non-production e Aiven Free non ha SLA né regione selezionabile.

## Fonti ufficiali
- Render Free: https://render.com/docs/free
- Render regioni: https://render.com/docs/regions
- Render primo deploy senza pagamento: https://render.com/docs/your-first-deploy
- Aiven Free PostgreSQL: https://aiven.io/docs/products/postgresql/concepts/pg-free-tier
- EthioDeploy termini: https://ethiodeploy.com/terms
- EthioDeploy privacy: https://ethiodeploy.com/privacy
- Enzonic Python: https://enzonic.com/python
- Enzonic termini: https://enzonic.com/legal/terms
- Enzonic privacy: https://enzonic.com/legal/privacy

## Esito
Micro-fase 13: **Completata come selezione documentale per staging**, non come provisioning. Il backend non è pubblicato e nessun account è stato creato. La micro-fase 14 resta bloccata fino al consenso esplicito e alla disponibilità degli account.
