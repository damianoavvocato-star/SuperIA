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

## Aggiornamento verifica documentale — 27 novembre 2026

Nuovo controllo delle fonti ufficiali Cloudflare, senza creare account né abilitare servizi:

- Workers AI Free mantiene una quota di **10.000 Neurons al giorno**; oltre la quota, sul piano Workers Free le richieste vengono rifiutate. Per superare il limite occorre passare a Workers Paid, che non è ammesso per SuperIA. Fonte: https://developers.cloudflare.com/workers-ai/platform/pricing/ (pagina aggiornata 18 novembre 2026).
- La documentazione aggiornata dichiara zero retention per richieste standard stateless e nessun uso del Customer Content per addestrare i modelli; logging, caching e funzioni stateful possono cambiare il trattamento e devono restare disattivati. Fonte: https://developers.cloudflare.com/workers-ai/platform/data-usage/ (aggiornata 16 ottobre 2026).
- Due candidati da valutare sul piano Free: `@cf/zai-org/glm-4.7-flash` e `@cf/google/gemma-4-26b-a4b-it`. Le pagine ufficiali documentano supporto chat completions/streaming e zero data retention: https://developers.cloudflare.com/workers-ai/models/glm-4.7-flash/ e https://developers.cloudflare.com/workers-ai/models/gemma-4-26b-a4b-it/.
- Attenzione: alcuni modelli richiedono un metodo di pagamento o Workers Paid; la lista viene aggiornata. Non utilizzare modelli che richiedono billing. Il changelog del 5 novembre 2026 documenta anche la deprecazione dal 20 novembre di vari modelli precedenti: https://developers.cloudflare.com/changelog/post/2026-11-05-planned-model-deprecations/.
- L'endpoint OpenAI-compatible del router SuperIA è già predisposto per Workers AI. La verifica pubblica rende Cloudflare il candidato prioritario da controllare nell'account, ma **non dimostra** che l'account dell'utente sia Free/no-card né che l'accesso al modello sia abilitato senza billing.

**Esito:** candidato tecnico aggiornato, non attivato. Restano obbligatori: verifica visiva dell'account Free senza metodo di pagamento, verifica che il modello selezionato non richieda billing, conferma hard-stop oltre quota e creazione autorizzata del token API. Tutte le attestazioni runtime restano `false` fino a tali verifiche. Nessun prompt è stato inviato a Cloudflare e nessun segreto è stato creato.


## Aggiornamento estensione provider — 27 novembre 2026

È stata eseguita una verifica aggiuntiva dei provider proposti per ampliare la rotazione. L'esito distingue disponibilità di un piano gratuito da idoneità a ricevere conversazioni private di SuperIA.

| Provider | Evidenza gratuita pubblica | Vincolo rilevante | Esito per la rotazione privata |
|---|---|---|---|
| GroqCloud | Piano Free documentato; 429 al superamento dei limiti; upgrade Developer richiede metodo di pagamento | Verificare account Free e controlli dati/ZDR effettivamente attivi | Candidato condizionato, secondo Cloudflare |
| NVIDIA NIM / Build | Alcuni endpoint sono marcati Free Endpoint | La pagina di prova dichiara registrazione di input/output e uso per migliorare prodotti/servizi NVIDIA | Escluso per prompt privati finché non esiste un endpoint/contratto con garanzie diverse |
| Mistral | La documentazione distingue modelli Labs gratuiti | I modelli Labs non supportano opt-out della raccolta dati né ZDR; ZDR documentato per piano pay-as-you-go | Non approvato per chat privata gratuita |
| Cohere | Trial key gratuita e limitata; documentazione indica 1.000 chiamate/mese per trial e alcuni modelli | Trial/prototipazione non dimostra idoneità di produzione, assenza di retention/training o quota gratuita permanente | Candidato da approfondire, non abilitabile |
| SambaNova | La pagina Plans/Billing attuale chiede metodo di pagamento e acquisto crediti per iniziare | Contraddice il requisito no-card/no-purchase | Escluso; non inserirlo nel router |
| Gemini API | Piano Free disponibile su modelli selezionati | Google dichiara che i contenuti Free sono usati per migliorare i prodotti; alcuni strumenti possono generare costi | Escluso dalla chat privata senza consenso separato e controlli dedicati |
| OpenRouter | Modelli con suffisso :free disponibili entro limiti | Il gateway inoltra i prompt a provider diversi; il piano Free non assicura una policy dati uniforme né routing basato su policy | Escluso dalla chat privata predefinita |
| Z.ai / SiliconFlow | Sono stati trovati riferimenti pubblici a modelli o accessi promozionali/free | Non è stata completata una verifica ufficiale sufficiente di quota permanente, no-card, hard-stop, privacy e condizioni d'uso applicabili | Non approvati; nessuna integrazione finché non verificati |

### Fonti aggiuntive consultate
- NVIDIA Build endpoint trial/privacy: https://build.nvidia.com/fastino/gliner2-pii (la pagina dichiara registrazione di input/output e uso per migliorare prodotti/servizi).
- Mistral model lifecycle: https://docs.mistral.ai/inference/model-lifecycle (Labs gratuiti senza opt-out e senza ZDR).
- Mistral Zero Data Retention: https://docs.mistral.ai/admin/monitor-comply/zero-data-retention (ZDR disponibile per organizzazioni idonee su pay-as-you-go; non prova disponibilità sul piano gratuito).
- Cohere trial limits: https://docs.cohere.com/docs/rate-limits e https://docs.cohere.com/v2/docs/cohere-faqs.
- SambaNova Plans: https://cloud.sambanova.ai/plans.
- Gemini pricing/data use: https://ai.google.dev/gemini-api/docs/pricing.
- Cloudflare pricing e data use: https://developers.cloudflare.com/workers-ai/platform/pricing/ e https://developers.cloudflare.com/workers-ai/platform/data-usage/.
- Groq rate limits e billing: https://console.groq.com/docs/rate-limits e https://console.groq.com/docs/billing-faqs.

### Decisione operativa
Non aumentare il numero di provider solo per avere più nomi in elenco. Per la chat privata la priorità è la protezione dei dati e l'impossibilità tecnica di generare addebiti. Al momento la rotazione di produzione può considerare soltanto Cloudflare Workers AI e GroqCloud, entrambi subordinati alla verifica account-level; nessuno è attivo. Gli altri servizi restano esclusi o in valutazione e non devono essere presentati come già integrati.

La rotazione tra account/chiavi dello stesso provider per eludere quote è vietata. Il failover deve usare soltanto provider indipendenti approvati, con hard-stop, cooldown e nessun fallback a pagamento.
