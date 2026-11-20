# SuperIA Privata — Android

## Stato della migrazione online (20 novembre 2026)

La schermata chat è stata collegata al client HTTP del backend SuperIA: invia il prompt ricostruito localmente a `POST /api/v1/chat`, usa HTTPS obbligatorio e richiede il Bearer token del backend. L'indirizzo e il token si configurano in **Chat → Impostazioni**; il token è cifrato con AES-GCM e una chiave non esportabile Android Keystore. Il manifest dichiara INTERNET e disabilita il traffico in chiaro.

La chat non richiama più il motore Gemma locale. Gemma e LiteRT-LM restano nel codice/build legacy finché il percorso online non è compilato, pubblicato, installato e collaudato end-to-end. Non cancellare il modello Gemma già presente sul telefono in questa fase.

**Non è ancora pronta all'uso autonomo:** manca un backend pubblico HTTPS, un token individuale sicuro ottenuto tramite pairing, la configurazione dei provider gratuiti verificati e il collaudo CI della nuova revisione. Non inserire token dei provider nell'app. Il client attuale usa una richiesta non-streaming e invia il prompt assemblato come un singolo messaggio; il limite server attuale è 12.000 caratteri per messaggio.


Prima base nativa Kotlin + Jetpack Compose.

## Requisiti di build
- Android Studio con JDK 17
- Android SDK Platform 35
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2

## Apertura
Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle. Collegare un dispositivo Android con debug USB abilitato o usare un emulatore.

## Funzioni presenti
- Dashboard Compose iniziale e pannello chat conversazionale.
- Importazione locale di PDF, DOCX, TXT e CSV tramite selettore Android.
- OCR offline italiano dei PDF scansiti con Tesseract, dopo importazione del file lingua `ita.traineddata`.
- Estrazione testo offline: PDFBox-Android per PDF testuali, parser DOCX per il corpo principale e lettura UTF-8 per TXT/CSV.
- Archivio dei testi estratti cifrato AES-GCM con chiave non esportabile Android Keystore, anteprima, selezione, eliminazione, ricerca BM25 e recupero automatico dei passaggi pertinenti nel prompt della chat locale.
- Inserimento e visualizzazione dei messaggi dell'utente.
- Cronologia chat locale cifrata con AES-GCM; chiave AES-256 non esportabile custodita in Android Keystore; pulsante per cancellare la cronologia.
- Lettura di produttore/modello, versione Android/API, RAM totale e disponibile esposte da Android, ABI CPU, spazio libero nell'area dati e stato termico.
- Pulsante per aggiornare il profilo dispositivo.

## Limiti noti
- La RAM totale letta da `ActivityManager.MemoryInfo` è quella esposta dal sistema; il valore può differire dalla RAM commerciale dichiarata e non identifica separatamente la RAM virtuale del produttore.
- Non legge IMEI, seriali, account o identificatori persistenti.
- Non misura ancora CPU/GPU/NPU, prestazioni LLM, consumo batteria o throttling nel tempo.
- La chat Android ora dispone di un client per il backend HTTPS free-only; il collegamento reale dipende dalla configurazione server/token e non è ancora collaudato end-to-end.
- Non contiene ancora pairing completo, GitHub OAuth o runtime AI locale integrato.
- La cronologia è cifrata a riposo; non è sincronizzata tra telefoni e non sostituisce un backup cifrato.
- Nessuna autorizzazione generale di archiviazione è richiesta: l'accesso ai file usa il selettore documenti Android.
- L'OCR dei PDF scansiti richiede il file `ita.traineddata` importato dall'utente e processa al massimo le prime 20 pagine; non è incluso nell'APK. La ricerca testuale BM25 multi-documento è disponibile e viene eseguita automaticamente sulle richieste chat; embeddings/ricerca semantica non sono ancora implementati. Limiti iniziali: 25 MB per file, 250 pagine PDF, 200.000 caratteri estratti; massimo 6.000 caratteri di contesto documentale per richiesta chat.
- I file originali non vengono copiati nell'archivio: viene conservato il solo testo estratto, cifrato AES-GCM nell'area privata dell'app con chiave dedicata in Android Keystore.

## Prossimi incrementi
Pubblicazione backend su hosting verificato gratuito, pairing sicuro con token individuali e revocabili, rate limiting/quote, test CI della connessione e collaudo end-to-end sui telefoni. Successivamente embeddings/ricerca semantica e riferimenti a pagina/paragrafo.


## Contratto del motore AI Android

È stato aggiunto il contratto Kotlin `InferenceEngine`, con richieste/risposte tipizzate, stati espliciti di disponibilità e un registro `InferenceEngineRegistry`. Il registro non contiene provider predefiniti: finché un runtime Android reale, un modello e la relativa licenza non sono verificati, la chat non deve dichiarare di poter generare risposte. Non è previsto alcun fallback cloud automatico.

I test unitari del registro verificano che un registro vuoto non dichiari inferenza disponibile e che venga selezionato solo un motore marcato come pronto. Il runtime concreto, il caricamento dei pesi e la generazione sul dispositivo restano da implementare e collaudare.

## Valutazione runtime locale (15 novembre 2026)

La prima opzione da prototipare è **LiteRT-LM Kotlin**: la documentazione ufficiale offre un'API Android con conversazioni multi-turno e streaming; il runtime dichiara supporto CPU/GPU e modelli `.litertlm`. La documentazione Android indica che l'inizializzazione del modello può richiedere diversi secondi e va eseguita su thread di background. I file modello devono restare fuori dall'APK, in storage dell'app, con download esplicito e verifica di dimensione/licenza.

**Blocco di compatibilità da risolvere prima di aggiungere la dipendenza:** il progetto usa Kotlin 2.1.10; la documentazione LiteRT-LM segnala che la release Android 0.18.0 usa metadata Kotlin 2.4 e richiede un compilatore Kotlin 2.3 o superiore. Non aggiungere quindi alla cieca la dipendenza 0.18.0: prima va valutato un aggiornamento coordinato di Kotlin/Compose/AGP e verificato con CI. La documentazione segnala anche 0.16.1 come compatibile con Kotlin 2.2+, che resta comunque più nuova dell'attuale compilatore del progetto.

MediaPipe LLM Inference non sarà la scelta iniziale: Google lo indica in manutenzione e raccomanda la migrazione a LiteRT-LM. **llama.cpp** resta un'alternativa tecnica da benchmarkare: dispone di istruzioni ufficiali per Android NDK e ABI arm64-v8a, ma richiede integrazione e gestione native più complesse.

Fonti tecniche:
- LiteRT-LM Kotlin API: https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/api/kotlin/getting_started.md
- Guida Android LiteRT-LM: https://developers.google.com/edge/litert-lm/android
- Stato MediaPipe LLM Inference: https://developers.google.com/edge/mediapipe/solutions/genai/llm_inference/android
- llama.cpp Android: https://github.com/ggml-org/llama.cpp/blob/master/docs/android.md

La selezione definitiva dipende da build riproducibile, licenza del modello scelto, uso RAM/temperatura e benchmark reali sul Realme C75 e sullo Xiaomi Mi 10T. Nessun runtime è ancora integrato e nessun modello è ancora installato.
## Runtime locale LiteRT-LM (prototipo integrato)

- Kotlin Gradle Plugin aggiornato a 2.2.0 per soddisfare i requisiti di metadata della versione LiteRT-LM selezionata; AGP 8.9.2 e Gradle 8.11.1 restano invariati.
- Dipendenza LiteRT-LM Android fissata a `0.16.1` (non usare `latest.release` in una build riproducibile).
- Aggiunto `LiteRtLmInferenceEngine`: esecuzione CPU locale, conversazione multi-turno mantenuta in memoria, streaming via Kotlin Flow, nessun fallback cloud, controllo esplicito della licenza e verifica del file `.litertlm`.
- Aggiunto catalogo iniziale `Gemma 3 1B IT INT4`; i pesi non sono inclusi nell'APK e non vengono scaricati automaticamente.
- Il file modello deve essere fornito dall'utente nella directory privata `files/models`. Prima di attivare l'esecuzione, l'app dovrà offrire importazione sicura del file e consenso esplicito ai termini Gemma.
- Requisito RAM preliminare impostato a 2 GB disponibili come soglia prudenziale, da rivedere dopo benchmark su telefoni reali. Non è una garanzia di compatibilità.

Il modello candidato è distribuito su Hugging Face con accesso soggetto all'accettazione dei Gemma Terms of Use. La variante `gemma3-1b-it-int4.litertlm` è indicata nella scheda modello come circa 584 MB. I benchmark pubblicati per LiteRT-LM provengono da Samsung S24 Ultra e non sono trasferibili automaticamente a Realme C75 o Xiaomi Mi 10T.

Fonti modello e runtime:
- LiteRT-LM Kotlin API 0.16.1: https://github.com/google-ai-edge/LiteRT-LM/blob/v0.16.1/docs/api/kotlin/getting_started.md
- Modello e varianti: https://huggingface.co/litert-community/Gemma3-1B-IT
- Termini Gemma: https://ai.google.dev/gemma/terms

### Chat collegata al runtime locale

- La chat ora usa `LiteRtLmInferenceEngine` per generare risposte reali quando il modello è presente e la licenza è stata accettata.
- La UI permette di aprire la pagina Hugging Face, leggere i termini Gemma, dichiararne l'accettazione e importare il file `.litertlm` tramite il selettore documenti Android.
- L'importazione copia il modello in `files/models`, verifica estensione e dimensione massima, calcola SHA-256 e non richiede permessi generali di archiviazione.
- Il pulsante Invia resta disabilitato finché runtime, modello, licenza e soglia RAM non risultano disponibili.
- La cronologia cifrata ora conserva anche il ruolo dei messaggi (utente/assistente), mantenendo compatibilità con le vecchie voci che non avevano il campo ruolo.
- Non scaricare il modello automaticamente: l'accesso Hugging Face è soggetto all'accettazione dei termini e l'utente deve scaricare il file corretto. Nessun token Hugging Face viene richiesto o incorporato nell'APK.
- La risposta AI usa streaming LiteRT-LM; l'uso CPU è la modalità iniziale per privilegiare compatibilità. GPU/NPU e accelerazione vanno valutate successivamente.


## Stato aggiornato al 16 novembre 2026

Le note storiche precedenti che descrivono la chat come non collegata a un runtime o il runtime come non integrato sono superate dagli incrementi successivi riportati in questo documento. La chat Android è ora collegata al prototipo LiteRT-LM 0.16.1 CPU; il catalogo include Gemma 3 1B IT INT4 e l'utente può accettare i termini e importare il file `.litertlm` localmente.

La CI Android [run 43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130) ha superato test unitari, compilazione APK Debug e 6 test strumentali Keystore su emulatore API 34. Questo non prova ancora il caricamento reale di Gemma né le prestazioni sui telefoni.

**Collaudo fisico ancora da eseguire:** seguire [docs/DEVICE_SMOKE_TEST.md](../docs/DEVICE_SMOKE_TEST.md) su Realme C75 e Xiaomi Mi 10T. Fino a quel momento non dichiarare il runtime validato sui dispositivi target.


## Fase 3 — Documenti e conoscenza personale (incremento corrente)

- La chat cerca automaticamente, in locale, nei testi di tutti i documenti importati prima di ogni nuova richiesta e quando si riprende una richiesta interrotta.
- Il recupero usa BM25 su blocchi testuali, seleziona al massimo 5 passaggi pertinenti e limita il contesto documentale complessivo a 6.000 caratteri.
- I passaggi recuperati sono accompagnati dal titolo del documento e dal numero del blocco; il contesto manuale selezionato dall'utente resta supportato.
- Lettura/decrittazione dell'archivio su dispatcher IO e ricerca su dispatcher Default: il lavoro documentale non viene eseguito sul thread UI.
- Il contenuto dei documenti viene etichettato nel prompt come contenuto non attendibile, non come istruzioni.
- Aggiunti test unitari per pertinenza, assenza di risultati, contesto manuale e limite caratteri.
- Non sono ancora presenti embeddings, ricerca semantica, citazioni con pagine PDF affidabili, indicizzazione incrementale persistente o test OCR su scansioni reali. La ricerca BM25 è lessicale: non garantisce di trovare sinonimi o concetti espressi con parole diverse.
- I file originali non vengono caricati su server: estrazione, archivio e ricerca restano sul dispositivo.

L'incremento è in attesa della verifica CI Android e non è ancora distribuito nell'APK installato sul telefono.


### Ricerca vettoriale semantica — base tecnica

- Aggiunto il contratto `LocalTextEmbeddingProvider` per un futuro modello di embedding eseguito interamente sul dispositivo, con metodi separati per indicizzare i documenti e codificare le query.
- Aggiunto `SemanticVectorSearch`: ranking cosine su vettori locali, normalizzazione per la magnitudine, controllo dimensioni, valori non finiti, vettori nulli, soglia minima e numero massimo di risultati.
- Aggiunti test unitari per ordinamento, similarità cosine, dimensioni incompatibili, vettori nulli, soglia e input non validi.
- **Non è ancora attiva la ricerca semantica nell'app**: manca il provider concreto e il modello di embedding da importare. Fino a quel momento la chat continua a usare il recupero BM25 già integrato, senza dichiarare che gli embeddings siano operativi.
- Il modello candidato è EmbeddingGemma V2 Text 270M, distribuito come file `.litertlm`. La documentazione Google descrive l'API `UniversalEmbedder` e l'esecuzione locale; prima di introdurla va risolta la compatibilità con il runtime LiteRT-LM 0.15.0 che è stato mantenuto per evitare di reintrodurre il crash già riscontrato sul telefono.
- Non aggiornare automaticamente il runtime dell'app né sostituire il modello Gemma chat: il modello di embedding dovrà essere opzionale, scaricato/importato separatamente e attivabile solo dopo test CI e prova sul Realme C75.

Riferimenti:
- Google AI Edge — Universal Embedder: https://developers.google.cn/edge/mediapipe/solutions/retrieval/universal_embedder
- Google AI Edge — Semantic Retriever: https://developers.google.cn/edge/mediapipe/solutions/retrieval/semantic_retriever
- Google AI Edge — Android setup e dipendenza tasks-retrieval: https://developers.google.cn/edge/mediapipe/solutions/setup_android

### Compatibilità embedding — verifica 18 novembre 2026

La verifica delle dipendenze ha evidenziato un conflitto concreto: `com.google.mediapipe:tasks-retrieval:1.1.1` dichiara una dipendenza transitiva da `com.google.ai.edge.litertlm:litertlm-android:0.17.0-alpha1`. La guida Kotlin ufficiale di LiteRT-LM segnala che le release 0.17.0 e successive qui considerate portano metadata Kotlin 2.4.0 e richiedono un compilatore Kotlin 2.3 o superiore; questo progetto usa Kotlin Gradle Plugin 2.2.0 e mantiene LiteRT-LM 0.15.0 per evitare la regressione ABI già osservata sul Realme C75.

**Decisione:** non aggiungere `tasks-retrieval` e non aggiornare il runtime della chat in questa fase. Il solo fatto che la libreria sia documentata da Google non dimostra compatibilità binaria con la combinazione corrente.

Percorso alternativo da verificare in un prototipo isolato: LiteRT CompiledModel 2.3.2 con un modello embedding compatibile in formato `.tflite`. Questo runtime è separato da LiteRT-LM e non richiede di sostituire il motore Gemma della chat. Prima dell'integrazione occorre verificare il modello concreto, la firma dei tensori, tokenizer/preprocessing, licenza e memoria su dispositivo. Non è ancora un provider attivo nell'app.

Fonti:
- LiteRT-LM Kotlin API e requisiti Kotlin: https://github.com/google-ai-edge/LiteRT-LM/blob/main/docs/api/kotlin/getting_started.md
- MediaPipe tasks-retrieval 1.1.1 e dipendenze: https://www.mavenpackages.com/packages/com.google.mediapipe/tasks-retrieval
- LiteRT CompiledModel Android: https://developers.google.com/edge/litert/android
