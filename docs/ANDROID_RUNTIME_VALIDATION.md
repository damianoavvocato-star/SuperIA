# Validazione runtime Android LiteRT-LM — 15 novembre 2026

## Esito CI verificato

- Repository: `damianoavvocato-star/SuperIA-Privata`
- Branch: `main`
- Commit: `272a7c805f030ca05eda8b66b02754f34eb021c6`
- Workflow Android debug build: [run 43746849747](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43746849747)
- Job `assemble-debug`: SUCCESS.
- `Run Android unit tests`: SUCCESS.
- `Assemble debug APK`: SUCCESS.
- `Upload debug APK`: SUCCESS.
- Artefatto: `superia-privata-debug`
- Dimensione archivio: 38,272,811 byte.
- SHA-256 dichiarato da GitHub per l'archivio: `b9297e396bbeb57cd6dc4647138664f132264ebc39fa3a5218765d1e7463bdcb`
- Scadenza artefatto: 13 febbraio 2027.

L'APK è stato compilato e caricato da GitHub Actions. Il link del run permette di scaricare l'artefatto dalla sezione Artifacts. La dimensione indicata è quella dell'archivio artefatto, non necessariamente quella dell'APK estratto.

## Verifica API di streaming

La documentazione ufficiale LiteRT-LM descrive `sendMessageAsync(text): Flow<Message>` e l'esempio raccoglie i messaggi con `collect { print(it) }`. La classe `Message` implementa `toString()` restituendo `contents.toString()`; `Contents.toString()` concatena i contenuti. Quindi l'attuale `message.toString()` estrae il contenuto testuale del messaggio e non la rappresentazione di debug dell'oggetto.

Riferimenti:
- [Guida Android LiteRT-LM](https://developers.google.com/edge/litert-lm/android)
- [Message.kt ufficiale](https://github.com/google-ai-edge/LiteRT-LM/blob/main/kotlin/java/com/google/ai/edge/litertlm/Message.kt)
- [Conversation.kt ufficiale](https://github.com/google-ai-edge/LiteRT-LM/blob/main/kotlin/java/com/google/ai/edge/litertlm/Conversation.kt)

## Cosa è verificato e cosa no

Verificato:
- I test unitari Android passano sul commit sopra indicato.
- La compilazione dell'APK debug passa.
- L'upload dell'artefatto passa.
- L'app contiene il collegamento UI -> adapter LiteRT-LM e il catalogo/import locale del modello.
- Il codice non prevede fallback cloud automatico.

Non ancora verificato:
- Download e importazione reale del modello Gemma sul telefono.
- Avvio del modello, prima inferenza e successive conversazioni end-to-end.
- Memoria RAM, tempi di avvio, token/s, temperatura e stabilità sul Realme C75 e sullo Xiaomi Mi 10T.
- Ripresa del contesto dopo ricreazione del runtime: implementata ricostruzione del prompt dagli ultimi 12 messaggi della cronologia cifrata (massimo 6.000 caratteri) e coperta da test unitari; manca la verifica con modello reale su dispositivo.
- Robustezza per errori di memoria, file modello corrotto, interruzione generazione e cambio modello.

## Prossimi gate obbligatori

1. Scaricare l'artefatto `superia-privata-debug` dal run CI ed estrarre l'APK.
2. Installarlo prima su un telefono e verificare apertura, permessi e schermata modello.
3. Accettare i termini Gemma e importare il file `.litertlm` corretto ottenuto dalla fonte ufficiale.
4. Eseguire un prompt breve e verificare che la risposta provenga dal runtime locale.
5. Ripetere sul secondo telefono e registrare RAM disponibile, tempo prima risposta, stabilità e consumo batteria.
6. Correggere i problemi riscontrati e ripetere CI.

La Fase 2 resta **in corso** finché i gate pratici sopra non sono superati. La riuscita di CI non equivale a un test d'inferenza reale.


## Chiarimento sull'estrazione del testo

Il controllo della sorgente ufficiale conferma che `Message.toString()` restituisce `contents.toString()` e che `Contents.toString()` concatena i contenuti. Inoltre, la guida Kotlin ufficiale usa `collect { print(it.toString()) }` per lo streaming. Non è quindi necessario sostituire `message.toString()` con un accesso interno o una conversione JSON: l'implementazione attuale è coerente con l'API documentata. Questo controllo statico non sostituisce comunque una prova con il modello installato sul dispositivo.

Fonti: [Message.kt](https://github.com/google-ai-edge/LiteRT-LM/blob/main/kotlin/java/com/google/ai/edge/litertlm/Message.kt), [guida Android LiteRT-LM](https://developers.google.com/edge/litert-lm/android).


## Aggiornamento corrente — commit 8ba080ad4f43690f25c63f74d2ff7c3941944b5a

- Android Actions: [run 43758098267](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43758098267) — **SUCCESS**. Test unitari, assembleDebug e upload artefatto tutti riusciti.
- Backend Actions: [run 43758098112](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43758098112) — **SUCCESS**.
- Artefatto: `superia-privata-debug`, ID `14178604470`, archivio 38.276.044 byte, SHA-256 `ff54a42a7fdcb49b986e9da31c0fdfe6939b49322241b49d9d6450fb6dbefda4`, scadenza 13 febbraio 2027.
- Nuovi test: `ChatPromptBuilderTest` verifica prompt vuoto/non vuoto, inclusione messaggi utente/assistente e limite di contesto.
- La cronologia cifrata viene usata per ricostruire il contesto conversazionale dopo la ricreazione del runtime: massimo 12 messaggi e 6.000 caratteri.
- L'importazione del modello preferisce la sostituzione atomica; se non supportata, usa backup/ripristino per preservare il modello precedente in caso di errore.
- L'adapter chiude il runtime nativo se l'inizializzazione fallisce e segnala un errore se il modello non produce testo.
- Il pannello impedisce l'importazione durante una generazione e non inghiotte la cancellazione delle coroutine.

**Limite residuo obbligatorio:** CI non dimostra che il file Gemma sia valido né che l'inferenza funzioni sui telefoni. Servono importazione del modello e prove reali su Realme C75 e Xiaomi Mi 10T, con misure di RAM, tempi di risposta, stabilità e temperatura.
