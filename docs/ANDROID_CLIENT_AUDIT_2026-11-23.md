# Audit client Android — micro-fase 05

Data: 23 novembre 2026  
Repository: `damianoavvocato-star/SuperIA`  
Ambito: audit statico dei sorgenti Android su `main`; nessun APK creato.

## File esaminati
- `android/app/src/main/java/it/superiaprivata/app/chat/OnlineAssistantClient.kt`
- `android/app/src/main/java/it/superiaprivata/app/chat/AssistantChatPanel.kt`
- `android/app/src/main/java/it/superiaprivata/app/chat/ChatHistoryStore.kt`
- `android/app/src/main/java/it/superiaprivata/app/chat/ChatPromptBuilder.kt`
- `android/app/src/test/java/it/superiaprivata/app/chat/OnlineAssistantClientTest.kt`
- `android/app/build.gradle.kts`
- `.github/workflows/android-build.yml`

## Verifiche positive
1. Le chiamate di pairing, readiness e chat rifiutano URL non HTTPS, URL con user-info, query o fragment e disabilitano i redirect HTTP.
2. Pairing usa `POST /api/v1/device/pair`; il codice viene richiesto in UI e svuotato dopo il successo. Il token e il device ID sono conservati tramite cifratura AES-GCM con chiave Android Keystore.
3. La chat usa `POST /api/v1/chat/stream`, Bearer per dispositivo, request ID UUID e limite prompt di 32.000 caratteri.
4. La cancellazione della coroutine scollega la connessione HTTP; gli eventi SSE delta/done/error sono gestiti dal client.
5. Cronologia e memoria progetto sono archiviate separatamente e cifrate AES-GCM con chiave non esportabile Android Keystore; la cronologia usa AtomicFile e compressione GZIP.
6. La ricostruzione del prompt limita cronologia e contesto documentale e separa esplicitamente memoria/documenti come contesto, non istruzioni.
7. La UI conserva le richieste pendenti e il request ID per il recupero manuale; i delta sono persistiti progressivamente.
8. Il client ha test unitari per HTTP non sicuro, credenziali mancanti, URL con parametri/user-info/fragment, prompt vuoto o eccessivo e request ID malformato.

## Rilievi e limiti
- Il metodo non-streaming `OnlineAssistantClient.send()` e il vecchio endpoint `/api/v1/chat` sono stati rimossi nella micro-fase 24; il client Android usa esclusivamente SSE.
- La readiness descrive disponibilità e configurazione dei provider in base al payload, ma non prova una generazione reale; l'interfaccia stessa avverte che il primo invio verifica la risposta.
- `ChatPromptBuilder` costruisce il contesto multi-turno in un unico prompt testuale inviato come singolo messaggio user. Non è un protocollo con ruoli multipli lato provider.
- La ripresa della richiesta ripete la generazione con lo stesso request ID; non riprende byte/token dello stream interrotto. I limiti exactly-once in caso di crash tra generazione provider e persistenza sono già documentati nel backend.
- L'archivio usa una chiave Android Keystore locale: backup/restore su altro dispositivo non rende automaticamente leggibili gli archivi cifrati; va mantenuta una chiara gestione del cambio dispositivo.
- L'audit statico non prova il comportamento su dispositivo fisico, la protezione effettiva del sistema operativo, né il collegamento a un backend pubblico.

## Esito
Micro-fase 05: **Completata** come audit statico e mappatura del client. I rilievi non sono stati modificati in questa fase per evitare refactor non coperti da test; sono candidati per fasi specifiche successive. La validazione CI Android è separata nella micro-fase 06 e resta da acquisire. Nessun APK generato.
