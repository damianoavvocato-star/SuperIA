# Audit dei fornitori AI gratuiti — SuperIA Privata

Data della verifica documentale: **20 novembre 2026**.

## Regola di ammissione

Un servizio entra nella rotazione di produzione solo se offre un'API realmente utilizzabile con una quota d'uso gratuita autentica, prezzo zero, nessuna carta/metodo di pagamento, nessun acquisto, ricarica in valuta (euro, dollari, dinari o altra moneta), deposito, addebito o passaggio automatico a pagamento. I crediti gratuiti interni al servizio (crediti, token, punti o unità di utilizzo non convertibili in denaro) sono ammessi e vanno consumati prima di passare al provider successivo. Deve fermarsi a quota esaurita e consentire l'uso previsto in Italia/UE. **Sono esclusi i servizi che richiedono o consentono ricariche/addebiti in valuta o che possono generare spese automatiche.** Non si escludono i crediti gratuiti interni non convertibili in denaro: si consumano fino a esaurimento e poi si passa al provider gratuito successivo. Non si usano account multipli o chiavi multiple per aggirare quote e termini.

**Stati:** CANDIDATO = piano gratuito documentato ma da collaudare; CONDIZIONATO = gratuito con restrizioni o modelli a pagamento da escludere; CREDITI GRATUITI = crediti interni non monetari utilizzabili fino a esaurimento; ESCLUSO = API a pagamento/non adatta; NON VERIFICATO = documentazione ufficiale insufficiente. Tutti gli stati diversi da CANDIDATO restano disabilitati.

## Verifica dei 75 nomi della lista iniziale

| N. | Servizio | Esito su prezzo, carta e billing | Decisione |
|---:|---|---|---|
| 1 | Google Gemini API / AI Studio | Piano Developer API Free senza collegamento a Cloud Billing; il piano Paid richiede billing. Nei piani Free i contenuti possono essere usati per migliorare i prodotti Google. | CANDIDATO, solo modelli/strumenti Free; privacy da valutare. |
| 2 | GroqCloud | Piano Free con limiti per modello. Il passaggio al Developer tier richiede metodo di pagamento e genera fatturazione. | CANDIDATO, solo account Free senza metodo di pagamento. |
| 3 | Cloudflare Workers AI | Workers Free include 10.000 Neurons/giorno; oltre la quota le richieste falliscono. Alcuni modelli richiedono metodo di pagamento o crediti prepagati. | CONDIZIONATO, allowlist di soli modelli Free. |
| 4 | Mistral La Plateforme | Free mode per Vibe/Studio; chiavi e uso API richiedono attivazione billing e pay-as-you-go. | ESCLUSO dall'API gratuita. |
| 5 | SambaNova Cloud | Uso gratuito dei modelli ospitati finché non viene collegato un metodo di pagamento. | CONDIZIONATO, mai collegare il metodo; verificare uso produzione. |
| 6 | NVIDIA NIM / Build | Accesso sviluppatori/prototipazione documentato; non è stata chiarita ufficialmente per tutti i modelli l'assenza di carta e billing. | NON VERIFICATO. |
| 7 | Cohere | Chiave Trial gratuita e limitata; chiavi Production e uso live hanno condizioni differenti. | CREDITI GRATUITI finché non viene confermata l'idoneità per produzione. |
| 8 | OpenRouter modelli :free | Modelli gratuiti senza acquisto crediti; piano Free con 50 richieste/giorno. Limiti più alti legati all'acquisto di crediti. | CANDIDATO, solo modelli :free, niente acquisto crediti. |
| 9 | Z.AI / Zhipu AI | Listino con prezzi per token; voci promozionali non dimostrano inferenza API gratuita ricorrente. | ESCLUSO per ora. |
| 10 | Ollama Cloud | Pay-as-you-go a pagamento per token; Pro/Max/Team a pagamento. | ESCLUSO dall'inferenza cloud gratuita. |
| 11 | OVHcloud AI Endpoints | Crediti/trial cloud temporanei; risorse cloud attivabili con billing. Nessuna quota API ricorrente senza billing verificata. | ESCLUSO dalla rotazione stabile. |
| 12 | Novita AI | Crediti Sandbox promozionali iniziali senza carta; non è una quota API ricorrente. | SOLO PROVA. |
| 13 | SenseNova Token Plan | Piano gratuito riportato da directory secondarie; carta, overage e uso commerciale non verificati su documentazione ufficiale. | NON VERIFICATO. |
| 14 | Synap / Linkrra | Nessuna documentazione ufficiale sufficiente su API gratuita, carta e billing. | NON VERIFICATO. |
| 15 | Liquid AI | Modelli/pesi open source non equivalgono a endpoint cloud gratuito del fornitore. | ESCLUSO come API cloud; modelli valutabili separatamente. |
| 16 | Dots Studio | API gratuita e condizioni di billing non verificate. | NON VERIFICATO. |
| 17 | Braintrust | Gateway/strumenti di osservabilità; non rende gratuita l'inferenza del provider sottostante. | ESCLUSO come provider di modelli. |
| 18 | Alibaba Cloud DashScope / Bailian | Possibili quote per modelli/prodotti specifici; quota ricorrente senza carta e senza billing non verificata. | NON VERIFICATO. |
| 19 | ModelScope | Hub di modelli e demo; API pubblica gratuita per uso app e condizioni non verificate. | NON VERIFICATO. |
| 20 | SiliconFlow | Termini ufficiali presenti, ma nessuna quota API ricorrente senza carta e senza addebiti verificata. | NON VERIFICATO. |
| 21 | Requesty | Gateway multi-provider; il gateway non rende gratuiti i modelli sottostanti. | ESCLUSO come fonte autonoma gratuita. |
| 22 | Kilo Gateway | Modelli gratuiti riportati in alcuni flussi; endpoint gateway, limiti e billing per uso app non verificati. | NON VERIFICATO. |
| 23 | Nous Portal | Quota ricorrente, carta, overage e uso commerciale non verificati. | NON VERIFICATO. |
| 24 | AIHubMix | Gateway multi-modello; quota gratuita con hard-stop a zero costi non verificata. | NON VERIFICATO. |
| 25 | DeepSeek API | Nessun piano API gratuito ricorrente verificato; API a consumo. | ESCLUSO. |
| 26 | Speka | Identità e documentazione ufficiale API gratuita non sufficientemente verificate. | NON VERIFICATO. |
| 27 | InferX | API, piano gratuito e billing non verificati. | NON VERIFICATO. |
| 28 | SCNet | API, piano gratuito e billing non verificati. | NON VERIFICATO. |
| 29 | Aion Labs | Presenza in directory non sufficiente; policy ufficiale API non verificata. | NON VERIFICATO. |
| 30 | LLM7.io | Endpoint gratuito riportato da directory; termini, privacy, limiti e uso commerciale non verificati ufficialmente. | NON VERIFICATO. |
| 31 | Typhoon | Quota API gratuita ricorrente e condizioni di billing non verificate. | NON VERIFICATO. |
| 32 | Vercel AI Gateway | Gateway per modelli terzi; costo inferenza dipende dal provider/modello. | ESCLUSO come provider autonomo gratuito. |
| 33 | Kilo Code | Strumento di coding; eventuali modelli interni gratuiti non sono automaticamente API pubblica riutilizzabile. | NON VERIFICATO come API; non conteggiare separatamente. |
| 34 | OpenCode Zen | Gateway/catalogo; quota inferenza gratuita permanente senza billing non confermata. | NON VERIFICATO. |
| 35 | Nous Research | Modelli/pesi aperti non equivalgono a endpoint cloud gratuito gestito. | ESCLUSO come API cloud per ora. |
| 36 | DeepInfra | Inferenza a consumo; nessuna quota gratuita ricorrente verificata. | ESCLUSO. |
| 37 | Together AI | Crediti promozionali non equivalgono a piano gratuito ricorrente. | SOLO PROVA, non produzione. |
| 38 | Fireworks AI | Crediti iniziali non equivalgono a quota gratuita ricorrente; uso a pagamento oltre i crediti. | SOLO PROVA, non produzione. |
| 39 | Cerebras | Trial di durata limitata con metodo di pagamento richiesto. | ESCLUSO per vincolo no-card/no-charge. |
| 40 | Hyperbolic | Crediti promozionali non dimostrano quota API ricorrente senza carta. | NON VERIFICATO. |
| 41 | Baseten | Deployment/inferenza a consumo; nessun piano gratuito ricorrente verificato. | ESCLUSO. |
| 42 | Replicate | API a consumo; nessuna quota gratuita ricorrente verificata. | ESCLUSO. |
| 43 | Modal | Crediti/compute per sviluppatori non costituiscono API LLM gratuita permanente. | SOLO PROVA per test. |
| 44 | AMD Developer Cloud | Risorse/programmi sviluppatori; API inferenza gratuita pubblica non verificata. | NON VERIFICATO. |
| 45 | Kaggle | Notebook e compute con quote; non endpoint API gestito per utenti app. | ESCLUSO dalla rotazione API. |
| 46 | Google Colab | Notebook/compute gratuito limitato; non servizio API di inferenza produzione. | ESCLUSO dalla rotazione API. |
| 47 | Hugging Face Inference Providers | Utenti Free senza crediti mensili; inferenza richiede crediti acquistati o piano superiore. | ESCLUSO come API gratuita. |
| 48 | Kimi / Moonshot AI | Nessuna quota API gratuita ricorrente verificata; prezzi a consumo/promozioni. | ESCLUSO per ora. |
| 49 | ModelScope (duplicato) | Stessa valutazione n. 19; non è capacità aggiuntiva. | NON VERIFICATO. |
| 50 | Novita AI (duplicato) | Stessa valutazione n. 12; crediti promozionali non ricorrenti. | SOLO PROVA. |
| 51 | GitHub Copilot Free | Piano gratuito per coding, non endpoint API per chat SuperIA. | ESCLUSO dalla rotazione chat. |
| 52 | Cursor Hobby | IDE/assistente coding; non endpoint API SuperIA. | ESCLUSO. |
| 53 | Windsurf / Devin Free | IDE/agent coding; non API di inferenza per app. | ESCLUSO. |
| 54 | Devin Free | Agente sviluppo; non endpoint general-purpose per SuperIA. | ESCLUSO. |
| 55 | Amazon Kiro | IDE/strumento sviluppo; non API inferenza gratuita per SuperIA. | ESCLUSO. |
| 56 | Trae | IDE AI; non provider API pubblico per chat. | ESCLUSO. |
| 57 | Bolt.new | Crediti/limiti interni del builder: ammessi per le funzioni integrate se gratuiti e senza addebiti; non è un endpoint chat generico. | NON PROVIDER CHAT; non escluso come strumento. |
| 58 | Lovable | Crediti/limiti interni del builder: ammessi per usare le funzioni Lovable integrate, se disponibili senza pagamenti; non è un endpoint chat generico. | NON PROVIDER CHAT; non escluso come strumento. |
| 59 | Vercel v0 | Generatore UI/app; non endpoint chat gratuito. | ESCLUSO. |
| 60 | Replit Starter | Ambiente sviluppo con quote proprie; non API general-purpose. | ESCLUSO. |
| 61 | Qoder | IDE coding; non API inferenza gratuita per SuperIA. | ESCLUSO. |
| 62 | Antigravity CLI | Strumento sviluppo; non endpoint inferenza gratuito. | ESCLUSO. |
| 63 | Canva Free | Applicazione grafica; non API inferenza per SuperIA. | ESCLUSO. |
| 64 | Figma Starter | Strumento progettazione; non API inferenza per SuperIA. | ESCLUSO. |
| 65 | Framer Free | Website builder; non API inferenza per SuperIA. | ESCLUSO. |
| 66 | Deepgram | $200 credito iniziale senza carta e senza scadenza dichiarata; poi pay-as-you-go. Non è quota ricorrente. | SOLO PROVA, hard-stop a credito zero, nessuna carta. |
| 67 | AssemblyAI | Trial fino a $50 senza carta; a credito esaurito le chiamate falliscono finché non si effettua upgrade. Autopay da non attivare. | SOLO PROVA, nessuna carta/upgrade, stop a zero. |
| 68 | Google Cloud Speech-to-Text | Quote e condizioni dipendono dal prodotto/periodo; modalità stabile senza billing account non verificata. | NON VERIFICATO; non confondere con Gemini AI Studio. |
| 69 | Groq Whisper | Modello audio dello stesso account Groq n. 2; usa il medesimo piano/quote account. | CANDIDATO come funzione audio di Groq, non provider aggiuntivo. |
| 70 | Cloudflare audio | Modello audio nello stesso budget Workers AI n. 3; consuma le stesse Neurons giornaliere. | CONDIZIONATO, non capacità aggiuntiva. |
| 71 | Hugging Face Spaces | Demo community con disponibilità/risorse non garantite; non API produzione gratuita. | ESCLUSO dalla rotazione produzione. |
| 72 | Kaggle (duplicato) | Stessa valutazione n. 45. | ESCLUSO. |
| 73 | ModelScope (duplicato) | Stessa valutazione n. 19. | NON VERIFICATO. |
| 74 | Liquid AI (duplicato) | Stessa valutazione n. 15. | ESCLUSO come API cloud. |
| 75 | NVIDIA NIM (duplicato) | Stessa valutazione n. 6. | NON VERIFICATO. |

## Nuovi candidati audio aggiunti

| Servizio | Verifica | Decisione |
|---|---|---|
| Cartesia | Piano Free con 20.000 crediti/mese TTS/STT; la pagina prezzi indica licenza per uso commerciale nel piano Pro, non nel Free. | NON ABILITARE in produzione finché i termini Free non autorizzano l'uso previsto. |
| Scaleway Generative APIs | Free Tier di 1.000.000 token e 60 minuti audio; l'eccedenza viene fatturata e il servizio è gratuito durante beta. | ESCLUSO finché non esiste hard-stop senza billing. |
| Gladia | 50 € crediti una tantum; wallet prepagato e funzione auto-top-up. | SOLO PROVA; nessuna carta e auto-top-up disattivato. |
| Speechmatics | Listino documenta pacchetti acquistati e pay-as-you-go con carta; nessuna quota ricorrente gratuita verificata. | ESCLUSO. |

## Prima allowlist da sottoporre a collaudo finale

1. Gemini API: solo piano Free; revisione privacy obbligatoria.
2. Groq: piano Free senza metodo di pagamento.
3. Cloudflare Workers AI: solo modelli Free che non richiedono billing.
4. OpenRouter: solo modelli :free e senza acquisto crediti.
5. SambaNova: account senza metodo di pagamento; verificare produzione.
6. NVIDIA NIM: solo dopo prova documentata no-card/no-billing.
7. Cohere: solo test finché non viene confermata idoneità produzione.
8. Cartesia: solo se il piano Free autorizza l'uso previsto.
9. Deepgram e AssemblyAI: esclusi perché l'offerta si basa su crediti monetari iniziali.

## Regole obbligatorie nel codice

- Provider attivo solo con crediti/quote gratuiti interni non convertibili in valuta, nessuna carta, nessun addebito/ricarica in valuta, hard-stop a zero e termini approvati.
- Non salvare metodi di pagamento; non fare top-up, upgrade, depositi o acquisti in valuta. I crediti gratuiti interni sono invece ammessi e vanno consumati.
- Non chiamare modelli/endpoint a pagamento.
- Escludere i provider che richiedono ricariche o addebiti espressi in euro, dollari, dinari o altra valuta; non escludere i crediti gratuiti interni non monetari.
- Stato non verificato = provider disabilitato.
- 402, errori billing, quota esaurita o richiesta upgrade = stop; mai tentare pagamenti.
- Rispettare rate limit e condizioni; non aggirare quote con account multipli.
- Non inviare documenti/dati personali finché privacy, training, retention e localizzazione non sono approvati.
- Hosting, database, storage, traffico e dominio devono anch'essi avere costi zero verificati: API gratuita non significa applicazione interamente gratuita.

## Fonti ufficiali consultate

- [Gemini API pricing](https://ai.google.dev/gemini-api/docs/pricing)
- [Firebase AI Logic — requisiti billing Gemini Developer API](https://firebase.google.com/docs/ai-logic/pricing)
- [Groq billing FAQ](https://console.groq.com/docs/billing-faqs)
- [Groq rate limits](https://console.groq.com/docs/rate-limits)
- [Cloudflare Workers AI pricing](https://developers.cloudflare.com/workers-ai/platform/pricing/)
- [Mistral usage and limits](https://docs.mistral.ai/admin/billing-usage/usage-limits)
- [Cohere going live](https://docs.cohere.com/docs/going-live)
- [Cohere rate limits](https://docs.cohere.com/v2/docs/rate-limits)
- [OpenRouter free models](https://openrouter.ai/collections/free-models/)
- [OpenRouter pricing](https://openrouter.ai/pricing)
- [Hugging Face Inference Providers pricing](https://huggingface.co/docs/inference-providers/main/pricing)
- [Ollama Cloud pricing](https://ollama.com/pricing)
- [Novita quickstart](https://docs.novita.ai/guides/quickstart)
- [OVHcloud trial project](https://docs.ovhcloud.com/en/guides/public-cloud/cross-functional/create-a-public-cloud-project)
- [Deepgram pricing](https://deepgram.com/pricing)
- [AssemblyAI free trial FAQ](https://support.assemblyai.com/articles/5370767329-can-i-sign-up-for-free)
- [Cartesia pricing](https://www.cartesia.ai/pricing)
- [Scaleway Generative APIs FAQ](https://www.scaleway.com/en/docs/generative-apis/faq/)
- [Gladia pricing](https://www.gladia.io/pricing)
- [Speechmatics pricing](https://www.speechmatics.com/pricing)

Le condizioni cambiano: verificare di nuovo prima di ogni rilascio e durante l'esercizio.
