# Matrice preliminare delle alternative gratuite e locali

> Documento di lavoro della Fase 0. Le soluzioni qui sotto sono candidate tecniche, non integrazioni già completate. La presenza di codice pubblico non garantisce che i pesi del modello, i dataset o ogni uso siano liberi: ogni licenza va controllata sul repository e sulla scheda del modello specifico prima del download e dell'integrazione.

## 1. Stack di base proposto

| Funzione | Candidato principale | Alternative / note | Stato |
|---|---|---|---|
| Runtime modelli linguistici locali | Ollama | llama.cpp per controllo più diretto e dispositivi con risorse limitate | Candidato prioritario |
| API di inferenza | API locale Ollama | llama.cpp server o LocalAI dopo benchmark | Da confrontare |
| Interfaccia chat di riferimento | Interfaccia SuperIA già presente | Jan/Open WebUI come riferimenti UX, non necessariamente da incorporare | Prototipo presente |
| Modelli linguistici | Modelli open-weight compatibili con Ollama | Selezione per italiano, dimensione, quantizzazione e licenza | Da benchmarkare |
| Ricerca nei documenti (RAG) | LlamaIndex o Haystack + archivio vettoriale locale | Evitare dipendenze cloud obbligatorie | Da valutare |
| OCR | Tesseract | OCR integrato nei flussi documentali; test su scansioni italiane | Da valutare |
| Immagini | ComfyUI | InvokeAI; scegliere modello e pesi separatamente | Da valutare |
| Audio: trascrizione | Whisper / whisper.cpp | Faster-Whisper come alternativa di runtime | Candidato |
| Audio: sintesi vocale | Piper | Verificare licenza di ogni voce e modello acustico | Da valutare |
| Video | FFmpeg per editing e pipeline; ComfyUI con modelli video compatibili | LTX-Video e altri modelli solo dopo verifica licenze e requisiti GPU | Da valutare |
| Coding agent | Aider o Cline con endpoint locale | Approvazione esplicita prima di scrivere file o eseguire comandi | Da valutare |
| Automazioni | Motore workflow interno minimale | n8n solo dopo verifica licenza e condizioni d'uso; non presumere che sia OSI open source | Da valutare |
| Applicazione Android | Client leggero collegato al PC in LAN | Inferenza sul telefono solo per modelli compatibili con RAM/SoC | Da progettare |

## 2. Mappatura preliminare delle famiglie di servizi

La matrice non afferma che un solo modello replichi ogni prodotto commerciale. Identifica le capacità da coprire con componenti riutilizzabili.

| Famiglia dell'inventario | Capacità da coprire | Componenti candidati | Criticità |
|---|---|---|---|
| Assistenti e ricerca (1–20) | Chat, ragionamento, strumenti, ricerca e citazioni | Ollama + modello linguistico locale; ricerca web come modulo separato e disattivabile | La ricerca web richiede Internet; qualità dipende dal modello |
| Immagini (21–40) | Generazione, modifica, inpainting, upscaling, OCR | ComfyUI/InvokeAI + modelli di diffusione selezionati | GPU/VRAM; licenze diverse tra modelli e versioni |
| Video (41–60) | Montaggio, sottotitoli, trascrizione, generazione | FFmpeg + Whisper + pipeline ComfyUI/modello video | Generazione video locale molto onerosa; non promettere equivalenza con servizi cloud |
| Audio e voce (61–83) | STT, TTS, separazione, doppiaggio, musica | Whisper/whisper.cpp, Piper e strumenti audio dedicati | Verificare diritti delle voci; musica generativa e separazione richiedono valutazione separata |
| Sviluppo, web e design (84–113) | Coding assistito, agenti di sviluppo, siti, UI e design | Modelli locali per coding; toolchain web open source e librerie UI | Verificare licenze dei modelli e separare editor proprietari da motori locali |
| Ricerca, scrittura e lingua (114–142) | Ricerca scientifica, Q&A documentale, scrittura, correzione e traduzione | RAG locale, modelli linguistici, OCR, motori di traduzione locali quando disponibili | Accuratezza, citazioni, qualità italiana e licenze dei dati |
| Agenti, professioni, 3D e analisi (143–182) | Workflow, agenti, supporto professionale, 3D, formazione, sicurezza e BI | Framework agentici locali, motori 3D e strumenti di analisi con formati aperti | Elevato rischio di servizi cloud a pagamento; sicurezza, validazione e licenze specifiche |

## 3. Gate obbligatori prima dell'integrazione

Per ogni componente candidato:
1. Aprire il repository ufficiale e la licenza del codice.
2. Aprire la scheda del modello e verificare la licenza dei pesi.
3. Verificare restrizioni d'uso, redistribuzione, attribuzione e uso commerciale.
4. Annotare dimensioni download, RAM/VRAM minima e raccomandata, CPU/GPU supportate.
5. Verificare se funziona senza rete dopo il download.
6. Verificare se invia telemetria o dati all'esterno e come disabilitarla.
7. Eseguire test ripetibili in italiano con dataset di prova non sensibile.
8. Salvare versione, hash, configurazione, tempi e risultati.
9. Decidere se il componente è adatto a PC, Android o solo a un server locale.
10. Documentare limiti e fallback, senza instradare automaticamente dati verso servizi a pagamento.

## 4. Architettura raccomandata per mantenere il progetto gratuito

- **Core**: FastAPI già presente, con interfacce provider astratte.
- **Moduli**: pacchetti separati per documenti, ricerca, immagini, audio, video e coding.
- **Motori locali**: processi opzionali avviati solo quando richiesti dall'utente.
- **Registro modelli**: metadati, licenza, versione, dimensione, hardware e stato dei test.
- **Connettività**: modalità offline esplicita; i moduli web devono dichiarare quando inviano richieste.
- **Sicurezza**: sandbox e conferma umana per file, comandi, automazioni e azioni esterne.
- **Android**: interfaccia remota al PC come primo traguardo; esecuzione locale sul telefono soltanto per modelli piccoli e compatibili.

## 5. Decisioni provvisorie

1. Mantenere Ollama come primo runtime, già collegato al prototipo, ma confrontarlo con llama.cpp.
2. Non sostituire il backend esistente con una piattaforma esterna.
3. Trattare ComfyUI, InvokeAI, Whisper e Piper come motori separati, attivabili su richiesta.
4. Non includere servizi cloud gratuiti con limiti come dipendenze essenziali.
5. Non dichiarare supporto Android nativo finché non esiste un client e non sono stati eseguiti test su dispositivo.
6. Non considerare equivalenti modelli open-weight e modelli completamente open source: registrare separatamente le licenze.

## 6. Riferimenti iniziali da verificare sulle fonti ufficiali

- Ollama: https://github.com/ollama/ollama
- llama.cpp: https://github.com/ggml-org/llama.cpp
- ComfyUI: https://github.com/comfyanonymous/ComfyUI
- InvokeAI: https://github.com/invoke-ai/InvokeAI
- Whisper: https://github.com/openai/whisper
- whisper.cpp: https://github.com/ggml-org/whisper.cpp
- Aider: https://github.com/Aider-AI/aider
- Cline: https://github.com/cline/cline
- Tesseract OCR: https://github.com/tesseract-ocr/tesseract
- LlamaIndex: https://github.com/run-llama/llama_index
- Haystack: https://github.com/deepset-ai/haystack

**Stato:** prima selezione di candidati per capacità; nessuna licenza o compatibilità hardware è considerata definitivamente approvata.


## 7. Prima verifica documentale delle licenze (15 novembre 2026)

Questa verifica riguarda il codice dei repository citati, non costituisce approvazione automatica di ogni modello, voce, checkpoint, dataset o dipendenza.

| Componente | Evidenza consultata | Esito preliminare | Conseguenza per SuperIA |
|---|---|---|---|
| Ollama | Repository ufficiale indica licenza MIT | Codice con licenza permissiva | Candidato idoneo per il runtime; verificare separatamente ogni modello scaricato |
| Whisper | Repository ufficiale dichiara MIT per codice e pesi del modello | Candidato permissivo per trascrizione | Candidato prioritario per test offline in italiano |
| whisper.cpp | File LICENSE ufficiale MIT | Candidato permissivo per runtime C/C++ | Da benchmarkare su PC e Android |
| Tesseract | Repository ufficiale Apache-2.0; dipendenze con licenze proprie | Candidato permissivo per OCR | Candidato per documenti e immagini; verificare anche i file lingua e dipendenze |
| n8n | Sustainable Use License e licenza Enterprise; il progetto dichiara di non definirsi open source OSI | Source-available/fair-code, non OSI open source | Non inserirlo nel nucleo open source; eventuale uso personale va valutato nei limiti della licenza e senza presumerne la redistribuibilità |
| AutoGPT | La licenza è mista: MIT per gran parte del repository, PolyForm Shield per la cartella autoGPT_platform secondo la ricognizione disponibile | Non trattare l'intero progetto come MIT | Valutare solo componenti e cartelle con licenza verificata; escludere la parte con licenza incompatibile |

### Fonti ufficiali consultate
- Ollama — https://github.com/ollama/ollama
- Whisper — https://github.com/openai/whisper
- whisper.cpp — https://github.com/ggml-org/whisper.cpp/blob/master/LICENSE
- Tesseract — https://github.com/tesseract-ocr/tesseract
- n8n license — https://github.com/n8n-io/n8n/blob/master/LICENSE.md
- n8n Community License — https://github.com/n8n-io/n8n-docs/blob/main/docs/n8n-community-license/README.md
- AutoGPT — https://github.com/Significant-Gravitas/AutoGPT

### Regola rafforzata
La dicitura “open source” sarà usata soltanto per componenti con licenza approvata OSI. Per licenze source-available, fair-code, commerciali o miste verrà usata la dicitura corretta e il componente non sarà considerato automaticamente idoneo. La licenza del codice non sostituisce la verifica dei pesi, dei dati, dei modelli vocali e dei plugin.


## Registro tecnico delle licenze

La verifica iniziale del codice sorgente e i blocchi ancora aperti sono documentati nel [registro tecnico dei componenti](COMPONENTS_LICENSE_REGISTER.md). Non installare o distribuire un candidato sulla sola base della licenza del repository: verificare anche dipendenze, modelli, pesi, voci, dataset e configurazione dei binari.
