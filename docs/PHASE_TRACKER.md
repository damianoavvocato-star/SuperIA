# Registro operativo delle fasi — SuperIA Privata

Aggiornato: 15 novembre 2026  
Ambito: esclusivamente il repository **SuperIA-Privata**. La roadmap ufficiale resta `docs/ROADMAP.md`; questo documento ne rende verificabili stato, attività residue e criteri di chiusura. Il progetto resta Android-only, personale, gratuito senza API a pagamento obbligatorie e local-first.

## Legenda degli stati

- **Documentazione consolidata**: analisi e decisioni scritte; non significa implementazione.
- **Implementazione + CI verificate**: codice presente e controlli automatici indicati superati; non equivale a test su telefono.
- **In corso / parziale**: esistono componenti, ma mancano funzioni o verifiche.
- **Da avviare**: pianificata, senza implementazione funzionale sufficiente.
- **Trasversale**: requisiti da applicare a tutte le fasi.

## Quadro complessivo

Sono previste **16 fasi numerate da 0 a 15**: una fase preparatoria (0) e quindici fasi di sviluppo/validazione (1–15). Nessuna fase viene dichiarata integralmente completata in questo registro: per la Fase 0 resta l'audit puntuale; per la Fase 1 mancano integrazioni e prove; per la Fase 2 manca il test end-to-end su dispositivi reali; le altre sono pianificate o trasversali.

| Fase | Stato operativo | Lavoro residuo principale |
|---|---|---|
| 0 — Inventario e progettazione | Documentazione consolidata; audit individuale incompleto | Completare verifica nominativa delle 182 IA: licenze/termini, costi correnti, disponibilità, artefatti, requisiti e alternative Android; aggiornare fonti e checksum |
| 1 — Fondamenta Android e connessioni | Parziale | Completare architettura/storage, pairing autenticato con handshake e trasporto reale, integrazione GitHub e test su telefoni; QR attuale è solo invito, non pairing |
| 2 — Motore AI e assistente | Implementazione + CI verificate; test su device mancanti | Verificare import e inferenza Gemma reale, streaming, memoria, cronologia, ripresa e stabilità su Realme C75 e Xiaomi Mi 10T; misurare RAM, tempi, temperatura e batteria |
| 3 — Documenti e conoscenza | Primo incremento implementato; CI Android superata; test pratici mancanti | Aggiungere retrieval semantico, citazioni pagina/paragrafo, OCR immagini autonome, retention e recupero/corruzione archivio; verificare import, OCR e chat con file reali |
| 4 — Ricerca, navigazione e traduzione | Non implementata nell'app Android | Ricerca web esplicita, lettura link, estrazione contenuti, traduzione, confronto fonti, date e citazioni |
| 5 — Immagini e grafica | Non implementata | Pipeline locale/ibrida, generazione/modifica, OCR immagini, sfondi/oggetti, restauro, upscaling, licenze e benchmark |
| 6 — Video | Non implementata | Import, editing, taglio, sottotitoli, trascrizione, traduzione/doppiaggio, riassunti e benchmark |
| 7 — Audio, voce e musica | Non implementata | STT/TTS, pulizia, separazione, doppiaggio e musica originale; consenso e diritti per voci |
| 8 — Programmazione e GitHub | Non implementata nell'app | Agente coding controllato, diff, branch, commit, issue/PR, Actions, log, test e APK con approvazioni |
| 9 — Produttività e dati | Non implementata | Documenti, email, presentazioni, fogli, CSV, formule, grafici, attività e database locali |
| 10 — Agenti e automazioni | Non implementata | Pianificazione multi-step, strumenti, workflow, schedulazione, audit e conferma umana per azioni sensibili |
| 11 — Aree professionali | Non implementata | Moduli specialistici con fonti, limiti, avvertenze e verifiche specifiche; non sostituire professionisti |
| 12 — Ecosistema modelli/componenti | Registry solo documentale; gestione runtime non implementata | Registry operativo, installazione/rimozione, versioni, dipendenze, cache, licenze, benchmark e fallback locale |
| 13 — App unica e interfacce | Scheletro UI Android, non piattaforma integrata | Dashboard modulare, progetti, gestione dispositivi, selezione nodo AI, accessibilità e sincronizzazione opt-in |
| 14 — Privacy e sicurezza | Protezioni locali parziali; hardening incompleto | Threat model, autenticazione, permessi, isolamento, backup/esportazione/cancellazione, segreti, audit e test di sicurezza |
| 15 — Collaudo e distribuzione | CI Android/backend presente; collaudo e release non completati | Suite completa, test device, sicurezza, prestazioni, compatibilità, pairing, APK release, aggiornamenti e manuale |



## Audit completo delle 16 fasi — 15 novembre 2026

Questo controllo confronta roadmap, file realmente presenti nella branch `main`, test e run GitHub Actions. La presenza di un file o di una voce in un registry non equivale a una funzione integrata.

| Fase | Evidenza concreta nel repository | Valutazione e blocchi |
|---|---|---|
| 0 | Catalogo, analisi funzionale, audit JSON con 182 voci, matrice alternative, registri componenti/modelli e registro licenze | **Documentazione consolidata, non chiusa.** Tutte le 182 voci risultano mappate ma marcate `mapped_not_individually_verified`; prezzi, termini e artefatti esatti, licenze di pesi/dati e compatibilità Android non sono verificati uno per uno. Il registro modelli è un elenco di candidati, non una selezione approvata. |
| 1 | App Kotlin/Compose, rilevamento profilo dispositivo, identità Keystore, QR invito a scadenza e helper di verifica firma | **Parziale.** La UI stessa dichiara che il QR non crea connessione né fiducia. Non risultano scanner/trasporto/handshake completo, gestione peer fidati, sincronizzazione, login o browser GitHub nell'app. |
| 2 | Chat Compose, adapter LiteRT-LM CPU, catalogo/import locale modello, consenso licenza, cronologia cifrata, memoria progetto editabile, retrieval lessicale e ripresa richiesta pending | **Implementazione + CI verificate; test dispositivo mancanti.** Run Android `43764049532` SUCCESS (unit test, APK e upload); backend `43764208860` SUCCESS. Nessuna prova reale con modello importato sui due telefoni. Cambio modello non disponibile/verificato; memoria automatica e riassunti progressivi non implementati. |
| 3 | PDF/DOCX/TXT/CSV, archivio cifrato, pannello Compose, BM25, OCR Tesseract per PDF scansiti con dizionario italiano importabile | **Primo incremento implementato; non chiusa.** Manca retrieval semantico, citazione verificabile di pagina/paragrafo, OCR immagini autonome, retention, test corruzione/recupero e prove pratiche. Il codice OCR è presente ma un messaggio di errore PDF nel sorgente è rimasto obsoleto e viene corretto in questo audit. |
| 4 | Nessun modulo Android di ricerca web, navigazione o traduzione nel tree corrente | **Non implementata.** Il prototipo backend non fornisce queste funzioni. |
| 5 | Nessuna pipeline Android di generazione/modifica immagini | **Non implementata.** I registry elencano candidati, non un motore integrato. |
| 6 | Nessun motore o editor video Android | **Non implementata.** FFmpeg è ancora bloccato in attesa di revisione della configurazione/licenza. |
| 7 | Nessun modulo Android STT/TTS/audio/musica | **Non implementata.** Whisper/whisper.cpp sono candidati, non runtime integrati; licenze dei modelli/voce da verificare. |
| 8 | Workflow GitHub Actions per build e test del repository; nessun agente coding o client GitHub Android | **Non implementata come funzione prodotto.** La CI esegue pipeline del repo, ma non costituisce agente coding dentro l'app. |
| 9 | Nessun modulo Android documenti ufficio, fogli, presentazioni, grafici o DB di produttività | **Non implementata.** L'importazione documentale Fase 3 non equivale a suite ufficio. |
| 10 | Nessun orchestratore agenti, scheduler o checkpoint multi-step nell'app | **Non implementata.** La ripresa dell'ultima richiesta chat pending è una funzione limitata della Fase 2, non workflow agentici. |
| 11 | Nessun modulo professionale dedicato nel tree | **Non implementata.** Richiede fonti, limiti e validazioni specifiche per ciascun dominio. |
| 12 | Registry JSON di componenti Android e generali, catalogo singolo Gemma e installer modello | **Solo base documentale e primo import.** Mancano gestione installazione/rimozione multi-modello, versioni, dipendenze, cache, benchmark e selezione/fallback automatici. |
| 13 | MainActivity con dashboard essenziale, chat, documenti, profilo dispositivo e QR dimostrativo | **Scheletro UI, non app unica completa.** Mancano navigazione modulare, progetti, gestione dispositivi e sincronizzazione opt-in. |
| 14 | AES-GCM/Android Keystore per cronologia e archivio documenti, consenso licenza modello, storage privato | **Parziale.** Mancano threat model completo, autenticazione/isolamento, policy di retention, export/backup cifrato, test di cancellazione/recupero, audit e test di sicurezza. Il prototipo web usa localStorage non cifrato e non va usato per dati riservati. |
| 15 | CI Android unit test + assembleDebug + upload artifact; CI backend pytest; workflow Pages per anteprima web | **CI parziale, non collaudo/rilascio.** Non risultano test strumentali, test su device, benchmark, security test, APK release firmato, procedura aggiornamento o manuale di rilascio. |

### Evidenze CI aggiornate
- Android run [43764049532](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43764049532), commit `5c4f8ab4`: **SUCCESS**, unit test Android, `assembleDebug` e upload APK.
- Backend run [43764208860](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43764208860): **SUCCESS**.
- Le CI non certificano inferenza reale, OCR pratico, pairing, prestazioni, sicurezza complessiva o compatibilità fisica.

### Correzioni e discrepanze rilevate
- Il registro Fase 3 conteneva una nota precedente che indicava CI/quote ancora pendenti: superata dalle run successive riportate sopra.
- `DocumentTextExtractor.kt` implementa già OCR fallback Tesseract, ma il messaggio mostrato quando l'OCR non estrae testo diceva ancora “OCR non ancora disponibile”; corretto per descrivere il tentativo OCR fallito.
- Il QR di pairing è correttamente etichettato come invito dimostrativo: non deve essere conteggiato come collegamento tra dispositivi.
- Il workflow Android esegue unit test JVM e build debug; non contiene test instrumented/E2E su telefono.
- Il prototipo FastAPI/web e la preview non devono essere scambiati per moduli Android nativi completati.

### Ordine consigliato di prosecuzione
1. Chiudere i gate pratici Fase 2 su entrambi i telefoni e registrare misure.
2. Stabilizzare Fase 3: test estrattori/OCR, archivio, retrieval semantico e citazioni.
3. Completare Fase 1 pairing reale e connessione GitHub, che sono prerequisiti per il lavoro multi-dispositivo.
4. Costruire la Fase 4 (ricerca/link/traduzione) e poi le pipeline multimediali Fasi 5–7.
5. Procedere con coding/produttività/agenti Fasi 8–10, quindi moduli specialistici Fase 11.
6. Rendere operativo il registry Fase 12 e completare la UX Fase 13.
7. Applicare hardening Fase 14 continuamente e svolgere il collaudo/release Fase 15 per ogni milestone.

**Conclusione dell'audit:** al 15 novembre 2026 nessuna delle 16 fasi soddisfa tutti i gate di chiusura. Fasi 0–3 hanno materiale concreto e implementazioni parziali; Fasi 4–13 sono per lo più da avviare; Fasi 14–15 sono trasversali/parziali. Non assegnare percentuali globali artificiali: la roadmap è composta da funzioni con dipendenze e pesi diversi.

## Gate obbligatori per chiudere una fase

Una fase può passare a **Completata** soltanto quando tutti i punti applicabili sono documentati:

1. Funzioni previste implementate e raggiungibili dall'interfaccia Android.
2. Test automatici pertinenti superati (unitari, integrazione e regressione).
3. Test pratico end-to-end su hardware reale o ambiente rappresentativo, con risultati registrati.
4. Requisiti hardware, versioni Android, limiti, modalità offline e uso rete documentati.
5. Licenze di codice, modelli, pesi e asset controllate; nessuna dipendenza obbligatoria a pagamento.
6. Privacy e sicurezza verificate; azioni esterne/distruttive protette da consenso o conferma.
7. Documentazione e istruzioni d'uso aggiornate; eventuali difetti residui classificati.

## Collaudo dispositivi: blocco attuale della Fase 2

Dispositivi di riferimento dichiarati:
- **Realme C75**: 8 GB RAM fisica; fino a 16 GB di espansione virtuale. La RAM virtuale non va conteggiata come RAM fisica.
- **Xiaomi Mi 10T**: 6 GB RAM; Snapdragon 865 / Adreno 650.

Per ciascun dispositivo registrare: modello esatto, versione Android, spazio libero, versione APK/commit, file modello e relativo SHA-256, consenso licenza, esito caricamento, prompt di prova, risposta generata, tempo al primo token e tempo totale, RAM prima/durante/dopo, temperatura, batteria, crash e ripetibilità. Non presumere il SoC del Realme senza rilevarlo sul dispositivo.

**Prerequisito utente**: il test richiede di installare l'APK debug, procurarsi il modello secondo i termini ufficiali e importarlo nell'app. La CI remota non può sostituire queste prove fisiche.

## Regole di aggiornamento

- Aggiornare lo stato solo con evidenze verificabili (commit, run CI, test report, dispositivo e data).
- Distinguere sempre **codice presente**, **CI superata**, **test reale superato** e **fase chiusa**.
- Non sommare la RAM virtuale a quella fisica.
- Non dichiarare disponibile una funzione solo perché compare nella roadmap o in una demo statica.
- Mantenere SuperIA-Privata separata da **Editore Foto e Video IA** e **Imperium_Italia**.


## Avanzamento Fase 3 — primo incremento implementato

Commit di implementazione (sequenza sulla branch `main`):
- `b346eebe`: estrattore locale PDF/DOCX/TXT/CSV.
- `e0970d90`: archivio privato del testo estratto.
- `dbdf403e`: pannello Compose per importazione, anteprima, selezione e cancellazione.
- `f191a521`: dipendenza PDFBox-Android 2.0.27.0 (Apache-2.0).
- `f7009a22`, `9934737f`, `bc985429`, `abb463d2`: integrazione dashboard/chat, contesto documentale limitato e test del prompt.
- `2a21d447`, `7dd6f999`, `f58b05ba`, `bdecd48b`, `bb8a8465`: retriever BM25 offline, test di ranking/limiti, correzioni tokenizer e UI di ricerca multi-documento.
- `270a39dc`, `28a3f4e0`, `e7689cb3`, `2f22048c`, `3b1c95f6`, `bee2e877`: runtime Tesseract offline, importazione protetta del dizionario italiano e OCR fallback per PDF scansiti.

Funzioni incluse nel primo incremento:
- Selettore Android senza permessi generali di archiviazione.
- Estrazione testo PDF tramite PDFBox-Android; estrazione del corpo principale DOCX; lettura UTF-8 TXT/CSV.
- Limiti iniziali: 25 MB per file, 250 pagine PDF, 200.000 caratteri estratti, 50 documenti, 200 MB di archivio e 6.000 caratteri documentali nel prompt.
- Salvataggio del solo testo estratto cifrato nella directory privata dell'app con AES-GCM/Android Keystore; anteprima e cancellazione dall'archivio.
- Passaggio esplicito dell'estratto selezionato o dei blocchi recuperati al prompt della chat LiteRT-LM; il contenuto del documento è etichettato come dato e non come istruzione.

Limiti e gate ancora aperti:
- OCR italiano offline dei PDF scansiti: implementato con Tesseract, subordinato all'importazione di `ita.traineddata`; limite iniziale 20 pagine per PDF. OCR immagini autonome non incluso.
- Indicizzazione semantica/RAG, ricerca su più documenti, citazioni di pagina/paragrafo: non implementati.
- L'archivio cifra il testo estratto con AES-GCM e una chiave non esportabile Android Keystore; restano da aggiungere policy di retention e test di recupero/corruzione.
- Non è ancora stato eseguito il build Android né la suite test dopo queste modifiche; attendere GitHub Actions.
- Test pratico su dispositivi Android e verifica con modello locale restano obbligatori.

La Fase 3 è quindi **parziale e non completata**. Non considerare disponibile l'analisi AI documentale finché CI e prova end-to-end su telefono con modello importato non risultano superate.


### Evidenze CI Fase 3

- Run Android [43760034745](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43760034745), commit `3b1c95f68b70c1fa5e0c41393fe34c0a49e721ed`: unit test, `assembleDebug` e upload APK tutti SUCCESS. Comprende il flusso documenti, BM25, OCR Tesseract e importazione del modello lingua.
- Artefatto intermedio `superia-privata-debug`: 59.733.545 byte, SHA-256 archivio `d3a8d4967f79f601ae398a645184c0f52d641f738d36ed467bc622f2c53e0adc`, scadenza 13 febbraio 2027. Questo artefatto precede l'ultimo limite di quota dell'archivio e non va considerato la build definitiva.
- Run [43760180500](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43760180500) verifica la revisione successiva con quote 50 documenti/200 MB; al momento dell'aggiornamento è ancora in esecuzione. Non dichiarare superata la CI della revisione finale finché il run non termina con SUCCESS.


## Nuovo requisito approvato: conversazione unica persistente — 15 novembre 2026

L'utente ha approvato una conversazione principale continua, senza obbligo di aprire nuove chat quando il contesto diventa lungo. È un requisito di prodotto da implementare, non una funzione già disponibile o verificata.

Attività da integrare nelle fasi:
- **Fase 2:** cronologia persistente cifrata, memoria di progetto strutturata, riepiloghi progressivi, ricostruzione del contesto entro i limiti del modello e cambio modello mantenendo la conversazione.
- **Fase 3:** retrieval locale su cronologia e documenti, citazioni/riferimenti verificabili e recupero selettivo dei contenuti pertinenti.
- **Fase 10:** salvataggio dello stato degli agenti e ripresa sicura dei workflow interrotti.
- **Fase 13:** interfaccia con conversazione principale persistente e continuità tra sessioni.
- **Fase 14:** cifratura, esportazione/cancellazione sotto controllo utente, riepiloghi correggibili e tracciabilità delle informazioni ricordate.

Criteri di accettazione: (1) riaprire la stessa conversazione dopo chiusura app e riavvio; (2) proseguire oltre la finestra di contesto del modello tramite riepiloghi e retrieval, senza perdere decisioni importanti; (3) cambiare modello senza iniziare una nuova conversazione; (4) riprendere attività interrotte mostrando stato e passaggi già completati; (5) consentire all'utente di verificare/correggere la memoria di progetto; (6) non presentare come ricordato ciò che non è presente nella cronologia o nei dati recuperati.

**Stato:** requisito approvato e registrato nella roadmap; implementazione e test dedicati ancora da pianificare/eseguire. Non dichiarare la funzione disponibile fino al superamento dei criteri sopra.


## Conversazione continua: primo incremento implementato — 15 novembre 2026

Nel modulo chat Android è stato aggiunto un primo blocco funzionale per la continuità della conversazione:
- La cronologia completa non viene più limitata agli ultimi 100 messaggi: viene serializzata, compressa GZIP e cifrata AES-GCM con chiave non esportabile Android Keystore.
- L'archivio viene scritto tramite Android AtomicFile per ridurre il rischio di perdere la cronologia se il processo si interrompe durante il salvataggio.
- È prevista la migrazione automatica della vecchia cronologia cifrata in SharedPreferences al nuovo archivio; la copia legacy viene rimossa solo dopo il salvataggio riuscito.
- Il prompt mantiene i messaggi recenti e recupera fino a 6 messaggi più vecchi pertinenti alla nuova richiesta con un semplice ranking lessicale locale.
- Aggiunta una memoria di progetto modificabile dall'utente, salvata separatamente e cifrata con la stessa chiave locale; viene inserita nel contesto quando si invia un messaggio.
- Aggiunti test unitari per il recupero di messaggi vecchi pertinenti e per il blocco di memoria di progetto nel prompt.

Commit principali: `05b09501` (archivio cronologia completa), `e9242651` (recupero messaggi pertinenti), `0b369469` (test), `e79ce1e1` (memoria progetto cifrata), `440e4c4d` (UI memoria progetto).

Verifica CI: run Android `43763662848` è stato avviato per l'ultima revisione ma risulta ancora in coda al momento della nota; run Android precedenti `43763593435`, `43763597203` e `43763600739` risultano in corso. Non dichiarare ancora superati i test/build della nuova implementazione.

Limiti ancora aperti: il recupero dei messaggi è lessicale e non semantico; la memoria di progetto è attualmente compilata/modificata dall'utente, non generata automaticamente dall'IA; non è ancora verificato il cambio modello senza perdita del filo; non sono ancora eseguiti test di riapertura/riavvio, stress su cronologie molto grandi o prove sui due telefoni reali. Il requisito "conversazione unica" è quindi **parzialmente implementato, non completato**.


## Conversazione continua: verifica CI e ripresa richieste interrotte — 15 novembre 2026

### Evidenze verificate del primo incremento
- Android run [43763662848](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43763662848), commit `440e4c4d`: unit test Android, assemble debug e upload APK **SUCCESS**.
- Backend run [43763683712](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43763683712), commit `0f7f2836`: test backend **SUCCESS**.
- Artefatto Android `superia-privata-debug`, 59.742.747 byte, digest SHA-256 archivio `49ae27b189478d391cc4e35dddded0f9dd78e895213e62874f9774cb62b8ffb1`, scadenza 13 febbraio 2027. È l'artefatto della revisione con memoria modificabile, non include le successive modifiche di ripresa richieste interrotte.

### Incremento ripresa richieste
- Commit `1f18aa7a`: ogni messaggio utente può conservare il flag `pending` nell'archivio cifrato; i record precedenti restano compatibili perché il campo mancante viene letto come `false`.
- Commit `5ce60335`: la UI evidenzia una richiesta rimasta senza risposta e permette di riprovarla dopo la riapertura, ricostruendo il prompt dalla cronologia precedente senza aggiungere un secondo messaggio utente.
- Commit `5c4f8ab4`: impedisce di inviare nuove richieste finché ne esiste una in stato pending.
- Il messaggio utente viene ora salvato prima di avviare l'inferenza; se il salvataggio fallisce, l'inferenza non parte.
- La richiesta resta pending se l'inferenza fallisce o il processo viene interrotto; dopo il riavvio l'utente può premere “Riprendi richiesta”.
- La ripresa è di tipo *at-least-once*: se l'app viene interrotta dopo che il modello ha prodotto una risposta ma prima che questa sia salvata, la richiesta può essere eseguita nuovamente. Non è garantita esecuzione exactly-once.

### CI dell'incremento di ripresa
- Commit `1f18aa7a`: backend run `43764011604` SUCCESS; Android run `43764011571` da verificare.
- Commit `5ce60335`: Android run `43764036071` e backend run `43764036082` da verificare.
- Commit `5c4f8ab4`: Android run `43764049532` e backend run `43764049485` da verificare.
- Non dichiarare superata la modifica finché le Actions dell'ultimo commit non terminano con SUCCESS.

### Limiti residui e criteri non ancora superati
- Recupero della cronologia ancora lessicale, non semantico/embedding.
- La memoria di progetto è manuale e modificabile; non esiste ancora un riepilogo progressivo generato automaticamente e verificabile.
- Cambio modello mantenendo il filo non è verificabile con l'attuale singolo modello locale integrato.
- Ripresa disponibile per l'ultima richiesta utente rimasta pending, non per workflow multi-step/agenti con checkpoint.
- Non sono stati eseguiti test strumentali di riavvio/process death, stress con cronologie grandi, migrazione su installazioni reali o test sui Realme C75 e Xiaomi Mi 10T.
- Nessun test end-to-end con modello importato su telefono reale.
- La continuità conversazionale è **parzialmente implementata**. Non chiudere il requisito trasversale né dichiarare conclusa la Fase 2/3/10/13/14 sulla sola base della CI.


### Esito finale CI ripresa richieste — aggiornamento 15 novembre 2026
- Android [run 43764049532](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43764049532), commit `5c4f8ab4bed0075ee6c84314f90934eed7b91c57`: workflow **SUCCESS**; unit test Android, assemble debug APK e upload artifact tutti SUCCESS.
- Backend [run 43764089944](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43764089944), commit `2241caac22d39125e570e9fb2a9fbb2a9f62dfe0`: workflow **SUCCESS**.
- Artefatto aggiornato `superia-privata-debug`, ID `14181935552`, 59.748.079 byte, digest SHA-256 archivio `a84696e6897848c5941ff877c1d8878e584f4a72af9e2e9252f7284d330b47f8`, scadenza 13 febbraio 2027.
- Anche Android run intermedio `43764036071` ha completato test/build/upload; il riferimento definitivo resta l'ultimo commit `5c4f8ab4`.
- La CI conferma compilazione e test automatici, non prova riavvio/process death, modello in esecuzione o comportamento su hardware reale.


## Copertura aggiuntiva test conversazione lunga — 15 novembre 2026

- Commit `ca49c9b3f8c5fd44349985ff335f219441bdd670`: aggiunti test unitari per verificare che i messaggi più pertinenti recuperati dalla cronologia mantengano l'ordine temporale originale, che il recupero sia limitato a `MAX_RECALLED_MESSAGES`, che i messaggi non pertinenti vengano esclusi e che memoria/documenti siano etichettati esplicitamente come contesto e non come istruzioni.
- Backend run [43765011829](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43765011829): **SUCCESS**.
- Android run [43765011809](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43765011809): ancora **IN_PROGRESS** al momento di questo aggiornamento; non dichiarare superati i nuovi test né disponibile il nuovo APK finché la run non termina.
- Questi test coprono la costruzione del contesto del prompt; non sostituiscono test Android strumentali su cifratura, migrazione, recupero dopo process death, inferenza incrementale o dispositivi reali.


## Output incrementale della risposta AI — 15 novembre 2026

- Implementata una callback opzionale `onPartialText` nel contratto `InferenceEngine`.
- L'adapter LiteRT-LM inoltra il testo accumulato a ogni emissione del flusso nativo, riportando l'aggiornamento sul dispatcher Main.
- La chat Compose aggiorna una riga temporanea dell'assistente mentre il modello genera, sia nell'invio normale sia nella ripresa di una richiesta pending.
- La riga temporanea viene rimossa in caso di errore; soltanto il risultato completato viene scritto come risposta definitiva nella cronologia cifrata.
- Commit: `4d061f54` (contratto callback), `44ab9105` (adapter LiteRT-LM), `b65f4006` (UI incrementale), `e15453ab` (pulizia output temporaneo in caso di errore).
- **Stato:** implementazione presente, ma non ancora convalidata dalla CI successiva a questi commit. Il comportamento dipende dal modo in cui la versione LiteRT-LM 0.16.1 emette i chunk; verificare build, test e comportamento effettivo su dispositivo. Non dichiarare ancora superato il test di streaming né la Fase 2.
- La visualizzazione incrementale non garantisce da sola il token streaming: il runtime può emettere blocchi di testo di dimensione variabile. Il test pratico deve confermare aggiornamenti visibili prima del completamento della generazione.

### CI dei test contesto conversazionale

- Android run [43765011809](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43765011809): **SUCCESS**, unit test, assembleDebug e upload APK.
- Backend run [43765011829](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43765011829): **SUCCESS**.
- Artefatto Android `superia-privata-debug`, ID `14181443808`, 59.748.118 byte, SHA-256 archivio `e4e14303623d858ae5d3548e119bb3d22f045b72c97b670feb835394bf57ca4c`, scadenza 13 febbraio 2027. È relativo al commit `ca49c9b3`, non contiene ancora l'incremento di output progressivo.


## Fase 2 — verifica streaming e limiti di generazione — 16 novembre 2026

- Verificata la documentazione ufficiale LiteRT-LM Kotlin: `sendMessageAsync(text)` restituisce un `Flow<Message>` di chunk incrementali; ogni elemento rappresenta il nuovo blocco emesso, non una copia cumulativa dell'intera risposta. Il client deve concatenare i chunk per ricostruire il testo completo.
- Riferimenti tecnici: [API Kotlin LiteRT-LM](https://developers.google.com/edge/litert-lm/android) e [implementazione Conversation.kt](https://github.com/google-ai-edge/LiteRT-LM/blob/main/kotlin/java/com/google/ai/edge/litertlm/Conversation.kt). La documentazione pubblica consultata descrive l'API corrente; il build della versione dipendenza fissata nel progetto resta il controllo di compatibilità effettivo.
- Estratto l'accumulo in `StreamingTextAccumulator` e aggiunti test unitari per concatenazione ordinata, testo cumulativo e chunk vuoti.
- L'adapter ora passa anche `InferenceRequest.maxOutputTokens` a `sendMessageAsync(..., maxOutputToken = ...)` e rifiuta limiti non positivi: prima il campo della richiesta non veniva applicato.
- Commit: `5b540fff` (accumulatore), `2a4f4573` (test), `415ff74c` (adapter), `59a393c0` (limite token).
- CI di riferimento precedente: Android run [43810811687](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43810811687) **SUCCESS** e backend run [43810811557](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43810811557) **SUCCESS** sul commit `875ded1e`; queste run precedono i quattro commit elencati sopra. La nuova CI deve essere eseguita e superata prima di convalidare questa revisione.
- **Residui che impediscono la chiusura Fase 2:** test strumentali di `ChatHistoryStore` (Android Keystore/AES-GCM, migrazione, archivio corrotto e recupero AtomicFile), test pratico con Gemma importato e inferenza/streaming visibili, verifica della persistenza dopo chiusura forzata/riavvio, misure RAM/tempi/temperatura/batteria su Realme C75 e Xiaomi Mi 10T. Restano inoltre fuori dal primo incremento la memoria automatica/riassunti progressivi, il retrieval semantico e il cambio modello senza perdere la conversazione.
- **Stato aggiornato:** codice streaming e limite token implementati; test unitari aggiunti; nuova CI in attesa. Fase 2 resta parziale e non chiusa. Nessun test fisico può essere simulato dalla CI remota.


### Test strumentali archivio conversazione — 16 novembre 2026

- Aggiunta suite Android instrumented `ChatHistoryStoreInstrumentedTest`, eseguibile su emulator API 35 con Android Keystore reale dell'emulatore.
- Casi coperti: persistenza di 160 messaggi e flag pending; assenza del testo in chiaro nell'archivio; round-trip/cancellazione della memoria progetto; rifiuto di un archivio cifrato alterato.
- Configurati AndroidX Test Runner/Core/JUnit e aggiornato `.github/workflows/android-build.yml` per eseguire `connectedDebugAndroidTest` tramite emulatore prima della build APK.
- Commit: `e25efe93` (suite strumentale), `c705785f` (dipendenze/runner), `4d8028f0` (workflow emulatore).
- **Stato:** test e workflow aggiunti; esito della prima esecuzione CI su emulatore ancora da verificare. Questi test non sostituiscono i test su Realme C75 e Xiaomi Mi 10T, né simulano interruzione fisica dell'alimentazione o ogni scenario di recovery AtomicFile.
