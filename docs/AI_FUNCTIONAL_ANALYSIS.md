# Analisi funzionale delle 182 IA — Fase 0

> Versione: 1.0 — 15 novembre 2026  
> Stato: mappatura preliminare delle capacità e delle alternative. Non equivale a verifica completa di ogni servizio, licenza, modello o benchmark.

## Obiettivo e metodo

Il progetto non copierà codice, pesi, marchi o servizi proprietari. Ricostruirà in un'unica applicazione le capacità utili tramite software e modelli utilizzabili legalmente, privilegiando esecuzione locale, assenza di API a pagamento e controllo dell'utente.

Legenda:
- **Locale**: obiettivo eseguibile sul PC senza inviare contenuti a provider.
- **Ibrido**: alcune funzioni richiedono Internet o servizi esterni; saranno disattivate finché l'utente non le abilita.
- **Da verificare**: licenza, pesi, condizioni del servizio, hardware e qualità vanno controllati per la versione scelta.
- Le alternative indicate sono candidati tecnici, non equivalenti perfetti né approvati automaticamente per ogni uso.

## 1. Mappatura dei 182 sistemi

### 1–20 — Chat, assistenti e ricerca

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 1 | ChatGPT | Chat generalista, ragionamento, strumenti | Ollama + modelli istruiti locali; motore strumenti | Locale |
| 2 | Claude | Scrittura, analisi, contesto lungo | Modello locale con contesto esteso + RAG | Locale |
| 3 | Gemini | Assistente multimodale | Modello testo/visione locale; connettori opzionali | Locale/ibrido |
| 4 | Microsoft Copilot | Assistenza e produttività | Motore chat + integrazioni documenti/calendario | Locale/ibrido |
| 5 | DeepSeek | Ragionamento e codice | Modelli open-weight compatibili con Ollama/llama.cpp | Locale |
| 6 | Grok | Chat e ricerca attuale | Modello locale + ricerca web esplicita | Ibrido |
| 7 | Meta AI | Chat e multimodalità | Modelli Llama con licenza verificata | Locale |
| 8 | Mistral AI | Chat, ragionamento, codice | Modelli Mistral con licenza verificata | Locale |
| 9 | Perplexity | Ricerca web con fonti | Motore di ricerca + sintesi locale + citazioni | Ibrido |
| 10 | Poe | Accesso a più bot/modelli | Registro multi-modello e profili di sistema | Locale |
| 11 | You.com | Ricerca e assistente | Ricerca web + RAG + chat | Ibrido |
| 12 | Character.AI | Personaggi e role-play | Schede personaggio + modelli locali | Locale |
| 13 | Pi AI | Conversazione naturale e supporto | Modello conversazionale locale | Locale |
| 14 | Qwen | Chat, codice e visione | Modelli Qwen, previa verifica licenza specifica | Locale |
| 15 | Kimi | Contesto lungo e documenti | RAG, chunking e modello con contesto lungo | Locale |
| 16 | ERNIE Bot | Chat e conoscenza | Modello multilingue locale alternativo | Locale |
| 17 | HuggingChat | Interfaccia a modelli aperti | Catalogo modelli + Ollama | Locale |
| 18 | Amazon Q | Assistenza su cloud e sviluppo | Assistente codice/documentazione locale | Locale |
| 19 | Phind | Ricerca orientata alla programmazione | Ricerca documentazione + modello codice | Ibrido |
| 20 | Duck.ai | Chat con attenzione alla privacy | Chat locale con provider esterni opzionali | Locale |

### 21–40 — Immagini e grafica

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 21 | Midjourney | Generazione immagini | ComfyUI + modello diffusion con licenza verificata | Locale |
| 22 | DALL·E | Generazione immagini da testo | Modello diffusion locale | Locale |
| 23 | GPT Image | Generazione e modifica immagini | Pipeline diffusion/inpainting locale | Locale |
| 24 | Stable Diffusion | Generazione e modifica | Stable Diffusion/derivati, licenza per checkpoint | Locale |
| 25 | FLUX | Generazione immagini | FLUX o alternativa, verificando licenza di ogni variante | Locale |
| 26 | Adobe Firefly | Generazione grafica | Diffusion locale + strumenti di compositing | Locale |
| 27 | Leonardo AI | Generazione e asset | ComfyUI + modelli specializzati | Locale |
| 28 | Ideogram | Immagini con testo | Modello text-rendering dedicato + compositing | Locale |
| 29 | Recraft | Grafica vettoriale e raster | Inkscape/SVG + modelli immagine | Locale |
| 30 | Playground AI | Generazione e modifica | ComfyUI + editor immagini | Locale |
| 31 | NightCafe | Generazione artistica | Interfaccia preset per modelli diffusion | Locale |
| 32 | Dream by WOMBO | Immagini stilizzate | Modelli diffusion e preset artistici | Locale |
| 33 | Krea AI | Generazione e iterazione visuale | ComfyUI + anteprima progressiva | Locale |
| 34 | ImagineArt | Generazione visuale | Pipeline diffusion modulare | Locale |
| 35 | Artbreeder | Fusione e variazione di immagini | Embedding, controlli latenti e compositing | Da verificare |
| 36 | Craiyon | Generazione semplice | Modello diffusion leggero | Locale |
| 37 | Canva AI | Design e impaginazione | Editor grafico + template + modelli locali | Locale |
| 38 | Freepik AI | Asset e grafica | Libreria locale + generazione diffusion | Locale |
| 39 | Picsart AI | Editing creativo | Editor immagini + segmentazione/inpainting | Locale |
| 40 | Remini | Miglioramento e restauro foto | Real-ESRGAN e modelli face-restoration con cautele | Locale |

### 41–60 — Video

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 41 | Sora | Generazione video | Pipeline video diffusion open-weight, se licenza/hardware consentono | Locale/da verificare |
| 42 | Google Veo | Generazione video | Modelli video aperti alternativi | Locale/da verificare |
| 43 | Runway | Generazione ed editing video | FFmpeg + modelli video/image-to-video aperti | Locale |
| 44 | Kling AI | Text/image-to-video | Modelli video locali alternativi | Locale/da verificare |
| 45 | Pika | Generazione e trasformazione video | FFmpeg + modelli video aperti | Locale |
| 46 | Luma Dream Machine | Video generativo | Modelli video open-weight selezionati | Locale/da verificare |
| 47 | Hailuo AI | Video generativo | Pipeline video locale | Locale/da verificare |
| 48 | PixVerse | Video creativo | Pipeline video + effetti FFmpeg | Locale |
| 49 | Vidu AI | Video da testo/immagine | Modelli video locali | Locale/da verificare |
| 50 | HeyGen | Avatar e presentatori | TTS + avatar/animazione locale; consenso obbligatorio | Locale |
| 51 | Synthesia | Video con avatar | TTS + avatar sintetici locali | Locale |
| 52 | D-ID | Animazione volto parlante | Lip-sync locale + TTS | Locale |
| 53 | InVideo AI | Montaggio da prompt | FFmpeg + storyboard + libreria media utente | Locale |
| 54 | VEED AI | Editing, sottotitoli e audio | FFmpeg + Whisper + editor | Locale |
| 55 | CapCut AI | Editing assistito | Timeline video + FFmpeg + modelli locali | Locale |
| 56 | Descript | Editing basato su trascrizione | Whisper + editor audio/video sincronizzato | Locale |
| 57 | OpusClip | Clip brevi da video lungo | Trascrizione + scoring segmenti + FFmpeg | Locale |
| 58 | Filmora AI | Editing e miglioramento | FFmpeg + modelli di restauro | Locale |
| 59 | Adobe Premiere AI | Editing professionale | Motore timeline + FFmpeg + moduli AI | Locale |
| 60 | Captions AI | Sottotitoli e video parlati | Whisper + TTS + lip-sync | Locale |

### 61–83 — Musica, audio, voce e traduzione

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 61 | Suno | Composizione musicale generativa | Modelli musicali aperti, dopo verifica licenza | Locale/da verificare |
| 62 | Udio | Generazione musicale | Modelli audio generativi aperti | Locale/da verificare |
| 63 | AIVA | Composizione assistita | MIDI + strumenti compositivi e modelli locali | Locale |
| 64 | Soundraw | Musica adattabile | Generazione MIDI/audio con controlli di durata | Locale |
| 65 | Mubert | Musica generativa | Modelli audio locali e sequenziatore | Locale/da verificare |
| 66 | Boomy | Creazione rapida di brani | Generazione musicale locale | Locale/da verificare |
| 67 | Loudly | Musica per contenuti | Motore musicale locale + preset | Locale |
| 68 | Beatoven AI | Musica per scene | Analisi scena + generazione musicale | Locale |
| 69 | Stable Audio | Generazione audio | Modello audio aperto con licenza specifica verificata | Locale/da verificare |
| 70 | MusicFX | Generazione musicale | Modelli musicali locali | Locale/da verificare |
| 71 | Eleven Music | Generazione musicale | Modelli audio locali | Locale/da verificare |
| 72 | ElevenLabs | TTS e clonazione vocale | Piper/Coqui o modelli TTS con licenza verificata | Locale |
| 73 | Murf AI | Voice-over | Piper/TTS locale + editor | Locale |
| 74 | PlayHT | TTS | Motore TTS locale | Locale |
| 75 | Resemble AI | Voce sintetica/clonazione | TTS locale; clonazione solo con consenso esplicito | Locale |
| 76 | Speechify | Lettura vocale | OCR + TTS locale | Locale |
| 77 | WellSaid Labs | Voice-over | TTS locale + controlli prosodici | Locale |
| 78 | Fish Audio | TTS e voci | Modelli TTS selezionati, licenze e consenso da verificare | Locale |
| 79 | Whisper | Riconoscimento vocale | Whisper/whisper.cpp | Locale |
| 80 | Rask AI | Traduzione e doppiaggio video | ASR + traduzione + TTS + sincronizzazione | Locale/ibrido |
| 81 | Dubverse | Doppiaggio e sottotitoli | ASR + traduzione + TTS | Locale |
| 82 | Papercup | Doppiaggio multilingue | Pipeline ASR/MT/TTS | Locale |
| 83 | Deepgram | Trascrizione e speech API | Whisper/whisper.cpp e VAD locale | Locale |

### 84–113 — Programmazione, web e design

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 84 | GitHub Copilot | Completamento codice | Modello codice locale + plugin IDE | Locale |
| 85 | Cursor | Editor con agente codice | VS Code/VSCodium + Continue/Cline + modello locale | Locale |
| 86 | Claude Code | Agente terminale/codice | Agente locale con sandbox e approvazioni | Locale |
| 87 | OpenAI Codex | Agente sviluppo | Agente codice locale + test e Git | Locale |
| 88 | Replit AI | Sviluppo in ambiente gestito | IDE locale + terminale isolato | Locale |
| 89 | Windsurf | IDE agentico | IDE + agente locale | Locale |
| 90 | Lovable | Generazione applicazioni web | Generatore scaffolding + framework web | Locale |
| 91 | Bolt.new | Prototipi web da prompt | Generatore codice + preview locale | Locale |
| 92 | Vercel v0 | Generazione UI | Generatore componenti UI | Locale |
| 93 | Tabnine | Completamento codice | Modello codice locale | Locale |
| 94 | Amazon Q Developer | Assistenza sviluppo | Assistente codice locale | Locale |
| 95 | Blackbox AI | Ricerca e completamento codice | Ricerca repository + modello codice | Locale/ibrido |
| 96 | Devin | Agente software autonomo | Agente multi-step in container isolato | Locale |
| 97 | Codeium | Completamento codice | Modello locale + plugin IDE | Locale |
| 98 | Sourcegraph Cody | Comprensione repository | Indicizzazione codice + RAG + modello locale | Locale |
| 99 | Firebase Studio | Ambiente sviluppo cloud | Toolchain locale + emulatori | Locale |
| 100 | Wix AI | Creazione siti | Generatore siti statici/CMS locale | Locale |
| 101 | Framer AI | Design e siti | Editor visuale + generazione componenti | Locale |
| 102 | Webflow AI | Siti visuali | Editor HTML/CSS visuale | Locale |
| 103 | Durable AI | Siti aziendali rapidi | Template + generazione contenuti | Locale |
| 104 | 10Web AI | Siti WordPress | WordPress locale + generazione layout | Locale |
| 105 | Hostinger AI | Siti e hosting assistito | Generatore statico locale; hosting non incluso | Locale |
| 106 | Figma AI | Design UI | Penpot/Inkscape + generazione asset | Locale/ibrido |
| 107 | Gamma | Presentazioni e documenti | Generatore slide HTML/PPTX | Locale |
| 108 | Beautiful.ai | Presentazioni automatiche | Motore layout slide + template | Locale |
| 109 | Microsoft Designer | Grafica e contenuti | Editor grafico + modelli immagine | Locale |
| 110 | Looka | Identità e loghi | SVG + generatore logo basato su template | Locale |
| 111 | Brandmark | Branding e loghi | Generatore vettoriale + palette | Locale |
| 112 | Kittl AI | Grafica e lettering | Editor SVG + modelli immagini | Locale |
| 113 | NotebookLM | Ricerca su fonti personali | RAG locale con citazioni e note | Locale |

### 114–142 — Ricerca, scrittura e traduzione

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 114 | Elicit | Ricerca scientifica | Ricerca bibliografica + estrazione strutturata | Ibrido |
| 115 | Consensus | Risposte basate su studi | Motore paper + RAG con citazioni | Ibrido |
| 116 | SciSpace | Lettura e spiegazione paper | Parser PDF + RAG + modelli locali | Locale |
| 117 | Semantic Scholar | Ricerca accademica | Indici bibliografici aperti/API consentite | Ibrido |
| 118 | Wolfram Alpha | Calcolo e conoscenza computazionale | SymPy + Python + unità di misura | Locale |
| 119 | Humata AI | Q&A su documenti | RAG locale + OCR | Locale |
| 120 | ChatPDF | Chat con PDF | Parser PDF + RAG + citazioni | Locale |
| 121 | Scholarcy | Sintesi paper | Estrazione sezioni + riassunto locale | Locale |
| 122 | Research Rabbit | Esplorazione citazioni | Grafo bibliografico + metadati | Ibrido |
| 123 | Scite | Analisi citazioni | Metadati citazioni e classificazione, se disponibili | Ibrido |
| 124 | Grammarly | Correzione e stile | LanguageTool + modello locale | Locale |
| 125 | QuillBot | Parafrasi e riscrittura | Modello linguistico locale | Locale |
| 126 | Jasper AI | Scrittura marketing | Modelli locali + template | Locale |
| 127 | Copy.ai | Copywriting e workflow | Modello locale + template | Locale |
| 128 | Writesonic | Contenuti e SEO | Modello locale + strumenti SEO non inclusi offline | Locale/ibrido |
| 129 | Rytr | Scrittura breve | Modello locale + preset | Locale |
| 130 | Wordtune | Riscrittura | Modello locale con controlli tono | Locale |
| 131 | Sudowrite | Scrittura creativa | Modello locale + strumenti narrativi | Locale |
| 132 | Anyword | Copy orientato alle performance | Modello locale + varianti; predizioni non equivalenti | Locale |
| 133 | LanguageTool | Correzione grammaticale | LanguageTool locale, licenza/versione da verificare | Locale |
| 134 | DeepL | Traduzione | Modelli MT locali, per esempio Bergamot/Marian se adeguati | Locale |
| 135 | Google Translate | Traduzione | Modelli offline/locali e dizionari | Locale |
| 136 | Microsoft Translator | Traduzione | Modelli MT locali | Locale |
| 137 | Reverso | Traduzione e contesto | MT locale + corpus/dizionari autorizzati | Locale |
| 138 | Lara Translate | Traduzione contestuale | Modello multilingue locale | Locale |
| 139 | ModernMT | Traduzione adattiva | Modello MT locale + glossari | Locale |
| 140 | LILT | Traduzione professionale | MT locale + memoria di traduzione | Locale |
| 141 | Smartling | Gestione localizzazione | TMS locale + glossari + workflow | Locale |
| 142 | Wordly | Interpretazione e sottotitoli live | ASR + MT + TTS; latenza da testare | Locale/ibrido |

### 143–182 — Agenti, professioni, 3D, formazione e analisi

| N. | Sistema di riferimento | Capacità da coprire | Candidato per SuperIA | Modalità |
|---:|---|---|---|---|
| 143 | Manus | Agente generalista | Agente locale con strumenti e sandbox | Locale |
| 144 | Zapier AI | Automazioni SaaS | Motore workflow locale + connettori autorizzati | Ibrido |
| 145 | Make AI | Automazioni visuali | Motore workflow locale | Locale |
| 146 | n8n | Workflow e integrazioni | n8n solo dopo verifica dei termini/licenza e del caso d'uso | Locale/ibrido |
| 147 | UiPath | RPA | Robot desktop locale e automazioni controllate | Locale |
| 148 | AutoGPT | Agenti autonomi | Framework agentico con limiti e approvazioni | Locale |
| 149 | CrewAI | Agenti collaborativi | Orchestratore multi-agente | Locale |
| 150 | LangChain | Catene e strumenti LLM | Libreria di orchestrazione | Locale |
| 151 | Lindy AI | Assistenti e workflow | Agente locale + scheduler e connettori | Locale |
| 152 | Genspark | Ricerca e agenti | Ricerca web + RAG + strumenti | Ibrido |
| 153 | Abridge | Trascrizione clinica | ASR locale; dati sanitari esclusi da invii esterni predefiniti | Locale |
| 154 | Nabla | Assistente clinico | Dettatura e strutturazione locale; non sostituisce il medico | Locale |
| 155 | OpenEvidence | Ricerca medica | RAG su fonti mediche autorizzate e aggiornate | Ibrido |
| 156 | Harvey AI | Assistente legale | RAG documentale e modelli locali; revisione professionale | Locale |
| 157 | CoCounsel | Ricerca e revisione legale | Ricerca documentale + citazioni + checklist | Locale |
| 158 | Lexis+ AI | Ricerca giuridica | RAG su fonti normative autorizzate e aggiornate | Ibrido |
| 159 | BloombergGPT | Analisi finanziaria | Modello generalista + dati finanziari autorizzati | Ibrido |
| 160 | AlphaSense | Ricerca intelligence aziendale | Ricerca documentale + RAG su fonti autorizzate | Ibrido |
| 161 | FinChat | Analisi finanziaria | Python/pandas + dati pubblici e importati dall'utente | Locale/ibrido |
| 162 | Planner 5D | Progettazione ambienti | Editor planimetrie 2D/3D + rendering | Locale |
| 163 | Interior AI | Design interni | Modelli immagine + controlli geometrici | Locale |
| 164 | RoomGPT | Restyling ambienti da foto | Image-to-image locale | Locale |
| 165 | Meshy AI | Generazione modelli 3D | Blender + modelli text/image-to-3D aperti | Locale |
| 166 | Tripo AI | Generazione 3D | Pipeline mesh locale e modelli compatibili | Locale |
| 167 | Rodin AI | Asset 3D | Modelli 3D open-weight, se licenza compatibile | Locale/da verificare |
| 168 | Microsoft Security Copilot | Analisi sicurezza difensiva | Scanner locali + correlazione log + modello locale | Locale |
| 169 | Darktrace AI | Rilevamento anomalie | Regole, anomaly detection e analisi log locale | Locale |
| 170 | Khanmigo | Tutor didattico | Tutor locale + esercizi e feedback | Locale |
| 171 | Duolingo Max | Apprendimento lingue | Tutor linguistico + spaced repetition | Locale |
| 172 | MagicSchool AI | Strumenti per docenti | Generatore lezioni, rubriche e schede | Locale |
| 173 | HubSpot Breeze | CRM e marketing | CRM locale + automazioni e assistente | Locale/ibrido |
| 174 | AdCreative.ai | Creatività pubblicitarie | Generazione asset + template + varianti | Locale |
| 175 | Intercom Fin | Assistenza clienti | Knowledge base RAG + chat e handoff | Locale/ibrido |
| 176 | Zendesk AI | Supporto clienti | Ticketing + classificazione + RAG | Locale/ibrido |
| 177 | Otter.ai | Trascrizione riunioni | Whisper + diarizzazione + note | Locale |
| 178 | Fireflies.ai | Trascrizione e analisi meeting | Whisper + diarizzazione + ricerca | Locale |
| 179 | Fathom AI | Note e riepiloghi meeting | Registrazione locale + ASR + sintesi | Locale |
| 180 | Julius AI | Analisi dati | Python + pandas + grafici e linguaggio naturale | Locale |
| 181 | Power BI Copilot | BI e report | Python/pandas + dashboard locali; non replica Power BI | Locale |
| 182 | Tableau AI | BI e visualizzazione | Motore analisi + grafici e dashboard locali | Locale |

## 2. Componenti condivisi proposti

Questi componenti possono coprire molte delle 182 capacità. La selezione definitiva dipende da test, licenze delle versioni e requisiti del dispositivo.

| Componente candidato | Ruolo centrale | Famiglie coperte |
|---|---|---|
| Ollama | Runtime e gestione modelli locali | Chat, codice, scrittura, RAG |
| llama.cpp | Inferenza locale efficiente e portabilità | Chat e modelli quantizzati |
| ComfyUI | Pipeline visuali per generazione immagini/video | Immagini, grafica, parte video |
| FFmpeg | Elaborazione e montaggio audio/video | Video, audio, conversioni |
| Whisper / whisper.cpp | Trascrizione vocale | Meeting, sottotitoli, doppiaggio |
| Piper o TTS alternativo | Sintesi vocale locale | Lettura, voice-over, doppiaggio |
| Tesseract | OCR | Documenti e immagini |
| LlamaIndex o Haystack | Indicizzazione e RAG | PDF, knowledge base, ricerca |
| SymPy / Python / pandas | Calcolo e analisi dati | Scienza, finanza, fogli e BI |
| Blender | Modellazione e rendering 3D | 3D, ambienti, asset |
| Inkscape / editor SVG | Grafica vettoriale | Loghi, design, illustrazione |
| Agente con sandbox | Esecuzione controllata di attività | Codice, workflow, automazioni |

**Attenzione licenze:** la licenza del programma non determina automaticamente quella dei modelli, dei pesi, dei dataset, delle voci o dei contenuti generati. Ogni risorsa deve avere una scheda licenza separata. n8n non va presentato come open source OSI senza qualificazioni; AutoGPT ha componenti con licenze differenti. Questi elementi richiedono verifica legale prima di distribuirli o incorporarli.

## 3. Requisiti PC e Android: strategia realistica

- **PC**: piattaforma principale per modelli più grandi, generazione immagini/video e sviluppo. L'uso CPU è possibile per diversi modelli ma può essere lento; GPU e VRAM accelerano i carichi.
- **Android**: interfaccia completa e controllo del PC in rete locale come percorso iniziale. L'inferenza direttamente sul telefono sarà un'opzione separata, limitata da RAM, spazio, calore e prestazioni.
- **Offline**: chat, documenti già scaricati, OCR, trascrizione e varie attività possono essere locali. Ricerca web, dati aggiornati, sincronizzazioni e servizi remoti richiedono connessione.
- **Nessuna falsa promessa**: generazione video avanzata e modelli di grandi dimensioni non sono garantiti su telefoni o PC modesti.
- **Profili hardware**: saranno definiti dopo aver rilevato CPU, RAM, GPU/VRAM, spazio libero e versione Android/Windows/Linux del dispositivo reale.

Come ordine di grandezza, la documentazione Ollama indica 8 GB di RAM disponibili per modelli 7B, 16 GB per 13B e 32 GB per 33B nella configurazione descritta; sono indicazioni generali, non garanzie di velocità né requisiti universali. Dimensioni e memoria dipendono da quantizzazione, contesto e runtime.

## 4. Priorità d'implementazione risultanti

1. **Fondamenta**: catalogo componenti, registro licenze, impostazioni privacy e rilevamento hardware.
2. **Assistente locale**: modelli, chat, profili, cronologia e scelta esplicita del provider.
3. **Documenti e ricerca personale**: importazione, OCR, RAG, citazioni e cancellazione dati.
4. **Voce e traduzione**: trascrizione, sintesi, sottotitoli e traduzione.
5. **Immagini e grafica**: generazione, editing e gestione risorse.
6. **Codice e produttività**: agente in sandbox, documenti, slide e analisi dati.
7. **Video, musica e 3D**: moduli opzionali con avviso sui requisiti hardware.
8. **Agenti e aree professionali**: solo con limiti, fonti e controlli adeguati.

## 5. Criteri di verifica ancora aperti

Per ognuna delle 182 voci, la mappatura funzionale va completata con:
- verifica che il servizio sia attivo, rinominato o ritirato;
- confronto delle funzioni effettive e dei limiti;
- licenza del codice, dei pesi e dei dataset;
- costo reale e condizioni del piano gratuito;
- requisiti hardware misurati;
- supporto Windows/Linux/macOS e Android;
- modalità offline e flussi di dati;
- test comparativi con esempi riproducibili;
- decisione motivata: integrare, sostituire, rendere opzionale o escludere.

## 6. Stato

- Nomi nell'inventario: 182/182.
- Mappatura preliminare capacità/candidato: 182/182.
- Verifica puntuale di servizio, licenze, costi e requisiti: da completare.
- Test d'integrazione e qualità: non ancora eseguiti per questa mappatura.
- Fase 0: **in corso**, non dichiarata completata.

La mappatura è una prima base di progettazione. Non afferma che ogni alternativa sia equivalente al prodotto commerciale né che ogni candidato sia già integrato nell'applicazione.
