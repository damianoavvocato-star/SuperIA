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
