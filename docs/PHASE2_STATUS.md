# Fase 2 — Motore AI centrale e assistente

## Stato

**Stato:** implementazione e CI superate; collaudo fisico in attesa.

La base Android dell'assistente locale è implementata e compilata. La Fase 2 resta aperta fino alla verifica end-to-end con il modello importato su dispositivi reali.

## Funzioni presenti

- Chat nativa Kotlin/Jetpack Compose.
- Adapter LiteRT-LM Android 0.16.1 con backend CPU e nessun fallback cloud.
- Catalogo iniziale Gemma 3 1B IT INT4; file `.litertlm` non incluso nell'APK.
- Apertura delle pagine ufficiali del modello e dei termini Gemma; consenso esplicito richiesto prima dell'importazione/uso.
- Importazione via Android document picker nello storage privato dell'app.
- Controllo estensione, file non vuoto, limite massimo 2 GB e calcolo SHA-256.
- Sostituzione preferibilmente atomica; fallback backup/ripristino quando il filesystem non supporta `ATOMIC_MOVE`.
- Cronologia chat AES-GCM con chiave Android Keystore.
- Ricostruzione del contesto dai 12 messaggi più recenti, con limite 6.000 caratteri, per mantenere continuità dopo la ricreazione del runtime.
- Pulizia delle risorse native se l'inizializzazione fallisce; errore esplicito se la generazione restituisce testo vuoto.
- Nessuna API a pagamento, nessun server obbligatorio e nessuna risposta simulata.

## Verifiche CI

Commit: `8ba080ad4f43690f25c63f74d2ff7c3941944b5a`

- [Android debug build e unit test — run 43758098267](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43758098267): SUCCESS.
- [Backend tests — run 43758098112](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43758098112): SUCCESS.
- APK debug artefact: `superia-privata-debug`, ID `14178604470`.
- Dimensione archivio: 38.276.044 byte.
- SHA-256 archivio: `ff54a42a7fdcb49b986e9da31c0fdfe6939b49322241b49d9d6450fb6dbefda4`.
- Scadenza artefatto: 13 febbraio 2027.

## Gate pratici ancora aperti

1. Ottenere il modello Gemma 3 1B IT INT4 dalla fonte ufficiale e accettarne i termini.
2. Installare l'APK e importare il file `.litertlm` sul Realme C75.
3. Eseguire richieste reali, riavviare l'app e verificare che il contesto recente venga mantenuto.
4. Registrare RAM disponibile, tempo alla prima risposta, velocità, temperatura, consumo batteria ed eventuali errori.
5. Ripetere le stesse prove sullo Xiaomi Mi 10T.
6. Correggere eventuali incompatibilità e ripetere CI.

Il modello pesa circa 584 MB secondo il catalogo attuale, ma l'utilizzo reale richiede memoria aggiuntiva per runtime e contesto. La soglia minima iniziale di 2 GB RAM disponibile è provvisoria, non una garanzia di prestazioni.

## Criterio di chiusura

La Fase 2 può essere dichiarata completata solo dopo inferenza locale reale su entrambi i telefoni, continuità della conversazione verificata e registrazione delle misure di stabilità/prestazioni. Il successo CI dimostra compilazione e test automatici, non sostituisce questo collaudo.


## Aggiornamento tecnico — 16 novembre 2026

- Aggiunti test unitari per limiti di memoria/cronologia e validazione del parametro `maxHistoryMessages` nel commit `516b5e2d9b3fdf2b8e4628472143480858075e05`; backend CI SUCCESS sul medesimo commit.
- Corretto un problema di duplicazione del contesto: il builder ricostruisce il transcript dall'archivio cifrato a ogni richiesta, mentre LiteRT-LM manteneva anche la conversazione in memoria. Ora, quando il modello non cambia, l'adapter riutilizza il motore ma crea una conversazione LiteRT-LM nuova per ogni richiesta, così la cronologia ricostruita viene inviata una sola volta. Modifica nel commit `5f660ca2978e2405810d1dda6b663ce30d7ff04d`.
- CI Android del commit `516b5e2...`: unit test SUCCESS; instrumentation emulator ancora in corso al controllo. CI Android del commit `5f660ca...`: appena avviata/queued, da verificare prima di considerare validata la modifica.
- Un precedente run Android (`43888225880`) è fallito durante il setup dell'emulatore per errore di lettura ZIP del pacchetto Android Emulator e porta TCP 5554 non raggiungibile; il job non ha raggiunto i test strumentali. Non è evidenza di un difetto dei test o del codice applicativo.

Restano obbligatori i test con modello Gemma realmente importato su Realme C75 e Xiaomi Mi 10T. Nessun risultato CI può sostituire queste prove.


## Aggiornamento CI e correzioni successive — 16 novembre 2026

- Commit `5f660ca2978e2405810d1dda6b663ce30d7ff04d`: conversazione LiteRT-LM ricreata a ogni turno mantenendo il motore del modello, per evitare di duplicare il transcript che viene ricostruito dall'archivio cifrato. Backend CI sul commit: SUCCESS.
- Commit `fa712c9b3e7064c607e17d97133a8ad5e6a0b2f9`: retry ADB e verifica esplicita del completamento del boot prima dei test strumentali. Backend CI sul commit: SUCCESS.
- Android CI sul commit `fa712c9...` (run `43890080541`): in corso durante l'ultimo controllo, ancora nella fase di setup Gradle; test unitari, instrumentation, APK e artifact non ancora attestati su questo commit.
- I run Android precedenti `43888872060` e `43889593074` risultavano ancora in corso nella fase di instrumentation. Non sono disponibili risultati finali e non vanno interpretati come successi.

**Gate aperti:** CI Android completa sul commit corrente; test Keystore strumentali; installazione e inferenza reale Gemma su Realme C75 e Xiaomi Mi 10T; misure di RAM, latenza, stabilità, temperatura e batteria. Fase 2 non chiusa.


## Aggiornamento CI — 16 novembre 2026 (run 43890341130)

- [Android run 43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130), commit `a82cf5ca255ebcb9b619d773768adb97b87facee`: test unitari Android SUCCESS, build APK debug SUCCESS e upload SUCCESS.
- Artifact `superia-privata-debug`, ID `14235164539`, 59.756.044 byte, SHA-256 archivio `663b62c4edb90da36e3885c7ab1d4fd0a9a8541a9bb18feb3207ffe44bc644c8`, scadenza 14 febbraio 2027.
- Al controllo delle 15:54 CET, i test strumentali Android Keystore risultavano ancora IN_PROGRESS; non si dichiara la run Android completa né la suite strumentale superata su questo commit.
- Backend run [43890360518](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890360518): SUCCESS.

**Stato Fase 2 invariato:** compilazione e test unitari della revisione verificati; test strumentali in corso; inferenza Gemma reale e collaudo sui due telefoni ancora da eseguire.


## Aggiornamento CI — 16 novembre 2026 (run 43890341130)

- [Android run 43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130), commit `a82cf5ca255ebcb9b619d773768adb97b87facee`: **SUCCESS**.
- Test unitari Android, build APK Debug e upload dell'artefatto: **SUCCESS**.
- Test strumentali Android Keystore: **6 test avviati e 6 terminati** su emulatore API 34; `connectedDebugAndroidTest` e Gradle **BUILD SUCCESSFUL**.
- Artefatto `superia-privata-debug`, ID `14235164539`, 59.756.044 byte; SHA-256 archivio `663b62c4edb90da36e3885c7ab1d4fd0a9a8541a9bb18feb3207ffe44bc644c8`; scadenza 14 febbraio 2027.
- I risultati CI non sostituiscono i test di inferenza locale Gemma reale, streaming, ripresa dopo chiusura/riavvio e benchmark su Realme C75 e Xiaomi Mi 10T.

**Stato:** CI Android completa superata sulla revisione indicata; Fase 2 ancora aperta fino al collaudo end-to-end e fisico sui due dispositivi.


## Prossimo gate operativo — 16 novembre 2026

La CI Android [43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130) è SUCCESS: unit tests, APK Debug e 6 test strumentali Keystore su emulatore API 34 completati. Tre controlli backend dei commit documentali precedenti sono anch'essi SUCCESS.

Il prossimo gate non è un altro test emulato: è la prova end-to-end con il modello Gemma importato sui dispositivi fisici. Procedura e scheda di misurazione: [DEVICE_SMOKE_TEST.md](DEVICE_SMOKE_TEST.md).

**Fase 2 resta parziale** fino ai risultati effettivi su Realme C75 e Xiaomi Mi 10T. Non è autorizzato dichiarare completata la fase sulla sola base della CI.
