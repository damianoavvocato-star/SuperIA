# Android SSE streaming reliability review — 23 novembre 2026

## Obiettivo
Verificare che un'interruzione o un evento SSE malformato non venga scambiato per una risposta completa e che la richiesta resti riprendibile.

## Risultati dell'audit statico
- `OnlineAssistantClient.consumeSseEvents` termina con errore se lo stream finisce senza evento `done`; se sono già arrivati frammenti, il messaggio esplicita che il testo parziale è conservato.
- Il client Android esegue `onDelta` per ogni frammento ricevuto. La UI aggiorna il placeholder assistant e chiama `StreamHistoryPersister.onDelta`.
- Il messaggio utente pendente viene marcato completato e il payload di retry rimosso soltanto dopo il ritorno regolare da `OnlineAssistantClient.stream`.
- In caso di eccezione, la UI conserva lo stato pendente e prova a fare flush in `NonCancellable`; il retry riutilizza request ID e payload congelato. Alla prima nuova delta il testo parziale visualizzato viene sostituito, non concatenato, evitando duplicazioni.

## Hardening introdotto
- Gli eventi `delta`, `done` ed `error` ora richiedono JSON valido.
- `delta.text` deve essere una stringa; payload malformati o con tipo errato causano errore esplicito.
- Aggiunti test per delta JSON malformato, testo non stringa e completamento malformato.
- Commit parser: `a248b47c73054781a9c803ea636cacc5fa883be6`.
- Commit test: `992f6cdee2b12ce8dc8fe36ce0628d3910096dff`.

## Esito CI e correzione
- Backend sul commit dei test: SUCCESS, run 45269370767.
- Android unit tests sui commit parser/test: FAILURE. I test che percorrono `JSONObject` falliscono in JVM locale perché Android fornisce stub non implementati per `org.json`; non è un errore di compilazione del parser.
- Aggiunta dipendenza `testImplementation("org.json:json:20250517")` per fornire una vera implementazione JSON ai test JVM: commit `20b33283961322f1adae8df2d29aae3cc98ef543`.
- CI Android e backend per il commit della dipendenza: run 45269768768 e 45269768629; all'ultimo controllo risultavano ancora in corso. Non dichiarare il fix validato finché Android non termina verde.

## Limiti non verificati
- Non è stato eseguito un test E2E con socket realmente interrotto e backend/provider reale.
- Non è stato eseguito force-stop/riavvio dell'app su dispositivo fisico o emulatore.
- Il callback di cancellazione disconnect è stato verificato staticamente; la prova pratica resta da fare.
- Micro-fase 29 resta aperta; gate 36 resta bloccato. Nessun APK e nessun servizio esterno attivato.
