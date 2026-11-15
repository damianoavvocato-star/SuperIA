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
| 0 — Inventario e progettazione | Documentazione consolidata, audit aperto | Verificare puntualmente licenze/termini, prezzi e disponibilità dei singoli artefatti, requisiti hardware, compatibilità e alternative; aggiornare registri e fonti |
| 1 — Fondamenta Android e connessioni | In corso / parziale | Completare architettura e storage, rilevamento hardware, pairing con handshake e rete, autenticazione/integrazione GitHub; test su entrambi i telefoni |
| 2 — Motore AI e assistente | Implementazione + CI verificate; collaudo fisico pendente | Importare modello Gemma autorizzato e provare inferenza reale, streaming, cronologia, riapertura sessione, errori e stabilità su Realme C75 e Xiaomi Mi 10T; misurare RAM, tempi, temperatura e batteria |
| 3 — Documenti e conoscenza | In corso: primo flusso Android implementato, CI da verificare | Importazione/estrazione locale PDF testuali, DOCX, TXT e CSV; archivio privato del testo cifrato AES-GCM con chiave Android Keystore; anteprima, ricerca testuale BM25 multi-documento e contesto dei blocchi recuperati nella chat. Restano embeddings/ricerca semantica, citazioni pagina/paragrafo, hardening e test end-to-end; OCR italiano offline aggiunto con dizionario importabile |
| 4 — Ricerca, navigazione e traduzione | Da avviare | Ricerca web esplicita, lettura link, estrazione contenuti, traduzione, confronto fonti, date e citazioni |
| 5 — Immagini e grafica | Da avviare | Pipeline locale/ibrida selezionabile, generazione/modifica, OCR, sfondi/oggetti, restauro, upscaling e verifica licenze/prestazioni |
| 6 — Video | Da avviare | Import, editing, taglio, sottotitoli, trascrizione, traduzione/doppiaggio, riassunti e benchmark |
| 7 — Audio, voce e musica | Da avviare | STT/TTS, pulizia, separazione, doppiaggio e musica originale; consenso e diritti per voci |
| 8 — Programmazione e GitHub | Da avviare | Agente coding controllato, diff, branch, commit, issue/PR, Actions, log, test e APK con approvazioni |
| 9 — Produttività e dati | Da avviare | Documenti, email, presentazioni, fogli, CSV, formule, grafici, attività e database locali |
| 10 — Agenti e automazioni | Da avviare | Pianificazione multi-step, strumenti, workflow, schedulazione, audit e conferma umana per azioni sensibili |
| 11 — Aree professionali | Da avviare | Moduli specialistici con fonti, limiti, avvertenze e verifiche specifiche; non sostituire professionisti |
| 12 — Ecosistema modelli/componenti | Da avviare | Registry operativo, installazione/rimozione, versioni, dipendenze, cache, licenze, benchmark e fallback locale |
| 13 — App unica e interfacce | Da avviare | Dashboard integrata, moduli, progetti, gestione dispositivi, accessibilità e sincronizzazione opt-in |
| 14 — Privacy e sicurezza | Trasversale; hardening da fare | Threat model, cifratura, autenticazione, permessi, isolamento, backup/cancellazione, segreti, audit e verifiche |
| 15 — Collaudo e distribuzione | Da avviare; CI parziale già presente | Suite test completa, prove su device, sicurezza, prestazioni, compatibilità, APK installabile, aggiornamenti e manuale |

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
- Limiti iniziali: 25 MB per file, 250 pagine PDF, 200.000 caratteri estratti e 6.000 caratteri documentali nel prompt.
- Salvataggio del solo testo estratto cifrato nella directory privata dell'app con AES-GCM/Android Keystore; anteprima e cancellazione dall'archivio.
- Passaggio esplicito dell'estratto selezionato o dei blocchi recuperati al prompt della chat LiteRT-LM; il contenuto del documento è etichettato come dato e non come istruzione.

Limiti e gate ancora aperti:
- OCR italiano offline dei PDF scansiti: implementato con Tesseract, subordinato all'importazione di `ita.traineddata`; limite iniziale 20 pagine per PDF. OCR immagini autonome non incluso.
- Indicizzazione semantica/RAG, ricerca su più documenti, citazioni di pagina/paragrafo: non implementati.
- L'archivio cifra il testo estratto con AES-GCM e una chiave non esportabile Android Keystore; restano da aggiungere gestione quote/retention e test di recupero/corruzione.
- Non è ancora stato eseguito il build Android né la suite test dopo queste modifiche; attendere GitHub Actions.
- Test pratico su dispositivi Android e verifica con modello locale restano obbligatori.

La Fase 3 è quindi **parziale e non completata**. Non considerare disponibile l'analisi AI documentale finché CI e prova end-to-end su telefono con modello importato non risultano superate.
