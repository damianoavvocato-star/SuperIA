# SuperIA Privata

Assistente AI personale, modulare e con priorità all'esecuzione locale. Questo repository è indipendente da **Editore Foto e Video IA**.

## Obiettivo
Riordinare in un'unica piattaforma le capacità delle 182 IA individuate nella ricognizione iniziale, usando alternative open source/gratuite e rispettando licenze e requisiti. Non si clonano codice, pesi o servizi proprietari.

## Vincoli del progetto
- Uso personale e riservato; repository e dati separati da Editore Foto e Video IA.
- Nessun abbonamento o API a pagamento obbligatori.
- Modelli locali e funzionamento offline dove possibile; Internet solo per funzioni che lo richiedono.
- Destinazione esclusiva Android (smartphone e tablet); nessun client PC previsto.
- Moduli progressivi, privacy e controllo dell'utente.

## Stato attuale
- Backend prototipo: FastAPI con provider Ollama locale, endpoint chat/modelli e interfaccia web dimostrativa. Il backend non è una dipendenza obbligatoria dell'app Android.
- App Android nativa: Kotlin/Jetpack Compose, rilevamento dispositivo, adapter LiteRT-LM 0.16.1 CPU, catalogo Gemma 3 1B IT INT4, importazione locale del modello con consenso licenza, cronologia chat e archivio documentale cifrati AES-GCM tramite Android Keystore.
- Continuità chat: il contesto recente viene ricostruito dalla cronologia cifrata (fino a 12 messaggi e 6.000 caratteri) quando il runtime viene ricreato.
- CI sul commit `8ba080ad4f43690f25c63f74d2ff7c3941944b5a`: test Android, compilazione APK e test backend tutti superati.
- APK debug disponibile come artefatto GitHub Actions; i pesi Gemma non sono inclusi e vanno scaricati/importati separatamente dopo aver accettato i termini.
- Fase 2: implementazione e CI verificate; collaudo end-to-end su Realme C75 e Xiaomi Mi 10T ancora necessario.
- Fase 3 (primo incremento): importazione locale PDF/DOCX/TXT/CSV, estrazione testo, archivio cifrato AES-GCM e passaggio di un documento selezionato o dei blocchi recuperati da BM25 al prompt chat. OCR italiano offline per PDF scansiti aggiunto (richiede il dizionario `ita.traineddata`); embeddings/ricerca semantica e citazioni di pagina sono ancora da implementare.
- Immagini, video, audio, coding avanzato e agenti non sono ancora implementati nell'app nativa.

## Roadmap completa
La pianificazione aggiornata in 16 fasi (0–15) è in [docs/ROADMAP.md](docs/ROADMAP.md); il registro operativo con stati e gate è in [docs/PHASE_TRACKER.md](docs/PHASE_TRACKER.md). L'architettura è Android-only; il registro dei runtime Android candidati è in [config/android-components.registry.json](config/android-components.registry.json). L'integrazione GitHub e il collegamento sicuro tra più telefoni sono specificati in [docs/GITHUB_AND_MULTI_DEVICE.md](docs/GITHUB_AND_MULTI_DEVICE.md). La Fase 0 comprende il [catalogo delle 182 IA](docs/AI_CATALOG.md), la [mappatura funzionale](docs/AI_FUNCTIONAL_ANALYSIS.md), l'[audit strutturato di tutte le 182 voci](docs/AI_182_AUDIT.json), le [decisioni architetturali](docs/PHASE0_DECISIONS.md), la [matrice delle alternative](docs/AI_ALTERNATIVES_MATRIX.md), il [registro delle licenze](docs/COMPONENTS_LICENSE_REGISTER.md) il [registro macchina dei componenti](config/components.registry.json) e il [registro dei modelli candidati](config/models.registry.json). La ricognizione documentale è consolidata; audit puntuali di prezzi, licenze dei singoli artefatti e benchmark hardware restano esplicitamente aperti.

## App Android nativa

Il progetto Kotlin/Jetpack Compose è in [android/](android/README.md). Include dashboard, rilevamento locale di hardware/RAM/storage/termica, chat LiteRT-LM CPU, catalogo Gemma 3 1B IT INT4, importazione del file `.litertlm` nello spazio privato, consenso esplicito ai termini del modello, cronologia chat e archivio documentale cifrati AES-GCM. Il contesto recente viene ricostruito dalla cronologia dopo la ricreazione del runtime. Il modello non è incluso nell'APK e non viene scaricato automaticamente.

La build e i test CI sono superati, ma la Fase 2 resta aperta fino alla prova pratica di inferenza con il modello importato su Realme C75 e Xiaomi Mi 10T. Vedi [stato Fase 2](docs/PHASE2_STATUS.md) e [validazione runtime](docs/ANDROID_RUNTIME_VALIDATION.md).

## Destinazione Android
La decisione tecnica Android-only e il piano dettagliato sono in [docs/ANDROID_ONLY_ARCHITECTURE.md](docs/ANDROID_ONLY_ARCHITECTURE.md). Il backend FastAPI resta un prototipo di riferimento: non sarà necessario per le funzioni principali dell'app Android.

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

## Modello locale
Installa Ollama, avvialo e scarica un modello compatibile, ad esempio `ollama pull llama3.2:3b`.
Variabili: `OLLAMA_MODEL` (predefinito `llama3.2:3b`) e `OLLAMA_BASE_URL` (predefinito `http://127.0.0.1:11434`).
Senza Ollama, le richieste AI restituiscono un errore esplicito. Nessun fallback cloud.

## Privacy
Nell'app Android la cronologia chat e il testo estratto dai documenti sono cifrati AES-GCM con chiavi non esportabili in Android Keystore. Il prototipo web/backend separato conserva cronologia e annotazioni nel localStorage del browser sul dispositivo corrente e non le cifra: non inserire dati altamente riservati in quel prototipo. Non inserire dati altamente riservati su dispositivi condivisi. Dettagli: [docs/PRIVACY.md](docs/PRIVACY.md).

Non esporre il server in rete senza autenticazione e revisione di sicurezza. Non inserire segreti nel repository.
