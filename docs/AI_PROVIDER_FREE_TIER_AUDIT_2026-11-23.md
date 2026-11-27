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


## Decisione aggiornata dell'utente — 27 novembre 2026

L'utente ha chiarito che accetta che i provider gratuiti possano raccogliere e utilizzare prompt e risposte per migliorare i propri prodotti. La raccolta dati per miglioramento non è quindi, da sola, motivo di esclusione. Prima dell'uso restano da comunicare le condizioni applicabili e da rispettare eventuali consensi/termini del provider.

La priorità di selezione diventa:
1. costo effettivo zero e nessuna possibilità di addebito;
2. piano API gratuito realmente utilizzabile (non solo chat web o crediti promozionali);
3. hard-stop o blocco tecnico prima di qualsiasi utilizzo a pagamento;
4. compatibilità API e stabilità sufficienti;
5. informativa chiara sulla raccolta e sull'eventuale uso dei prompt.

### Pool ampliato da implementare/verificare
- **Cloudflare Workers AI**: candidato primario già supportato dal router; restano da verificare account e quota effettiva.
- **GroqCloud**: candidato API gratuito; già supportato dal router; verificare limiti e assenza di metodo di pagamento sul singolo account.
- **Google Gemini API Free**: da includere esplicitamente nel pool; la pagina prezzi dichiara input/output gratuiti su modelli selezionati e indica che i contenuti Free sono usati per migliorare i prodotti Google. Disabilitare strumenti/feature con costi separati e non configurare mai un piano Paid.
- **OpenRouter modelli :free**: da includere come gateway opzionale solo con model ID che termina in :free. La pagina ufficiale dichiara $0 per varianti :free e limiti di 20 richieste/minuto e 50 richieste/giorno; il limite più alto richiede acquisto crediti, quindi SuperIA non deve acquistare crediti né fare upgrade. Il routing automatico openrouter/free va usato solo se il comportamento resta limitato a modelli gratuiti.
- **Cohere Trial API**: nuovo candidato da implementare/verificare; documentazione ufficiale dichiara trial keys gratuite ma limitate (1.000 chiamate/mese per le chiavi trial e alcuni modelli). Non considerarlo production-ready senza test tecnico e verifica dei termini correnti.
- **Mistral API**: non aggiungere al pool API gratuito al momento: la documentazione corrente dice che Free mode non include API access e che creare chiavi/usare API richiede billing activation e pay-as-you-go. La disponibilità di modelli Labs nella chat/web app non equivale ad API gratuita.
- **SambaNova**: resta escluso finché il flusso di attivazione richiede metodo di pagamento/acquisto crediti.
- **NVIDIA Build, Z.ai, SiliconFlow**: mantenere in valutazione; aggiungere solo dopo verifica documentale corrente di accesso API senza carta, costo zero e hard-stop.

### Protezioni tecniche obbligatorie
- `SUPERIA_FREE_ONLY=true` resta obbligatorio.
- Ogni provider deve avere attestazioni separate per account Free, assenza di metodo di pagamento, hard-stop, assenza di billing monetario e presa visione della policy dati.
- Non configurare carte, crediti, auto-recharge, pay-as-you-go, upgrade o billing automatico.
- Su 402/403 il provider viene disabilitato; su 429 viene messo in cooldown o disabilitato secondo il codice quota specifico; nessun fallback verso modello/endpoint a pagamento.
- Il failover tra provider indipendenti è consentito; la rotazione tra account/chiavi dello stesso servizio per eludere limiti è vietata.
- Il router attualmente supporta Gemini, Groq, OpenRouter e Cloudflare; Cohere richiede adattatore dedicato (schema API/streaming diverso) e test prima dell'inserimento nel pool runtime.
- Nessun provider è attivo finché non vengono configurati account e segreti autorizzati; questo audit non ha creato account, chiavi o servizi.

### Fonti ufficiali aggiornate
- Gemini pricing e uso dati: https://ai.google.dev/gemini-api/docs/pricing (Free tier: token gratuiti su modelli selezionati; contenuti usati per migliorare prodotti; alcune funzioni Search possono avere costi oltre soglia).
- OpenRouter modelli gratuiti: https://openrouter.ai/collections/free-models/ (varianti :free $0, limiti 20 RPM/50 RPD; 1.000 RPD solo dopo acquisto di $10 di crediti, da non utilizzare).
- OpenRouter pricing: https://openrouter.ai/pricing.
- Cohere rate limits: https://docs.cohere.com/docs/rate-limits (trial key gratuite e limitate; 1.000 chiamate/mese per trial keys e alcuni modelli).
- Mistral usage and limits: https://docs.mistral.ai/admin/billing-usage/usage-limits/ (Free mode non include API access; API richiede billing activation e pay-as-you-go).
- Cloudflare pricing: https://developers.cloudflare.com/workers-ai/platform/pricing/.
- Groq rate limits: https://console.groq.com/docs/rate-limits.

Questa decisione sostituisce le precedenti esclusioni basate unicamente sul riutilizzo dei dati per migliorare i modelli. Non sostituisce le verifiche economiche, contrattuali e tecniche.


## Ricognizione estesa provider pubblici — 27 novembre 2026

Questa ricognizione amplia la ricerca oltre i provider già presenti nel router. È una verifica documentale pubblica: non crea account, non prova le condizioni del singolo account e non equivale a testare le API con chiavi reali. Un provider viene considerato approvabile solo dopo aver verificato in modo esplicito: API utilizzabile senza pagamento, nessuna carta/metodo di pagamento, quota gratuita ricorrente o chiaramente delimitata, blocco tecnico prima di qualsiasi addebito, termini applicabili e compatibilità tecnica.

### Provider con evidenza di API gratuita pubblica

| Provider | Evidenza e limiti pubblici | Rischi / punti non verificati | Esito provvisorio SuperIA |
|---|---|---|---|
| Google Gemini API Free | Token input/output gratuiti su modelli selezionati; limiti per progetto/modello visibili in AI Studio. | Google dichiara che i contenuti Free sono usati per migliorare i prodotti. Evitare grounding e strumenti con tariffazione separata; verificare che nessun progetto sia collegato a billing. | **Candidato**: router già compatibile; configurare solo modelli Free e blocco prima di qualsiasi funzione a pagamento. |
| GroqCloud Free | Limiti Free pubblicati per modello; ad esempio GPT-OSS 120B: 30 RPM, 1.000 RPD, 8.000 TPM e 200.000 TPD. Il superamento restituisce 429. | Limiti effettivi dipendono dall'organizzazione/account. Verificare che non sia presente metodo di pagamento e che l'upgrade Developer non sia attivo. | **Candidato prioritario**: router già compatibile; resta verifica account-level. |
| Cloudflare Workers AI Free | Workers Free include 10.000 Neurons al giorno; sul piano Free le richieste oltre quota falliscono. | Alcuni modelli richiedono metodo di pagamento/Workers Paid o crediti. La lista dei modelli soggetti a billing cambia: non usare modelli non ammessi dalla allowlist. | **Candidato prioritario**: router già compatibile; mantenere hard-stop Cloudflare 3036 e allowlist stretta. |
| OpenRouter modelli `:free` | Varianti con suffisso `:free` a $0; 20 RPM e 50 richieste/giorno. Il limite di 1.000 RPD richiede acquisti di crediti ed è vietato. | OpenRouter inoltra i prompt a provider sottostanti; privacy e retention possono variare per modello/provider. Un modello con prezzo $0 senza suffisso `:free` può comunque richiedere account finanziato. | **Candidato condizionato**: router già compatibile; accettare esclusivamente ID `:free`, non usare routing che possa selezionare modelli a pagamento, mostrare il provider sottostante. |
| Cohere Trial API | Chiavi trial gratuite; documentazione v2 indica 1.000 chiamate/mese per trial keys e alcuni modelli, con 20 richieste/minuto per vari modelli Chat. | È una quota trial, non una garanzia di produzione o di disponibilità permanente; assenza di carta e comportamento al superamento devono essere verificati nell'account. | **Candidato tecnico**: adapter dedicato necessario per API v2 e streaming; non attivare prima dei test e della verifica economica. |
| Z.AI | Documentazione/prezzi pubblici riportano modelli Flash come GLM-4.7-Flash, GLM-4.5-Flash e GLM-4.6V-Flash a prezzo Free. | La documentazione pubblica non chiarisce in modo sufficiente durata della gratuità, limiti, requisiti carta/telefono/regione e policy dati applicabile all'API developer. Altri modelli GLM sono a pagamento. | **Candidato da verificare**: non abilitare finché tutti i punti account-level non sono confermati; allowlist solo dei modelli esplicitamente Free. |
| SenseNova Token Plan / SenseTime | FAQ ufficiale indica un piano beta pubblico Free a RMB 0 e 1.500 chiamate per modello ogni 5 ore; modelli e limiti speciali possono essere esclusi. | Offerta beta soggetta a cambiamento; disponibilità e condizioni per account fuori dalla Cina, carta, residenza, policy dati e blocco dopo quota non sono sufficientemente chiare. | **Candidato sperimentale**: utile per prove limitate, non inserirlo nel pool automatico prima di verifica geografica/contrattuale e test API. |
| SEA-LION / AI Singapore | Pagina ufficiale propone API gratuita per proof-of-concept; limite dichiarato 10 chiamate/minuto. API compatibile OpenAI secondo guida pubblica. | Il servizio è dichiarato per prototipazione, non produzione; i termini consentono uso dei contenuti per sviluppare/migliorare i servizi. Quota totale, carta, regioni e garanzia di continuità non sono definite nella pagina pubblica consultata. | **Candidato di laboratorio**: non usarlo come provider primario di chat; verificare termini e disponibilità prima di qualsiasi uso. |
| Synap / Linkrra | Pagina ufficiale dichiara 100.000 token/giorno, 50 richieste/giorno e 10 richieste/minuto per la free tier; l'API risponde 402 quando il piano gratuito non consente il modello/uso richiesto, invece di passare a pagamento. | Un solo plafond per account o indirizzo di rete; capacità gratuita aggregata può esaurirsi; extra e altri modelli richiedono crediti. Operatore e infrastruttura sono più piccoli rispetto ai provider principali; privacy, stabilità e trattamento dei dati richiedono verifica ulteriore. | **Candidato secondario**: interessante per il failover solo dopo verifica termini/privacy e test di stabilità; usare una chiave Free isolata e rifiutare ogni endpoint/modello che richieda crediti. |
| Novita AI — Apodex 1.1 Mini | La pagina prezzi mostra input e output a prezzo Free per Apodex 1.1 Mini. | Non è stata confermata dalla documentazione pubblica consultata una quota ricorrente, l'assenza di carta, il comportamento al superamento o le condizioni privacy del modello gratuito. | **Non ancora approvato**: prezzo $0 da solo non prova un piano API gratuito senza addebiti. |

### Provider e offerte esaminate ma non idonee o non confermate

| Provider / servizio | Risultato della verifica | Decisione |
|---|---|---|
| Mistral API | La documentazione aggiornata dichiara che Free mode non include API access; creare chiavi e usare API richiede billing activation e pay-as-you-go. | **Escluso** dal pool gratuito API. |
| GitHub Models | GitHub dichiara il servizio ritirato dal 30 luglio 2026; playground, catalogo e API non sono più disponibili. | **Escluso / servizio ritirato**. |
| Cerebras | Le offerte di prova trovate sono crediti una tantum (ad esempio $5 per 30 giorni) con metodo di pagamento verificato; non sono una quota gratuita permanente conforme al requisito. | **Escluso**. |
| NVIDIA Build / NIM | Le offerte gratuite incontrate sono endpoint/trial con limiti o crediti non equivalenti a un piano API gratuito stabile. Alcune pagine dichiarano logging di input/output e uso per migliorare i servizi. | **Non approvato** per la rotazione automatica; rivalutare solo con quota e termini espliciti no-card/no-billing. |
| SambaNova Cloud | Le informazioni pubbliche consultate sono discordanti sulle condizioni del piano Free e sul metodo di pagamento. La precedente verifica del flusso di attivazione aveva rilevato richiesta di metodo di pagamento/crediti. | **Escluso in via prudenziale** finché una verifica ufficiale aggiornata non dimostri il contrario; non riaggiungere al router. |
| SiliconFlow | Catalogo e pagine prezzi mostrano numerosi modelli a pagamento; non è stata trovata prova ufficiale sufficientemente completa di una quota API gratuita permanente, no-card e con hard-stop. | **In verifica**, non approvato. |
| DeepSeek API | La documentazione prezzi consultata descrive consumo a token e saldo/crediti; le menzioni di grant promozionali non dimostrano una quota gratuita ricorrente per ogni account. | **Non approvato** come provider gratuito permanente. |
| Hugging Face Inference Providers | Il piano gratuito non fornisce una quota inference API sufficientemente stabile per questa architettura; alcune opzioni dipendono da crediti/piani a pagamento. | **Non approvato**. |
| OpenAI API e Anthropic API | Non è emerso un piano API gratuito ricorrente adatto al requisito SuperIA; le offerte Chat/web non equivalgono a API gratuite. | **Esclusi**. |
| Together AI, Fireworks AI, Replicate, Nebius, Hyperbolic e altri host GPU/API | Le offerte emerse sono principalmente crediti iniziali, trial, prezzi pay-as-you-go o condizioni no-card/hard-stop non dimostrate. | **Non approvati** finché non viene provato un piano API gratuito ricorrente e senza possibilità di addebito. |

### Altri nomi da tenere nel radar, senza considerarli verificati

La ricerca ha inoltre intercettato OpenTyphoon, BroadNet/BroadRouter, SenseNova, provider regionali e aggregatori di modelli. Le directory di terze parti sono utili per scoprire nomi, ma non costituiscono prova di gratuità o di condizioni contrattuali. Per ogni nuovo nome occorre risalire a pricing, termini, privacy, rate limits e API docs del provider originale. Non si autorizza l'integrazione sulla base di directory o articoli comparativi.

### Fonti consultate

**Fonti ufficiali**
- Gemini pricing e uso dati: https://ai.google.dev/gemini-api/docs/pricing
- Groq rate limits: https://console.groq.com/docs/rate-limits
- Cloudflare Workers AI pricing: https://developers.cloudflare.com/workers-ai/platform/pricing/
- OpenRouter free models e limiti: https://openrouter.ai/collections/free-models/ e https://openrouter.ai/pricing
- Cohere v2 rate limits: https://docs.cohere.com/v2/docs/rate-limits
- Mistral usage and limits: https://docs.mistral.ai/admin/billing-usage/usage-limits/
- GitHub Models retirement: https://docs.github.com/en/github-models
- Novita AI pricing: https://novita.ai/pricing
- SEA-LION free API: https://sea-lion.ai/try-sea-lion/
- SenseTime FAQ: https://sensetime.com/en/faq
- SenseTime Token Plan docs: https://token.sensetime.com/docs
- SenseTime public-beta announcement: https://sensetime.com/en/news/sensenova-u1-5-lite-token-plan-20260911-1741
- Synap free tier e condizioni: https://synap.linkrra.com/free e https://synap.linkrra.com/terms.html
- SiliconFlow catalogo: https://www.siliconflow.com/models
- DeepSeek pricing: https://api-docs.deepseek.com/quick_start/pricing/

**Directory e verifiche comparative usate solo per scoprire candidati e individuare contraddizioni (non come prova contrattuale):**
- https://llmlatency.dev/free-llm-api
- https://pricepertoken.com/free-ai-api
- https://broadrouter.com/data/llm-api-free-tiers-and-rate-limits/
- https://mvalentsev.github.io/awesome-free-ai-coding/
- https://github.com/ai-gateways/awesome-free-ai-providers

### Graduatoria operativa aggiornata

1. **Cloudflare Workers AI e GroqCloud**: primi da verificare nell'account e mantenere come candidati prioritari.
2. **Gemini Free e OpenRouter :free**: tecnicamente già supportati dal router; possono essere valutati secondo la preferenza dell'utente sui dati, ma con modelli/endpoint rigorosamente gratuiti e hard-stop.
3. **Cohere Trial**: implementazione adapter e test dedicati; quota limitata, quindi utile come riserva.
4. **Z.AI e SenseNova**: potenziale quota interessante, ma prima servono verifiche account, regione, termini e stop economico.
5. **SEA-LION e Synap**: riserve sperimentali, con limiti/obiettivi di prototipazione o capacità meno garantita.
6. **Novita AI / Apodex**: non abilitare finché non si chiariscono quota, account e hard-stop.
7. **Mistral, GitHub Models, Cerebras e provider solo trial/crediti**: non inserirli nel pool gratuito permanente secondo le condizioni attualmente documentate.

**Regola finale:** nessun provider è attivo per effetto di questa ricognizione. Non sono stati creati account, API key, crediti, metodi di pagamento o servizi. Tutte le attestazioni runtime restano false fino alla verifica reale autorizzata. La priorità è aumentare il numero di provider realmente gratuiti senza introdurre neppure un rischio teorico di addebito.


## Cohere v2 adapter — implementation progress 27 November 2026

The backend router now has a Cohere-specific adapter path at `https://api.cohere.com/v2/chat`, using Bearer authentication. It normalizes the v2 non-streaming `message.content[]` text blocks and the streaming SSE `content-delta` event into SuperIA's existing response format. Cohere has been added to the router's provider order and the same five free-only attestations are mandatory: global free-only mode, operator approval of the Free plan, no payment method, hard stop, no fiat billing, and data-policy acknowledgement.

The adapter does **not** make Cohere automatically active. It only becomes eligible when an operator supplies a trial key and model ID in the backend secret environment and explicitly attests to all conditions. A 401/402/403 response persistently disables the provider; 429 triggers cooldown; there is no paid fallback. The API key remains backend-only.

Official Cohere docs confirm:
- v2 chat endpoint, Bearer auth, message format and streaming SSE content-delta: https://docs.cohere.com/v2/reference/chat-stream and https://docs.cohere.com/v2/docs/chat-api
- Evaluation/trial keys are free but limited; trial keys and production keys on newer model variants are limited to 1,000 calls/month. Trial Chat API rate limits are 20 requests/minute for the listed Command variants: https://docs.cohere.com/v2/docs/rate-limits

**Remaining activation gate:** confirm from the actual Cohere account that the key is an evaluation/trial key, no payment method is attached or required, the chosen model is included in the trial limits, and exhaustion hard-stops rather than switching to a paid key or plan. No key/account was created and no live request was made. Do not mark the Cohere provider attestation true until these checks are completed.


## Z.AI adapter and strict model allowlist — 27 November 2026

Official Z.AI pricing page currently lists exactly these text/vision API models with input, cached input and output all marked Free:
- `glm-4.7-flash`
- `glm-4.5-flash`
- `glm-4.6v-flash`

Source: https://docs.z.ai/guides/overview/pricing (pricing rows 84–85 and 96). The general OpenAI-compatible API endpoint is documented as `https://api.z.ai/api/paas/v4`; the chat endpoint is `/chat/completions`. The built-in Web Search tool is priced separately at $0.01/use, so SuperIA must never request it. API endpoint reference is corroborated by the provider's integration documentation; only use the general API endpoint with the three zero-price model IDs above, never Coding Plan endpoints or paid models.

Z.AI's official terms distinguish individual consumer content from API developer/end-user content: API End User Content is used to provide the API, comply with law, enforce policies and prevent abuse, and is not used to improve services unless the developer explicitly agrees. The user's general acceptance of model-improvement data use does not waive SuperIA's need to disclose provider terms. Source: https://chat.z.ai/legal-agreement/terms-of-service (Additional Terms for API Services).

**Implementation:** router now has a Z.AI adapter and a hard-coded allowlist for the three exact zero-price model IDs. Any paid/non-listed model (including GLM-5.3-Flash, GLM-4.7-FlashX and GLM-4.6V-FlashX) is rejected before any HTTP request. A 401/402/403 response persistently disables Z.AI; no paid fallback exists. The built-in Web Search and other paid tools are not invoked.

**Activation still blocked pending account verification:** public docs do not conclusively establish whether a payment method is required for every region/account, or expose public free-model rate limits. Before activation, verify the specific account can create a key without card/payment, that the selected model appears as $0 in that account, and that quota exhaustion returns an error rather than charging. No Z.AI account/key was created and no live request was made.


## Addendum verifica provider successiva — 27 novembre 2026

### Synap / Linkrra — adapter fail-closed aggiunto, attivazione ancora bloccata

La documentazione ufficiale del servizio pubblica una quota API gratuita senza carta per il solo modello `qwen3-coder-30b`:
- 100.000 token/giorno e 50 richieste/giorno nei primi 7 giorni; successivamente 50.000 token/giorno e 10 richieste/giorno.
- Limite di 10 richieste/minuto per chiave e massimo 4.096 token in output per richiesta.
- Quota condivisa per account/rete e ulteriore capacità gratuita aggregata; il servizio può rifiutare richieste se la capacità non è disponibile.
- Il sito account dichiara che la registrazione gratuita richiede Google sign-in e non richiede carta; oltre la quota Free, i modelli/crediti a pagamento richiedono un acquisto separato.
- API OpenAI-compatible: `https://synap402.com/v1/chat/completions`.
- Privacy policy: le richieste API con chiave non conservano prompt/completions e non sono usate per addestrare modelli; restano metadati d'uso (token, modello, timestamp). I nodi GPU sono gestiti anche da operatori indipendenti, che il provider dichiara vincolati a non conservare i contenuti.
- Termini: servizio distribuito senza SLA di uptime; l'operatore può cambiare limiti e condizioni. È vietato aggirare rate limit e quote.

**Implementazione su main:** aggiunto provider `synap` al router con allowlist hard-coded del solo `qwen3-coder-30b`, endpoint OpenAI-compatible, supporto ai formati chat e SSE standard. Un HTTP 402 disabilita persistentemente il provider (gestione già comune); per prudenza anche qualsiasi HTTP 429 disabilita persistentemente Synap, senza retry automatico e senza fallback a pagamento. Le attestazioni `APPROVED_FREE`, `NO_PAYMENT_METHOD`, `HARD_STOP`, `NO_FIAT_BILLING` e `DATA_POLICY_APPROVED` restano obbligatorie. Nessun account o API key è stato creato, nessun provider è attivo.

**Limiti della verifica:** la quota e la politica sono dichiarazioni del provider, non un test del singolo account o della regione. Prima dell'attivazione occorre verificare che l'account creato dall'utente sia effettivamente Free, senza carta/metodo di pagamento, e che il pannello mostri la quota corrente. La capacità condivisa e l'assenza di SLA rendono Synap una riserva secondaria, non il provider primario.

Fonti ufficiali:
- Free tier e quote: https://synap.linkrra.com/free.html
- Account e registrazione senza carta: https://synap.linkrra.com/account.html
- API e modello: https://synap402.com/v1 e https://synap.linkrra.com/
- Privacy API: https://synap.linkrra.com/privacy.html
- Termini e assenza di SLA: https://synap.linkrra.com/terms.html

### SenseNova Token Plan — documentazione aggiornata

La pagina ufficiale Token Plan indica piano Free durante la beta pubblica a $0/RMB 0, con 1.500 chiamate per modello ogni 5 ore per SenseNova 6.8 Flash Lite e SenseNova U1 Fast. La documentazione API corrente descrive l'endpoint chat OpenAI-compatible `https://token.sensetime.com/api/v1/chat/completions` e il model ID `sensenova-6.8-flash-lite`. Le autorizzazioni e le quote effettive sono comunque subordinate al workspace autenticato; i dettagli di streaming non sono definiti in modo sufficiente nelle pagine pubbliche consultate. Il vecchio endpoint `token.sensenova.cn/v1` e il modello `sensenova-u1.5-lite` descritti in un annuncio precedente riguardano soprattutto generazione/edizione immagini e non vanno confusi con l'endpoint chat corrente.

**Decisione:** nessun adapter chat aggiunto. Restano da verificare carta/metodo di pagamento, idoneità geografica per l'Italia, termini privacy/account e streaming. Non inserire nel pool finché questi aspetti non sono risolti.

Fonti ufficiali:
- Piano e quote: https://www.sensenova.ai/token-plan
- FAQ e modelli correnti: https://www.sensetime.com/en/faq
- Chat API: https://token.sensetime.com/docs/api-reference/endpoint/llm/chat
- Centro documentazione: https://token.sensetime.com/docs

### SEA-LION / AI Singapore — conferma POC, non produzione

Le pagine ufficiali confermano un'API gratuita per proof-of-concept, compatibile OpenAI, con limite di 10 richieste/minuto per utente e una sola chiave trial per utente. La guida richiede accesso al Playground tramite Google e specifica che il piano POC non è progettato per carichi di produzione. Non è stata trovata conferma sufficiente che ogni account/regione sia esente da carta o che esista un hard-stop documentato e verificabile su tutti gli errori di quota.

**Decisione:** mantenere come candidato di laboratorio, non integrarlo nel failover della chat privata finché no-card e comportamento di quota non siano confermati.

Fonti ufficiali:
- FAQ: https://github.com/aisingapore/sealion/blob/main/overview/faq.md
- API e rate limit: https://github.com/aisingapore/sealion/blob/main/guides/inferencing/api.md
- Pagina di accesso: https://sea-lion.ai/try-sea-lion/

### Stato sintetico dopo l'addendum

| Provider | Stato tecnico | Stato account/economico | Uso previsto |
|---|---|---|---|
| Synap | Adapter aggiunto; test CI da confermare | Free/no-card dichiarato dal provider; account non verificato | Riserva secondaria, solo dopo verifica account |
| SenseNova | Nessun adapter | Beta Free pubblica, ma no-card/Italia/streaming non risolti | In valutazione |
| SEA-LION | Nessun adapter | POC gratuito, no-card/hard-stop non risolti | Solo laboratorio |
| Cohere | Adapter aggiunto | Trial e no-card da verificare sull'account | Riserva condizionata |
| Z.AI | Adapter aggiunto | No-card e limiti account da verificare | Riserva condizionata |

Nessun provider è stato attivato. Non sono stati creati account, chiavi, carte, crediti o servizi esterni.


## Addendum provider SambaNova Cloud — 27 novembre 2026

### Esito verifica ufficiale: Free Tier senza metodo di pagamento

La documentazione ufficiale SambaNova distingue esplicitamente:
- **Free Tier:** applicato quando non è collegato alcun metodo di pagamento.
- **Developer Tier:** applicato quando viene collegato un metodo di pagamento.

Per i modelli di produzione pubblicati nella tabella Free Tier, i limiti sono **20 richieste/minuto, 20 richieste/giorno e 200.000 token/giorno per modello**. I tre modelli di produzione ammessi dal router sono:
- `DeepSeek-V3.1`
- `Meta-Llama-3.3-70B-Instruct`
- `gpt-oss-120b`

La documentazione separa i modelli Preview (DeepSeek-V3.2 e gemma-4-31B-it) e avverte che sono solo per valutazione/sperimentazione, con capacità limitata e possibile rimozione a breve termine: per questo **non** sono inclusi nell'allowlist di produzione.

API OpenAI-compatible:
- Endpoint: `https://api.sambanova.ai/v1/chat/completions`
- Autenticazione: Bearer API key.
- Streaming: formato SSE compatibile con Chat Completions.
- Le risposte API indicano l'utilizzo dei limiti tramite header dedicati; gli errori di quota sono espliciti.

**Privacy:** il Cloud End User License Agreement stabilisce che il contenuto del cliente viene elaborato per fornire il servizio o quando richiesto dalla legge; i query log e i dati d'uso non contenenti Customer Content possono essere utilizzati per amministrare, sviluppare, migliorare e gestire prodotti/servizi. Non è quindi corretto descrivere la policy come "nessuna raccolta dati"; la policy è compatibile con la preferenza espressa dall'utente, ma deve essere dichiarata chiaramente nell'app.

**Implementazione su main:** aggiunto adapter `sambanova` al router, endpoint OpenAI-compatible, allowlist hard-coded dei soli tre modelli di produzione gratuiti senza metodo di pagamento. Le attestazioni free-only rimangono obbligatorie. Un HTTP 402 disabilita il provider attraverso la protezione generale; qualsiasi HTTP 429 disabilita persistentemente il provider fino a verifica manuale, senza retry automatico e senza fallback a pagamento. Nessun provider è stato attivato e nessun account/chiave è stato creato.

**Limite operativo:** 20 richieste al giorno per modello è una quota molto contenuta. SambaNova è una riserva aggiuntiva, non un provider sufficiente da solo per una chat quotidiana intensa. Il router deve comunque mantenere gli altri provider gratuiti e interrompere le richieste quando tutti hanno esaurito le quote.

Fonti:
- Rate limits ufficiali: https://docs.sambanova.ai/docs/en/models/rate-limits
- Quickstart/dashboard ufficiale e endpoint: https://cloud.sambanova.ai/dashboard
- Cloud End User License Agreement e trattamento Customer Content / Service Usage Data: https://sambanova.ai/cloud-end-user-license-agreement


## Candidati esaminati ed esclusi — aggiornamento 27 novembre 2026

### Aion Labs — escluso (API key richiede carta)
La pagina pricing presenta un Free Tier a $0, ma la pagina ufficiale Rate Limits chiarisce che le API key Free si attivano solo dopo la verifica di una carta tramite Stripe (dichiarata non addebitata). I limiti gratuiti pubblicati sono 15 RPM e 20.000 token/giorno. La presenza obbligatoria della carta contrasta con i requisiti SuperIA; non integrare.
Fonti: https://www.aionlabs.ai/pricing/ ; https://www.aionlabs.ai/docs/rate-limits/

### Typhoon / OpenTyphoon — escluso dalla chat di produzione
L'API ufficiale offre accesso gratuito OpenAI-compatible e pubblica limiti di 5 richieste/secondo e 200/minuto per i modelli testuali. Tuttavia la documentazione definisce l'offerta come gratuita per uso leggero/prototipazione e indirizza il traffico di produzione all'infrastruttura a pagamento Together AI. I termini consentono inoltre l'uso degli Input per sviluppare e addestrare i modelli; la retention non è definita chiaramente. Non viene quindi aggiunto al router di produzione. Può restare un candidato per laboratorio isolato, previa valutazione privacy.
Fonti: https://docs.opentyphoon.ai/en/ ; https://docs.opentyphoon.ai/en/rate-limits/ ; https://docs.opentyphoon.ai/en/api-reference/ ; https://opentyphoon.ai/tac

### Regolo.ai (Italia) — escluso come quota ricorrente
È un'opzione italiana interessante per giurisdizione e zero data retention dichiarata, ma l'offerta gratuita è un solo periodo di 30 giorni e fino a 1 milione di token al giorno. Dopo la scadenza l'account passa al piano Pay-as-you-Go; il provider dichiara che non scatta un addebito automatico, ma l'uso successivo è a pagamento. Non soddisfa il requisito di gratuità permanente e hard-stop garantito senza spesa. Non integrare.
Fonti: https://regolo.ai/pricing/ ; https://regolo.ai/terms-and-conditions/

### Novita AI — escluso (crediti/referral, non free tier ricorrente)
La pagina prezzi ufficiale espone prezzi per token per i modelli LLM; la dicitura "Free to start" non equivale a modelli gratuiti a costo zero. Le offerte segnalate da terzi sono crediti referral, non una quota API gratuita rinnovabile. Non integrare.
Fonti: https://novita.ai/pricing ; https://blogs.novita.ai/free-llm-api-comparison-2026/

### LLM7.io — escluso (restrizioni sul downstream e limiti non garantiti)
I termini aggiornati del servizio descrivono un'offerta gratuita orientata a educazione/ricerca/sperimentazione, con quote modificabili senza preavviso e senza garanzie di disponibilità. Inoltre vietano di usare i normali limiti individuali per offrire accesso downstream o fare da gateway/proxy senza approvazione scritta preventiva. SuperIA è un'app che inoltra le richieste degli utenti: non usare LLM7 senza un'autorizzazione scritta specifica per questo scenario.
Fonti: https://github.com/chigwell/llm7.io/blob/main/TERMS.md

### NVIDIA NIM hosted Free Endpoints — escluso per ora
NVIDIA descrive gli endpoint hosted gratuiti come adatti a prototipazione entro limiti documentati, ma non abbiamo verificato limiti correnti, hard-stop di spesa e idoneità di utilizzo continuativo in produzione. Le licenze NIM scaricabili non equivalgono agli endpoint API hosted. Nessuna integrazione finché non è disponibile una prova account-level completa e no-card.
Fonti: https://docs.nvidia.com/nim/large-language-models/latest/about-nim-llm/nim-offerings.html ; https://forums.developer.nvidia.com/t/nim-question/364032

### Decisione
Non aggiungere provider solo perché una pagina li chiama "free". Il requisito di ammissione resta: quota API rinnovabile e senza scadenza, nessuna carta/metodo di pagamento, nessun credito acquistato o referral, hard-stop effettivo, modello identificato e autorizzazione compatibile con l'uso multiutente di SuperIA. Le offerte che non soddisfano ogni punto restano escluse o in laboratorio separato.


## Rettifica documentale: SambaNova e verifica SenseNova — 27 novembre 2026

Questa sezione prevale sulle precedenti righe di questo documento che indicavano SambaNova come escluso o non presente nel router: quelle righe erano diventate obsolete dopo la verifica ufficiale successiva e l'integrazione dell'adapter.

### SambaNova Cloud — stato corretto
- La documentazione ufficiale consultata per l'adapter conferma un Free Tier senza metodo di pagamento collegato; il Developer Tier è associato al collegamento di un metodo di pagamento.
- L'adapter è presente in `services/api/app/providers/free_router.py`, con allowlist limitata ai tre modelli di produzione `DeepSeek-V3.1`, `Meta-Llama-3.3-70B-Instruct`, `gpt-oss-120b`.
- Il router applica le attestazioni account-level free-only e il blocco persistente in caso di HTTP 402/429. Il provider non è attivo: non sono stati creati account o API key e non è stata eseguita alcuna chiamata reale.
- La suite backend sul commit `72f0249f4968e44ce0272b34a5c569fbb93335ae` ha superato **172 test** (1 warning). Il successivo commit documentale `2777188867f335c59c3ae0f6a6f2762593144548` ha completato con successo il workflow backend `46064312749`.
- Fonti ufficiali: https://docs.sambanova.ai/docs/en/models/rate-limits ; https://sambanova.ai/cloud-end-user-license-agreement.

### SenseNova / SenseTime — candidato ancora NON approvato
- La pagina ufficiale Token Plan descrive un piano Free durante la public beta a $0/mese e dichiara 1.500 chiamate per modello ogni 5 ore; la pagina consultata elenca SenseNova 6.7 Flash-Lite e SenseNova U1 Fast. I piani Lite/Pro sono indicati come in arrivo: https://www.sensenova.ai/token-plan.
- La FAQ ufficiale SenseTime consultata riporta la stessa quota e chiarisce che l'accesso richiede registrazione e creazione di API key tramite la piattaforma: https://sensetime.com/en/faq.
- Una comunicazione ufficiale successiva del settembre 2026 promuove inoltre U1.5 Lite con quota beta, mentre altri documenti tecnici pubblici descrivono endpoint/modelli di generazioni differenti. La discrepanza tra modello, regione e piattaforma va risolta prima di implementare un adapter: https://sensetime.com/en/news/sensenova-u1-5-lite-token-plan-20260911-1741.
- Non è stata trovata conferma ufficiale sufficiente che un account registrato dall'Italia possa attivare l'API senza carta/metodo di pagamento, né una garanzia documentata di hard-stop prima di ogni possibile tariffazione al termine della beta. La procedura pubblica può inoltre richiedere verifica dell'identità.
- Esito: **candidato sperimentale, non approvato e non integrato**. Nessuna registrazione, chiave o chiamata API effettuata. Non aggiungere al router finché non sono verificati in modo ufficiale accesso dall'Italia, assenza di carta, quota effettiva, blocco economico, termini e schema streaming del modello esatto.

### Regola di prevalenza
Le sezioni precedenti che classificano SambaNova come escluso per richiesta di carta/acquisto crediti sono superate dall'aggiornamento ufficiale e dall'adapter testato sopra. Il provider resta comunque disattivato fino alla verifica dell'account reale e all'autorizzazione dell'utente. La valutazione SenseNova resta prudenziale e non autorizza alcuna attivazione.


## Verifica SEA-LION aggiornata — 27 novembre 2026
- La pagina ufficiale distingue esplicitamente l'API gratuita per **POC/prototipazione** (limite dichiarato: 10 chiamate/minuto) dal percorso per **produzione**, indirizzato a cloud partner: https://sea-lion.ai/try-sea-lion/.
- La guida API ufficiale descrive una trial API key, ottenuta dopo accesso al Playground tramite Google, con una sola chiave per utente e limite di 10 RPM: https://docs.sea-lion.ai/guides/inferencing/api.
- L'annuncio ufficiale SEA-LION v5 ribadisce che le trial key sono rate-limited e **non destinate all'uso in produzione**; indica un partner di inferenza per la distribuzione produttiva: https://sea-lion.ai/blog/announcing-sea-lion-v5-our-largest-model-frontier-level-performance-and-open/.
- Non risulta quindi una quota API gratuita permanente autorizzata per un gateway multiutente come SuperIA. Il solo fatto che i pesi siano open/free non equivale a un endpoint hosted gratuito per produzione.
- Esito aggiornato: **escluso dal pool automatico di produzione**; eventualmente utilizzabile solo in un ambiente POC isolato e non con le conversazioni degli utenti. Nessun account, chiave o test API creato.
