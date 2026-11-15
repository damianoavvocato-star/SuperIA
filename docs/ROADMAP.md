# Roadmap generale — SuperIA Privata

## Obiettivo e decisioni vincolanti

Costruire un'unica piattaforma Android personale e non commerciale che riunisca, in un'interfaccia sola, il maggior numero possibile di capacità delle 182 IA e di nuove funzioni richieste dall'utente. Le capacità si acquisiscono tramite componenti open source compatibili, replica funzionale indipendente e connettori/API ufficiali. L'uso personale non annulla automaticamente copyright, licenze o termini dei servizi: non si copiano codice, pesi, asset o servizi proprietari senza autorizzazione. Il processo è definito in [docs/FUNCTION_REPLICATION_AND_INTEGRATION.md](FUNCTION_REPLICATION_AND_INTEGRATION.md).

Decisioni da mantenere in tutte le fasi:
- Uso personale e riservato; progetto completamente separato da **Editore Foto e Video IA** (repository, codice, dati e configurazioni distinti).
- Gratuità come requisito: nessun abbonamento o servizio/API a pagamento obbligatorio. Eventuali servizi esterni devono essere facoltativi e disattivati per impostazione predefinita.
- Esecuzione locale e offline dove tecnicamente possibile; le funzioni che richiedono Internet (ricerca web, download, servizi esterni) devono dichiararlo.
- Destinazione esclusiva Android (smartphone e tablet); non sviluppare client PC. Moduli attivabili progressivamente.
- Priorità alla privacy, controllo dell'utente e approvazione prima di operazioni distruttive o azioni esterne.
- Ogni modello/strumento deve essere verificato per licenza, requisiti hardware, compatibilità e possibilità di uso commerciale/personale.
- Nessuna fase è dichiarata completata solo perché il codice è stato caricato: servono test pertinenti, verifica pratica e documentazione dei limiti.

## Decisione Android-only

Il prodotto finale è esclusivamente Android. Lo stack, la strategia prestazioni, i moduli e i gate sono descritti in [docs/ANDROID_ONLY_ARCHITECTURE.md](ANDROID_ONLY_ARCHITECTURE.md). Il backend FastAPI già presente è un prototipo e non deve essere una dipendenza necessaria dell'app. Il piano di rilevamento hardware e benchmark comparativo dei telefoni è in [docs/DEVICE_DETECTION_AND_BENCHMARK_PLAN.md](DEVICE_DETECTION_AND_BENCHMARK_PLAN.md).

## Documentazione della Fase 0

Il catalogo nominativo è in [docs/AI_CATALOG.md](AI_CATALOG.md); la prima matrice delle alternative gratuite/locali è in [docs/AI_ALTERNATIVES_MATRIX.md](AI_ALTERNATIVES_MATRIX.md); la [mappatura funzionale preliminare di tutte le 182 IA](AI_FUNCTIONAL_ANALYSIS.md) associa ogni voce a capacità, candidati tecnici e modalità locale/ibrida; il [registro tecnico dei componenti e delle licenze](COMPONENTS_LICENSE_REGISTER.md) separa le licenze del codice da quelle di modelli, pesi e dipendenze; il [registro macchina JSON](../config/components.registry.json) traccia candidati, stato di approvazione e controlli richiesti; l'[audit strutturato delle 182 voci](AI_182_AUDIT.json) registra per ogni sistema capacità, alternativa, modalità, costi, licenze e compatibilità ancora da verificare; le [decisioni architetturali](PHASE0_DECISIONS.md) fissano i criteri di selezione e i limiti Android; il [registro modelli candidati](../config/models.registry.json) mantiene le famiglie da benchmarkare senza approvarne automaticamente i pesi. La ricognizione documentale è consolidata, mentre la verifica puntuale dei piani commerciali, dei singoli artefatti e dei benchmark su hardware reale resta aperta.

## Fasi riorganizzate

| Fase | Obiettivo | Contenuti principali | Stato reale |
|---|---|---|---|
| 0 | Inventario delle 182 IA e progettazione funzionale | Catalogo e mappatura 182/182; audit strutturato; decisioni architetturali; candidati e registro licenze | Ricognizione documentale consolidata; verifica puntuale di servizi, modelli e hardware ancora aperta |
| 1 | Fondamenta Android e connessioni | Kotlin/Compose; architettura modulare; storage; profilo dispositivo; pairing sicuro tra telefoni; login GitHub; browser repository; build debug | In corso: scheletro Kotlin/Compose e rilevamento base aggiunti; primitive iniziali per identità Keystore e inviti QR a scadenza; pipeline SDK corretta e in verifica; handshake, rete, GitHub e test reali ancora da completare |
| 2 | Motore AI centrale e assistente | Chat; scelta e gestione modelli locali; Ollama/provider; streaming; cronologia; memoria controllata dall'utente; prompt; ruoli; gestione errori; interfaccia conversazionale | In corso: UI Compose e cronologia cifrata; contratto Kotlin InferenceEngine e registro con selezione esplicita e stati di disponibilità; runtime concreto, modello/pesi, generazione, streaming, allegati e voce ancora da collegare |
| 3 | Documenti e conoscenza personale | Caricamento PDF/DOCX/TXT/CSV; estrazione testo; OCR; riassunti; domande sui file; ricerca semantica; RAG; citazioni e riferimenti alle pagine; archivio locale | Pianificata |
| 4 | Ricerca, navigazione e traduzione | Ricerca web quando richiesta; confronto fonti; verifica date; traduzione; riassunti di pagine; estrazione contenuti da link pubblici; gestione fonti e citazioni | Pianificata |
| 5 | Immagini e grafica | Generazione da testo; modifica immagini; inpainting/outpainting; rimozione/sostituzione sfondo e oggetti; restauro; upscaling; OCR immagini; grafica, loghi e layout | Pianificata |
| 6 | Video e contenuti visuali in movimento | Generazione video da testo/immagine; editing; taglio e montaggio; sottotitoli; trascrizione; traduzione e doppiaggio; riassunto video; clip brevi; elaborazione batch | Pianificata |
| 7 | Audio, voce e musica | Speech-to-text; text-to-speech; voci; clonazione vocale solo con consenso e diritti; doppiaggio; pulizia audio; separazione sorgenti; trascrizione; musica originale; effetti sonori | Pianificata |
| 8 | Programmazione, GitHub e creazione software | Agente coding; analisi repository; branch; modifica file e diff; commit; issue; pull request; review; GitHub Actions; log e test; creazione app/APK e documentazione | Pianificata |
| 9 | Produttività, dati e strumenti da ufficio | Testi, email e documenti; presentazioni; fogli di calcolo; formule; analisi CSV; grafici; dashboard; pianificazione; gestione attività e basi dati locali | Pianificata |
| 10 | Agenti e automazioni | Agenti specializzati; pianificazione multi-step; strumenti e plugin; flussi automatici; esecuzioni programmate; collaborazione tra agenti; registro attività; approvazione umana per azioni sensibili | Pianificata |
| 11 | Aree professionali e specialistiche | Supporto giuridico; ricerca sanitaria e scientifica; finanza e contabilità; istruzione; marketing; architettura; CAD/3D; design; cybersecurity difensiva; analisi e simulazioni, con limiti e verifiche specifici | Pianificata |
| 12 | Ecosistema di modelli e componenti | Registro modelli; installazione/rimozione; versioni; dipendenze; cache; download; licenze; benchmark; selezione automatica del modello adatto; fallback solo locale e configurabile | Pianificata |
| 13 | Applicazione unica e interfacce | Dashboard; moduli; progetti; gestione dispositivi associati; selezione nodo AI; sincronizzazione solo se scelta e protetta; accessibilità e UX mobile | Pianificata |
| 14 | Privacy, sicurezza e controllo | Modalità offline; cifratura dei dati locali; autenticazione; permessi; backup/esportazione; cancellazione; isolamento moduli; protezione segreti; audit; consenso e conferma delle azioni esterne | Requisito trasversale, hardening da fare |
| 15 | Collaudo, ottimizzazione e distribuzione | Test unitari/integrati; test su dispositivi reali; sicurezza; prestazioni; uso RAM/GPU; compatibilità modelli; pairing tra telefoni; integrazione GitHub; build APK Android; installazione; aggiornamenti; manuale e rilascio | Pianificata |

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
- Versioni Android, RAM, chipset, acceleratori e modalità offline sono dichiarati come supportati, parziali o non applicabili.

## Stato verificato al 15 novembre 2026

- Il repository contiene un backend FastAPI iniziale, provider Ollama, endpoint health/capabilities/models/chat, interfaccia web, cronologia conversazioni nel browser, esportazione e annotazione personale locale.
- La nota personale locale viene aggiunta al contesto della chat; non è apprendimento del modello e non è una memoria persistente lato server.
- I workflow GitHub Actions recenti risultano con esito SUCCESS per i test backend. Questo non equivale ancora a un collaudo completo su Ollama, Android.
- Le fasi 0–2 non sono da considerare completate: l'inventario funzionale va consolidato e il prototipo della chat va ulteriormente verificato.
- L'app Android ha uno scheletro Kotlin/Compose, profilo dispositivo, primitive iniziali per identità crittografica/inviti QR, interfaccia chat e cronologia locale cifrata con Android Keystore. La chat non esegue ancora inferenza; pairing completo, runtime AI e moduli immagini/video/audio/agenti non sono ancora operativi.
- La build Android del commit `c6654884036b916a7d8265dd1c0c386b11f96a1e` è terminata con SUCCESS su GitHub Actions (run 43745811323); i test backend sullo stesso commit sono SUCCESS (run 43745811311). Le modifiche successive alla cronologia cifrata sono ancora da compilare e verificare.
- La build Android debug del commit `e91033d8bd49c52561c0b3f7df514a5b84f6c4e4` è SUCCESS (run 43745987105); artefatto `superia-privata-debug` presente, 16.059.886 byte, scadenza 13 febbraio 2027. Questo APK precede il nuovo contratto inference e non contiene ancora tali file.
- I test backend del commit `4b952b65805d105eb6274beb4d661f1ea5c79f45` sono SUCCESS (run 43745991308).
- Aggiunto il contratto Android `InferenceEngine`, il registro `InferenceEngineRegistry` e test unitari per registro vuoto/selezione solo di provider pronti; la build e i nuovi test Kotlin devono ancora essere verificati da Actions.
- Nessun runtime Android concreto o modello locale è ancora integrato; nessuna risposta AI viene ancora generata dall'app.

## Regola di separazione

Tutto il lavoro di questa roadmap riguarda esclusivamente **SuperIA Privata**. Non modificare repository, file, database, impostazioni o pipeline di **Editore Foto e Video IA**.
