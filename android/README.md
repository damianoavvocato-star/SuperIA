# SuperIA — Android

## Direzione tecnica attuale
L'app Android è **online-only per l'inferenza AI**. Non usa Gemma, LiteRT-LM, Ollama o altri modelli LLM locali. Le risposte sono richieste al backend SuperIA attraverso HTTPS.

## Funzioni presenti nel codice
- App nativa Kotlin + Jetpack Compose.
- Chat unica con cronologia locale cifrata AES-GCM.
- Client HTTPS per `POST /api/v1/chat`, con Bearer token.
- Pairing dispositivo tramite `POST /api/v1/device/pair`; il token e il device ID vengono conservati cifrati tramite Android Keystore. Il codice di pairing non viene salvato.
- Token individuali revocabili, con scadenza gestita dal backend.
- Configurazione endpoint/token nelle impostazioni e verifica backend tramite `/health`.
- Importazione PDF, DOCX, TXT e CSV; estrazione testo e archivio documentale cifrato.
- Ricerca lessicale BM25 e recupero dei passaggi pertinenti nel prompt.
- OCR offline dei PDF scansiti tramite Tesseract, se è stato importato il dizionario italiano.
- Profilo dispositivo e diagnostica di base.
- Salvataggio della richiesta pendente e possibilità di riprenderla in caso di errore di rete.

## Rimozione del modello locale
La dipendenza LiteRT-LM e i sorgenti Android per motore, catalogo e importazione Gemma sono stati rimossi. La nuova versione esegue una sola volta la migrazione che elimina la precedente cronologia locale della chat; non elimina la memoria di progetto né l'archivio documenti.

## Verifica CI — 21 novembre 2026
La pipeline Android [44925187326](https://github.com/damianoavvocato-star/SuperIA/actions/runs/44925187326) sul commit `9ce190cd87ed8c0114a5213ebbb534929e066c1c` è terminata **SUCCESS al secondo tentativo**. Sono riusciti test unitari, build debug, build APK firmato, upload artefatto e test strumentali Android Keystore su emulatore.
Artefatto: `superia-privata-installable`, ID `14716277709`, circa 31,2 MB; scadenza 19 febbraio 2027.
Questa CI non sostituisce il collaudo pratico su Realme C75 e Xiaomi Mi 10T.

## Requisiti per la connessione online
- Backend HTTPS pubblico, funzionante e con database persistente.
- Rate limiting IP reale sul gateway/WAF per il pairing prima dell'esposizione pubblica.
- Token individuale ottenuto tramite pairing e revocabile.
- Provider gratuiti attivati solo dopo audit delle condizioni: nessun pagamento, carta, ricarica o addebito automatico.
- Quota esaurita = stop o passaggio a un altro provider gratuito approvato; mai fallback a pagamento.
- Le chiavi dei provider restano sul server e non vengono inserite nell'APK.

## Stato di rilascio
La build Android e i test CI sono superati, compresi i test Keystore sull'emulatore. **L'app non è ancora collegata a un backend pubblico operativo**: hosting, database, rate limiting gateway, segreti di produzione, provider e collaudo end-to-end restano da completare.

## Requisiti di build
- Android Studio con JDK 17.
- Android SDK Platform 35.
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2.

Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle.
