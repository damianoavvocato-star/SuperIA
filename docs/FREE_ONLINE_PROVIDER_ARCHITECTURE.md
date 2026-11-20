# Architettura online gratuita e continuità chat

## Decisione di prodotto

SuperIA deve presentarsi come un solo assistente e una sola conversazione persistente. La selezione e la rotazione dei provider sono responsabilità del backend e non devono richiedere all'utente di scegliere modelli o aprire una nuova chat.

**Vincolo economico inderogabile:** nessun provider a pagamento, nessuna API a consumo, nessun piano premium e nessun fallback che possa generare addebiti. Il backend deve usare esclusivamente endpoint e quote esplicitamente gratuiti. Non devono comparire pulsanti, notifiche o richieste di pagamento. Se tutte le quote gratuite sono esaurite o un servizio è indisponibile, la richiesta resta in coda o viene sospesa fino al ripristino della disponibilità; non si passa mai a un servizio a pagamento.

Questa architettura non può promettere inferenza online illimitata e sempre disponibile: i provider gratuiti applicano quote, rate limit, condizioni e possono modificarli o ritirare il piano gratuito. Anche hosting, database, traffico, storage e monitoraggio del backend devono avere un piano gratuito sufficiente oppure essere ospitati su infrastruttura già disponibile senza costi. Non attivare risorse con fatturazione automatica.

## Esperienza utente

- Un'unica chat con identificativo stabile e cronologia indipendente dai provider.
- Nessuna scelta obbligatoria del modello e nessuna schermata di configurazione API.
- Il cambio provider è trasparente quando la richiesta può essere ripetuta in sicurezza.
- Nessun messaggio che inviti a pagare o passare a un piano premium.
- Se nessun provider gratuito è disponibile, mostrare solo un messaggio neutro: «Il servizio AI è temporaneamente occupato. La conversazione è salvata; riproveremo appena sarà disponibile.»
- Non fingere che la risposta sia stata generata e non perdere il testo dell'utente.

## Architettura

Android app -> backend SuperIA -> orchestratore provider -> provider gratuiti approvati.

Il backend, non il telefono, custodisce le chiavi API. Le chiavi non vanno mai inserite nell'APK, nel repository, nei log o nelle risposte al client.

Componenti backend:
1. **Conversation Store**: messaggi, identificativo conversazione, timestamp, stato turno e versione del contesto.
2. **Context Builder**: prepara il contesto dalla cronologia SuperIA, dalla memoria e dai documenti autorizzati; applica il limite di contesto del provider scelto senza cancellare la cronologia originale.
3. **Provider Registry**: allowlist dei provider/modelli verificati come gratuiti, con modalità, limiti, licenza/termini, supporto streaming, contesto e data ultima verifica.
4. **Quota Manager**: tiene traccia di rate limit, quote note, errori 429/503, cooldown e finestre di rinnovo. Non deve stimare quote sconosciute come disponibilità garantita.
5. **Router**: sceglie il provider gratuito disponibile secondo priorità e capacità richieste.
6. **Turn Manager**: assegna un ID univoco a ogni richiesta, conserva lo stato e impedisce che un retry produca due risposte definitive.
7. **Audit e privacy**: registra solo metadati tecnici minimali; non registra prompt o documenti in chiaro nei log.

## Rotazione e sicurezza dei retry

- Tentare un solo provider alla volta.
- Passare al successivo per errori transitori documentati (rate limit, quota esaurita, indisponibilità, timeout prima che la generazione sia iniziata).
- Rispettare Retry-After e cooldown; evitare cicli rapidi e richieste duplicate.
- Se la connessione cade dopo che il provider potrebbe aver generato la risposta, verificare lo stato del turno prima di riprovare. Non inviare nuovamente in modo cieco richieste non idempotenti.
- Salvare la risposta definitiva una sola volta; lo streaming provvisorio non deve duplicare messaggi.
- Se il provider successivo ha un contesto più piccolo, riassumere/compattare il contesto per il solo prompt, mantenendo intatta la cronologia completa nell'archivio SuperIA.
- Se la richiesta richiede una capacità non disponibile negli altri provider (immagine, audio, tool call, JSON strutturato), non degradare silenziosamente: dichiarare che quella funzione è temporaneamente non disponibile.

## Continuità conversazionale

La conversazione non appartiene a Gemini, Groq, Cloudflare o ad altro provider: appartiene a SuperIA.

Per ogni turno:
- conservare messaggio utente, risposta, stato, provider usato e ID turno;
- ricostruire il contesto dai messaggi persistenti, non da una sessione volatile del provider;
- mantenere memoria e documenti nel database SuperIA con cifratura e autorizzazioni;
- supportare conversazioni molto lunghe con riepiloghi progressivi versionati e recupero dei messaggi originali;
- usare gli stessi ID conversazione e turno durante il fallback;
- permettere la ripresa dopo chiusura dell'app o perdita di rete.

Il cambio di provider può produrre differenze di stile o qualità, e il nuovo modello non eredita la memoria interna del precedente: riceve solo il contesto ricostruito da SuperIA.

## Regole di costo non aggirabili

- Lista consentita esclusivamente per provider e modelli con quota gratuita verificata.
- Disabilitare billing, auto top-up, upgrade automatici e modelli a pagamento ove la piattaforma lo consenta.
- Non configurare mai una carta di pagamento per attivare un provider gratuito.
- Non effettuare fallback verso modelli tariffati, anche se gratuiti non disponibili.
- Nessuna richiesta al provider deve essere eseguita se il costo non è noto o se il piano gratuito non è verificabile.
- Budget massimo monetario configurato a zero; il backend deve rifiutare ogni provider che non possa garantire la modalità gratuita.
- Alert interni agli amministratori per quote/termini cambiati; nessuna promozione o richiesta di pagamento all'utente.
- Verificare anche i costi indiretti: hosting backend, database, storage documenti, egress, domini e monitoraggio. Non dichiarare il prodotto totalmente gratuito finché questi elementi non hanno una copertura gratuita verificata.

## Privacy e consenso

Il passaggio tra provider è trasparente nell'interfaccia, ma non deve essere nascosto il fatto che i dati possono essere trattati da fornitori esterni. Prima del primo utilizzo cloud, presentare un'informativa chiara e un consenso esplicito; offrire controlli per non inviare documenti o dati sensibili. La trasparenza sulla privacy non va rimossa per rendere invisibile il cambio del provider.

Non inviare automaticamente interi archivi o documenti: trasmettere solo i frammenti strettamente necessari e autorizzati. Applicare le condizioni e le policy del singolo provider.

## Provider candidati

GroqCloud, Gemini API, Cloudflare Workers AI, OpenRouter e altri provider sono soltanto candidati. Nessuno è automaticamente approvato: prima dell'integrazione verificare per ciascun modello:
- piano gratuito effettivo e disponibilità per il paese/account;
- quote, rate limit, rinnovo e limiti di contesto;
- termini su prompt/output, conservazione e training;
- compatibilità API, streaming, strumenti e modalità richieste;
- necessità di carta o attivazione billing;
- costi indiretti e restrizioni di utilizzo.

Rimuovere immediatamente dalla allowlist ogni endpoint che richieda pagamento, carta, credito prepagato o piano tariffato.

## Piano di implementazione e gate

1. Audit del prototipo FastAPI e scelta di hosting realmente gratuito senza billing automatico.
2. Contratti comuni provider, registry e allowlist gratuita.
3. Quota Manager, cooldown e router con test simulati 429/503/timeout.
4. Conversation Store e Turn Manager idempotente con test di retry e riavvio.
5. Context Builder con memoria e documenti, mantenendo la cronologia completa.
6. API Android streaming e ripresa dello stesso turno.
7. Test end-to-end con provider gratuiti reali e controllo dei costi pari a zero.
8. Revisione privacy, segreti, rate limiting anti-abuso e test di carico.

Non dichiarare completata la funzione finché i test reali dimostrano fallback, continuità della stessa chat, ripresa dopo interruzione e assenza di addebiti. Non sostituire il motore Android funzionante prima che il percorso cloud sia collaudato.


## Catalogo esteso dei fornitori gratuiti (20 novembre 2026)

Il catalogo deve essere **estensibile**: aggiungere ogni fornitore legittimo che offra un endpoint API utilizzabile senza addebito ricorrente, senza confondere un piano gratuito continuativo con una prova temporanea, crediti promozionali o accesso solo al playground. L'elenco seguente è una lista iniziale di candidati da verificare prima dell'attivazione; non è una garanzia di disponibilità o di idoneità commerciale.

### Candidati con tier gratuito continuativo o modalità gratuita documentata

| Fornitore | Tipo di accesso gratuito rilevato | Trattamento nel router |
|---|---|---|
| Google Gemini API / AI Studio | Modelli selezionati con quote e limiti per account/modello | Candidato prioritario; verificare termini EEA, privacy e modello gratuito effettivo |
| GroqCloud | Piano Free con rate limit per modello | Candidato prioritario; rimanere sul piano Free e non attivare Developer/billing |
| Cloudflare Workers AI | Quota gratuita giornaliera in Neurons | Candidato; monitorare consumo condiviso e costi di Workers/hosting |
| Mistral La Plateforme | Modalità gratuita per test/prototipazione con limiti visibili in console | Candidato condizionato; controllare termini e uso consentito prima di produzione |
| SambaNova Cloud | Accesso gratuito limitato per modello/account secondo il tier corrente | Candidato condizionato; mai collegare metodo di pagamento |
| NVIDIA NIM / Build | Catalogo per sviluppatori e quote/limiti variabili | Candidato condizionato; verificare se l'endpoint API selezionato è gratuito e senza carta |
| Cohere | Chiavi trial con numero limitato di chiamate e restrizioni d'uso | Solo ambiente di prova finché non è verificato un tier continuativo e idoneo al prodotto |
| OpenRouter modelli `:free` | Modelli gratuiti con limiti aggregati di richieste e disponibilità variabile | Candidato condizionato; filtrare esclusivamente modelli a costo zero e provider sottostanti compatibili con privacy |
| Vercel AI Gateway | Eventuali crediti/quote gratuite dipendenti dal piano e dalle condizioni correnti | Non attivare finché non è dimostrato costo zero ricorrente senza billing |
| LLM7.io e altri endpoint comunitari | Offerte gratuite pubbliche che possono cambiare rapidamente | Sperimentale; isolamento, privacy e stabilità da verificare prima di qualunque uso reale |

### Accessi che NON vanno contati come capacità gratuita permanente

- Crediti una tantum di prova o promozionali (ad esempio trial a tempo o credito iniziale): non sono quota gratuita ricorrente e non entrano nel pool di produzione.
- Hugging Face Inference Providers quando l'account dispone soltanto di crediti mensili limitati: non considerarlo capacità illimitata; verificare il piano gratuito effettivo.
- Cerebras, Fireworks, Together AI, DeepInfra, Amazon Bedrock e altri servizi con crediti promozionali o tariffazione dopo il trial: esclusi dal router gratuito, salvo futura introduzione di un vero tier continuativo senza carta e senza billing.
- GitHub Models: non considerarlo provider API disponibile; la disponibilità del servizio può essere ritirata o cambiare. La connessione GitHub per repository, issue e workflow è una funzione distinta dall'inferenza AI.
- Non usare account personali o credenziali condivise, account multipli creati per eludere le quote, proxy o rotazione di chiavi per aggirare i termini. La rotazione deve rispettare le quote permesse da ciascun fornitore.

### Verifica obbligatoria e manutenzione del catalogo

Per ogni provider e modello registrare:
- URL ufficiale di pricing, rate limits, terms, privacy e acceptable use;
- data/ora dell'ultima verifica, paese/area geografica e requisiti account;
- se serve carta, se il billing può attivarsi automaticamente e come disabilitarlo;
- limiti per minuto/giorno/mese, token, concorrenza, contesto e modalità;
- uso commerciale/produzione consentito o vietato;
- conservazione, uso per training, revisione umana e localizzazione dei dati;
- supporto effettivo per italiano, streaming, tool calling, JSON, immagini, audio e file;
- stato: `approved-free`, `trial-only`, `needs-review`, `disabled`.

Un controllo automatico periodico deve segnalare modifiche; la verifica delle condizioni ufficiali va ripetuta prima di ogni release. Se la pagina ufficiale è ambigua o richiede carta/billing per la specifica API, lo stato resta `needs-review` o `disabled`. Mai dedurre la gratuità da un articolo di terzi soltanto.

### GitHub e servizi esterni scelti dall'utente

La connessione GitHub è opzionale e serve a collegare repository, issue, pull request e workflow; non costituisce un motore AI gratuito né una fonte di quote AI. SuperIA resta gratuita e utilizzabile senza GitHub.

Se in futuro l'utente sceglie volontariamente di collegare un proprio account o una propria chiave API di un servizio esterno, tale integrazione è separata e facoltativa:
- SuperIA non vende, attiva o fattura abbonamenti esterni;
- nessun abbonamento esterno è richiesto per le funzioni native gratuite di SuperIA;
- non utilizzare mai una credenziale personale per generare costi senza consenso esplicito e una chiara indicazione che il servizio è esterno;
- la modalità predefinita rimane `free-only`, con budget monetario pari a zero;
- l'utente può scollegare l'integrazione e continuare a usare tutte le funzioni SuperIA che non dipendono da quel servizio;
- non mostrare paywall, upsell o richieste di pagamento SuperIA.

### Principio di rotazione equa

La rotazione non deve essere usata per aggirare rate limits, quote giornaliere, limiti per account o termini dei provider. Ogni fornitore ha il proprio account e la propria quota legittima. Quando una quota è esaurita, marcarla in cooldown fino al rinnovo e provare un altro fornitore soltanto se il suo uso è consentito. Se tutte le quote sono esaurite, conservare la richiesta e riprenderla in seguito, senza passare a endpoint a pagamento.
