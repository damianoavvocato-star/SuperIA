# Verifica persistenza chat durante lo streaming — 23 novembre 2026

## Modifiche verificate

- Commit `ec03c7d329f6c5945e7e7d29735ae674372f5ea1`: corretto il test della generazione ID. Con gli ID 42, 43 e 44 già occupati, il candidato 42 deve produrre 45; la funzione di produzione era corretta, l'aspettativa del test era errata.
- Commit `12fe5d19bbc58d7c1f4f39a2ff747a4093d1a347`: il normale invio salva la cronologia con messaggio utente pending e segnaposto assistant prima di iniziare la richiesta di rete. Nel percorso di ripresa è stato rimosso un salvataggio duplicato; resta un singolo salvataggio del segnaposto prima dello streaming.
- La persistenza dei delta continua a usare snapshot cifrati periodici e una coda serializzata con versioni, in modo che uno snapshot precedente non sovrascriva uno più recente.

## CI sul commit 12fe5d1

- Android unit tests: [run 45158387411](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387411) — SUCCESS, `gradle --no-daemon testDebugUnitTest`, BUILD SUCCESSFUL in 38s.
- Backend tests: [run 45158387265](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387265) — SUCCESS, **116 passed, 1 warning in 1.57s**.
- Il workflow Android esegue soltanto `testDebugUnitTest`; non esegue assembleDebug/assembleRelease e non produce APK.

## Stato e limiti

Questa verifica conferma i test automatici e il percorso di salvataggio preventivo nel codice. Non dimostra ancora il recupero dopo terminazione forzata del processo, riavvio del telefono, sospensione Android o perdita reale della rete. Queste prove richiedono un dispositivo/emulatore e il backend online reale; le micro-fasi 26, 28 e 29 restano quindi in attesa di verifica pratica secondo il piano. Il gate 36 resta bloccato e non è autorizzata alcuna build APK.

## Prossima verifica tecnica

Eseguire test strumentali su dispositivo/emulatore per: invio e chiusura forzata prima del primo delta; chiusura durante delta; riapertura e retry con lo stesso request ID; risposta già completata/replay; rete interrotta e ripristinata. Verificare inoltre che l'archivio AES-GCM rimanga leggibile dopo ogni interruzione.


## Verifica aggiuntiva dopo l'hardening — 23 novembre 2026

Sul commit `786823c12afee6f4c94c942f4afd1c250f6993bb` i controlli Android unit e backend hanno entrambi concluso con successo. Il commit successivo `827ad1b35e41b00767235dab5a67496c75becad9` ha inoltre riallineato il KDoc di `StreamHistoryPersister` e aggiunto il test `messageIdGenerationSkipsOccupiedIdAfterOverflow`, che verifica che la generazione dell'ID salti anche `Long.MIN_VALUE` quando è già occupato dopo il wrap da `Long.MAX_VALUE`.

- Android CI [run 45161032871](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45161032871): **SUCCESS**, workflow limitato a `testDebugUnitTest`.
- Backend CI [run 45161032819](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45161032819): **SUCCESS**.
- Le prove CI non sostituiscono il test instrumented/E2E su dispositivo reale: terminazione forzata, riavvio, rete interrotta e recupero dello stream restano da verificare. Non dichiarare completate le micro-fasi 26, 28 o 29 sulla sola base di questi run.


## Verifica della sincronizzazione del recupero — 23 novembre 2026

Il commit `790e88da53c2e4e9bf649e465f354e85cd7cad0d` coordina anche i salvataggi di recupero con la persistenza periodica dei delta. Nei percorsi di invio normale e retry, il persister viene creato prima dello streaming; i catch di cancellazione, errore di memoria ed eccezione ordinaria invocano `flush()` in contesto non cancellabile, invece di scrivere direttamente un nuovo snapshot fuori dalla coda serializzata. In questo modo i salvataggi finali condividono mutex e controllo versione con gli snapshot periodici.

- Android CI [run 45162213509](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45162213509): **SUCCESS**, test unitari Android completati.
- Backend CI [run 45162213485](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45162213485): **SUCCESS**, test backend completati.
- Il workflow Android continua a eseguire soltanto `gradle --no-daemon testDebugUnitTest`; nessun task di assemblaggio APK è stato eseguito.

Questa è evidenza CI, non prova pratica su telefono. Restano da eseguire test di chiusura forzata, riavvio, sospensione e perdita/ripristino della rete; le micro-fasi 26, 28 e 29 non vengono chiuse e il gate 36 resta bloccato.

## Sincronizzazione archivio tra istanze — 23 novembre 2026

I commit `e811ca6be1f7811a205c393e36165ea4c893a73b`, `92d11e3724b759516241771ea7be0669aa955735` e `878a4323ebe03f109902e0d763e8fd8f955a77f2` estendono la protezione al ciclo completo di cifratura e aggiungono test instrumented per:
- due istanze di `ChatHistoryStore` che scrivono contemporaneamente trascrizioni complete;
- letture concorrenti durante la sostituzione dell'archivio, che devono restituire soltanto uno snapshot completo;
- letture/scritture concorrenti della memoria di progetto;
- propagazione degli errori dai task concorrenti tramite `Future.get(timeout)`.

La sincronizzazione usa il monitor condiviso statico `ARCHIVE_WRITE_LOCK` anche durante l'accesso/creazione della chiave Android Keystore, cifratura AES-GCM, lettura e scrittura atomica. I metodi annidati riutilizzano lo stesso monitor Java reentrant.

### CI commit 878a432

- Android [run 45211341253](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45211341253): **SUCCESS**, workflow `android-unit-tests`; task `gradle --no-daemon testDebugUnitTest`.
- Backend [run 45211341376](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45211341376): **SUCCESS**, workflow `test`.
- Branch: solo `main`.

**Limite esplicito:** i nuovi test si trovano in `androidTest` e non sono eseguiti da `testDebugUnitTest`. Non risultano ancora eseguiti su emulatore/dispositivo; non si dichiara quindi superata la verifica pratica della micro-fase 29. Restano necessari i test instrumented e le prove force-stop/reopen/retry, perdita rete e recupero. Nessun APK generato; nessun servizio esterno attivato.

### Verifica aggiuntiva della prima inizializzazione Keystore — commit f459de5

Il test instrumented `concurrentStoreInstancesSerializeAtomicTranscriptWrites` elimina ora la chiave Android Keystore dedicata prima di avviare due istanze dello store in parallelo. In questo modo il test, quando sarà eseguito su Android, coprirà anche la condizione di prima inizializzazione concorrente della chiave, non soltanto la scrittura con una chiave già presente.

CI del commit `f459de5fe9a5756b688f4acafa2caab91b810cab` avviata:
- Android [run 45213497131](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213497131): in esecuzione al momento dell'aggiornamento.
- Backend [run 45213496563](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45213496563): in esecuzione al momento dell'aggiornamento.

Il test resta instrumented e richiede un device/emulatore; non viene eseguito dal task unitario CI. Non considerare superata la verifica pratica della micro-fase 29.
