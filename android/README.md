# SuperIA Privata — Android

## Direzione tecnica attuale
L'app Android è **online-only per l'inferenza AI**. Non usa Gemma, LiteRT-LM, Ollama o altri modelli LLM locali. Le risposte arrivano dal backend SuperIA attraverso HTTPS.

## Funzioni presenti nel codice
- App nativa Kotlin + Jetpack Compose.
- Chat unica con cronologia locale cifrata AES-GCM.
- Client HTTPS per `POST /api/v1/chat`, con Bearer token.
- Configurazione endpoint/token nelle impostazioni; token cifrato tramite Android Keystore.
- Verifica di raggiungibilità del backend tramite `/health`.
- Importazione PDF, DOCX, TXT e CSV; estrazione testo e archivio documentale cifrato.
- Ricerca lessicale BM25 e recupero dei passaggi pertinenti nel prompt.
- OCR offline dei PDF scansiti tramite Tesseract, se è stato importato il dizionario italiano.
- Profilo dispositivo e diagnostica di base.

## Rimozione del modello locale
La dipendenza LiteRT-LM e i sorgenti Android per motore, catalogo e importazione Gemma sono stati rimossi. La nuova versione cancella una sola volta la precedente cronologia locale della chat durante la migrazione online; da quel momento la cronologia cifrata viene riutilizzata per le conversazioni online.

## Requisiti di connessione
- Backend HTTPS pubblico e funzionante.
- Token individuale, revocabile e ottenuto tramite pairing sicuro.
- Provider gratuiti attivati solo dopo audit delle condizioni: nessun pagamento, carta, ricarica o addebito automatico.
- Quota esaurita = stop o passaggio a un altro provider gratuito approvato; mai fallback a pagamento.
- Le chiavi dei provider restano sul server e non vengono inserite nell'APK.

## Stato di rilascio
Il client online è implementato nel codice, ma hosting pubblico, pairing definitivo, configurazione dei provider e collaudo end-to-end restano da completare. La build Android successiva alla rimozione di LiteRT-LM deve ancora essere compilata e verificata in CI e su dispositivo.

## Requisiti di build
- Android Studio con JDK 17.
- Android SDK Platform 35.
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2.

Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle.
