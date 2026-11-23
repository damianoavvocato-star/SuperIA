# Revisione recupero chat Android — 23 novembre 2026

## Ambito
Revisione statica di `AssistantChatPanel.kt`, `ChatHistoryStore.kt`, `OnlineAssistantClient.kt` e `ChatHistoryStoreInstrumentedTest.kt` sul branch `main`. Nessun backend/provider esterno è stato attivato; nessun APK è stato assemblato.

## Risultati confermati

1. **Ripristino dopo riapertura:** la composable carica la cronologia cifrata quando viene ricreata. Una riga `pending` resta visibile nel pannello “Richiesta non completata” e l'utente può scegliere esplicitamente “Riprendi richiesta”. Non c'è retry automatico all'avvio, evitando invii inattesi.
2. **Identità idempotente:** il retry riusa `requestId`; per le richieste legacy senza ID genera un UUID e lo salva prima della chiamata di rete.
3. **Payload immutabile:** le richieste nuove persistono l'array provider-ready prima di aprire lo stream. I retry riutilizzano `requestMessages` cifrato, senza ricostruirlo da memoria/documenti che potrebbero essere cambiati. Le richieste legacy senza payload lo costruiscono una sola volta e lo persistono prima del retry.
4. **Nessun duplicato di righe:** la riga utente pendente e il placeholder assistant sono salvati prima dello stream; il retry riutilizza il placeholder assistant successivo alla riga pendente. Il primo delta del nuovo stream sostituisce il testo assistant parziale obsoleto; i delta successivi vengono concatenati.
5. **Pulizia:** al completamento il flag pending viene rimosso e il payload temporaneo viene azzerato. La cronologia ordinaria resta cifrata AES-GCM tramite Android Keystore e scritta con AtomicFile.
6. **Validazione:** il decoder verifica ID univoci, request ID UUID univoci, ruoli ammessi, payload soltanto su righe user pendenti, massimo 60 messaggi, limite totale del prompt e corrispondenza dell'ultimo turno user con la riga pendente.

## Evidenze test
- Android unit tests: [run 45268526027](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526027) — SUCCESS; job `android-unit-tests` completato.
- Backend tests: [run 45268526344](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45268526344) — SUCCESS; job `test` completato.
- La suite strumentale include `pendingRequestIdSurvivesStoreRecreationForSafeRetry`, ora esteso al round-trip del payload originale cifrato. Questa suite **non** viene eseguita dal workflow `testDebugUnitTest`; non è quindi una prova su emulatore o telefono.
- Il workflow Android verificato esegue soltanto `gradle --no-daemon testDebugUnitTest`; non è stata eseguita alcuna task di assemblaggio APK.

## Rischi residui / criteri di chiusura
- Eseguire la suite `androidTest` su emulatore e poi su dispositivo fisico, verificando force-stop, riapertura, payload e request ID identici, risposta parziale, retry e assenza di righe duplicate.
- Verificare la disconnessione reale durante SSE e il successivo retry; la cancellazione ASGI è stata corretta lato backend ma non è ancora provata end-to-end su rete reale.
- Verificare sospensione/riattivazione, cambio Wi-Fi/mobile e perdita rete. Il comportamento resta manuale: dopo riapertura l'utente deve premere “Riprendi richiesta”.
- Non dichiarare exactly-once assoluto: un crash server dopo la generazione provider ma prima della persistenza della risposta resta una finestra non eliminabile con la sola idempotenza client.

## Stato
La revisione statica non ha evidenziato una nuova regressione nel percorso di retry. La micro-fase 29 resta **in attesa di verifica strumentale reale**; micro-fasi 26 e 28 restano in attesa di E2E rete/processo; gate 36 resta bloccato. Nessun APK.

## Nuovi casi di integrità del retry — 23 novembre 2026
- Aggiunti tre test alla suite androidTest:
  - UUID duplicato tra due richieste pendenti: il salvataggio deve essere rifiutato e l'archivio precedente deve restare intatto.
  - Payload di retry associato a un messaggio non più pendente: rifiuto.
  - Payload congelato non coerente con il testo del messaggio utente pendente: rifiuto.
- Commit dei test: 13c3f3e7d5781f1779d324477c71a617f83eaf7e.
- Precisazione: il test pendingRequestIdSurvivesStoreRecreationForSafeRetry crea una nuova istanza di ChatHistoryStore nello stesso processo. Verifica il round-trip dell'archivio cifrato, ma non simula un vero processo Android terminato e riavviato.
- Questi tre nuovi casi sono test strumentali non eseguiti dalla task CI testDebugUnitTest. La loro esecuzione su emulatore/dispositivo resta aperta; nessun risultato runtime viene dichiarato.

## Verifica CI successiva — 23 novembre 2026
- Android unit tests [run 45272097900](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272097900): **SUCCESS** sul commit `13c3f3e7d5781f1779d324477c71a617f83eaf7e`.
- Backend tests [run 45272097827](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272097827): **SUCCESS** sullo stesso commit.
- Verifica documentazione [run 45272126971](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272126971) e tracker [run 45272141250](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45272141250): **SUCCESS**.
- Questi workflow non eseguono la suite `androidTest`: i tre nuovi test strumentali restano non eseguiti. Nessuna prova force-stop/process-death o dispositivo reale è stata effettuata.
- Stato invariato: micro-fase 29 aperta in attesa di esecuzione strumentale; nessun APK assemblato.


## Revisione finale test strumentali e vincolo di esecuzione — 23 novembre 2026
- Corretto il commento del test `pendingRequestIdSurvivesStoreRecreationForSafeRetry`: la nuova istanza di store verifica il recupero nello stesso processo Android e non simula process death. Commit `90fc1150650278b78f5e14fb5516d18cf58a6b26`.
- Ricontrollati i test strumentali: coprono cifratura AES-GCM, perdita chiave, archivio AtomicFile/backup, migrazione legacy, cancellazione, concorrenza, payload congelato e integrità UUID. Non emerge un nuovo difetto statico certo che giustifichi modifiche al comportamento applicativo.
- Il workflow Android resta intenzionalmente limitato a `gradle --no-daemon testDebugUnitTest`; non va ampliato con `connectedDebugAndroidTest` perché tale task costruisce/installerebbe gli APK debug e test, in conflitto con il vincolo del progetto di non assemblare APK prima del gate finale.
- Di conseguenza, la suite `androidTest` non può essere eseguita nel rispetto simultaneo dei vincoli attuali. Per sbloccarla occorre una futura autorizzazione esplicita a generare/installare build di test non distribuibili, oppure rivedere formalmente il vincolo APK. Non ho avviato task di assemblaggio.
- La verifica di vero process death/force-stop, riavvio, rete interrotta e dispositivo fisico resta obbligatoria prima di chiudere la microfase 29; nessuna di queste prove è dichiarata eseguita.


## Conferma CI dopo la revisione — 24 novembre 2026
- Android unit tests [run 45273970255](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273970255): **SUCCESS**, commit `90fc1150650278b78f5e14fb5516d18cf58a6b26`.
- Backend tests [run 45273970258](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273970258): **SUCCESS**, stesso commit.
- Controllo documentazione [run 45273993198](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45273993198): **SUCCESS**, commit `311234e4b421e4ddf3a4de0ef1c588fdb05c354f`.
- Confermato che il branch `main` è aggiornato al commit `1331a58f87c9abcf423602d1ba34b2d99b89ddc0`. La suite CI Android resta limitata a `testDebugUnitTest`; nessun test strumentale, APK, emulatore o dispositivo fisico è stato eseguito.
- Nessuna modifica al giudizio di chiusura: micro-fase 29 aperta in attesa di test strumentali e prove reali; gate 36 bloccato dalle dipendenze esterne e dall'E2E.
