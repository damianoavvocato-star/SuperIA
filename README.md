# SuperIA Privata

Assistente AI personale e modulare, con direzione di prodotto **online-only e free-only**. Questo repository è indipendente da **Editore Foto e Video IA**.

## Obiettivo
Riordinare in un'unica piattaforma le capacità delle 182 IA individuate nella ricognizione iniziale, usando alternative open source/gratuite e rispettando licenze e requisiti. Non si clonano codice, pesi o servizi proprietari.

## Vincoli del progetto
- Uso personale e riservato; repository e dati separati da Editore Foto e Video IA.
- Nessun abbonamento o API a pagamento obbligatori.
- Inferenza finale esclusivamente online tramite provider verificati a costo zero; nessun fallback locale o a pagamento nella versione finale. Il runtime Gemma locale resta temporaneamente disponibile solo nella build legacy di transizione e non va rimosso dal dispositivo finché la nuova chat online non supera i test end-to-end.
- Destinazione esclusiva Android (smartphone e tablet); nessun client PC previsto.
- Moduli progressivi, privacy e controllo dell'utente.

## Stato attuale
- Backend prototipo FastAPI: router online free-only con allowlist fail-closed e autenticazione Bearer sulle API private (provider disabilitati per default, nessun fallback a pagamento); non è ancora pubblicato. Il client Android HTTPS è stato aggiunto e la chat è stata spostata sul backend, ma la connessione non è ancora collaudata: mancano hosting pubblico, token individuali/pairing e test CI della revisione. L'inferenza locale Gemma è solo transitoria e verrà rimossa dalla versione finale dopo la verifica del backend online. La verifica dei fornitori è in [docs/FREE_PROVIDER_BILLING_AUDIT.md](docs/FREE_PROVIDER_BILLING_AUDIT.md) e la configurazione è in [docs/ONLINE_BACKEND_IMPLEMENTATION.md](docs/ONLINE_BACKEND_IMPLEMENTATION.md).
- App Android nativa: Kotlin/Jetpack Compose, rilevamento dispositivo, chat HTTPS online free-only con token backend cifrato in Android Keystore, cronologia chat e archivio documentale cifrati AES-GCM. Il runtime LiteRT-LM/Gemma resta nel codice legacy di transizione, ma non è più invocato dalla schermata chat.
- Continuità chat: il contesto recente viene ricostruito dalla cronologia cifrata (fino a 12 messaggi e 6.000 caratteri) a ogni richiesta online.
- Ultima build Android verificata: run [43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130), commit `a82cf5ca255ebcb9b619d773768adb97b87facee`: test unitari e `assembleDebug` superati; APK debug caricato come artefatto `superia-privata-debug` (ID `14235164539`, 59.756.044 byte, SHA-256 archivio `663b62c4edb90da36e3885c7ab1d4fd0a9a8541a9bb18feb3207ffe44bc644c8`, scadenza 14 febbraio 2027). Al controllo del 16 novembre 2026 i test strumentali Keystore erano ancora in esecuzione; non sono quindi dichiarati superati su questa revisione.
- I pesi Gemma non sono inclusi nell'APK e vanno scaricati/importati separatamente dopo aver accettato i termini.
- Fase 2: implementazione e CI verificate; collaudo end-to-end su Realme C75 e Xiaomi Mi 10T ancora necessario.
- Fase 3 (primo incremento): importazione locale PDF/DOCX/TXT/CSV, estrazione testo, archivio cifrato AES-GCM e passaggio di un documento selezionato o dei blocchi recuperati da BM25 al prompt chat. OCR italiano offline per PDF scansiti aggiunto (richiede il dizionario `ita.traineddata`); embeddings/ricerca semantica e citazioni di pagina sono ancora da implementare.
- Immagini, video, audio, coding avanzato e agenti non sono ancora implementati nell'app nativa.

## Roadmap completa
La pianificazione aggiornata in 16 fasi (0–15) è in [docs/ROADMAP.md](docs/ROADMAP.md); il registro operativo con stati e gate è in [docs/PHASE_TRACKER.md](docs/PHASE_TRACKER.md). L'architettura è Android-only; il registro dei runtime Android candidati è in [config/android-components.registry.json](config/android-components.registry.json). L'integrazione GitHub e il collegamento sicuro tra più telefoni sono specificati in [docs/GITHUB_AND_MULTI_DEVICE.md](docs/GITHUB_AND_MULTI_DEVICE.md). La Fase 0 comprende il [catalogo delle 182 IA](docs/AI_CATALOG.md), la [mappatura funzionale](docs/AI_FUNCTIONAL_ANALYSIS.md), l'[audit strutturato di tutte le 182 voci](docs/AI_182_AUDIT.json), le [decisioni architetturali](docs/PHASE0_DECISIONS.md), la [matrice delle alternative](docs/AI_ALTERNATIVES_MATRIX.md), il [registro delle licenze](docs/COMPONENTS_LICENSE_REGISTER.md) il [registro macchina dei componenti](config/components.registry.json) e il [registro dei modelli candidati](config/models.registry.json). La ricognizione documentale è consolidata; audit puntuali di prezzi, licenze dei singoli artefatti e benchmark hardware restano esplicitamente aperti.

## App Android nativa

Il progetto Kotlin/Jetpack Compose è in [android/](android/README.md). Include dashboard, rilevamento locale di hardware/RAM/storage/termica, client chat HTTPS verso il backend online free-only, configurazione endpoint/token cifrata in Android Keystore, cronologia chat e archivio documentale cifrati AES-GCM. Il runtime LiteRT-LM/Gemma resta temporaneamente nel codice legacy e non è usato dalla chat. Il contesto recente viene ricostruito dalla cronologia dopo la ricreazione del runtime. Il modello non è incluso nell'APK e non viene scaricato automaticamente.

La nuova integrazione HTTPS Android deve ancora superare CI e collaudo end-to-end: il backend non è pubblicato e non è stato fornito un endpoint/token. La vecchia build locale resta da conservare finché la nuova build online non è validata. Vedi [stato Fase 2](docs/PHASE2_STATUS.md) e [validazione runtime](docs/ANDROID_RUNTIME_VALIDATION.md).

## Audit hosting e costi
La valutazione dei servizi hosting compatibili con il vincolo senza carta e senza addebiti è in [docs/FREE_HOSTING_AUDIT.md](docs/FREE_HOSTING_AUDIT.md). Cloudflare Workers Free è soltanto un candidato tecnico da verificare; nessun hosting è stato attivato.

## Destinazione Android
L'app finale è esclusivamente Android, ma la generazione delle risposte IA avverrà online tramite il backend SuperIA e i provider gratuiti approvati. Il backend sarà quindi necessario per la chat online. L'app manterrà localmente la cronologia cifrata e invierà al backend il contesto necessario, così il cambio di provider non interromperà la conversazione.

## Avvio prototipo backend
Requisiti: Python 3.11+.

```bash
cd services/api
python -m venv .venv
# Windows: .venv\Scripts\activate
# Linux/macOS: source .venv/bin/activate
pip install -r requirements-dev.txt
uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```

Interfaccia: http://127.0.0.1:8000 — API docs: http://127.0.0.1:8000/docs

## Modello locale legacy
La versione finale della chat è online-only. Il runtime Gemma locale appartiene esclusivamente alla build di transizione e va mantenuto sul dispositivo finché la nuova chat online non è stata collegata, installata e collaudata end-to-end. Non installare Ollama per la versione finale e non eliminare Gemma dalla build legacy prima del superamento del gate di migrazione.

## Privacy
Nell'app Android la cronologia chat e il testo estratto dai documenti sono cifrati AES-GCM con chiavi non esportabili in Android Keystore. Il prototipo web/backend separato conserva cronologia e annotazioni nel localStorage del browser sul dispositivo corrente e non le cifra: non inserire dati altamente riservati in quel prototipo. Non inserire dati altamente riservati su dispositivi condivisi. Dettagli: [docs/PRIVACY.md](docs/PRIVACY.md).

Non esporre il server in rete senza autenticazione e revisione di sicurezza. Non inserire segreti nel repository.


## Verifica Android CI — 16 novembre 2026

- [Android debug build — run 43890341130](https://github.com/damianoavvocato-star/SuperIA-Privata/actions/runs/43890341130), commit `a82cf5ca255ebcb9b619d773768adb97b87facee`: **SUCCESS**.
- Test unitari Android: SUCCESS.
- Test strumentali Android Keystore: 6 test avviati e 6 terminati su emulatore API 34; task `connectedDebugAndroidTest`: SUCCESS.
- APK Debug compilato e caricato come artefatto `superia-privata-debug` (ID `14235164539`, 59.756.044 byte; SHA-256 archivio `663b62c4edb90da36e3885c7ab1d4fd0a9a8541a9bb18feb3207ffe44bc644c8`; scadenza 14 febbraio 2027).
- L'APK è una build Debug, non una release firmata. Il modello Gemma non è incluso e va importato separatamente.
- La Fase 2 resta aperta: inferenza Gemma reale, continuità/ripresa dopo interruzioni e misure sui dispositivi Realme C75 e Xiaomi Mi 10T non sono ancora verificate.
