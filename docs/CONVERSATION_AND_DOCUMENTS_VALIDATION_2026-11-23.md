# Continuità conversazione e documenti — micro-fasi 29–35

Data: 23 novembre 2026

## 29 — conversazione persistente e memoria
- Cronologia compressa e cifrata AES-GCM con chiave non esportabile Android Keystore.
- AtomicFile protegge l'archivio chat da scritture interrotte.
- Gli ID stabili delle richieste pendenti sono serializzati e ripristinati; gli instrumented test coprono 160 messaggi, cifratura at-rest, migrazione legacy, archivio corrotto, memoria e pending request ID dopo ricreazione dello store.
- Aggiunta UI per modificare, salvare cifrata e cancellare la memoria di progetto.
- Aggiunta esportazione esplicita della cronologia in TXT.
- Limite: gli instrumented test Android Keystore sono presenti in `androidTest` ma non eseguiti perché il workflow attuale non deve creare test APK; serve prova su dispositivo reale. La memoria è manuale/editabile, non ancora un riassunto automatico verificabile.

## 30 — documenti, retrieval e privacy
- Import PDF/DOCX/TXT/CSV ed estrazione locale con limiti: 25 MB, 250 pagine PDF, 200.000 caratteri; OCR italiano offline fino a 20 pagine.
- Archivio cifrato AES-GCM con Android Keystore; ora scrittura AtomicFile e ID UUID per evitare collisioni.
- Ricerca BM25 locale, massimo 5 passaggi e 6.000 caratteri, con etichette fonte e blocco.
- Il prompt completo è limitato a 32.000 caratteri; non viene inviato il file originale, solo il contesto testuale necessario dopo invio chat.
- Aggiunti esportazione esplicita del testo estratto in TXT e cancellazione del singolo documento.
- Policy e controlli descritti in [CHAT_AND_DOCUMENT_DATA_PRIVACY.md](CHAT_AND_DOCUMENT_DATA_PRIVACY.md).
- Limite: OCR, Android Keystore e repository file non sono stati collaudati su dispositivo reale; esportazioni sono copie in chiaro nella destinazione scelta.

## 31 — deploy staging
Bloccata: blueprint Render Free e selezione Aiven sono preparati, ma account, database e deploy non creati senza consenso esplicito. Vedi [RENDER_STAGING_DEPLOY_GUIDE.md](RENDER_STAGING_DEPLOY_GUIDE.md).

## 32 — E2E backend-provider
Bloccata: nessun provider reale attivo, nessuna API key o account provider creato. Non è possibile dichiarare un test di inferenza reale.

## 33 — quote e indisponibilità
Test automatici coprono 401/402/403, 408/429/5xx, errori rete, cooldown, failover solo tra provider gratuiti approvati, nessun provider disponibile, DB non pronto e stop senza fallback a pagamento. I test sono simulati/mocked; provider reale resta fase 32.
Evidenza backend [run 750](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45095467059): **110 passed, 1 warning in 1.29s**, PostgreSQL effimero.

## 34 — sicurezza e abuso
Verificati token individuali, scadenza, revoca, rotazione compare-and-swap, isolamento idempotenza tra dispositivi, payload oversized, ruoli non validi, redazione segreti e fencing concorrente. La parte WAF/IP rate limit resta bloccata alla fase 12; quindi questa micro-fase resta parziale.

## 35 — dispositivi Android reali
Bloccata fino a backend/provider disponibili e consenso per staging. Nessun telefono fisico è stato collegato per il test E2E.

## Evidenza Android
GitHub Actions Android [run 252](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45095467026), commit `83e03d32b6a8d82d2423e16ba57c38045b24b019`: **SUCCESS**, `testDebugUnitTest`, BUILD SUCCESSFUL in 38s. Il workflow non ha eseguito assembleDebug/assembleRelease né prodotto APK.

Stato: 33 completata a livello di test simulati; 29–30 parziali in attesa di dispositivo; 31–32 e 35 bloccate da dipendenze esterne; 34 parziale per WAF.
