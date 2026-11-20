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
