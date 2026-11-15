# SuperIA Privata — Android

Prima base nativa Kotlin + Jetpack Compose.

## Requisiti di build
- Android Studio con JDK 17
- Android SDK Platform 35
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2

## Apertura
Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle. Collegare un dispositivo Android con debug USB abilitato o usare un emulatore.

## Funzioni presenti
- Dashboard Compose iniziale e pannello chat conversazionale.
- Inserimento e visualizzazione dei messaggi dell'utente.
- Cronologia chat locale cifrata con AES-GCM; chiave AES-256 non esportabile custodita in Android Keystore; pulsante per cancellare la cronologia.
- Lettura di produttore/modello, versione Android/API, RAM totale e disponibile esposte da Android, ABI CPU, spazio libero nell'area dati e stato termico.
- Pulsante per aggiornare il profilo dispositivo.

## Limiti noti
- La RAM totale letta da `ActivityManager.MemoryInfo` è quella esposta dal sistema; il valore può differire dalla RAM commerciale dichiarata e non identifica separatamente la RAM virtuale del produttore.
- Non legge IMEI, seriali, account o identificatori persistenti.
- Non misura ancora CPU/GPU/NPU, prestazioni LLM, consumo batteria o throttling nel tempo.
- La chat Android non è ancora collegata a un runtime AI: l'invio memorizza il testo, non genera risposte.
- Non contiene ancora pairing completo, GitHub OAuth o runtime AI locale integrato.
- La cronologia è cifrata a riposo; non è sincronizzata tra telefoni e non sostituisce un backup cifrato.
- Nessuna autorizzazione speciale è richiesta in questa prima schermata.

## Prossimi incrementi
Collegamento a un runtime AI locale Android, allegati con selettore documenti, test strumentali per archivio cifrato, inventario acceleratori supportati, benchmark controllato e completamento del pairing sicuro.


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
