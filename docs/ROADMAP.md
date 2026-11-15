# Roadmap generale — SuperIA Privata

## Obiettivo e decisioni vincolanti

Costruire un'unica piattaforma personale che riunisca, in un'interfaccia sola, le principali capacità delle 182 IA elencate nella ricognizione iniziale. Non si copiano codice, pesi, servizi proprietari o funzioni a pagamento: si integrano modelli, librerie e strumenti open source o gratuiti, rispettando licenze e condizioni d'uso.

Decisioni da mantenere in tutte le fasi:
- Uso personale e riservato; progetto completamente separato da **Editore Foto e Video IA** (repository, codice, dati e configurazioni distinti).
- Gratuità come requisito: nessun abbonamento o servizio/API a pagamento obbligatorio. Eventuali servizi esterni devono essere facoltativi e disattivati per impostazione predefinita.
- Esecuzione locale e offline dove tecnicamente possibile; le funzioni che richiedono Internet (ricerca web, download, servizi esterni) devono dichiararlo.
- Supporto previsto per PC e Android, con moduli attivabili progressivamente.
- Priorità alla privacy, controllo dell'utente e approvazione prima di operazioni distruttive o azioni esterne.
- Ogni modello/strumento deve essere verificato per licenza, requisiti hardware, compatibilità e possibilità di uso commerciale/personale.
- Nessuna fase è dichiarata completata solo perché il codice è stato caricato: servono test pertinenti, verifica pratica e documentazione dei limiti.

## Documentazione della Fase 0

Il catalogo nominativo è in [docs/AI_CATALOG.md](AI_CATALOG.md); la prima matrice delle alternative gratuite/locali è in [docs/AI_ALTERNATIVES_MATRIX.md](AI_ALTERNATIVES_MATRIX.md); la [mappatura funzionale preliminare di tutte le 182 IA](AI_FUNCTIONAL_ANALYSIS.md) associa ogni voce a capacità, candidati tecnici e modalità locale/ibrida; il [registro tecnico dei componenti e delle licenze](COMPONENTS_LICENSE_REGISTER.md) separa le licenze del codice da quelle di modelli, pesi e dipendenze; il [registro macchina JSON](../config/components.registry.json) traccia candidati, stato di approvazione e controlli richiesti; l'[audit strutturato delle 182 voci](AI_182_AUDIT.json) registra per ogni sistema capacità, alternativa, modalità, costi, licenze e compatibilità ancora da verificare; le [decisioni architetturali](PHASE0_DECISIONS.md) fissano i criteri di selezione e i limiti PC/Android. La ricognizione documentale è consolidata, mentre la verifica puntuale dei piani commerciali, dei singoli artefatti e dei benchmark su hardware reale resta aperta.

## Fasi riorganizzate

| Fase | Obiettivo | Contenuti principali | Stato reale |
|---|---|---|---|
| 0 | Inventario delle 182 IA e progettazione funzionale | Catalogo e mappatura 182/182; audit strutturato; decisioni architetturali; candidati e registro licenze | Ricognizione documentale consolidata; verifica puntuale di servizi, modelli e hardware ancora aperta |
| 1 | Fondamenta tecniche | Repository separato; architettura modulare; backend/API; configurazione; logging essenziale; struttura moduli; documentazione; ambiente di sviluppo | Base iniziale presente, da completare |
| 2 | Motore AI centrale e assistente | Chat; scelta e gestione modelli locali; Ollama/provider; streaming; cronologia; memoria controllata dall'utente; prompt; ruoli; gestione errori; interfaccia conversazionale | Prototipo iniziale presente, in corso |
| 3 | Documenti e conoscenza personale | Caricamento PDF/DOCX/TXT/CSV; estrazione testo; OCR; riassunti; domande sui file; ricerca semantica; RAG; citazioni e riferimenti alle pagine; archivio locale | Pianificata |
| 4 | Ricerca, navigazione e traduzione | Ricerca web quando richiesta; confronto fonti; verifica date; traduzione; riassunti di pagine; estrazione contenuti da link pubblici; gestione fonti e citazioni | Pianificata |
| 5 | Immagini e grafica | Generazione da testo; modifica immagini; inpainting/outpainting; rimozione/sostituzione sfondo e oggetti; restauro; upscaling; OCR immagini; grafica, loghi e layout | Pianificata |
| 6 | Video e contenuti visuali in movimento | Generazione video da testo/immagine; editing; taglio e montaggio; sottotitoli; trascrizione; traduzione e doppiaggio; riassunto video; clip brevi; elaborazione batch | Pianificata |
| 7 | Audio, voce e musica | Speech-to-text; text-to-speech; voci; clonazione vocale solo con consenso e diritti; doppiaggio; pulizia audio; separazione sorgenti; trascrizione; musica originale; effetti sonori | Pianificata |
| 8 | Programmazione e creazione software | Generazione e spiegazione codice; debug; refactoring; test; analisi repository; creazione siti/app; assistenza Flutter/Android; build APK; documentazione tecnica | Pianificata |
| 9 | Produttività, dati e strumenti da ufficio | Testi, email e documenti; presentazioni; fogli di calcolo; formule; analisi CSV; grafici; dashboard; pianificazione; gestione attività e basi dati locali | Pianificata |
| 10 | Agenti e automazioni | Agenti specializzati; pianificazione multi-step; strumenti e plugin; flussi automatici; esecuzioni programmate; collaborazione tra agenti; registro attività; approvazione umana per azioni sensibili | Pianificata |
| 11 | Aree professionali e specialistiche | Supporto giuridico; ricerca sanitaria e scientifica; finanza e contabilità; istruzione; marketing; architettura; CAD/3D; design; cybersecurity difensiva; analisi e simulazioni, con limiti e verifiche specifici | Pianificata |
| 12 | Ecosistema di modelli e componenti | Registro modelli; installazione/rimozione; versioni; dipendenze; cache; download; licenze; benchmark; selezione automatica del modello adatto; fallback solo locale e configurabile | Pianificata |
| 13 | Applicazione unica e interfacce | Dashboard unificata; moduli attivabili; interfaccia coerente; gestione progetti e file; cronologia; impostazioni; accessibilità; UX mobile; sincronizzazione solo se scelta e protetta | Pianificata |
| 14 | Privacy, sicurezza e controllo | Modalità offline; cifratura dei dati locali; autenticazione; permessi; backup/esportazione; cancellazione; isolamento moduli; protezione segreti; audit; consenso e conferma delle azioni esterne | Requisito trasversale, hardening da fare |
| 15 | Collaudo, ottimizzazione e distribuzione | Test unitari/integrati; test su dispositivi reali; sicurezza; prestazioni; uso RAM/GPU; compatibilità modelli; build PC e APK Android; installazione; aggiornamenti; manuale e rilascio | Pianificata |

## Copertura funzionale della ricognizione iniziale

Le 182 IA di riferimento vanno ricondotte a capacità, non replicate una per una come servizi separati. La piattaforma dovrà coprire almeno questi gruppi:
1. Assistenti conversazionali, ragionamento e ricerca risposte.
2. Generazione e modifica immagini, design e grafica.
3. Generazione, montaggio, editing e comprensione video.
4. Musica, composizione, effetti, sintesi e riconoscimento vocale.
5. Traduzione, doppiaggio, sottotitoli e trascrizione.
6. Programmazione, sviluppo software e strumenti per sviluppatori.
7. Documenti, scrittura, presentazioni e produttività.
8. Ricerca web, conoscenza, PDF e analisi delle fonti.
9. Dati, fogli di calcolo, visualizzazione e business intelligence.
10. Agenti, automazioni e strumenti specializzati.
11. Applicazioni professionali: legale, salute/ricerca, finanza, istruzione, marketing, architettura, 3D e sicurezza informatica difensiva.

Il catalogo dettagliato dei 182 nomi dovrà associare a ciascuno: categoria, funzioni distintive, alternative open source/gratuite, licenza, necessità di Internet, requisiti hardware, stato di integrazione e test.

## Criteri di completamento di ogni fase

Una fase passa a "completata" solo quando:
- le funzioni previste sono implementate e accessibili dall'interfaccia;
- i test automatici pertinenti passano;
- almeno un test pratico end-to-end è stato eseguito;
- privacy, licenze, limiti e requisiti sono documentati;
- la funzione non dipende da API a pagamento obbligatorie;
- PC/Android e modalità offline sono dichiarati come supportati, parziali o non applicabili.

## Stato verificato al 15 novembre 2026

- Il repository contiene un backend FastAPI iniziale, provider Ollama, endpoint health/capabilities/models/chat, interfaccia web, cronologia conversazioni nel browser, esportazione e annotazione personale locale.
- La nota personale locale viene aggiunta al contesto della chat; non è apprendimento del modello e non è una memoria persistente lato server.
- I workflow GitHub Actions recenti risultano con esito SUCCESS per i test backend. Questo non equivale ancora a un collaudo completo su Ollama, PC e Android.
- Le fasi 0–2 non sono da considerare completate: l'inventario funzionale va consolidato e il prototipo della chat va ulteriormente verificato.
- Nessun modulo immagini, video, audio, agenti o APK Android è dichiarato già realizzato.

## Regola di separazione

Tutto il lavoro di questa roadmap riguarda esclusivamente **SuperIA Privata**. Non modificare repository, file, database, impostazioni o pipeline di **Editore Foto e Video IA**.
