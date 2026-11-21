# SuperIA — Android

## Direzione tecnica attuale
L'app Android è online-only per l'inferenza AI. Non usa Gemma, LiteRT-LM, Ollama o altri modelli LLM locali. Le risposte sono richieste al backend SuperIA attraverso HTTPS.

## Funzioni presenti nel codice
- App nativa Kotlin + Jetpack Compose.
- Chat unica con cronologia locale cifrata AES-GCM.
- Client HTTPS per POST /api/v1/chat, con Bearer token.
- Pairing dispositivo tramite POST /api/v1/device/pair; token e device ID vengono conservati cifrati tramite Android Keystore. Il codice di pairing non viene salvato.
- Token individuali revocabili, con scadenza gestita dal backend.
- Configurazione endpoint/token e verifica della disponibilità del backend tramite endpoint autenticati.
- Importazione PDF, DOCX, TXT e CSV; estrazione testo e archivio documentale cifrato.
- Ricerca lessicale BM25 e recupero dei passaggi pertinenti nel prompt.
- OCR offline dei PDF scansiti tramite Tesseract, se è stato importato il dizionario italiano.
- Profilo dispositivo e diagnostica di base.
- Salvataggio della richiesta pendente e possibilità di riprenderla in caso di errore di rete.

## Rimozione del modello locale
La dipendenza LiteRT-LM e i sorgenti Android per motore, catalogo e importazione Gemma sono stati rimossi. La migrazione dell'app aggiornata elimina la precedente cronologia locale della chat una sola volta; non elimina la memoria di progetto né l'archivio documenti.

## Verifica CI e blocco APK
Il workflow Android è stato modificato per eseguire soltanto i test unitari: gradle --no-daemon testDebugUnitTest. Non deve eseguire assembleDebug, assembleRelease, firma o upload di APK. Non dichiarare un nuovo esito CI senza una run successiva verificata.

Su richiesta dell'utente, nessun APK intermedio sarà generato. La build APK definitiva potrà essere prodotta soltanto dopo il completamento e la verifica end-to-end di tutte le funzioni necessarie alla chat online.

## Requisiti per la connessione online
- Backend HTTPS pubblico, funzionante e con database persistente.
- Rate limiting IP reale sul gateway/WAF per il pairing prima dell'esposizione pubblica.
- Token individuale ottenuto tramite pairing e revocabile.
- Provider gratuiti attivati solo dopo audit delle condizioni: nessun pagamento, carta, ricarica o addebito automatico.
- Quota esaurita = stop o passaggio a un altro provider gratuito approvato; mai fallback a pagamento.
- Le chiavi dei provider restano sul server e non vengono inserite nell'APK.

## Stato di rilascio
La chat e i componenti online sono presenti nel codice, ma l'app non è ancora collegata a un backend pubblico operativo. Hosting, database, rate limiting gateway, segreti di produzione, provider e collaudo end-to-end restano da completare. Il progetto non è pronto per il rilascio.

## Requisiti di build
- Android Studio con JDK 17.
- Android SDK Platform 35.
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2.

Aprire questa cartella (android/) in Android Studio e attendere la sincronizzazione Gradle.