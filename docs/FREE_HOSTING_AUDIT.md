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
| EthioDeploy Free | Candidato da verificare | Dichiara nessuna carta, un progetto, 256 MB/mezzo core e database da 512 MB; sospende il servizio dopo 30 minuti senza traffico. Il piano Pro è separato, ma manca ancora una conferma contrattuale chiara sul blocco automatico e sull'assenza di addebiti dopo l'esaurimento delle quote. |
| Enzonic Python Free / Deploy | Candidato da verificare | Dichiara un server Python gratuito da 512 MB, 50% CPU, 10 GB e senza carta; la sezione Deploy dichiara inoltre risorse gratuite e deploy FastAPI. È necessario verificare condizioni, limiti effettivi, stabilità e comportamento all'esaurimento prima di affidargli dati o backend. |
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

### FastAPI Cloud
- Hobby è dichiarato gratuito e senza carta; 180 replica-ore mensili corrispondono a 7,5 giorni di esecuzione continua per una singola replica, quindi non basta per un servizio sempre acceso.
- Il piano Hobby include anche 5 GB di dati in uscita e 500 minuti di build.
- La documentazione distingue Hobby e Pro e descrive addebiti di overage per Pro. Non è stata trovata una garanzia sufficiente che l'account Hobby blocchi ogni risorsa all'esaurimento; resta non approvato finché questo punto non è verificato.

### EthioDeploy
- Free: un progetto, 256 MB, mezzo core, database da 512 MB; nessuna carta dichiarata.
- Il servizio si addormenta dopo 30 minuti senza traffico e si riavvia alla richiesta successiva.
- Il disco viene cancellato a ogni deploy: usare database persistente, non file locali.
- Pro è un piano separato; non passare a Pro né aggiungere risorse a pagamento. Prima dell'uso va provato che il piano Free si arresti senza fatturazione.

### Enzonic
- La pagina Python dichiara un server gratuito per account, 512 MB RAM, 50% CPU, 10 GB storage e nessuna carta.
- La pagina Deploy dichiara FastAPI/Flask e risorse gratuite; va verificato se la quota Deploy e quella del server Python sono prodotti distinti.
- Prima dell'uso controllare termini, conservazione dei dati, localizzazione dei server, politica di sospensione, limiti e comportamento quando si raggiunge la quota. Nessuna credenziale o progetto è stato creato.

### Waifly
- I termini prevedono sospensione per inattività e possibili sospensioni/cancellazioni per superamento risorse.
- La sezione 6 prevede una possibile commissione amministrativa di 1 € per verifica/recupero account: escluso in base al criterio economico richiesto.

## Gate di approvazione

1. Verifica manuale dell'account: registrazione e uso Free senza carta, PayPal, wallet o altro metodo di pagamento richiesto/collegato.
2. Nessun prodotto Paid, prova gratuita convertibile, abbonamento o servizio a consumo attivo.
3. Prova documentata che ogni quota gratuita si arresti con errore o sospensione e non generi addebiti.
4. Test di sleep/risveglio, persistenza, backup, localizzazione dati e limiti CPU/RAM.
5. Test provider senza crediti, errori 401/402/403/429 e indisponibilità: ogni scenario deve fallire in modo chiuso, conservare la conversazione lato app e non tentare servizi a pagamento.
6. Chiavi provider e token API solo server-side, mai in APK, codice sorgente o variabili pubbliche.

## Decisione

Nessun hosting è stato attivato o pubblicato. Cloudflare Workers Free, EthioDeploy ed Enzonic sono i candidati da verificare ulteriormente; FastAPI Cloud resta sospeso finché non è dimostrato l'hard-stop delle quote Hobby. Waifly è escluso. Nessun account, carta, abbonamento o credenziale è stato creato o inserito.

## Fonti

- Cloudflare Workers pricing: https://developers.cloudflare.com/workers/platform/pricing/
- Cloudflare Workers limits: https://developers.cloudflare.com/workers/platform/limits/
- FastAPI su Python Workers: https://developers.cloudflare.com/workers/languages/python/packages/fastapi/
- Cloudflare billing profile: https://developers.cloudflare.com/billing/get-started/create-billing-profile/
- FastAPI Cloud pricing: https://fastapicloud.com/pricing/
- FastAPI Cloud usage/billing: https://fastapicloud.com/docs/team-management/billing/usage-based-billing/
- EthioDeploy free backend: https://ethiodeploy.com/free-backend-hosting
- EthioDeploy FastAPI guide: https://ethiodeploy.com/deploy/fastapi
- Enzonic Python hosting: https://enzonic.com/python
- Enzonic pricing: https://enzonic.com/pricing
- Waifly free Python hosting: https://waifly.com/guides/free-python-hosting
- Waifly terms: https://waifly.com/cgu
- Hugging Face Spaces: https://huggingface.co/docs/hub/spaces-overview
- Koyeb pricing FAQ: https://www.koyeb.com/docs/faqs/pricing
- Render free services: https://render.com/docs/free
