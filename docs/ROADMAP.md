> **Avviso di prevalenza — 23 novembre 2026:** le sezioni storiche di questo documento che propongono inferenza LLM locale, Ollama, llama.cpp, Gemma o LiteRT-LM non descrivono l'architettura corrente e non autorizzano implementazioni. La decisione vigente per SuperIA è inferenza AI esclusivamente online tramite backend HTTPS SuperIA e provider gratuiti approvati. Elaborazioni locali non-LLM (archivio, OCR, indicizzazione e gestione file) restano valutabili separatamente.

# Roadmap generale — SuperIA

## Aggiornamento vincolante — 21 novembre 2026

Questo aggiornamento prevale sulle note storiche più vecchie presenti nel documento.

- La direzione attuale è online-only: inferenza AI tramite backend SuperIA HTTPS. Gemma/LiteRT-LM e il runtime LLM locale sono stati rimossi dal progetto Android; le vecchie sezioni che descrivono inferenza locale sono archivio storico, non attività correnti.
- La chat Android, il pairing individuale, la cronologia cifrata, la memoria di progetto, la preparazione del contesto e il router backend sono incrementi presenti nel codice, ma non equivalgono a un servizio online pronto.
- Il backend non è pubblicato, non è collegato a un database di produzione e non ha provider reali attivati. Restano da completare hosting gratuito verificato, database persistente, rate limiting reale al gateway/WAF, provider approvati, streaming, failover, gestione quote e collaudo end-to-end.
- Il workflow Android corrente è solo test unitari (gradle --no-daemon testDebugUnitTest). Non deve assemblare, firmare o caricare APK.
- Blocco APK: non generare APK intermedi. Una singola build APK definitiva sarà consentita soltanto dopo che tutte le funzioni necessarie alla chat online saranno implementate e verificate end-to-end.
- Per lo stato operativo e i criteri aggiornati consultare docs/PHASE_TRACKER.md, docs/PHASE2_STATUS.md e docs/ONLINE_ONLY_MIGRATION_PLAN.md.

Le voci storiche successive in questo documento possono contenere esiti di build e artefatti APK prodotti prima del blocco. Sono registrazioni del passato e non autorizzano nuove build.

## Obiettivo e decisioni vincolanti

Costruire un'unica piattaforma Android personale e non commerciale che riunisca, in un'interfaccia sola, il maggior numero possibile di capacità delle 182 IA e di nuove funzioni richieste dall'utente. Le capacità si acquisiscono tramite componenti open source compatibili, replica funzionale indipendente e connettori/API ufficiali. L'uso personale non annulla automaticamente copyright, licenze o termini dei servizi: non si copiano codice, pesi, asset o servizi proprietari senza autorizzazione. Il processo è definito in [docs/FUNCTION_REPLICATION_AND_INTEGRATION.md](FUNCTION_REPLICATION_AND_INTEGRATION.md).

Decisioni da mantenere in tutte le fasi:
- Uso personale e riservato; progetto completamente separato da **Editore Foto e Video IA** (repository, codice, dati e configurazioni distinti).
- Gratuità come requisito: nessun abbonamento o servizio/API a pagamento obbligatorio. Eventuali servizi esterni devono essere facoltativi e disattivati per impostazione predefinita.
- Inferenza AI esclusivamente online tramite backend SuperIA HTTPS; elaborazioni locali non-LLM (archivio, OCR e gestione file) possono restare sul dispositivo. Le funzioni che richiedono Internet devono dichiararlo.
- Destinazione esclusiva Android (smartphone e tablet); non sviluppare client PC. Moduli attivabili progressivamente.
- Priorità alla privacy, controllo dell'utente e approvazione prima di operazioni distruttive o azioni esterne.
- Ogni modello/strumento deve essere verificato per licenza, requisiti hardware, compatibilità e possibilità di uso commerciale/personale.
- Nessuna fase è dichiarata completata solo perché il codice è stato caricato: servono test pertinenti, verifica pratica e documentazione dei limiti.

## Decisione Android-only

Il prodotto client è esclusivamente Android. Lo stack, la strategia prestazioni, i moduli e i gate sono descritti in [docs/ANDROID_ONLY_ARCHITECTURE.md](ANDROID_ONLY_ARCHITECTURE.md). Per l'inferenza AI il backend FastAPI SuperIA è una dipendenza necessaria; deve essere pubblicato in modo sicuro e gratuito prima dell'uso reale. Il piano di rilevamento hardware e benchmark comparativo dei telefoni è in [docs/DEVICE_DETECTION_AND_BENCHMARK_PLAN.md](DEVICE_DETECTION_AND_BENCHMARK_PLAN.md).

## Documentazione della Fase 0

Il catalogo nominativo è in [docs/AI_CATALOG.md](AI_CATALOG.md); la prima matrice delle alternative gratuite/locali è in [docs/AI_ALTERNATIVES_MATRIX.md](AI_ALTERNATIVES_MATRIX.md); la [mappatura funzionale preliminare di tutte le 182 IA](AI_FUNCTIONAL_ANALYSIS.md) associa ogni voce a capacità, candidati tecnici e modalità locale/ibrida; il [registro tecnico dei componenti e delle licenze](COMPONENTS_LICENSE_REGISTER.md) separa le licenze del codice da quelle di modelli, pesi e dipendenze; il [registro macchina JSON](../config/components.registry.json) traccia candidati, stato di approvazione e controlli richiesti; l'[audit strutturato delle 182 voci](AI_182_AUDIT.json) registra per ogni sistema capacità, alternativa, modalità, costi, licenze e compatibilità ancora da verificare; le [decisioni architetturali](PHASE0_DECISIONS.md) fissano i criteri di selezione e i limiti Android; il [registro modelli candidati](../config/models.registry.json) mantiene le famiglie da benchmarkare senza approvarne automaticamente i pesi. La ricognizione documentale è consolidata, mentre la verifica puntuale dei piani commerciali, dei singoli artefatti e dei benchmark su hardware reale resta aperta.

## Fasi riorganizzate

Per il quadro operativo aggiornato di tutte le fasi, le attività residue e i gate di completamento, consulta il [Registro operativo delle fasi](PHASE_TRACKER.md). La roadmap resta il piano ufficiale; il registro distingue documentazione, implementazione, CI e collaudo pratico.

| Fase | Obiettivo | Contenuti principali | Stato reale |
|---|---|---|---|
| 0 | Inventario delle 182 IA e progettazione funzionale | Catalogo e mappatura 182/182; audit strutturato; decisioni architetturali; candidati e registro licenze | Documentazione consolidata; audit individuale delle 182 voci, licenze, costi, artefatti e compatibilità ancora aperto |
| 1 | Fondamenta Android e connessioni | Kotlin/Compose; architettura modulare; storage; profilo dispositivo; pairing sicuro tra telefoni; login GitHub; browser repository; build debug | Parziale: app e rilevamento base presenti; QR è solo invito, senza connessione/fiducia; handshake, rete, GitHub e test reali mancanti |
| 2 | Motore AI centrale e assistente | Chat online; provider gratuiti; streaming; cronologia cifrata; memoria controllata dall'utente; ricostruzione del contesto; gestione errori e failover | Client Android, pairing, cronologia/memoria e router presenti nel codice; backend pubblico, provider attivi, streaming e test end-to-end ancora mancanti |
| 3 | Documenti e conoscenza personale | Importazione/estrazione locale PDF, DOCX, TXT e CSV; archivio cifrato; BM25; OCR PDF scansiti via Tesseract | Primo incremento implementato e CI Android superata; retrieval semantico, citazioni precise, OCR immagini autonome, hardening e test pratici mancanti |
| 4 | Ricerca, navigazione e traduzione | Ricerca web quando richiesta; confronto fonti; verifica date; traduzione; riassunti di pagine; estrazione contenuti da link pubblici; gestione fonti e citazioni | Non implementata nell'app Android |
| 5 | Immagini e grafica | Generazione da testo; modifica immagini; inpainting/outpainting; rimozione/sostituzione sfondo e oggetti; restauro; upscaling; OCR immagini; grafica, loghi e layout | Non implementata |
| 6 | Video e contenuti visuali in movimento | Generazione video da testo/immagine; editing; taglio e montaggio; sottotitoli; trascrizione; traduzione e doppiaggio; riassunto video; clip brevi; elaborazione batch | Non implementata; FFmpeg in attesa di revisione build/licenze |
| 7 | Audio, voce e musica | Speech-to-text; text-to-speech; voci; clonazione vocale solo con consenso e diritti; doppiaggio; pulizia audio; separazione sorgenti; trascrizione; musica originale; effetti sonori | Non implementata; Whisper/whisper.cpp sono candidati |
| 8 | Programmazione, GitHub e creazione software | Agente coding; analisi repository; branch; modifica file e diff; commit; issue; pull request; review; GitHub Actions; log e test; creazione app/APK e documentazione | Non implementata come funzione nell'app; la CI del repository non è un agente coding |
| 9 | Produttività, dati e strumenti da ufficio | Testi, email e documenti; presentazioni; fogli di calcolo; formule; analisi CSV; grafici; dashboard; pianificazione; gestione attività e basi dati locali | Non implementata |
| 10 | Agenti e automazioni | Agenti specializzati; pianificazione multi-step; strumenti e plugin; flussi automatici; esecuzioni programmate; collaborazione tra agenti; registro attività; approvazione umana per azioni sensibili | Non implementata; la ripresa dell'ultima richiesta chat non è orchestrazione agentica |
| 11 | Aree professionali e specialistiche | Supporto giuridico; ricerca sanitaria e scientifica; finanza e contabilità; istruzione; marketing; architettura; CAD/3D; design; cybersecurity difensiva; analisi e simulazioni, con limiti e verifiche specifici | Non implementata |
| 12 | Ecosistema di modelli e componenti | Registro di provider/API e componenti; versioni; dipendenze; licenze; quote; benchmark; selezione e rotazione dei provider online gratuiti | Registry documentale presente; orchestrazione e gestione operativa multi-provider ancora da completare |
| 13 | Applicazione unica e interfacce | Dashboard; moduli; progetti; gestione dispositivi associati; selezione nodo AI; sincronizzazione solo se scelta e protetta; accessibilità e UX mobile | Scheletro UI Android presente; dashboard modulare, progetti, dispositivi e sync non implementati |
| 14 | Privacy, sicurezza e controllo | Modalità offline; cifratura dei dati locali; autenticazione; permessi; backup/esportazione; cancellazione; isolamento moduli; protezione segreti; audit; consenso e conferma delle azioni esterne | Cifratura e consenso parzialmente presenti; hardening, threat model e test sicurezza mancanti |
| 15 | Collaudo, ottimizzazione e distribuzione | Test unitari/integrati; test su dispositivi reali; sicurezza; prestazioni; pairing; verifica online end-to-end; APK definitivo e manuale | Chat online non collaudata end-to-end; workflow Android limitato ai test unitari; APK bloccato fino al completamento di tutte le funzioni chat |

## Copertura funzionale della ricognizione iniziale

Le 182 IA di riferimento vanno ricondotte a capacità, non replicate una per una come servizi separati. La piattaforma dovrà coprire almeno questi gruppi:
1. Assistenti conversazionali, ragionamento e ricerca risposte.
2. Generazione e modifica immagini, design e grafica.
3. Generazione, montaggio, editing e comprensione video.
4. Musica, composizione, effetti, sintesi e riconoscimento vocale.
5. Traduzione, doppiaggio, sottotitoli e trascrizione.
6. Programmazione, sviluppo software e strumenti per sviluppatori.
7. Documenti, scrittura, presentazioni e produttività.
8. Ricerca web, conoscenza, PDF e analisi delle fonti.
9. Dati, fogli di calcolo, visualizzazione e business intelligence.
10. Agenti, automazioni e strumenti specializzati.
11. Applicazioni professionali: legale, salute/ricerca, finanza, istruzione, marketing, architettura, 3D e sicurezza informatica difensiva.

Il catalogo dettagliato dei 182 nomi dovrà associare a ciascuno: categoria, funzioni distintive, alternative open source/gratuite, licenza, necessità di Internet, requisiti hardware, stato di integrazione e test.

## Criteri di completamento di ogni fase

Una fase passa a "completata" solo quando:
- le funzioni previste sono implementate e accessibili dall'interfaccia;
- i test automatici pertinenti passano;
- almeno un test pratico end-to-end è stato eseguito;
- privacy, licenze, limiti e requisiti sono documentati;
- la funzione non dipende da API a pagamento obbligatorie;
- Versioni Android, RAM, chipset, acceleratori e modalità offline sono dichiarati come supportati, parziali o non applicabili.

## Stato verificato al 15 novembre 2026

- Il repository contiene un backend FastAPI iniziale, provider Ollama, endpoint health/capabilities/models/chat, interfaccia web, cronologia conversazioni nel browser, esportazione e annotazione personale locale.
- La nota personale locale viene aggiunta al contesto della chat; non è apprendimento del modello e non è una memoria persistente lato server.
- I workflow GitHub Actions recenti risultano con esito SUCCESS per i test backend. Questo non equivale ancora a un collaudo completo su Ollama, Android.
- Le fasi 0–2 non sono da considerare completate: l'inventario funzionale va consolidato e il prototipo della chat va ulteriormente verificato.
- L'app Android ha uno scheletro Kotlin/Compose, profilo dispositivo, primitive iniziali per identità crittografica/inviti QR, interfaccia chat e cronologia locale cifrata con Android Keystore. La chat non esegue ancora inferenza; pairing completo, runtime AI e moduli immagini/video/audio/agenti non sono ancora operativi.
- La build Android del commit `c6654884036b916a7d8265dd1c0c386b11f96a1e` è terminata con SUCCESS su GitHub Actions (run 43745811323); i test backend sullo stesso commit sono SUCCESS (run 43745811311). Le modifiche successive alla cronologia cifrata sono ancora da compilare e verificare.
- La build Android debug del commit `e91033d8bd49c52561c0b3f7df514a5b84f6c4e4` è SUCCESS (run 43745987105); artefatto `superia-privata-debug` presente, 16.059.886 byte, scadenza 13 febbraio 2027. Questo APK precede il nuovo contratto inference e non contiene ancora tali file.
- I test backend del commit `4b952b65805d105eb6274beb4d661f1ea5c79f45` sono SUCCESS (run 43745991308).
- Aggiunto il contratto Android `InferenceEngine`, il registro `InferenceEngineRegistry` e test unitari per registro vuoto/selezione solo di provider pronti; la build e i nuovi test Kotlin devono ancora essere verificati da Actions.
- Nessun runtime Android concreto o modello locale è ancora integrato; nessuna risposta AI viene ancora generata dall'app.

## Regola di separazione

Tutto il lavoro di questa roadmap riguarda esclusivamente **SuperIA**. Non modificare repository, file, database, impostazioni o pipeline di **Editore Foto e Video IA**.


## Aggiornamento motore AI locale — 15 novembre 2026

- Kotlin Gradle Plugin aggiornato a 2.2.0; AGP 8.9.2 e Gradle 8.11.1 invariati.
- Dipendenza LiteRT-LM Android 0.16.1 aggiunta e adapter Kotlin locale implementato, CPU-first e senza fallback cloud.
- Il catalogo propone Gemma 3 1B IT INT4; modello esterno all'APK, circa 584 MB secondo la scheda Hugging Face, con termini Gemma da accettare prima dell'uso.
- L'adapter richiede file locale, licenza approvata e almeno 2 GB di RAM disponibile secondo la soglia iniziale. La soglia è provvisoria.
- Non dichiarare ancora l'assistente operativo: la build con LiteRT-LM deve superare CI; l'importazione modello, il collegamento alla UI e i test fisici sui due telefoni sono ancora da completare.


### Collegamento chat e modello — 15 novembre 2026

La UI Compose è stata aggiornata per importare un file LiteRT-LM dalla memoria accessibile tramite selettore Android, mostrare l'informativa/consenso Gemma e abilitare l'invio soltanto quando il runtime, il file, la licenza e la soglia RAM sono pronti. La cronologia AES-GCM conserva ora messaggi utente e risposte dell'assistente. Il modello non è incluso né scaricato dall'app. La build CI del commit con questi cambiamenti deve ancora essere completata; l'esecuzione effettiva del modello richiede anche importazione e test su device reali.


Verifica CI intermedia: sul commit `272a7c805f030ca05eda8b66b02754f34eb021c6` il task `testDebugUnitTest` è SUCCESS; il task `assembleDebug` è ancora in corso nel run 43746849747. Il run non è da considerare completato e non è ancora disponibile un nuovo APK con LiteRT-LM.


## Correzione stato CI Android — 15 novembre 2026

La precedente nota che indicava il run 43746849747 come ancora in corso è superata. Il run è terminato con **SUCCESS**: test Android, `assembleDebug` e upload APK sono tutti riusciti sul commit `272a7c805f030ca05eda8b66b02754f34eb021c6`. L'artefatto `superia-privata-debug` è disponibile (archivio 38.272.811 byte; scadenza 13 febbraio 2027). Dettagli e limiti sono registrati in [docs/ANDROID_RUNTIME_VALIDATION.md](ANDROID_RUNTIME_VALIDATION.md).

La documentazione ufficiale LiteRT-LM conferma che `Message.toString()` restituisce i contenuti del messaggio, quindi l'uso corrente nello streaming non stampa la rappresentazione di debug dell'oggetto. Restano obbligatori importazione del modello e test end-to-end su entrambi i telefoni: **la Fase 2 è ancora in corso** e non è dichiarata completata.


### Verifica finale CI e streaming LiteRT-LM — 15 novembre 2026

- Run Android `43746849747`: `assemble-debug` SUCCESS; i passaggi `Run Android unit tests`, `Assemble debug APK` e `Upload debug APK` sono tutti SUCCESS.
- Artefatto `superia-privata-debug`: 38.272.811 byte (archivio), SHA-256 `b9297e396bbeb57cd6dc4647138664f132264ebc39fa3a5218765d1e7463bdcb`, scadenza 13 febbraio 2027.
- La verifica della sorgente/API ufficiale conferma che `Message.toString()` restituisce il contenuto testuale (`Contents.toString()` concatena i contenuti) e che la guida Kotlin usa lo stesso approccio nel `Flow.collect`. Non è richiesta una modifica al codice per questo punto.
- La Fase 2 resta in corso: nessun test end-to-end è stato ancora eseguito con Gemma importato su Realme C75 o Xiaomi Mi 10T. Non dichiarare completata l'inferenza locale fino a tali prove.
- Dettagli, digest e gate pratici: [docs/ANDROID_RUNTIME_VALIDATION.md](ANDROID_RUNTIME_VALIDATION.md).


## Aggiornamento Fase 2 — 15 novembre 2026 (stato corrente)

Questo aggiornamento sostituisce le note precedenti che descrivevano la chat Android come non ancora collegata a un runtime. Sul commit `8ba080ad4f43690f25c63f74d2ff7c3941944b5a` sono verificati:
- test unitari Android superati, inclusi i test del ricostruttore di contesto conversazionale;
- compilazione APK debug superata e artefatto caricato;
- test backend superati;
- adapter LiteRT-LM 0.16.1 CPU collegato alla UI;
- importazione modello in area privata con controllo estensione, limite dimensione, SHA-256 e sostituzione protetta da backup se lo spostamento atomico non è disponibile;
- cronologia chat cifrata AES-GCM con Android Keystore;
- ricostruzione del contesto a partire dagli ultimi 12 messaggi, limitata a 6.000 caratteri;
- chiusura delle risorse native anche quando l'inizializzazione del runtime fallisce e gestione esplicita delle risposte vuote.

CI: Android run [43758098267](https://github.com/damianoavvocato-star/SuperIA/actions/runs/43758098267); backend run [43758098112](https://github.com/damianoavvocato-star/SuperIA/actions/runs/43758098112). Artefatto `superia-privata-debug`, archivio 38.276.044 byte, SHA-256 `ff54a42a7fdcb49b986e9da31c0fdfe6939b49322241b49d9d6450fb6dbefda4`, scadenza 13 febbraio 2027.

**La Fase 2 non viene dichiarata completata**: manca la prova end-to-end con il modello Gemma scaricato/importato e inferenza locale reale su Realme C75 e Xiaomi Mi 10T. Dettagli e gate in [docs/PHASE2_STATUS.md](PHASE2_STATUS.md) e [docs/ANDROID_RUNTIME_VALIDATION.md](ANDROID_RUNTIME_VALIDATION.md).


### Primo incremento Fase 3 — 15 novembre 2026

Aggiunto un flusso Android iniziale per importare PDF, DOCX, TXT e CSV tramite il selettore documenti di sistema. L'estrazione avviene localmente; PDFBox-Android 2.0.27.0 (Apache-2.0) gestisce i PDF testuali, mentre DOCX/TXT/CSV hanno parser locali. Il solo testo estratto viene conservato nell'area privata dell'app; la UI permette anteprima, selezione, eliminazione e passaggio di un singolo estratto (massimo 6.000 caratteri) al prompt della chat LiteRT-LM.

Limiti dichiarati: 25 MB per file, 250 pagine PDF, 200.000 caratteri estratti; OCR Tesseract italiano sulle prime 20 pagine dei PDF scansiti dopo importazione di `ita.traineddata`. Restano embeddings/ricerca semantica, citazioni di pagina/paragrafo, OCR su immagini autonome, gestione quote/retention e test di recupero/corruzione dell'archivio; BM25 lessicale multi-documento è il primo livello di retrieval locale. Build, test automatici e prove su dispositivi dopo queste modifiche sono pendenti. La Fase 3 resta aperta.


## Requisito vincolante: conversazione continua e memoria di lavoro

Decisione approvata dall'utente il 15 novembre 2026: SuperIA deve offrire una conversazione principale persistente e continuabile nel tempo, senza obbligare l'utente ad aprire una nuova chat quando il contesto diventa lungo. La cronologia deve sopravvivere alla chiusura dell'app e al riavvio del dispositivo; il sistema deve mantenere un registro strutturato di obiettivi, decisioni, istruzioni, file e attività svolte, con riepiloghi progressivi e recupero selettivo dei messaggi/documenti pertinenti quando il limite di contesto del modello viene raggiunto. Il passaggio tra modelli deve preservare la stessa conversazione e il relativo contesto.

Il requisito non implica che l'intera cronologia venga caricata in ogni richiesta: il motore deve ricostruire il contesto utile usando cronologia persistente, memoria di progetto, riepiloghi verificabili e retrieval locale. Deve essere possibile riprendere attività interrotte e distinguere chiaramente attività completate, in corso e bloccate. Conservazione locale cifrata, controllo utente su esportazione/cancellazione, privacy e funzionamento offline dove possibile sono obbligatori. Nessuna promessa di memoria perfetta: il sistema deve mostrare e consentire di correggere i riepiloghi e non inventare dettagli mancanti.

Questo requisito attraversa almeno Fase 2 (motore/chat/memoria), Fase 3 (archivio e retrieval documentale), Fase 10 (continuità degli agenti e ripresa dei workflow), Fase 13 (interfaccia conversazionale unica) e Fase 14 (privacy e controlli). Criteri di accettazione: conversazione riaperta dopo chiusura/riavvio; contesto recuperato oltre la finestra del modello; cambio modello senza perdere il filo; ripresa di un'attività interrotta; riepiloghi modificabili e fonti di memoria rintracciabili; nessuna richiesta di nuova chat imposta dall'app.


## Prevalenza del piano operativo — 23 novembre 2026
La roadmap a 16 fasi sotto riportata è una vista macro e contiene registrazioni storiche. Per l'ordine di lavoro, lo stato puntuale e i criteri verificabili prevale il [Piano operativo a 50 micro-fasi](MASTER_MICRO_PHASE_PLAN.md), insieme al [Registro operativo aggiornato](PHASE_TRACKER.md). Non interpretare le vecchie celle di stato o le descrizioni antecedenti al 23 novembre come stato corrente.

Stato corrente da usare: micro-fasi 01, 02, 03 e 04 completate. Il client Android usa già POST /api/v1/chat/stream; lo streaming è implementato in prima versione, ma il collaudo end-to-end con provider reale resta aperto. Il backend non è pubblicato e nessun provider reale è attivo. Il workflow Android resta test-only e il blocco APK è assoluto fino al gate della micro-fase 36.
