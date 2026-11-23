# Privacy dati chat e documenti — 23 novembre 2026

## Conversazioni e memoria
- La cronologia viene compressa e cifrata AES-GCM con chiave non esportabile in Android Keystore.
- Le scritture usano Android AtomicFile per conservare l'archivio precedente in caso di interruzione durante il salvataggio.
- Gli ID stabili delle richieste pendenti sono salvati nella cronologia cifrata, così l'app può riaprire una richiesta dopo il riavvio.
- La memoria di progetto è un testo modificabile dall'utente, cifrato con una chiave separata Android Keystore; può essere salvata o cancellata dalle Impostazioni.
- L'utente può esportare esplicitamente la cronologia in un file TXT o cancellare la cronologia. L'esportazione crea una copia in chiaro nella posizione scelta dall'utente.
- La cancellazione della cronologia non cancella automaticamente la memoria di progetto; le due operazioni sono separate.

## Documenti
- PDF, DOCX, TXT e CSV vengono estratti sul dispositivo; l'archivio contiene il testo estratto cifrato AES-GCM con Android Keystore.
- BM25 ricerca localmente fino a 5 passaggi pertinenti; ogni passaggio conserva il titolo del documento e il numero del blocco come etichetta fonte.
- Il contesto automatico è limitato a 6.000 caratteri. Il prompt complessivo, incluse memoria, cronologia e documenti, è limitato a 32.000 caratteri.
- La chat non carica automaticamente il file originale. Invia al backend soltanto il contesto testuale costruito per la richiesta, quando l'utente invia il messaggio.
- Limiti import: 25 MB/file, 250 pagine PDF, 200.000 caratteri estratti, 50 documenti, 200 MB totali; OCR scansioni limitato alle prime 20 pagine.
- L'utente può eliminare un documento o esportare il testo estratto in TXT. La copia esportata è in chiaro e fuori dall'archivio cifrato dell'app.
- Il dizionario OCR importato resta locale; non viene inviato al backend.

## Controlli utente
- Svuota cronologia chat.
- Esporta cronologia chat in TXT.
- Modifica, salva o cancella memoria di progetto.
- Elimina singolo documento.
- Esporta testo estratto in TXT.
- Rimuovi il contesto documentale dalla prossima richiesta.

## Limiti e rischi residui
- Le copie esportate sono in chiaro: la protezione dipende dalla destinazione scelta dall'utente.
- Non esiste una retention automatica a tempo; la conservazione locale continua finché l'utente non cancella.
- Le prove Android Keystore/AtomicFile sono presenti in `androidTest`, ma il workflow corrente esegue soltanto `testDebugUnitTest`; non sono stati eseguiti test su telefono reale.
- I provider AI reali sono tutti disabilitati. Prima di inviare dati personali, serve completare la verifica account-level della privacy e delle impostazioni ZDR del provider; Gemini Free e OpenRouter Free non sono approvati per la chat privata.
- Il backend non è ancora pubblicato e non va usato per dati riservati.

Nessun file originale, cronologia o memoria viene caricato automaticamente per la sola apertura della schermata.
