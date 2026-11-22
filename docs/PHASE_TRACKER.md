# Registro operativo delle fasi — SuperIA

Aggiornato: 21 novembre 2026
Ambito: esclusivamente il repository SuperIA. Questo registro separa codice presente, test CI, collaudo reale e rilascio.

## Regole inderogabili
- SuperIA deve restare gratuita: niente pagamenti, carte, ricariche, acquisti, overage fatturabili o fallback a pagamento.
- I crediti gratuiti dei provider sono ammessi anche se espressi in valuta o token; quando terminano si passa a un provider gratuito approvato oppure si interrompe il servizio senza addebiti.
- Non creare account multipli né ruotare chiavi per aggirare i termini dei fornitori.
- Le chiavi dei provider restano esclusivamente sul backend.
- Inferenza AI esclusivamente online. Gemma/LiteRT-LM e il runtime locale sono stati rimossi dal progetto Android.
- Cronologia e memoria di progetto restano cifrate localmente; al backend si invia soltanto il contesto necessario.
- SuperIA resta indipendente da Editore Foto e Video IA e Imperium_Italia.
- BLOCCO APK: non generare, assemblare, firmare o pubblicare APK intermedi. L'APK definitivo sarà autorizzato soltanto dopo il completamento e la verifica end-to-end di tutte le funzioni necessarie alla chat online.

## Stato generale verificato
- Repository: https://github.com/damianoavvocato-star/SuperIA, branch predefinito main.
- Android: client HTTPS, chat, pairing individuale, token cifrato tramite Android Keystore, cronologia cifrata, memoria di progetto, ricostruzione del contesto e funzioni documentali presenti nel codice.
- Backend: prototipo FastAPI, autenticazione per dispositivo, pairing, revoca, endpoint chat e stato provider; non è pubblicato in produzione.
- Provider: router free-only presente, provider disabilitati per default; nessun provider di produzione è stato attivato o verificato su account reale.
- Hosting e database: nessun servizio di produzione creato o collegato; i candidati gratuiti restano da verificare prima della registrazione.
- CI Android: workflow modificato per eseguire soltanto gradle --no-daemon testDebugUnitTest. La modifica rimuove assemblaggio debug/release, firma, upload APK e test strumentali che richiedono un APK.
- Non dichiarare un esito CI successivo a tale modifica senza una run verificata. Nessun APK va prodotto durante il completamento della chat online.

## Stato delle 16 fasi

| Fase | Ambito | Stato attuale | Lavoro residuo / criterio di chiusura |
|---|---|---|---|
| 0 | Inventario e progettazione | Documentazione presente, audit incompleto | Verificare individualmente le 182 IA: termini, licenze, prezzi, disponibilità, API e alternative; il catalogo non equivale a integrazione. |
| 1 | Fondamenta Android e connessioni | Parziale | Completare configurazione, pairing/connessione reali, gestione dispositivi e test su hardware. Il pairing backend richiede rate limiting IP effettivo sul gateway/WAF. |
| 2 | Motore AI e chat | Parziale, online-only | Completare e collaudare conversazione multi-turno, streaming, errori, retry, failover, quota esaurita, continuità del contesto e ripresa dopo riavvio. |
| 3 | Documenti e conoscenza | Primo incremento presente | Test pratici PDF/DOCX/TXT/CSV, OCR, archivio cifrato, recupero, citazioni e limiti; migliorare retrieval dove necessario. |
| 4 | Ricerca, navigazione e traduzione | Non integrata nell'app | Ricerca web esplicita, analisi link, estrazione contenuti, traduzione e citazioni. |
| 5 | Immagini e grafica | Non implementata | Pipeline generazione/modifica, OCR immagini e verifica licenze/costi. |
| 6 | Video | Non implementata | Import, montaggio, sottotitoli, trascrizione, traduzione e rendering. |
| 7 | Audio, voce e musica | Non implementata | STT/TTS, pulizia, doppiaggio e gestione dei diritti/consensi. |
| 8 | Programmazione e GitHub | Non integrata come funzione prodotto | Agente coding controllato, diff, branch, commit, issue/PR e test con approvazioni. |
| 9 | Produttività e dati | Non implementata | Documenti, fogli, presentazioni, CSV, formule, grafici e database. |
| 10 | Agenti e automazioni | Non implementata | Workflow multi-step, pianificazione, ripresa, audit e conferme per azioni sensibili. |
| 11 | Aree professionali | Non implementata | Moduli specialistici con fonti, limiti e verifiche specifiche. |
| 12 | Ecosistema modelli/componenti | Registry documentale | Gestione operativa di componenti, versioni, dipendenze, licenze e compatibilità. |
| 13 | App unica e interfacce | UI Android di base | Completare interfaccia unificata, conversazione persistente, progetti, accessibilità e gestione dispositivi. |
| 14 | Privacy e sicurezza | Protezioni parziali | Threat model, rate limiting, retention, export/cancellazione, isolamento, minimizzazione log e test di sicurezza. |
| 15 | Collaudo e distribuzione | Non chiusa; APK bloccato | Test unitari/integrati, prove su telefoni reali, sicurezza, prestazioni e verifica online end-to-end. Solo dopo autorizzare l'unica build APK definitiva. |

## Chat online: checklist obbligatoria prima dell'APK
1. Backend HTTPS pubblicato su hosting gratuito verificato, senza carta, pagamenti o overage.
2. Database persistente configurato e verificato; niente stato di produzione su filesystem temporanei come /tmp.
3. Rate limiting IP reale sul gateway/WAF prima di abilitare il pairing pubblico.
4. Pairing individuale, token cifrato sul dispositivo, hash lato server, scadenza e revoca testati.
5. Almeno un provider AI gratuito verificato su account reale; chiavi solo server-side.
6. Rotazione tra provider gratuiti con cooldown, hard-stop e nessun fallback a pagamento.
7. Streaming della risposta, timeout, errori HTTP, retry e ripresa delle richieste interrotte verificati.
8. Conversazione multi-turno con cronologia, memoria e documenti; contesto pertinente senza inviare tutta la cronologia.
9. Quota esaurita, provider indisponibile, rete assente, riavvio backend e database testati.
10. Privacy: minimizzazione dati/log, retention, revoca dispositivo, export/cancellazione e segreti verificati.
11. Test end-to-end completati su backend e dispositivi Android reali.
12. Documentazione aggiornata e tutti i gate superati.

Finché anche un solo punto obbligatorio resta aperto, non generare APK.

## Sicurezza del backend
- SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=true è soltanto un gate di configurazione: non implementa il rate limiting. Non abilitarlo in produzione finché la protezione effettiva non è configurata e provata sul gateway/WAF.
- /health verifica la disponibilità del processo, non quella dei provider o la piena operatività.
- Il backend non deve esporre chiavi, token o dettagli sensibili di configurazione.
- PostgreSQL persistente è necessario per lo stato condiviso di produzione; SQLite su /tmp è ammesso soltanto per test/sviluppo.
- Non attivare provider o servizi hosting finché gratuità, assenza di carta, assenza di billing automatico e hard-stop non sono verificati.

## Criteri di aggiornamento
- Registrare commit, run CI, test report e dispositivo per ogni verifica.
- Distinguere sempre codice presente, test CI superato, test reale superato e funzione pronta.
- Non attribuire percentuali globali arbitrarie alle fasi.
- Non dichiarare un servizio online operativo finché non è pubblicato e testato.
- Non creare APK intermedi: il blocco è vincolante fino alla chiusura della checklist chat online.

## Aggiornamento 21 novembre 2026 — streaming chat
- Backend: endpoint SSE autenticato `/api/v1/chat/stream` aggiunto.
- Router: parsing dei delta OpenAI-compatible; failover limitato alla fase precedente al primo token; niente cambio silenzioso dopo output parziale.
- Android: client SSE e aggiornamento progressivo della risposta nella chat unica; salvataggio del testo parziale in caso di errore.
- Test: aggiunti test backend e Android. Esito CI ancora non verificato; test end-to-end con provider reale, cancellazione di rete e ripresa completa restano aperti.
- Questa funzione è quindi **implementata a livello iniziale, non ancora validata**. Non sblocca la generazione dell'APK.

- Ulteriore protezione Android: la cancellazione della coroutine chiude la connessione HTTP sottostante; aggiunto test backend che impedisce il failover dopo l'emissione di un frammento.

- Il comando di ripresa conserva nella cronologia il testo parziale già mostrato, ma lo esclude dal contesto della nuova generazione per evitare che venga ripetuto come risposta completa.


- Persistenza durante lo streaming: Android salva snapshot cifrati della conversazione a intervalli limitati (circa 900 ms), con scritture serializzate e controllo versione per impedire che uno snapshot più vecchio sovrascriva uno più recente. Al termine viene forzato un salvataggio completo; in caso di errore/cancellazione resta attivo il salvataggio di recupero già esistente.
- Limite noto: il server non dispone ancora di un registro persistente di idempotenza per richiesta. Un retry dopo perdita della connessione può quindi avviare una nuova generazione, non riprendere quella precedente. Non dichiarare garantita l'esecuzione exactly-once finché request ID, stato e risposta non sono registrati atomicamente nel database persistente condiviso.
