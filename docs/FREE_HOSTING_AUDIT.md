# Audit hosting gratuito per SuperIA Privata

Aggiornato: 20 novembre 2026. Obiettivo inderogabile: backend HTTPS senza carta/metodo di pagamento, senza addebiti, ricariche, upgrade automatici o overage fatturabili. Un piano chiamato "Free" non basta: vanno controllati account, limiti, comportamento a quota zero e termini.

## Valutazione

| Servizio | Esito | Motivo |
|---|---|---|
| Hugging Face Spaces (Docker/Gradio) | Escluso | Il CPU Basic gratuito non è utilizzabile per Docker/Gradio senza un piano PRO/Team/Enterprise. |
| Koyeb | Escluso | La FAQ documenta verifica con carta e preautorizzazione di 29 USD; non rispetta il requisito senza carta. |
| Render Free | Non approvato | Il piano include servizi gratuiti ma documenta fatturazione di traffico aggiuntivo; non è provato un hard-stop che impedisca qualsiasi addebito. |
| Cloudflare Workers Free + Python Workers | Candidato tecnico, non ancora approvato | Piano Free predefinito con 100.000 richieste/giorno e limite CPU di 10 ms per invocazione; FastAPI è supportato via ASGI. Prima dell'uso bisogna confermare che la creazione/uso dell'account non richieda carta, che nessun prodotto a pagamento sia attivo e che il superamento delle quote fallisca senza fatturazione. |

## Vincoli tecnici Cloudflare da verificare

- Workers Free: 100.000 richieste al giorno e 10 ms CPU per invocazione; memoria 128 MB. Il limite CPU può essere troppo stretto per l'avvio e la gestione FastAPI/Pydantic: serve un prototipo misurato.
- D1 Free: 10 database, massimo 500 MB per database, 5 GB complessivi e limiti giornalieri; quando le quote free sono esaurite le query vengono rifiutate. Non usare un database con piano paid.
- Python Workers supporta FastAPI tramite `workers.asgi.entrypoint`; non è dimostrato che il backend attuale sia trasferibile senza modifiche. Il router attuale usa SQLite e `httpx`; persistenza e librerie vanno testate nel runtime Workers prima di approvare la migrazione.
- I segreti provider e il token API devono essere configurati come Secrets, mai in `vars`, nel codice o nell'APK.
- Non attivare Workers Paid, D1 Paid, Durable Objects a pagamento, R2 o altri add-on che richiedano un metodo di pagamento.
- Nessuna credenziale Cloudflare è stata inserita e nessun servizio è stato creato o pubblicato.

## Gate di approvazione

1. Verifica manuale dell'account Cloudflare: nessuna carta o altro metodo di pagamento richiesto/collegato e nessun abbonamento o prodotto a pagamento attivo.
2. Conferma che le quote Workers/D1 Free si arrestino con errore e non generino addebiti.
3. Prototipo FastAPI su Python Workers, test su limiti CPU/memoria, compatibilità `httpx`, gestione segreti e persistenza D1.
4. Test di quota esaurita e provider senza crediti: tutte le richieste devono fallire in modo chiuso.
5. Solo dopo questi controlli si può decidere se usarlo. In caso di dubbio, non attivare.

## Fonti ufficiali

- Cloudflare Workers pricing: https://developers.cloudflare.com/workers/platform/pricing/
- Cloudflare Workers limits: https://developers.cloudflare.com/workers/platform/limits/
- FastAPI su Python Workers: https://developers.cloudflare.com/workers/languages/python/packages/fastapi/
- Python Workers packages: https://developers.cloudflare.com/workers/languages/python/packages/
- D1 limits: https://developers.cloudflare.com/d1/platform/limits/
- Cloudflare secrets: https://developers.cloudflare.com/workers/configuration/secrets/
- Hugging Face Spaces: https://huggingface.co/docs/hub/spaces-overview
- Koyeb pricing FAQ: https://www.koyeb.com/docs/faqs/pricing
- Render free services: https://render.com/docs/free
