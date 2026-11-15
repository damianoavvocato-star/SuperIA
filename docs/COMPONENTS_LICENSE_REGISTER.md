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
| LlamaIndex core | Indicizzazione e RAG | MIT per il pacchetto core esaminato | Il progetto è composto da più pacchetti: verificare la licenza di ciascun pacchetto effettivamente installato e delle dipendenze. | Candidato RAG, installazione minima |
| ComfyUI | Pipeline generazione immagini | GPL-3.0 | Copyleft: prima di incorporare, modificare o distribuire l'applicazione insieme a ComfyUI, fare revisione compatibilità e modalità di collegamento. Plugin e modelli hanno licenze proprie. | Candidato opzionale, isolato come processo esterno |
| Blender | 3D e rendering | GPL-3.0-or-later dichiarata per Blender | Le dipendenze incluse hanno licenze differenti; controllare avvisi di terze parti e modalità di distribuzione. | Candidato opzionale, tool esterno |
| n8n | Workflow e automazioni | Sustainable Use License / Enterprise License (non OSI open source) | Non presentarlo come open source OSI. Valutare attentamente uso interno, embedding, redistribuzione e offerta a terzi. | Non includere nel nucleo; valutare alternativa |
| AutoGPT | Agenti | Licenza mista: MIT per gran parte del repository; PolyForm Shield per autogpt_platform | Non attribuire MIT all'intero repository. Verificare cartella, commit e dipendenze prima dell'uso. | Non integrare finché non isolata la parte necessaria |

## Candidati da non approvare ancora

| Componente | Motivo del blocco temporaneo |
|---|---|
| llama.cpp | La ricerca preliminare ha individuato file secondari MIT, ma occorre verificare il file LICENSE principale del commit scelto e i subcomponenti. |
| FFmpeg | La licenza dipende dalle opzioni di compilazione e dalle librerie abilitate: build LGPL e build GPL/non-free non sono intercambiabili. Selezionare e fissare una build, poi esaminare configurazione e avvisi. |
| Piper TTS | Verificare repository mantenuto, licenza della versione scelta, voci e modelli vocali; non riutilizzare automaticamente licenze di fork diversi. |
| Haystack | Verificare il repository e la licenza della versione/pacchetto specifico prima di selezionarlo rispetto a LlamaIndex. |
| Inkscape | Verificare licenza del codice e delle dipendenze per la versione da distribuire. |
| SymPy e pandas | Verificare i file LICENSE delle versioni bloccate nel lockfile e le dipendenze effettive. |
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

- 9 componenti registrati.
- 7 candidati o casi con classificazione preliminare del codice.
- 6 gruppi di componenti ancora bloccati per verifica puntuale.
- Nessuna approvazione generale dei pesi/modelli.
- Nessuna nuova dipendenza installata nel prodotto in questo passaggio.
- Registro iniziale creato; audit completo ancora aperto.
