# Collaudo pratico Android e chat online — SuperIA

Aggiornato: 21 novembre 2026

## Stato e vincolo APK
Questo protocollo sostituisce le vecchie istruzioni per scaricare APK e importare Gemma. Il runtime locale Gemma/LiteRT-LM è stato rimosso e non deve essere testato o reinstallato.

Il 24 novembre 2026 l'utente ha autorizzato una deroga circoscritta: GitHub Actions può generare e installare APK **debug temporanei esclusivamente nell'emulatore CI** per eseguire i test strumentali delle microfasi 29–30. Questa autorizzazione non consente build release, firma, distribuzione, pubblicazione o installazione di APK intermedi su telefoni personali. L'APK definitivo resta bloccato fino al superamento del gate online e all'autorizzazione finale. I test su dispositivi fisici sono una verifica separata e non sono attestati dai test CI.


## Collaudo su telefono fisico — microfasi 29 e 30

Il 24 novembre 2026 l'utente ha scelto esplicitamente di aggiungere il collaudo su telefono Android fisico per aumentare l'affidabilità delle verifiche. È quindi autorizzata una build **debug temporanea, non release**, limitata a questa validazione. Non è un APK finale, non va pubblicato o distribuito ad altri.

Il workflow `android-physical-device-test-apks` genera e conserva per 3 giorni:
- `app-debug.apk`: applicazione da installare sul telefono di prova;
- `app-debug-androidTest.apk`: pacchetto dei test strumentali.

### Procedura tecnica (serve un PC con Android Platform Tools/ADB)
1. Scaricare entrambi gli APK dall'artifact `superia-physical-device-test-apks` della GitHub Actions run.
2. Attivare temporaneamente Opzioni sviluppatore e Debug USB sul telefono, collegarlo al PC e autorizzare il computer.
3. Verificare il collegamento con `adb devices`.
4. Installare l'app e il pacchetto test:
   `adb install -r app-debug.apk`
   `adb install -r app-debug-androidTest.apk`
5. Eseguire la suite strumentale:
   `adb shell am instrument -w it.superiaprivata.app.test/androidx.test.runner.AndroidJUnitRunner`
6. Per il test di riavvio processo, eseguire prima il metodo di preparazione, arrestare davvero il processo e poi il metodo di verifica:
   `adb shell am instrument -w -e class it.superiaprivata.app.chat.ProcessRestartRecoveryInstrumentedTest#writePendingStateForProcessRestart it.superiaprivata.app.test/androidx.test.runner.AndroidJUnitRunner`
   `adb shell am force-stop it.superiaprivata.app`
   `adb shell am instrument -w -e class it.superiaprivata.app.chat.ProcessRestartRecoveryInstrumentedTest#verifyPendingStateAfterProcessRestart it.superiaprivata.app.test/androidx.test.runner.AndroidJUnitRunner`
7. Salvare l'output integrale dei test, modello telefono, versione Android e commit testato. Rimuovere i due pacchetti debug al termine del collaudo.

Se il PC non è disponibile, non improvvisare l'installazione del pacchetto test dal telefono: organizzare il test con ADB è necessario per lanciare in modo affidabile la suite strumentale.

### Stato
- Il run CI `45486302611` ha superato i test unitari Android ma **non ha eseguito alcun test strumentale**: l'emulatore non riusciva ad avviare il servizio Package Manager (`Can't find service: package`) durante l'installazione dell'APK. Non è un fallimento dimostrato del codice dei test.
- La build fisica separata prepara i due APK temporanei; l'esito reale dipenderà dall'esecuzione sul dispositivo.
- Microfasi 29 e 30 restano aperte fino alla registrazione dei risultati; il controllo su dispositivo fisico è ora parte del percorso di accettazione.
- Nessuna build release, firma di distribuzione o pubblicazione è autorizzata.

## Gate prima del test su telefono
1. Backend HTTPS pubblicato su hosting verificato gratuito, senza carta o addebiti.
2. Database persistente configurato e verificato dopo riavvio.
3. Rate limiting IP reale sul gateway/WAF attivo e testato prima di abilitare il pairing.
4. Provider AI gratuito approvato, modello/API verificati e chiavi conservate solo sul server.
5. Router verificato per failover, cooldown, quota esaurita e hard-stop senza pagamenti.
6. Endpoint chat con streaming, timeout, retry e gestione errori verificati.
7. Test backend e Android unitari superati; nessuna build APK prodotta.

## Piano di collaudo end-to-end dopo il gate APK
Quando il gate precedente è completo e la build definitiva è stata autorizzata, eseguire le prove su Realme C75 e Xiaomi Mi 10T.

### Connessione e pairing
- Verificare che l'endpoint HTTPS sia validato e che HTTP non venga accettato.
- Associare il dispositivo con codice temporaneo; verificare che il codice non venga salvato.
- Verificare che token e device ID siano cifrati localmente e che sul backend venga conservato soltanto l'hash del token.
- Verificare scadenza, revoca e rifiuto dei token scaduti/revocati.
- Verificare il rate limiting reale sul gateway/WAF con richieste ripetute e registrare il risultato.

### Conversazione
- Inviare più messaggi consecutivi e verificare che le risposte tengano conto dei turni precedenti.
- Chiudere e riaprire l'app; verificare cronologia cifrata, memoria e continuità della conversazione.
- Provare una conversazione lunga oltre la finestra del singolo prompt e verificare recupero selettivo senza inventare ricordi.
- Verificare streaming progressivo, annullamento, timeout, errori HTTP, retry e ripresa delle richieste pendenti.
- Verificare che il passaggio tra provider non apra una nuova conversazione e non perda il contesto.

### Provider, quote e rete
- Simulare provider non disponibile, risposta 429, errore 5xx e quota gratuita esaurita.
- Verificare cooldown e passaggio al successivo provider gratuito approvato.
- Quando tutti i provider sono indisponibili, verificare che la richiesta resti recuperabile e che non avvenga alcun addebito o fallback a pagamento.
- Disattivare la rete durante invio e risposta; verificare messaggio di stato e recupero senza duplicazioni.
- Riavviare backend e database; verificare persistenza di token, stato provider e cronologia prevista.

### Documenti e privacy
- Importare PDF, DOCX, TXT e CSV; verificare estrazione, OCR se applicabile, retrieval e contesto inviato.
- Verificare che i dati dei documenti non siano inviati se non selezionati/necessari.
- Verificare revoca dispositivo, cancellazione/esportazione prevista e minimizzazione dei log.
- Controllare che nessuna chiave provider, pairing code o token in chiaro compaia in log o risposte API.

## Scheda risultati da compilare
- Dispositivo e versione Android:
- Versione app/commit:
- Endpoint backend e data test:
- Pairing e revoca:
- Conversazione multi-turno e ripresa:
- Streaming e gestione errori:
- Provider primario e failover:
- Test quota zero e hard-stop:
- Documenti/OCR:
- Riavvio backend/database:
- Privacy e log:
- Tempi, crash, rete e batteria:
- Esito complessivo e difetti aperti:

Non dichiarare il collaudo superato se una prova è stata saltata o non è riproducibile. Non creare una nuova build per correggere singoli difetti prima che tutte le funzioni chat online siano pronte; raccogliere i difetti e integrarli prima dell'unica build definitiva.

## Preparazione del collaudo recovery — 24 novembre 2026

Questa sezione è una preparazione documentale e statica: **non attesta l'esecuzione di test strumentali o prove su dispositivo**.

### Verifiche statiche effettuate
- Suite `ChatHistoryStoreInstrumentedTest` presente con casi per: archivio cifrato AES-GCM, memoria cifrata, perdita della chiave Keystore (fail-closed e file preservato), archivio corrotto, recupero AtomicFile da backup, cancellazione senza resurrection, migrazione legacy, concorrenza lettura/scrittura, ID e payload pendenti.
- Il test `pendingRequestIdSurvivesStoreRecreationForSafeRetry` crea una nuova istanza dello store nello stesso processo: **non** simula process death e non deve essere conteggiato come prova di riavvio.
- La suite Android CI continua a eseguire esclusivamente `gradle --no-daemon testDebugUnitTest`. Il task `connectedDebugAndroidTest` costruisce e installa APK di test: resta vietato dal blocco APK attuale e non è stato avviato.

### Sequenza da eseguire quando il gate e la build definitiva saranno autorizzati
1. Registrare commit, versione Android, modello dispositivo, livello API e stato iniziale di rete/batteria.
2. Installare una sola build definitiva autorizzata; verificare avvio, pairing HTTPS e persistenza delle credenziali.
3. Inviare una domanda e interrompere la rete durante lo streaming; verificare che la risposta parziale resti visibile e la richiesta resti recuperabile.
4. Chiudere il processo app dal sistema operativo (non solo ricreare Activity), riaprire e verificare che la richiesta pendente e il payload originale siano recuperati.
5. Ripristinare la rete e usare “Riprendi richiesta”; verificare stesso request ID, nessun duplicato nella cronologia e completamento coerente.
6. Ripetere con Wi-Fi e rete mobile, includendo sospensione/riattivazione e riavvio del dispositivo.
7. Verificare separatamente cancellazione cronologia, cancellazione memoria progetto, esportazione TXT e comportamento fail-closed in caso di archivio/chiave non disponibili.
8. Allegare log sanitizzati e risultati per ogni caso; non includere token, codici pairing, chiavi o contenuti privati.

### Criteri di accettazione recovery
- Nessun messaggio utente o risposta già persistita viene duplicato dopo retry.
- Il retry usa lo stesso request ID e lo stesso payload provider-ready salvato prima della prima chiamata di rete.
- Una risposta parziale non viene spacciata per risposta completata; un EOF senza evento SSE `done` resta un errore recuperabile.
- Dati cifrati corrotti o chiave Keystore mancante non vengono trasformati silenziosamente in cronologia/memoria vuota né sovrascritti.
- La cancellazione esplicita non permette il ripristino di copie `.bak`.
- Ogni caso deve essere eseguito e registrato su entrambi i dispositivi previsti; una prova mancante mantiene aperta la microfase 29.

**Stato aggiornato al 24 novembre 2026:** autorizzati APK debug temporanei esclusivamente in CI per i test strumentali. Il workflow esegue test su emulatore API 35 e sequenza force-stop/recupero. La verifica su telefono fisico, Wi-Fi/rete mobile e riavvio del dispositivo resta obbligatoria e separata.
