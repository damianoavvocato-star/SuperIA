# Collaudo pratico Android e chat online — SuperIA

Aggiornato: 21 novembre 2026

## Stato e vincolo APK
Questo protocollo sostituisce le vecchie istruzioni per scaricare APK e importare Gemma. Il runtime locale Gemma/LiteRT-LM è stato rimosso e non deve essere testato o reinstallato.

Su richiesta dell'utente, non generare né installare APK intermedi. La pipeline Android resta limitata ai test unitari. Il collaudo su telefono verrà eseguito soltanto dopo il completamento del backend online e la preparazione autorizzata dell'unica build APK definitiva.

## Gate prima del test su telefono
1. Backend HTTPS pubblicato su hosting verificato gratuito, senza carta o addebiti.
2. Database persistente configurato e verificato dopo riavvio.
3. Rate limiting IP reale sul gateway/WAF attivo e testato prima di abilitare il pairing.
4. Provider AI gratuito approvato, modello/API verificati e chiavi conservate solo sul server.
5. Router verificato per failover, cooldown, quota esaurita e hard-stop senza pagamenti.
6. Endpoint chat con streaming, timeout, retry e gestione errori verificati.
7. Test backend e Android unitari superati; nessuna build APK prodotta.

## Piano di collaudo end-to-end dopo il gate APK
Quando il gate precedente è completo e la build definitiva è stata autorizzata, eseguire le prove su Realme C75 e Xiaomi Mi 10T.

### Connessione e pairing
- Verificare che l'endpoint HTTPS sia validato e che HTTP non venga accettato.
- Associare il dispositivo con codice temporaneo; verificare che il codice non venga salvato.
- Verificare che token e device ID siano cifrati localmente e che sul backend venga conservato soltanto l'hash del token.
- Verificare scadenza, revoca e rifiuto dei token scaduti/revocati.
- Verificare il rate limiting reale sul gateway/WAF con richieste ripetute e registrare il risultato.

### Conversazione
- Inviare più messaggi consecutivi e verificare che le risposte tengano conto dei turni precedenti.
- Chiudere e riaprire l'app; verificare cronologia cifrata, memoria e continuità della conversazione.
- Provare una conversazione lunga oltre la finestra del singolo prompt e verificare recupero selettivo senza inventare ricordi.
- Verificare streaming progressivo, annullamento, timeout, errori HTTP, retry e ripresa delle richieste pendenti.
- Verificare che il passaggio tra provider non apra una nuova conversazione e non perda il contesto.

### Provider, quote e rete
- Simulare provider non disponibile, risposta 429, errore 5xx e quota gratuita esaurita.
- Verificare cooldown e passaggio al successivo provider gratuito approvato.
- Quando tutti i provider sono indisponibili, verificare che la richiesta resti recuperabile e che non avvenga alcun addebito o fallback a pagamento.
- Disattivare la rete durante invio e risposta; verificare messaggio di stato e recupero senza duplicazioni.
- Riavviare backend e database; verificare persistenza di token, stato provider e cronologia prevista.

### Documenti e privacy
- Importare PDF, DOCX, TXT e CSV; verificare estrazione, OCR se applicabile, retrieval e contesto inviato.
- Verificare che i dati dei documenti non siano inviati se non selezionati/necessari.
- Verificare revoca dispositivo, cancellazione/esportazione prevista e minimizzazione dei log.
- Controllare che nessuna chiave provider, pairing code o token in chiaro compaia in log o risposte API.

## Scheda risultati da compilare
- Dispositivo e versione Android:
- Versione app/commit:
- Endpoint backend e data test:
- Pairing e revoca:
- Conversazione multi-turno e ripresa:
- Streaming e gestione errori:
- Provider primario e failover:
- Test quota zero e hard-stop:
- Documenti/OCR:
- Riavvio backend/database:
- Privacy e log:
- Tempi, crash, rete e batteria:
- Esito complessivo e difetti aperti:

Non dichiarare il collaudo superato se una prova è stata saltata o non è riproducibile. Non creare una nuova build per correggere singoli difetti prima che tutte le funzioni chat online siano pronte; raccogliere i difetti e integrarli prima dell'unica build definitiva.