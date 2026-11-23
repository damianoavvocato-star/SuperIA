# Piano operativo SuperIA — 50 micro-fasi

Aggiornato: 23 novembre 2026 — micro-fase 04 verificata  
Ambito: esclusivamente `damianoavvocato-star/SuperIA`. Questo piano scompone le 16 macro-aree della roadmap in 50 unità di lavoro verificabili. Non sostituisce i requisiti vincolanti di README e PHASE_TRACKER: li rende eseguibili passo per passo.

## Regole di esecuzione

- Si lavora su una micro-fase per volta, in ordine, senza dichiararla conclusa finché il relativo criterio di accettazione non è verificato.
- Ogni micro-fase registra: obiettivo, file/area interessata, modifica, test, risultato CI o prova pratica, commit e limiti residui.
- Stati ammessi: **Da iniziare**, **In corso**, **In attesa di verifica**, **Bloccata da dipendenza**, **Completata**. Il codice scritto da solo non basta per lo stato Completata.
- Una micro-fase completata non viene riaperta senza annotare il motivo e la regressione.
- Prima di iniziare una fase si controllano le dipendenze e il branch head di `main`; dopo la modifica si controllano diff, test pertinenti e CI disponibile.
- Non creare rami aggiuntivi per frammentare il lavoro: al controllo del 23 novembre 2026 il repository ha soltanto `main` e nessuna pull request. Usare commit piccoli e leggibili su `main`, salvo futura necessità tecnica documentata.
- **Blocco APK assoluto**: nessun assembleDebug/assembleRelease, firma, upload o distribuzione di APK intermedi. Un'unica build APK finale soltanto dopo la micro-fase 36 e dopo tutte le verifiche obbligatorie della chat online.
- Nessun costo: niente carte, pagamenti, ricariche, overage, account multipli o elusione dei limiti. Crediti gratuiti legittimi ammessi; quando finiscono, stop o rotazione verso altro provider gratuito approvato.
- Inferenza esclusivamente online tramite backend SuperIA HTTPS. Nessun runtime LLM locale e nessuna reintroduzione di Gemma/Ollama.
- SuperIA resta separata da Editore_Foto_Video_IA e Imperium_Italia.

## Quadro di avanzamento

| Gruppo | Micro-fasi | Obiettivo |
|---|---:|---|
| Ricognizione e fondamenta | 01–07 | Inventario, decisioni, repository, documentazione e test di base |
| Sicurezza e infrastruttura online | 08–18 | Identità, pairing, protezioni, hosting, database e segreti |
| Motore chat online | 19–30 | Provider gratuiti, streaming, retry, memoria, documenti e privacy |
| Collaudo e sblocco chat | 31–36 | Deploy, prove reali, sicurezza, telefoni e gate finale chat |
| Espansione funzionale | 37–50 | Ricerca, traduzione, media, coding, produttività, agenti e rilascio |

## Elenco delle 50 micro-fasi

### A. Ricognizione e fondamenta

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 01 | Inventario e riallineamento policy delle 182 IA | Conteggio 182/182 verificato; policy online-only applicata ai 182 record; nessuna voce è dichiarata integrata o verificata; audit di provider, licenze e quote resta alla micro-fase 19 e ai relativi test. | Completata |
| 02 | Consolidamento delle decisioni vincolanti | Un'unica policy corrente conferma Android-only, online-only, gratuità, privacy e separazione dei progetti; registri e documenti legacy sono marcati come storici/superati. | Completata |
| 03 | Audit repository, branch e cronologia | Branch, PR, commit recenti, workflow e file principali sono registrati; incongruenze documentali corrette o marcate storiche; residui legacy documentati senza cancellazioni non verificate. | Completata |
| 04 | Allineamento della documentazione | README, ROADMAP, PHASE_TRACKER e documenti di migrazione non si contraddicono; le note storiche sono chiaramente archiviate. | Completata |
| 05 | Audit del client Android | Configurazione HTTPS, token, storage cifrato, gestione errori e UI chat sono mappati rispetto al codice corrente. | In attesa di verifica |
| 06 | Baseline dei test Android senza APK | Il workflow esegue solo testDebugUnitTest; il risultato CI del commit corrente è acquisito e non contiene task di assemblaggio APK. | In attesa di verifica |
| 07 | Baseline dei test backend | Suite backend e PostgreSQL CI sono riproducibili; esito, commit, numero test e warning sono registrati. | In attesa di verifica |

### B. Sicurezza e infrastruttura online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 08 | Validazione centralizzata della configurazione backend | Variabili obbligatorie, valori non validi e configurazioni insicure vengono rifiutati con comportamento fail-closed e test. | Da iniziare |
| 09 | Autenticazione individuale dei dispositivi | Token individuali, hash lato server, scadenza e isolamento tra dispositivi sono coperti da test. | Parziale |
| 10 | Pairing sicuro | Codice pairing, emissione token, limiti, scadenza e risposte d'errore sono verificati. | Parziale |
| 11 | Revoca e rotazione credenziali dispositivo | Revoca immediata, token scaduti e tentativi successivi sono verificati sia lato server sia Android. | Parziale |
| 12 | Rate limiting IP effettivo | Gateway/WAF applica limiti reali prima del pairing pubblico; prove dimostrano blocco, finestra e recupero. Il flag applicativo da solo non vale come prova. | Bloccata da dipendenza esterna |
| 13 | Selezione hosting gratuito | Provider hosting verificato su termini attuali: nessuna carta, pagamento, overage o conversione automatica a piano pagato. | Da iniziare |
| 14 | Pubblicazione backend HTTPS di staging | Backend raggiungibile tramite HTTPS, segreti protetti e deploy ripetibile; health e readiness sono distinti. | Bloccata da dipendenza esterna |
| 15 | Database PostgreSQL persistente | Database condiviso e persistente configurato; nessuno stato di produzione dipende da filesystem temporanei. | Bloccata da dipendenza esterna |
| 16 | Migrazione e verifica dati | Migrazione SQLite/PostgreSQL verificata con conteggi, hash, schema e test di rollback/errore. | Parziale |
| 17 | Gestione dei segreti di produzione | Chiavi provider, token amministrativi e chiave idempotenza sono solo nel secret manager; rotazione e assenza segreti nei log testate. | Da iniziare |
| 18 | Readiness e diagnostica operativa | Endpoint diagnostici distinguono processo, database, autenticazione e provider senza rivelare segreti. | Da iniziare |

### C. Motore chat online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 19 | Audit provider AI gratuiti | Ogni provider candidato è verificato su termini, quota gratuita reale, carta richiesta, hard-stop, modelli e limiti. | Da iniziare |
| 20 | Attivazione controllata del primo provider gratuito | Una sola integrazione è provata con credenziale reale server-side e senza rischio di addebito. | Bloccata da dipendenza esterna |
| 21 | Guard-rail free-only | Configurazione errata, quota finita, 402 e billing non consentito causano stop; nessun fallback pagato è possibile. | Parziale |
| 22 | Rotazione tra provider approvati | Cooldown, selezione alternativa, quote e indisponibilità sono testati; nessuna rotazione di account/chiavi per eludere limiti. | Parziale |
| 23 | Contratto conversazionale multi-turno | Messaggi, ruoli, limiti, contesto e identificativi richiesta sono coerenti tra Android e backend. | Parziale |
| 24 | Streaming SSE lato backend | Eventi delta/done/error, delimitatori, headers, timeout e chiusura stream sono verificati. | Implementata, verifica aggiuntiva |
| 25 | Streaming SSE lato Android | Parsing incrementale, ordine dei delta, completamento, errori e cancellazione sono coperti da test. | Parziale |
| 26 | Heartbeat e connessioni lente | Heartbeat, rinnovo lease, timeout proxy e cancellazione provider alla chiusura client sono verificati. | Implementata, verifica end-to-end aperta |
| 27 | Idempotenza e deduplicazione richieste | Request ID, hash payload, replay cifrato, lease e fencing sono testati su PostgreSQL; limiti exactly-once sono documentati. | CI backend superata, test reale aperto |
| 28 | Retry e ripresa dopo interruzione | Retry usa lo stesso ID, non duplica risposte già persistite e comunica chiaramente quando deve rigenerare invece di riprendere lo stream. | Parziale |
| 29 | Continuità della conversazione e memoria | Cronologia persistente, riepiloghi verificabili, recupero contesto e ripresa dopo riavvio funzionano senza imporre una nuova chat. | Parziale |
| 30 | Contesto documentale e privacy dei dati | PDF/DOCX/TXT/CSV, retrieval, citazioni, minimizzazione contesto, log, retention, export e cancellazione sono testati e documentati. | Primo incremento presente |

### D. Collaudo e sblocco della chat online

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 31 | Deploy di staging ripetibile | Deploy, migrazioni, restart, rollback e monitoraggio essenziale sono ripetibili senza interventi manuali nascosti. | Bloccata da dipendenza esterna |
| 32 | Test end-to-end backend-provider | Pairing, autenticazione, chat multi-turno, streaming e persistenza sono provati contro provider reale gratuito. | Bloccata da dipendenza esterna |
| 33 | Test quote finite e indisponibilità | Esaurimento quota, provider down, 401/403/429/5xx, rete assente e database non disponibile non producono addebiti o perdita silenziosa. | Da iniziare |
| 34 | Test sicurezza e abuso | Rate limit, revoca, accesso incrociato, input oversized, segreti, log e concorrenza superano prove mirate. | Da iniziare |
| 35 | Collaudo su telefoni Android reali | Pairing e chat sono provati su dispositivi fisici, con Wi-Fi/mobile, sospensione app, riavvio, rete interrotta e ripresa. | Bloccata da dipendenza esterna |
| 36 | Gate finale chat online | Tutti i punti 1–12 del PHASE_TRACKER sono chiusi con evidenze. Solo dopo questa fase si può autorizzare la singola build APK finale della chat. | Bloccata dalle fasi precedenti |

### E. Espansione funzionale dell'applicazione

| N. | Micro-fase | Criterio di completamento | Stato iniziale |
|---:|---|---|---|
| 37 | Ricerca web e analisi link | Ricerca su richiesta, estrazione pagine/link, fonti, date e citazioni sono integrate e testate. | Da iniziare |
| 38 | Traduzione e localizzazione | Traduzione testi/documenti e gestione lingua sono integrate con controlli qualità e limiti dichiarati. | Da iniziare |
| 39 | Generazione immagini | Pipeline gratuita approvata o componente legittimo, limiti e risultati sono verificati. | Da iniziare |
| 40 | Modifica immagini e OCR | Editing, rimozione/sostituzione elementi, OCR e gestione file sono integrati e testati. | Da iniziare |
| 41 | Comprensione ed editing video | Import, analisi, taglio, sottotitoli, traduzione e rendering sono verificati su file di prova. | Da iniziare |
| 42 | Trascrizione e analisi audio | Speech-to-text, diarizzazione se supportata e pulizia audio sono integrate con limiti documentati. | Da iniziare |
| 43 | Sintesi vocale e doppiaggio | Text-to-speech, voci e doppiaggio rispettano consensi, diritti e vincoli dei provider. | Da iniziare |
| 44 | Agente programmazione e GitHub | Analisi repository, patch, diff, branch, commit, PR e Actions richiedono permessi minimi e approvazioni. | Da iniziare |
| 45 | Produttività e dati | Documenti, fogli, CSV, formule, grafici, presentazioni ed esportazione sono testati. | Da iniziare |
| 46 | Agenti e automazioni | Workflow multi-step, pianificazione, ripresa, audit e conferme per azioni sensibili sono funzionanti. | Da iniziare |
| 47 | Moduli specialistici e catalogo IA | Le capacità delle 182 IA sono mappate a moduli integrati o alternative legittime, con licenze e stato reale verificati. | Da iniziare |
| 48 | Interfaccia unica e gestione progetti | Chat principale, progetti, moduli, dispositivi e attività sono accessibili senza frammentare la conversazione. | Da iniziare |
| 49 | Prestazioni, accessibilità e compatibilità | Test su telefoni/tablet supportati, consumi, accessibilità, stabilità e limiti hardware sono documentati. | Da iniziare |
| 50 | Collaudo completo e rilascio finale | Tutti i moduli inclusi superano test automatici e pratici; documentazione, privacy, licenze e unico pacchetto finale sono verificati. | Da iniziare |

## Regola pratica per ogni micro-fase

Prima di lavorare:
1. leggere stato e dipendenze della fase;
2. controllare branch `main` e commit corrente;
3. scegliere una sola unità di lavoro.

Dopo aver lavorato:
1. controllare il diff;
2. eseguire soltanto i test pertinenti;
3. attendere e verificare CI quando disponibile;
4. registrare commit e prova;
5. aggiornare lo stato senza anticipare il completamento;
6. passare alla micro-fase successiva soltanto se quella corrente è completata o se il blocco è esterno e chiaramente documentato.

## Stato al momento della suddivisione — 23 novembre 2026

- Branch presente: `main` soltanto; nessuna pull request trovata.
- Ultimi commit comprendono hardening SSE, heartbeat, idempotenza/fencing e test client Android.
- Backend CI run 577: 60 test superati, 1 warning, nessun fallimento, sul commit `036a2be916c7828be6c492d43b6b37fcc1dcacd3`. Questo non è un test end-to-end reale.
- I test Android aggiunti più di recente non risultano ancora verificati da una run CI consultabile; non marcarli come superati.
- Backend pubblico, database di produzione, provider gratuito reale, rate limiting edge e test su telefoni reali restano dipendenze aperte.
- Nessun APK intermedio deve essere prodotto.


### Evidenza micro-fase 01 — 23 novembre 2026

- Conteggio JSON verificato: 182/182 record, numerazione completa da 1 a 182.
- Tutti i 182 record sono stati riallineati alla policy di inferenza online tramite backend SuperIA; strumenti locali ammessi solo per attività non-LLM.
- 16 riferimenti candidati a inferenza LLM locale sono stati sostituiti con il percorso provider online approvato.
- Matrice alternative e analisi funzionale marcate come documenti storici dove proponevano local-first; registro componenti marca Ollama e llama.cpp come superati/non integrabili per inferenza.
- Non è stata dichiarata alcuna verifica individuale di prezzi, termini, quote, licenze, disponibilità o compatibilità: tali controlli restano esplicitamente aperti e saranno eseguiti sulle alternative/provider effettivamente selezionati.
- Verifica strutturale post-commit: nessun candidato nei 182 record conserva riferimenti a inferenza LLM locale; nessun record è stato marcato come integrato.


### Evidenza micro-fase 02 — 23 novembre 2026

- README e PHASE_TRACKER restano le fonti operative correnti; ROADMAP, decisioni Fase 0 e architettura Android ora marcano esplicitamente le proposte di inferenza LLM locale come storiche e superate.
- Il registro modelli locali è stato declassato a riferimento storico: candidati, runtime e decisioni non autorizzano download o integrazioni.
- La policy corrente è coerente: client Android, inferenza AI esclusivamente online via backend HTTPS, provider gratuiti verificati, nessun fallback locale LLM o a pagamento; elaborazioni locali non-LLM consentite solo previa verifica.
- Verifica documentale completata; non equivale a deploy o collaudo runtime.


### Evidenza micro-fase 03 — 23 novembre 2026

Audit statico registrato in [docs/REPOSITORY_AUDIT_2026-11-23.md](REPOSITORY_AUDIT_2026-11-23.md). Repository pubblico non archiviato, default branch `main`, un solo branch e zero PR. Workflow Android esegue solo `testDebugUnitTest`; backend CI run 605 SUCCESS sul commit precedente all'audit; web-preview deploya soltanto `preview/`. README aggiornati al percorso SSE; guida backend token condiviso, architettura e privacy del prototipo marcate storiche; registro componenti Android riallineato. Residui `services/api/app/static/index.html`, `providers/ollama.py` e route legacy `/api/v1/chat` sono stati documentati e non rimossi: richiedono una micro-fase dedicata di pulizia e test.
\n\n### Esito micro-fase 04 — 23 novembre 2026\nDocumentazione principale allineata alla policy online-only, al piano operativo da 50 micro-fasi e allo stato reale della chat SSE. README, ROADMAP, PHASE0_DECISIONS e PHASE_TRACKER riportano la prevalenza del piano corrente; le indicazioni obsolete nel verbale storico sono esplicitamente superate. ONLINE_BACKEND_IMPLEMENTATION resta marcato come storico e rimanda alla documentazione corrente. Verificati i contenuti aggiornati via GitHub dopo i commit. Nessun codice runtime modificato; nessun APK generato.\n