# Audit hosting gratuito per SuperIA Privata

Aggiornato: 21 novembre 2026. Obiettivo inderogabile: backend HTTPS senza carta/metodo di pagamento, senza addebiti, ricariche, upgrade automatici o overage fatturabili. Un piano chiamato "Free" non basta: vanno controllati account, limiti, comportamento a quota zero e termini.

## Valutazione

| Servizio | Esito | Motivo |
|---|---|---|
| Hugging Face Spaces (Docker/Gradio) | Escluso | Il CPU Basic gratuito non è utilizzabile per Docker/Gradio senza un piano PRO/Team/Enterprise. |
| Koyeb | Escluso | La FAQ documenta verifica con carta e preautorizzazione di 29 USD; non rispetta il requisito senza carta. |
| Render Free | Non approvato | Il piano include servizi gratuiti ma documenta fatturazione di traffico aggiuntivo; non è provato un hard-stop che impedisca qualsiasi addebito. |
| Cloudflare Workers Free + Python Workers | Candidato gratuito, test tecnico necessario; nessun account/servizio attivato | La documentazione dice che il piano Workers Free è disponibile per impostazione predefinita e che un metodo di pagamento serve per acquistare prodotti/servizi Cloudflare. Non documenta però, da sola, ogni schermata di registrazione/account per l'Italia: prima dell'attivazione va verificato che la procedura concreta non chieda carta o pagamento. |

## Verifiche ufficiali Cloudflare aggiornate al 21 novembre 2026

- Workers Free: 100.000 richieste al giorno, reset a mezzanotte UTC, 10 ms di CPU per richiesta, 128 MB di memoria e 50 subrequests per invocazione.
- Superata la quota giornaliera, Cloudflare restituisce Error 1027. Per le route configurabili esistono modalità fail-open e fail-closed: per un backend privato è obbligatoria la modalità fail-closed, così il traffico non aggira il backend.
- Superato il limite CPU, Cloudflare restituisce Error 1102. La stessa documentazione segnala che attività più pesanti come autenticazione e parsing di payload possono richiedere 10–20 ms: il limite Free di 10 ms è quindi un rischio concreto per FastAPI e va misurato con un prototipo.
- FastAPI è supportato in Python Workers tramite `workers.asgi.entrypoint(app)`; questo non prova automaticamente che tutte le dipendenze e il codice attuale di SuperIA siano compatibili.
- La documentazione billing indica che un metodo di pagamento è richiesto per acquistare prodotti/servizi Cloudflare. Non aggiungere mai un metodo di pagamento né passare al piano Workers Paid.
- Non attivare Workers Paid, D1 Paid, Durable Objects a pagamento, R2 o altri add-on che richiedano un metodo di pagamento.
- Se le quote Free non sono sufficienti, il sistema deve fermarsi: nessun upgrade automatico, nessun prodotto a pagamento e nessun fallback fatturabile.

## Vincoli tecnici da verificare prima dell'approvazione

1. Registrazione e uso del solo piano Free senza carta, PayPal, wallet o altro metodo di pagamento richiesto.
2. Nessun prodotto Paid, prova gratuita convertibile, abbonamento o servizio a consumo attivo.
3. Configurazione effettiva fail-closed per il raggiungimento della quota giornaliera.
4. Prototipo FastAPI su Python Workers: misurare startup CPU, CPU per richiesta, memoria, autenticazione, parsing JSON, chiamate HTTPS e compatibilità di tutte le dipendenze.
5. Verificare la persistenza: il router attuale usa SQLite locale; il filesystem del Worker non va considerato archivio persistente. Valutare D1 soltanto se limiti e modalità Free sono verificati e il codice viene adattato.
6. Testare quota esaurita, provider senza crediti, errori 401/402/403/429 e indisponibilità: ogni scenario deve fallire in modo chiuso, conservare la conversazione lato app e non tentare servizi a pagamento.
7. Chiavi dei provider e token API solo come Secrets server-side, mai in variabili pubbliche, codice sorgente o APK.

## Decisione

Cloudflare Workers Free resta il candidato più promettente tra quelli esaminati, ma non è ancora approvato per la produzione. Il limite di 10 ms può renderlo inadatto al backend attuale senza una versione molto leggera; inoltre la registrazione senza metodo di pagamento e il comportamento fail-closed devono essere verificati concretamente. Nessuna credenziale Cloudflare è stata inserita e nessun servizio è stato creato o pubblicato.

## Fonti ufficiali

- Workers pricing: https://developers.cloudflare.com/workers/platform/pricing/
- Workers limits (limiti CPU, richieste giornaliere, Error 1027 e modalità fail-closed): https://developers.cloudflare.com/workers/platform/limits/
- FastAPI su Python Workers: https://developers.cloudflare.com/workers/languages/python/packages/fastapi/
- Python Workers packages: https://developers.cloudflare.com/workers/languages/python/packages/
- D1 limits: https://developers.cloudflare.com/d1/platform/limits/
- Cloudflare secrets: https://developers.cloudflare.com/workers/configuration/secrets/
- Creazione profilo billing (metodo di pagamento richiesto per acquistare prodotti/servizi): https://developers.cloudflare.com/billing/get-started/create-billing-profile/
- Hugging Face Spaces: https://huggingface.co/docs/hub/spaces-overview
- Koyeb pricing FAQ: https://www.koyeb.com/docs/faqs/pricing
- Render free services: https://render.com/docs/free
