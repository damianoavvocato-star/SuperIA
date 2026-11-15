# Fase 0 — Esito della ricognizione e decisioni tecniche

Data: 15 novembre 2026

## Esito documentale

La ricognizione è stata consolidata in quattro livelli:
1. Catalogo nominativo: 182 sistemi di riferimento, numerati e raggruppati per area.
2. Mappatura funzionale: capacità da ricostruire e candidati tecnici per tutte le 182 voci.
3. Registro macchina: AI_182_AUDIT.json, con una scheda strutturata per ciascuna voce.
4. Registro software/licenze: COMPONENTS_LICENSE_REGISTER.md e ../config/components.registry.json.

**Importante:** completare la ricognizione non significa aver verificato individualmente ogni servizio commerciale o aver installato 182 prodotti. La matrice distingue le funzioni da ricostruire dalle alternative candidate. Prezzi, free tier, stato del servizio e condizioni cambiano nel tempo; non sono trasformati in dipendenze del progetto.

## Decisioni architetturali

### 1. Un solo nucleo, moduli riutilizzabili
- LLM e ragionamento: provider locale intercambiabile (Ollama/llama.cpp/LocalAI dopo benchmark).
- Documenti e conoscenza: parser, OCR, ricerca full-text, embeddings e RAG.
- Ricerca esterna: connettore web separato, attivato solo su richiesta.
- Immagini: pipeline diffusion modulare, modelli e checkpoint registrati separatamente.
- Video: FFmpeg solo dopo selezione della build e revisione licenze; modelli video opzionali.
- Audio: ASR, TTS e diarizzazione come moduli distinti; licenze di ogni voce separate.
- Coding: agente in sandbox, accesso repository con permessi minimi, test e approvazione prima di modifiche distruttive.
- Dati e ufficio: Python, pandas, SymPy e generatori documenti.
- Agenti e automazioni: scheduler e workflow con audit log e approvazione umana.
- Aree professionali: strumenti di supporto, non sostituti di professionisti; fonti e limiti visibili.

### 2. Android-only (decisione aggiornata)
- Il prodotto finale è esclusivamente un'app Android nativa (smartphone e tablet); non è previsto un client PC né un PC remoto obbligatorio.
- L'app deve funzionare localmente e offline per tutte le funzioni per cui esistono modelli/componenti compatibili.
- Stack candidato: Kotlin + Jetpack Compose; LiteRT-LM per LLM on-device; LiteRT CompiledModel per modelli ML specializzati; runtime alternativi solo dopo benchmark.
- GPU/NPU non sono garantite: selezione runtime dinamica, fallback CPU e benchmark sul dispositivo reale.
- Non promettere video generativo avanzato o modelli grandi su ogni smartphone.
- I profili prestazionali si fissano dopo il rilevamento esplicito del dispositivo e misure di RAM, temperatura, storage, batteria e velocità.

### 3. Costi e gratuità
- Nessuna API a pagamento o abbonamento è requisito del nucleo.
- Codice open source, pesi aperti, gratuito da scaricare e utilizzo senza limiti sono concetti distinti.
- Le risorse online gratuite possono avere quote, limiti, account, restrizioni geografiche o condizioni che cambiano. Non sono dipendenze affidabili.
- Il costo operativo locale è hardware, elettricità, spazio disco e tempo di elaborazione.

### 4. Privacy e sicurezza
- Cloud disattivato per impostazione predefinita.
- Nessun upload di documenti, audio, immagini o conversazioni senza azione esplicita.
- Modelli e componenti scaricati con provenienza, versione, checksum e licenza registrati.
- Agenti e strumenti esterni in processi isolati, con permessi minimi.
- Azioni che inviano dati, pubblicano contenuti, cancellano o modificano file richiedono conferma.
- Log locale, esportazione e cancellazione controllata dei dati.

## Selezione preliminare dei modelli linguistici

Il registro dei candidati è in config/models.registry.json. Le famiglie Qwen, Llama 3.2, Gemma 4 e Phi sono soltanto piste da benchmarkare: non si attribuisce una licenza uniforme a tutta una famiglia. Per ogni download vanno fissati modello esatto, revisione, scheda ufficiale, licenza, checksum/digest e quantizzazione. La documentazione ufficiale Meta descrive Llama 3.2 1B/3B quantizzati per dispositivi edge e integrazione ExecuTorch; ciò non dimostra ancora che l'app SuperIA funzioni su uno specifico telefono. Qwen precisa che le condizioni dei pesi dipendono dal modello specifico. Ollama espone digest e metadati del modello locale, utili per registrare l'artefatto effettivo. <https://dev.meta.ai/llama/docs/model-cards-and-prompt-formats/llama3_2> · <https://github.com/QwenLM/qwen> · <https://github.com/ollama/ollama/blob/main/docs/api.md>

## Matrice di decisione iniziale

| Area | Decisione | Dipendenze candidate | Stato |
|---|---|---|---|
| Chat/LLM | Integrare per primo | Ollama, llama.cpp | Candidati, benchmark necessario |
| Documenti/OCR/RAG | Integrare dopo il nucleo | Tesseract, parser, LlamaIndex/Haystack | Licenze e test da completare |
| Ricerca/traduzione | Modulo opzionale | ricerca web + MT locale | Fonti e licenze da verificare |
| Immagini | Modulo Android opzionale | LiteRT e pipeline mobile compatibile | Modelli e prestazioni da verificare |
| Video | Modulo Android | API multimediali Android; FFmpeg solo se build e licenza approvate | Editing prima; generazione avanzata non garantita offline |
| Audio | ASR prima, TTS dopo | Whisper/whisper.cpp; TTS da selezionare | Voci e pesi da verificare |
| Coding | Integrare in sandbox | agente codice + modello locale | Progettazione/test necessari |
| Produttività/dati | Integrare gradualmente | Python, pandas, SymPy | Candidati |
| Agenti/RPA | Solo dopo sicurezza | orchestratore locale | Componenti con licenze miste da isolare |
| Professionale | Supporto con limiti | RAG su fonti autorizzate | Non certificato per decisioni professionali |
| 3D | Modulo Android opzionale | Visualizzatore e strumenti compatibili mobile | Da valutare; Blender desktop escluso dal prodotto |
| LLM Android on-device | Runtime nativo Kotlin | LiteRT-LM; llama.cpp come alternativa da benchmarkare | Non implementato |

## Elementi che richiedono test su dispositivi e versioni reali
- Compatibilità e velocità su smartphone/tablet Android reali.
- Qualità in italiano per i modelli candidati.
- Consumo RAM/VRAM, temperatura, autonomia e tempi reali.
- Build Android e APK.
- Licenza effettiva di ogni checkpoint, voce, dataset e binario finale.
- Completezza delle funzioni rispetto ai servizi commerciali.

Questi sono test di validazione e implementazione, non ragioni per inventare risultati nella ricognizione.

## Gate di uscita dalla Fase 0

La documentazione di ricognizione e le decisioni architetturali sono consolidate. La fase non va marcata come **100% verificata** finché non sono completati l'audit aggiornato di stato e condizioni dei servizi, l'approvazione degli artefatti specifici, i benchmark su dispositivi Android reali e la verifica legale della distribuzione dei componenti scelti.

Per avanzare senza bloccare il progetto, la Fase 1 può partire con il nucleo già presente e con dipendenze minime; i moduli non approvati restano disattivati.
