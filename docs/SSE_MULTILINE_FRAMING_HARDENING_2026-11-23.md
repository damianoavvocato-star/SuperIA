# SSE multiline framing hardening — 23 novembre 2026

## Modifica
Il parser Android ora accumula tutte le righe `data:` fino alla riga vuota che delimita l'evento SSE, concatenandole con newline come previsto dal protocollo. I commenti heartbeat vengono ignorati. Gli eventi incompleti a EOF non vengono dispatchati; il flusso viene considerato interrotto e la richiesta resta riprendibile.

## Test aggiunti
- Evento delta JSON suddiviso su più righe `data:`, ricomposto e consegnato come un solo delta.
- Evento delta senza delimitatore finale: non viene consegnato e EOF non viene scambiato per completamento.
- Restano attivi i test per JSON malformato, tipo errato, evento done malformato, heartbeat, error dopo delta e EOF senza done.

## Commit
- Parser: `772f711040c1930eaa25d27883903b7122d1b3e8`.
- Test: `5edf07e801c326f0eb98d44dcdaa187c479580d2`.

## CI
- Android run 45270145822 e backend run 45270145607, avviate dal commit parser, risultavano ancora in corso al momento della verifica.
- La validazione finale richiede Android unit tests verdi sul commit con i nuovi test.
- Nessun APK: workflow Android limitato a `testDebugUnitTest`.

## Limiti
Il test usa un reader in memoria: non sostituisce un test E2E con connessione TCP interrotta né il collaudo su dispositivo fisico. Micro-fase 29 resta aperta; gate 36 resta bloccato.

## Correzione fixture dopo la prima CI — 23 novembre 2026
La prima CI Android sul commit dei test ha evidenziato 6 fallimenti. L'analisi dei casi ha mostrato che `trimIndent()` rimuove le righe vuote finali delle fixture: alcuni test non terminavano quindi l'ultimo evento con il delimitatore SSE richiesto. Non era un errore di compilazione Kotlin. Le fixture dei test per eventi completi ora aggiungono esplicitamente il delimitatore; i test EOF distinguono il delta correttamente terminato ma senza `done` dall'evento lasciato intenzionalmente incompleto.

- Commit test aggiornati: `547b6bb8e9ed9dd074ef0ba88fdbdfc05ddcafb2`.
- Android run 45270440049 e backend run 45270440091: **in corso** all'ultimo controllo. Nessun risultato verde dichiarato.

## Esito CI e test di conservazione parziale — 23 novembre 2026
- Android unit tests run 45270440049: **SUCCESS** sul commit `547b6bb8e9ed9dd074ef0ba88fdbdfc05ddcafb2`.
- Backend tests run 45270440091: **SUCCESS** sul medesimo commit.
- Aggiunto un test ulteriore: un delta valido deve restare consegnato al callback quando il successivo evento delta contiene JSON malformato.
- Commit del nuovo test: `e083de6094fab41afe21fd7c04cf05d3f344af2d`. La CI di questo nuovo commit deve ancora concludersi.
- Verifica statica: la cancellazione coroutine chiama `connection.disconnect()`; ciò non sostituisce il test reale di rete interrotta.


## Verifica CRLF, dati SSE vuoti e campi di estensione — 23 novembre 2026
- Il parser distingue ora un evento senza campo `data:` da un campo `data:` presente ma vuoto: quest'ultimo viene dispatchato correttamente e un `done` vuoto è rifiutato come JSON non valido. Commit parser `fd2f7f3e489289d27bd8c7f93a67bef30e6bf966`.
- Aggiunti test per terminatori CRLF e `done` con `data:` esplicitamente vuoto. Commit test `0b84d9fa662f847ec3e87a9581afc7ab45df4a70`.
- Android unit tests [45271269847](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271269847) e Backend tests [45271269817](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271269817): **SUCCESS**.
- Aggiunto ulteriore test che verifica l'ignorare corretto di campi SSE di estensione (`id`, `retry`, campo sconosciuto) e di un tipo evento non gestito, mantenendo il successivo delta/done. Commit test `46ebb5a6376bb048c5e4a391a6b7a29056f310ae`; CI Android e backend avviate, esito da verificare.
- Nessun APK; workflow Android resta limitato a `testDebugUnitTest`. Micro-fase 29 aperta per i test strumentali su emulatore/dispositivo e prove reali di rete/riavvio. Gate 36 bloccato.


## Validazione finale campi di estensione SSE — 23 novembre 2026
- Il test `sseParserIgnoresUnknownFieldsAndEventTypes` è stato validato: Android unit tests [45271516710](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516710) e Backend tests [45271516892](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271516892): **SUCCESS**.
- Sono inoltre concluse con **SUCCESS** le verifiche backend sui commit di report [45271549194](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271549194) e tracker [45271565435](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45271565435).
- Il test copre campi `id`, `retry`, campi sconosciuti e un tipo evento non gestito, seguito da delta e done validi.
- Questo completa la validazione CI del blocco di hardening SSE descritto qui; non dimostra la resilienza a una disconnessione TCP reale né il comportamento dopo force-stop/riavvio su dispositivo.
- Micro-fase 29 resta aperta in attesa dei test strumentali e delle prove pratiche; gate 36 resta bloccato. Nessun APK generato.
