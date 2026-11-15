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

### 2. PC e Android
- PC è il nodo principale per modelli medi/grandi, generazione immagini/video, sviluppo e batch.
- Android è prima di tutto client/controller: chat, gestione attività, upload controllato e collegamento al PC sulla rete privata.
- Inferenza sul telefono come modulo autonomo solo per modelli leggeri e dispositivi compatibili, dopo misure reali di RAM, temperatura, storage e velocità.
- Non promettere video generativo avanzato o modelli grandi su ogni smartphone.
- Nessun requisito hardware dell'utente è stato inventato: i profili prestazionali si fissano dopo un rilevamento esplicito del dispositivo.

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

## Matrice di decisione iniziale

| Area | Decisione | Dipendenze candidate | Stato |
|---|---|---|---|
| Chat/LLM | Integrare per primo | Ollama, llama.cpp | Candidati, benchmark necessario |
| Documenti/OCR/RAG | Integrare dopo il nucleo | Tesseract, parser, LlamaIndex/Haystack | Licenze e test da completare |
| Ricerca/traduzione | Modulo opzionale | ricerca web + MT locale | Fonti e licenze da verificare |
| Immagini | Modulo PC opzionale | ComfyUI/InvokeAI + checkpoint selezionato | GPL e licenze modelli da esaminare |
| Video | Modulo PC opzionale | FFmpeg build controllata + modelli | Bloccato fino a revisione build |
| Audio | ASR prima, TTS dopo | Whisper/whisper.cpp; TTS da selezionare | Voci e pesi da verificare |
| Coding | Integrare in sandbox | agente codice + modello locale | Progettazione/test necessari |
| Produttività/dati | Integrare gradualmente | Python, pandas, SymPy | Candidati |
| Agenti/RPA | Solo dopo sicurezza | orchestratore locale | Componenti con licenze miste da isolare |
| Professionale | Supporto con limiti | RAG su fonti autorizzate | Non certificato per decisioni professionali |
| 3D | Opzionale PC | Blender e modelli 3D | GPL/dipendenze da verificare |
| Android on-device | Non bloccare l'app sul runtime PC | LiteRT/ONNX/llama.cpp mobile da valutare | Non implementato |

## Elementi che richiedono test su dispositivi e versioni reali
- Compatibilità e velocità su PC e telefono dell'utente.
- Qualità in italiano per i modelli candidati.
- Consumo RAM/VRAM, temperatura, autonomia e tempi reali.
- Build Android e APK.
- Licenza effettiva di ogni checkpoint, voce, dataset e binario finale.
- Completezza delle funzioni rispetto ai servizi commerciali.

Questi sono test di validazione e implementazione, non ragioni per inventare risultati nella ricognizione.

## Gate di uscita dalla Fase 0

La documentazione di ricognizione e le decisioni architetturali sono consolidate. La fase non va marcata come **100% verificata** finché non sono completati l'audit aggiornato di stato e condizioni dei servizi, l'approvazione degli artefatti specifici, i benchmark su PC/Android reali e la verifica legale della distribuzione dei componenti scelti.

Per avanzare senza bloccare il progetto, la Fase 1 può partire con il nucleo già presente e con dipendenze minime; i moduli non approvati restano disattivati.
