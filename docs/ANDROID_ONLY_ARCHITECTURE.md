# Architettura Android-only — SuperIA Privata

Data decisione: 15 novembre 2026

## Decisione vincolante

Il prodotto è destinato esclusivamente ad Android (smartphone e tablet). Non sviluppare client PC, APK desktop, pannelli web come prodotto finale o dipendenze da un PC remoto. Il backend FastAPI esistente è un prototipo di riferimento e non deve essere un requisito per usare l'app sul telefono: le funzioni principali devono essere native e locali.

## Obiettivo

Una sola applicazione Android, in italiano, modulare e utilizzabile senza abbonamenti o API a pagamento obbligatorie. Deve riunire le capacità delle 182 IA in moduli coerenti, senza copiare prodotti, codice, pesi o servizi proprietari. L'app deve privilegiare l'elaborazione on-device; Internet sarà richiesto solo per funzioni che per natura necessitano di fonti o servizi online, e sempre con indicazione e consenso.

## Stack Android candidato

| Livello | Scelta iniziale | Motivo e condizione |
|---|---|---|
| App | Kotlin + Jetpack Compose | UI nativa, accessibilità, gestione lifecycle e processi Android |
| LLM locale | LiteRT-LM Android (Kotlin) | Runtime Google attuale per chat, streaming, tool use e modelli aperti; benchmark obbligatorio |
| Modelli ML specializzati | LiteRT CompiledModel | Inferenza CPU/GPU e acceleratori supportati; NPU solo se effettivamente disponibile sul dispositivo/runtime |
| Motore alternativo | llama.cpp Android/NDK | Fallback/benchmark per modelli GGUF, non doppio runtime obbligatorio |
| Speech-to-text | whisper.cpp Android | Trascrizione locale; modelli e licenze verificati separatamente |
| OCR | ML Kit on-device o Tesseract | Selezione dopo test offline, accuratezza italiano e dimensione |
| Media | Android Media APIs + FFmpeg solo con build/licenza approvata | Evitare binari non verificati e dipendenze inutili |
| Dati locali | Room + DataStore + Android Keystore | Persistenza, preferenze e protezione delle chiavi |
| Background | WorkManager | Download, indicizzazione e lavori differibili, con limiti batteria/rete |
| UI file/media | Android Photo Picker e Storage Access Framework | Accesso selettivo ai contenuti, senza permessi generali non necessari |

MediaPipe LLM Inference non va scelto per nuove integrazioni: la documentazione Google lo indica in modalità manutenzione e raccomanda la migrazione a LiteRT-LM. Non fissare versioni 'latest' in build riproducibili: prima di integrare, selezionare una release stabile specifica e bloccarla.

## Strategia prestazioni

1. Rilevare all'avvio RAM disponibile, ABI, Android API level, spazio libero e acceleratori realmente supportati.
2. Eseguire un breve benchmark locale e scegliere il profilo più veloce stabile: NPU/GPU solo se supportate e verificate; altrimenti CPU.
3. Fornire profili modello: leggero, bilanciato e qualità massima compatibile. La scelta automatica non deve superare i limiti termici o di memoria.
4. Scaricare i modelli separatamente dall'APK, su richiesta, mostrando dimensione, licenza, spazio richiesto e checksum.
5. Gestire memoria e contesto, streaming, interruzione generazione, quantizzazione e fallback; evitare di tenere più modelli grandi in RAM contemporaneamente.
6. Monitorare temperatura, crash, memoria e batteria; ridurre carico e notificare l'utente quando necessario.
7. Non promettere che ogni funzione avanzata (video generativo, grandi LLM, modelli diffusion) sia eseguibile offline su ogni smartphone. Se non sostenibile sul dispositivo, mostrare chiaramente il limite e offrire solo un'elaborazione online facoltativa e gratuita quando realmente disponibile, senza API a pagamento obbligatorie.

## Moduli funzionali previsti

- Assistente e ragionamento: chat, modelli selezionabili, prompt, conversazioni e memoria controllata dall'utente.
- Documenti: PDF, DOCX, TXT, immagini, OCR, riassunti, ricerca locale e risposte con riferimenti alle pagine.
- Ricerca e traduzione: ricerca web opt-in, estrazione di link accessibili, traduzione locale quando il modello è disponibile.
- Immagini: analisi, OCR, modifica e generazione; generazione locale solo su dispositivi che superano il benchmark.
- Video: player, estrazione audio/frame, taglio, sottotitoli e montaggio; generazione video locale resta sperimentale e non requisito minimo.
- Audio: registrazione autorizzata, trascrizione, sintesi vocale, pulizia e traduzione/doppiaggio.
- Coding: lettura e generazione di codice, editor mobile e operazioni repository con conferma.
- Produttività: note, testi, tabelle, CSV, documenti e presentazioni.
- Agenti: strumenti con permessi minimi, cronologia azioni e conferma prima di invii, pubblicazioni, acquisti o cancellazioni.
- Aree specialistiche: moduli di supporto con fonti, avvisi e limiti espliciti; non sostituiscono professionisti.

## Privacy e sicurezza Android

- Modalità offline realmente utilizzabile per i moduli locali.
- Nessun caricamento automatico di file o conversazioni.
- Dati locali protetti con cifratura appropriata e chiavi custodite da Android Keystore.
- Permessi runtime minimi; Photo Picker/SAF preferiti all'accesso generalizzato.
- Download verificati, provenienza, licenze, versioni e SHA-256 registrati.
- Nessuna esecuzione di codice arbitrario senza sandbox e consenso.
- Backup, esportazione e cancellazione disponibili e spiegati.
- Telemetria disattivata per impostazione predefinita; nessun segreto nei log.

## Fasi Android aggiornate

0. Inventario delle 182 IA, audit licenze/costi e decisione Android-only. Documentazione già mappata; verifiche puntuali ancora aperte.
1. Fondamenta Android: progetto Kotlin/Compose, architettura modulare, storage, permessi, tema, navigazione, CI e build debug.
2. Motore AI: integrazione LiteRT-LM, download modelli, benchmark, chat streaming e gestione memoria.
3. Documenti e RAG locale.
4. Ricerca web opt-in, link e traduzione.
5. Immagini e grafica.
6. Video e montaggio mobile.
7. Audio e voce.
8. Coding e strumenti sviluppatore.
9. Produttività e dati.
10. Agenti e automazioni con conferme.
11. Moduli professionali.
12. Registro e gestione modelli/componenti Android.
13. Rifinitura UX, accessibilità e integrazione moduli.
14. Sicurezza, privacy e protezione dati.
15. Test su dispositivi reali, ottimizzazione batteria/termica, APK firmabile e distribuzione.

## Gate di accettazione

Una funzione è completa solo se è presente nell'app Android, ha test automatici pertinenti, è stata provata su dispositivo reale, documenta licenza e limiti, funziona offline se dichiarata offline e non richiede servizi a pagamento. La compatibilità va dichiarata per versione Android, RAM, chipset e modello specifico.

## Informazioni hardware ancora necessarie

Non è ancora stato eseguito un benchmark sul telefono dell'utente. Prima di scegliere il modello predefinito e la quantizzazione finale occorre rilevare modello esatto, RAM, SoC/GPU, versione Android e spazio libero. Fino ad allora i modelli nel registro restano candidati e non approvati.
