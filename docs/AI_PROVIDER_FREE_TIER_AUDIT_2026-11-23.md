# Audit provider AI gratuiti — micro-fase 19

Data verifica: 23 novembre 2026  
Esito: audit pubblico completato; **nessun provider attivato**. La verifica account-level (carta/metodo di pagamento, hard-stop effettivo, ZDR e condizioni mostrate nel pannello) richiede account e consenso dell'utente.

## Sintesi decisionale

| Provider | Piano gratuito pubblico | Quota/stop | Privacy | Esito SuperIA |
|---|---|---|---|---|
| GroqCloud | Free tier; l'upgrade Developer richiede un metodo di pagamento | Limiti per modello/organizzazione; 429 al superamento | Inferenza non conservata per default; log temporanei fino a 30 giorni per affidabilità/abusi; ZDR configurabile per account idonei | Candidato condizionato a verifica no-card e ZDR attivo |
| Cloudflare Workers AI | 10.000 Neurons/giorno su Workers Free | Oltre quota le richieste falliscono; Workers Paid è necessario per uso ulteriore e fattura l'eccedenza | Zero retention per inferenza stateless e nessun training su Customer Content; storage/logging volontari cambiano il trattamento | Candidato prioritario, solo modelli che non richiedono billing method; verifica account no-card |
| OpenRouter | 25+ modelli gratuiti; varianti `:free` senza acquisto crediti | 20 RPM e 50 RPD senza acquisto; l'acquisto di 10 USD aumenta il limite | I dati sono inoltrati al provider del modello; provider possono trattenere/addestrare; Free non offre Data Policy-Based Routing | Non approvato per chat privata; solo eventuale contenuto pubblico/non sensibile con provider specifico verificato |
| Gemini API | Free tier limitato a modelli selezionati | Quote gratuite; paid tier e strumenti di grounding possono generare costi | Il contenuto del Free tier è usato per migliorare i prodotti Google | Non approvato per la chat privata; richiederebbe consenso esplicito all'uso dei prompt per miglioramento |
| SambaNova Cloud | Il piano denominato Free richiede aggiunta di metodo di pagamento e acquisto crediti | Crediti acquistati | Da verificare dopo il pagamento | **Escluso**: richiede metodo di pagamento e acquisto crediti |

## Dettagli

### GroqCloud
- La tabella ufficiale Free mostra, per i modelli chat attuali come `openai/gpt-oss-120b`, 30 RPM, 1.000 RPD, 8.000 TPM e 200.000 TPD. I limiti sono per modello/organizzazione e possono variare; la pagina account è la fonte esatta.
- Il superamento dei limiti produce HTTP 429; non implica automaticamente upgrade.
- Il piano Developer richiede un metodo di pagamento e usa fatturazione a consumo; non deve essere attivato.
- La documentazione dati dichiara che l'inferenza non viene conservata per default, ma può essere temporaneamente registrata fino a 30 giorni per affidabilità/abusi. ZDR è configurabile per clienti idonei.
- Condizioni per approvazione: account Free senza metodo di pagamento; ZDR visibilmente attivo; modello presente nella pagina Free dell'account; API key server-only.

### Cloudflare Workers AI
- Workers Free include 10.000 Neurons al giorno. Oltre il limite, le operazioni falliscono; Workers Paid è necessario per proseguire e fattura le unità eccedenti.
- Alcuni modelli sono esplicitamente soggetti a metodo di pagamento/piano Paid: non inserirli nella allowlist.
- La policy dati dichiara zero retention per richieste inference stateless e nessun training su Customer Content. Non usare logging/caching o storage esterno che persista i prompt.
- Condizioni per approvazione: account Workers Free senza metodo di pagamento; modello specifico con prezzo Free e nessun billing method; hard-stop verificato; endpoint e account ID server-only.

### OpenRouter
- Il piano Free elenca 25+ modelli gratuiti e 50 richieste/giorno. Le varianti con suffisso `:free` non richiedono acquisto crediti; l'aumento a 1.000 RPD richiede acquisto di almeno 10 USD ed è escluso.
- Il piano Free non offre Data Policy-Based Routing. I provider a valle hanno pratiche diverse e alcuni possono usare prompt/output per training o retention.
- Non attivare per la conversazione privata di default. Se in futuro si considera per dati pubblici, va fissato un provider specifico, verificata la relativa privacy e disabilitato il fallback automatico verso provider non conformi.

### Gemini API
- Il Free tier offre token gratuiti per un sottoinsieme di modelli, ma la pagina prezzi indica che i contenuti del Free tier sono usati per migliorare i prodotti Google.
- Paid tier e tool di grounding possono introdurre addebiti; nessun account di fatturazione deve essere collegato e nessun tool con pricing a consumo va attivato.
- Non approvato per prompt privati senza consenso esplicito e distinto dell'utente.

### SambaNova
- La pagina ufficiale Plan and Billing indica che il piano Free richiede di aggiungere un metodo di pagamento e acquistare crediti per eseguire richieste.
- Escluso in modo definitivo dal pool SuperIA in base al requisito no-card/no-purchase.

## Policy applicativa
- Nessun provider è attivo nel repository o in produzione.
- Le variabili `APPROVED_FREE`, `NO_PAYMENT_METHOD`, `HARD_STOP`, `NO_FIAT_BILLING` devono essere vere solo dopo verifica dell'account reale.
- La validazione richiede ora un'attestazione distinta `SUPERIA_PROVIDER_<ID>_DATA_POLICY_APPROVED=true` oltre alle quattro attestazioni economiche; il default resta false. SambaNova è stato rimosso dall'ordine dei provider e le sue variabili ambientali vengono rifiutate.
- Nessuna chiave è stata creata, nessun account provider è stato registrato, nessun metodo di pagamento aggiunto. Il router non include più SambaNova e rifiuta esplicitamente le sue variabili.

## Fonti ufficiali
- Groq limits: https://console.groq.com/docs/rate-limits
- Groq billing FAQ: https://console.groq.com/docs/billing-faqs
- Groq data controls: https://console.groq.com/docs/your-data
- Cloudflare Workers AI pricing: https://developers.cloudflare.com/workers-ai/platform/pricing/
- Cloudflare Workers AI data usage: https://developers.cloudflare.com/workers-ai/platform/data-usage/
- OpenRouter pricing: https://openrouter.ai/pricing
- OpenRouter free models: https://openrouter.ai/collections/free-models/
- OpenRouter privacy: https://openrouter.ai/privacy/
- Gemini API pricing: https://ai.google.dev/gemini-api/docs/pricing
- SambaNova plans: https://cloud.sambanova.ai/plans

## Esito
La verifica delle pagine pubbliche è completata. La micro-fase 19 resta **bloccata** per la verifica account-level no-card, hard-stop e privacy; non attivare provider sulla sola base dei documenti pubblici.
