# Piano funzionale — chat multiple, progetti separati e cancellazione completa

Data: 25 novembre 2026
Stato al 27 novembre 2026: registro cifrato, cronologia/memoria per workspace, migrazione legacy, repository documenti isolato, UI multi-chat e cancellazione locale coordinata sono implementati su main. CI Android API 34: 38/38 test strumentali superati; writer e verifica dopo force-stop superati. Restano limiti espliciti su media Galleria, archivio Documenti globale/OCR, backend remoto e prova su telefono fisico.

## Obiettivo

SuperIA deve consentire più conversazioni indipendenti e progetti separati. L'utente può passare da un progetto all'altro senza mescolare cronologia, memoria, documenti o file generati. L'eliminazione di una chat/progetto deve rimuovere i dati gestiti da SuperIA e, per i file multimediali esportati nella Galleria, avviare la procedura Android autorizzata quando richiesta.

## Modello funzionale

- Una chat ha ID UUID stabile, titolo, data creazione/ultima modifica, tipo (chat o progetto), stato archivio e cronologia.
- Nuova chat crea un contenitore vuoto e lo rende attivo.
- Ogni chat mantiene cronologia, memoria contestuale, documenti importati (copie private/estratti), richieste pendenti e riferimenti ai media.
- Rinominare, archiviare, ripristinare ed eliminare sono operazioni distinte.
- Un progetto è una chat con memoria e risorse proprie; nessuna memoria globale deve essere aggiunta automaticamente a un altro progetto.
- La chat storica attuale viene migrata in modo conservativo in una chat predefinita, senza perdere messaggi o richieste pendenti. Migrazione atomica e ripetibile; in caso di archivio corrotto o chiave mancante, bloccare e preservare i dati, mai sovrascrivere con archivio vuoto.

## Cancellazione completa

Alla conferma dell'utente, l'eliminazione deve:
1. bloccare nuove scritture e richieste pendenti per la chat selezionata;
2. cancellare cronologia e memoria cifrate della chat, relativi backup AtomicFile e metadati;
3. cancellare documenti/estratti importati nella copia privata di SuperIA e i relativi backup;
4. cancellare output generati, anteprime, cache, file temporanei e riferimenti locali attribuiti alla chat;
5. eliminare dal database remoto cronologia, memoria, documenti e artefatti della chat, quando il backend online sarà operativo, con endpoint idempotente e verifica ownership;
6. rimuovere la chat dall'elenco e scegliere una chat valida o una nuova chat vuota.

La cancellazione è definitiva nell'ambito dei dati sotto il controllo di SuperIA: niente cestino interno e niente ripristino.

## Galleria Android: limiti e comportamento richiesto

- I file generati devono essere inizialmente salvati in uno spazio privato dell'app, organizzati per chat ID, con metadati che associano ogni esportazione alla chat.
- L'esportazione nella Galleria è una copia esplicita dell'utente, non il luogo primario di lavoro.
- Registrare l'URI MediaStore dei soli file creati/esportati da SuperIA, insieme all'ID chat e al MIME type.
- Alla cancellazione, eliminare direttamente i file ancora nello spazio privato dell'app.
- Per elementi MediaStore creati dall'app, usare le API Android appropriate; se il sistema richiede consenso (ad esempio per file non più direttamente eliminabili dall'app), mostrare il dialogo di autorizzazione di Android e completare solo dopo esito positivo.
- Non cancellare immagini, video o documenti preesistenti dell'utente, file importati da altre app, né file copiati/spostati fuori dal perimetro tracciato da SuperIA.
- Se un file è stato rinominato, spostato, copiato o l'URI non è più valido, dichiarare che non è stato possibile rimuovere quella copia; non simulare un successo.
- Mostrare un riepilogo degli elementi eliminati e di quelli per cui Android non ha concesso la rimozione.

## Sicurezza e integrità

- Isolamento rigoroso per chat ID in storage locale e backend.
- Nessun ID/path fornito dall'utente può puntare fuori dalle directory canoniche SuperIA.
- Le operazioni di delete devono essere idempotenti, atomiche dove possibile e resistenti a crash/retry.
- Non cancellare la chiave Keystore condivisa finché esistono altre chat cifrate con quella chiave. La cancellazione della singola chat elimina solo i suoi archivi.
- Una futura funzione "elimina tutti i dati SuperIA" sarà separata e richiederà conferma distinta.
- I log non devono contenere testo chat, contenuto documenti o token.
- La cancellazione locale non deve essere dichiarata come cancellazione da backup del sistema operativo o da eventuali log tecnici del provider; i limiti di retention remota devono essere documentati.

## Sequenza di implementazione

1. Audit completo dei punti di persistenza, memoria, documenti, allegati, cache e UI attuali.
2. Introdurre archivio multi-chat cifrato con ID stabili, migrazione legacy atomica e test di recupero.
3. Collegare cronologia e memoria all'ID chat attivo; aggiungere creazione, selezione, rinomina e archivio.
4. Associare documenti e file locali a chat ID, senza rompere import/export esistente.
5. Introdurre registro delle esportazioni MediaStore e flusso di eliminazione autorizzato Android.
6. Implementare eliminazione coordinata locale/remota, con retry e risultati parziali espliciti.
7. Integrare elenco chat/progetti nel Menu e test UI/accessibilità.
8. Test unitari e strumentali per migrazione, isolamento, cancellazione, crash/retry e MediaStore; verificare CI senza confondere test JVM, emulator e telefono fisico.

## Criteri di completamento

- Due chat simultanee mantengono dati, memoria e documenti completamente separati.
- Riavvio app non perde né scambia chat.
- Cancellare una chat non altera le altre.
- Tutti i dati gestiti da SuperIA attribuiti alla chat sono rimossi localmente e, quando disponibile, anche dal backend.
- File esportati nella Galleria vengono rimossi solo dopo il consenso Android richiesto; copie esterne/non più controllabili sono segnalate senza dichiarazioni false.
- I test di migrazione e cancellazione non fanno ricomparire dati da backup AtomicFile.
- Nessun provider, hosting, database o servizio esterno viene attivato senza consenso esplicito; nessun costo è ammesso.


## Stato effettivo e limiti residui — 27 novembre 2026

- **Implementato:** creazione e selezione chat/progetti, rinomina, archivio, cronologia e memoria cifrate per workspace, migrazione legacy verificata, documenti importati nella chat isolati, contesto documentale selezionato associato al workspace, cancellazione definitiva locale con preflight e fail-closed.
- **Verificato in CI emulator API 34:** 38 test strumentali superati; test separati di scrittura dello stato pendente e recupero dopo `force-stop` entrambi `OK (1 test)`. Backend pytest e Android unit test superati nello stesso ciclo CI.
- **Non dichiarare:** cancellazione di foto/video dalla Galleria. Non esiste ancora un registro di URI MediaStore generati da SuperIA; i contenuti esportati fuori dall'app e i servizi esterni non sono controllabili.
- **Residui:** il pannello Documenti/OCR generale resta un archivio globale distinto e non viene eliminato insieme alla singola chat; non è stato implementato un endpoint remoto di cancellazione per workspace; nessun backend pubblico/provider è attivo; nessun test su telefono fisico è stato eseguito.
- **Prossima priorità sicura:** chiudere l'audit funzionale UI/accessibilità e chiarire il perimetro del pannello Documenti globale prima di ampliare la cancellazione ai media. La prova fisica resta distinta dalla CI emulator.
