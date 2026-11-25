# Audit iniziale — multi-chat e cancellazione per chat

Data: 25 novembre 2026
Branch verificato: main
Ambito: revisione statica dei file Android correnti; nessun test runtime eseguito.

## File verificati

- `android/app/src/main/java/it/superiaprivata/app/chat/ChatHistoryStore.kt`
- `android/app/src/main/java/it/superiaprivata/app/chat/AssistantChatPanel.kt`
- `android/app/src/main/java/it/superiaprivata/app/documents/LocalDocumentRepository.kt`
- `android/app/src/main/java/it/superiaprivata/app/MainActivity.kt`

## Stato attuale accertato

### Cronologia e memoria
- `ChatHistoryStore` salva un singolo archivio `conversation_history.enc` e una singola memoria `project_memory.enc` in `filesDir`.
- La cronologia è compressa e cifrata AES-GCM con chiave Android Keystore; usa `AtomicFile`.
- `AssistantChatPanel` carica una sola cronologia e una sola memoria all'avvio.
- Il comando UI `Svuota` chiama `historyStore.clear()`: svuota la cronologia corrente ma non è una cancellazione di chat/progetto e non elimina la memoria progetto.
- Non esiste un identificatore stabile di conversazione né un indice/lista di chat.

### Documenti
- `LocalDocumentRepository` conserva tutti i documenti cifrati sotto `filesDir/documents`, senza chat ID.
- Il prompt builder legge l'intero archivio documenti; l'interfaccia dichiara che il documento resta consultabile anche nelle prossime chat.
- L'importazione collega il testo estratto alla conversazione corrente in memoria UI, ma il documento persistito resta globale e non attribuito a una conversazione.
- La cancellazione selettiva per chat non è quindi implementabile in sicurezza finché i documenti non hanno un proprietario chat esplicito.

### UI e file esportati
- `AssistantChatPanel` mostra una singola schermata Chat, Impostazioni, esportazione TXT e comando Svuota.
- `MainActivity` alterna Chat e Menu/Funzioni; non esiste ancora un elenco chat/progetti.
- L'esportazione TXT usa il picker Android `CreateDocument`: il file viene scritto nell'URI scelto dall'utente. Non viene conservato un registro di queste copie e SuperIA non deve presumere di poterle eliminare in seguito.
- Non sono stati individuati nel codice verificato flussi di generazione immagini/video con salvataggio MediaStore. Il modulo multimediale è separato e va auditato prima di estendere la cancellazione a output futuri.

## Rischi e decisioni tecniche

1. Non trasformare il singolo archivio in un elenco senza migrazione atomica: il vecchio archivio deve essere conservato fino a quando il nuovo archivio multi-chat è stato scritto e verificato.
2. Non associare automaticamente tutti i documenti preesistenti alla prima chat e poi cancellarli: finché non è chiara la provenienza, conservarli in una raccolta legacy separata e chiedere una scelta/migrazione controllata.
3. Ogni richiesta pendente/retry deve restare vincolata al suo chat ID e non essere riprodotta in un'altra conversazione.
4. Le copie esportate con il picker sono sotto il controllo dell'utente; per i futuri media generati occorre un registro delle URI MediaStore create da SuperIA.
5. Il delete locale deve rimuovere soltanto dati attribuiti con certezza alla chat; copie esterne, rinominate o spostate non vanno dichiarate cancellate.
6. La memoria progetto attuale è globale: la migrazione deve mantenerla nella chat legacy/default, senza propagarla alle nuove chat.

## Esito

L'audit conferma che la funzione richiesta è un cambiamento strutturale, non una modifica cosmetica. Il lavoro deve procedere per passi piccoli: archivio multi-chat cifrato e migrazione, poi isolamento documenti e UI, quindi tracciamento MediaStore e cancellazione coordinata. Nessun test nuovo, APK, deploy, backend o servizio esterno è stato avviato in questo audit.
