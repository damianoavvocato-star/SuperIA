# Architettura implementativa — isolamento dati multi-chat

Data: 25 novembre 2026

## Stato di partenza verificato
- `AssistantChatPanel` crea un solo `ChatHistoryStore(context)` per tutta la schermata.
- Il pannello carica la cronologia una volta con `remember(historyStore)` e la mantiene in una `mutableStateListOf`.
- La memoria progetto viene caricata una volta dallo stesso store e viene passata a ogni richiesta AI.
- `StreamHistoryPersister` e le routine di retry scrivono direttamente nello store catturato all'avvio del pannello.
- Il contesto documenti usa `LocalDocumentRepository(context).list()`, quindi l'intero archivio documenti condiviso entra nel contesto delle richieste.
- La cancellazione corrente `historyStore.clear()` cancella la cronologia globale, non memoria, documenti o esportazioni.

## Vincolo architetturale
Non basta cambiare il titolo o aggiungere un selettore: lo store e il persister devono essere ricreati per l'ID attivo, e ogni coroutine di generazione deve continuare a scrivere nello store che possiede la richiesta anche se l'utente naviga altrove. Non si deve mai cambiare l'ID sotto una richiesta pendente.

## Strategia di migrazione sicura
1. Creare un workspace legacy stabile e conservarne UUID nell'indice cifrato.
2. Copiare byte-per-byte gli archivi cifrati legacy di cronologia e memoria nei percorsi del workspace legacy; la copia usa la medesima chiave Keystore già esistente, senza decrittare o ricifrare.
3. Verificare che gli archivi di destinazione si aprano e decifrino correttamente prima di aggiornare l'indice/marker di migrazione.
4. Scrivere il marker di migrazione con AtomicFile/commit durevole.
5. Solo dopo il marker verificato, considerare i vecchi percorsi come alias obsoleti; non eliminarli nella stessa prima release di migrazione.
6. In caso di crash, ripetere la copia in modo idempotente; non sovrascrivere una destinazione valida e non interpretare un archivio illeggibile come vuoto.
7. Testare interruzione tra ogni passaggio e preservazione dei payload pendenti (requestId + requestMessages).

## API storage richiesta
- `ChatHistoryStore(context, workspaceId)` deve risolvere percorsi distinti per cronologia e memoria.
- Le API senza workspaceId restano temporaneamente compatibili per i test e per il recupero legacy, non per nuove chat.
- I metodi `save/load/clear` e `saveProjectMemory/loadProjectMemory/clearProjectMemory` devono operare esclusivamente sul workspace indicato.
- Un workspace non deve essere cancellato dall'indice finché i relativi archivi, documenti e riferimenti media non sono stati eliminati o esplicitamente segnalati come residui.

## UI e concorrenza
- La navigazione chat deve avvenire solo quando non c'è una generazione in corso, oppure mantenere il job associato al suo workspace e impedire scritture nello workspace visualizzato per errore.
- Cambiando workspace si ricostruiscono cronologia, memoria, allegati selezionati e stato errori specifici; configurazione backend e pairing restano invece globali al dispositivo.
- Ogni callback asincrona deve catturare un ID immutabile e controllare che non scriva su un workspace diverso.
- La selezione attiva deve sopravvivere al riavvio e ricadere su un workspace valido se l'ultimo è stato archiviato.

## Documenti e cancellazione
- `LocalDocumentRepository` dovrà accettare workspaceId e salvare sotto `filesDir/documents/<workspaceId>/`, con registro delle esportazioni per workspace.
- I documenti globali esistenti non vengono assegnati automaticamente a tutti i workspace. Durante la migrazione restano nello spazio legacy e visibili solo al workspace legacy.
- La cancellazione coordina prima il blocco scritture/generazioni, poi archivi, AtomicFile backup, documenti, cache e registro MediaStore; il workspace viene tolto dall'indice solo dopo esito coerente.
- MediaStore richiede API Android e consenso di sistema quando applicabile; file importati o copie esterne non vanno cancellati.

## Test obbligatori prima dell'attivazione UI
- Isolamento di due cronologie e due memorie.
- Retry pendente conserva requestId e requestMessages dopo migrazione.
- Crash/retry in ogni fase della migrazione.
- Navigazione durante stream non provoca scritture incrociate.
- Cancellazione di A non tocca B, inclusi .bak e file documenti.
- Indice corrotto o chiave mancante fallisce in modo esplicito senza creare archivi vuoti.
- Test strumentali emulatori e CI devono avere esito verificabile; la suite non va dichiarata superata se non eseguita.

## Non obiettivi di questo incremento
Nessun deploy, provider AI, database, account esterno, APK release o modifica al backend.

## Stato di implementazione — migrazione legacy
- `ChatWorkspaceStore.ensureLegacyWorkspace()` crea un workspace con UUID stabile per la conversazione preesistente.
- `ChatHistoryStore.migrateToWorkspace()` normalizza prima il vecchio formato SharedPreferences, copia byte-per-byte gli archivi cifrati AtomicFile e rilegge la destinazione per verificarne il contenuto. Se trova una destinazione differente, interrompe senza sovrascriverla.
- `ChatWorkspaceMigration` registra un marker durevole soltanto dopo la verifica. La migrazione è ripetibile dopo un'interruzione e non elimina i file sorgente.
- `AssistantChatPanel` esegue la migrazione prima del caricamento e non richiama più `clearLegacyLocalHistoryOnce()`, che era distruttiva per l'archivio legacy.
- La migrazione riguarda solo cronologia e memoria. I documenti restano condivisi/legacy finché non sarà implementato il repository per workspace.
