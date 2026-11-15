# Registro tecnico dei componenti — SuperIA Privata

> Fase 0 — registro iniziale, revisione 1  
> Data di controllo documentale: 15 novembre 2026  
> Stato: verifica preliminare delle licenze del codice sorgente. Non costituisce parere legale e non certifica licenze di pesi, modelli, dataset, plugin, checkpoint o binari precompilati.

## Regole del registro

- **Codice**: licenza dichiarata dal repository del progetto.
- **Modello/pesi/dati**: licenza separata, da controllare per ogni file/versione scaricata.
- **Dipendenze**: possono avere licenze differenti; generare e conservare un inventario SBOM prima di distribuire.
- **Uso privato**: non equivale automaticamente a diritto di redistribuzione o incorporazione in un prodotto.
- **Decisione**: “candidato” non significa già integrato o approvato.

## Registro componenti verificati

| Componente | Funzione prevista | Licenza del codice verificata | Condizioni/attenzioni | Decisione attuale |
|---|---|---|---|---|
| Ollama | Runtime locale LLM e API | MIT | La licenza del runtime non concede automaticamente diritti sui modelli scaricati. Verificare ogni modello. | Candidato primario per il motore PC |
| OpenAI Whisper | Speech-to-text | MIT | Verificare licenze e provenienza di modelli, eventuali conversioni e dataset aggiuntivi. | Candidato per trascrizione |
| whisper.cpp | Runtime C/C++ per Whisper | MIT | Verificare modello Whisper utilizzato e licenze delle dipendenze/binari distribuiti. | Candidato per trascrizione locale |
| Tesseract OCR | OCR | Apache-2.0 | Il progetto segnala dipendenze con licenze distinte; verificare Leptonica e pacchetti binari. | Candidato per OCR |
| PdfBox-Android 2.0.27.0 | Estrazione testo PDF nell'app Android | Apache-2.0 | Dipendenza AAR da Maven Central; verificare e includere nell'SBOM le dipendenze transitive Bouncy Castle e i relativi avvisi prima di una release. | Integrato nel prototipo Android; build/compatibilità da verificare |
| Tesseract4Android 4.4.0 | OCR offline italiano su PDF scansiti | Apache-2.0 per wrapper; Tesseract Apache-2.0 | Distribuito tramite JitPack; include Leptonica, libjpeg e libpng con licenze proprie. Verificare SBOM/avvisi e versione esatta. Il file ita.traineddata della release tessdata_fast 4.1.0 è importato separatamente dall'utente; il repository tessdata dichiara Apache-2.0 per i file lingua, ma conservare versione e checksum del file usato. | Integrato nel prototipo Android; build/compatibilità da verificare |
| LlamaIndex core | Indicizzazione e RAG | MIT per il pacchetto core esaminato | Il progetto è composto da più pacchetti: verificare la licenza di ciascun pacchetto effettivamente installato e delle dipendenze. | Candidato RAG, installazione minima |
| ComfyUI | Pipeline generazione immagini | GPL-3.0 | Copyleft: prima di incorporare, modificare o distribuire l'applicazione insieme a ComfyUI, fare revisione compatibilità e modalità di collegamento. Plugin e modelli hanno licenze proprie. | Candidato opzionale, isolato come processo esterno |
| Blender | 3D e rendering | GPL-3.0-or-later dichiarata per Blender | Le dipendenze incluse hanno licenze differenti; controllare avvisi di terze parti e modalità di distribuzione. | Candidato opzionale, tool esterno |
| n8n | Workflow e automazioni | Sustainable Use License / Enterprise License (non OSI open source) | Non presentarlo come open source OSI. Valutare attentamente uso interno, embedding, redistribuzione e offerta a terzi. | Non includere nel nucleo; valutare alternativa |
| AutoGPT | Agenti | Licenza mista: MIT per gran parte del repository; PolyForm Shield per autogpt_platform | Non attribuire MIT all'intero repository. Verificare cartella, commit e dipendenze prima dell'uso. | Non integrare finché non isolata la parte necessaria |

## Candidati da non approvare ancora

| Componente | Motivo del blocco temporaneo |
|---|---|
| FFmpeg | Il codice base è LGPL-2.1-or-later; parti opzionali GPL si attivano con `--enable-gpl`; `--enable-nonfree` produce binari non redistribuibili. La build e le librerie esterne cambiano gli obblighi. |
| Piper TTS | Il repository storico `rhasspy/piper` è archiviato dal 6 ottobre 2025 e dichiara MIT; lo sviluppo è indicato come trasferito a `OHF-Voice/piper1-gpl`. Le voci hanno licenze per singolo modello. La presenza di eSpeak NG e relativi componenti richiede revisione della distribuzione. |
| Inkscape | Verificare licenza del codice e delle dipendenze per la versione da distribuire. |
| Modelli immagine/video/audio | Nessuna approvazione generica: controllare modello, versione, pesi, dataset, restrizioni commerciali/personali e condizioni di output. |

## Fonti ufficiali consultate

- Ollama, LICENSE: https://github.com/ollama/ollama/blob/main/LICENSE
- OpenAI Whisper, LICENSE: https://github.com/openai/whisper/blob/main/LICENSE
- whisper.cpp, LICENSE: https://github.com/ggml-org/whisper.cpp/blob/master/LICENSE
- Tesseract, README e LICENSE: https://github.com/tesseract-ocr/tesseract
- LlamaIndex core, LICENSE: https://github.com/run-llama/llama_index/blob/main/llama-index-core/LICENSE
- ComfyUI, LICENSE: https://github.com/Comfy-Org/ComfyUI/blob/master/LICENSE
- Blender, licenze e dipendenze: https://github.com/blender/blender/blob/main/release/license/license.md
- n8n, LICENSE: https://github.com/n8n-io/n8n/blob/master/LICENSE.md
- AutoGPT, repository e licenze: https://github.com/Significant-Gravitas/AutoGPT

Le fonti sono riferimenti da ricontrollare sul commit/tag effettivamente bloccato. Per Whisper, n8n e AutoGPT si mantiene inoltre la cautela già riportata nella matrice alternative. Un URL di licenza non sostituisce la verifica del contenuto esatto della versione installata.

## Requisiti tecnici per approvazione

Ogni componente candidato deve ricevere:
1. repository ufficiale e URL del file LICENSE;
2. tag/commit esatto e checksum dell'archivio/binario;
3. licenza SPDX o classificazione motivata;
4. inventario dipendenze/SBOM;
5. licenza separata per modelli, pesi, tokenizer, voci e dataset;
6. requisiti OS, CPU, RAM, GPU/VRAM, spazio e Android;
7. modalità offline, telemetria e rete;
8. test funzionali e prestazionali;
9. procedura di aggiornamento e rimozione;
10. decisione approvata: includere, isolare, opzionale o escludere.

## Architettura raccomandata per ridurre il rischio

- Il backend SuperIA comunica con i motori tramite interfacce/CLI/API locali documentate.
- Componenti GPL o con licenze particolari restano processi opzionali isolati finché non è stata verificata la compatibilità distributiva.
- Ogni modello scaricato ha un record autonomo con URL di origine, licenza, versione, checksum e data.
- Le funzionalità cloud restano disattivate per impostazione predefinita e non sono necessarie al nucleo.
- Prima di creare APK o pacchetti PC, generare SBOM, includere avvisi/licenze richiesti e verificare i termini di ogni binario distribuito.

## Stato

- 13 componenti software con licenza del codice documentata a livello preliminare.
- 4 nuovi candidati aggiunti in questa revisione: llama.cpp, Haystack, pandas e SymPy.
- 3 componenti software restano da sbloccare o verificare prima della selezione: FFmpeg, Piper e Inkscape.
- I modelli e le voci restano sempre soggetti a verifica individuale, non conteggiati come componenti approvati.
- Nessuna approvazione generale dei pesi/modelli.
- Nessuna nuova dipendenza installata nel prodotto in questo passaggio.
- Registro iniziale creato; audit completo ancora aperto.


## Aggiornamento verifica puntuale — 15 novembre 2026

Ulteriori file ufficiali consultati:

- llama.cpp, file LICENSE principale: https://github.com/ggml-org/llama.cpp/blob/master/LICENSE — MIT. La decisione resta subordinata al pin di un commit e all'inventario dei componenti/avvisi.
- Haystack, LICENSE: https://github.com/deepset-ai/haystack/blob/main/LICENSE — Apache-2.0.
- pandas, LICENSE: https://github.com/pandas-dev/pandas/blob/main/LICENSE — BSD 3-Clause.
- SymPy, LICENSE: https://github.com/sympy/sympy/blob/master/LICENSE — BSD-style, con sezioni che riportano licenze di componenti incorporati.
- FFmpeg, pagina legale ufficiale: https://www.ffmpeg.org/legal.html e LICENSE: https://github.com/FFmpeg/FFmpeg/blob/master/LICENSE.md — base LGPL-2.1-or-later; le opzioni GPL e non-free cambiano la licenza/redistribuibilità del risultato. Per la prima build di SuperIA è preferibile non distribuire un binario FFmpeg finché non è stata definita e verificata la configurazione.
- Piper storico: https://github.com/rhasspy/piper — repository archiviato il 6 ottobre 2025, licenza MIT dichiarata; il repository indica lo spostamento dello sviluppo a https://github.com/OHF-Voice/piper1-gpl. Non selezionare automaticamente il nuovo progetto né distribuire voci senza verificare la licenza specifica del modello e delle dipendenze.

### Decisioni aggiornate

- **Aggiunti come candidati di codice**: llama.cpp, Haystack, pandas, SymPy.
- **FFmpeg**: mantenuto in stato bloccato fino alla scelta e verifica di una build precisa; non usare build con `--enable-nonfree` per distribuzione.
- **Piper**: non approvato per il nucleo; progetto storico archiviato e sviluppo trasferito a un progetto GPL. Valutare solo dopo verifica tecnica e legale della versione, del motore fonetico e di ogni voce.
- **Inkscape**: ancora da verificare sul commit/versione che si intenderebbe distribuire.
- Nessuna dipendenza è stata installata o inclusa nell'applicazione in questo aggiornamento.


### PdfBox-Android — integrazione Fase 3

- Repository ufficiale: https://github.com/TomRoush/PdfBox-Android
- Coordinate Maven: `com.tom-roush:pdfbox-android:2.0.27.0`
- Licenza del progetto: Apache-2.0, come dichiarato dal repository.
- Utilizzo limitato all'estrazione del testo da PDF nell'app Android; non è un servizio remoto.
- La release e le dipendenze transitive devono essere bloccate e incluse nell'SBOM della futura distribuzione. L'integrazione nel prototipo non equivale ad approvazione per la release finale.


### OCR offline Android — Tesseract4Android

- Wrapper: https://github.com/adaptech-cz/Tesseract4Android — Apache-2.0.
- Motore Tesseract: https://github.com/tesseract-ocr/tesseract — Apache-2.0.
- Il wrapper include dipendenze native (Leptonica, libjpeg, libpng) con licenze da riportare nell'SBOM.
- Dipendenza prototipale: `com.github.adaptech-cz.Tesseract4Android:tesseract4android:4.4.0` tramite JitPack.
- Dati lingua italiani non incorporati nell'APK: l'utente importa `ita.traineddata` nell'area privata. Il repository tessdata dichiara Apache-2.0 per i file lingua; archiviare comunque versione e checksum del file effettivamente usato.
- L'OCR viene eseguito sul dispositivo; nessun servizio cloud è richiesto.
