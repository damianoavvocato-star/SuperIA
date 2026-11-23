# Regression test sicurezza pairing e anti-spoofing — 23 novembre 2026

## Obiettivo
Rafforzare la prova automatica del gate applicativo di pairing prima che il WAF/gateway condiviso sia realmente disponibile. Questo lavoro non sostituisce la micro-fase 12 e non abilita il pairing pubblico.

## Modifica
Commit `17a26100832138f37571f258ba9ee00fc393c0d0`:
- Aggiunto test parametrico per i valori non confermati del flag `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED`: vuoto, `false`, `0`, `yes`, `enabled`.
- Il test invia anche header `X-Forwarded-For`, `X-Real-IP` e `Forwarded` falsificati.
- Sostituisce temporaneamente la funzione di emissione token con una guardia che fallisce se il codice la invoca: prova quindi che gli header spoofati non superano il gate e che nessun token viene emesso.
- Verifica risposta HTTP 503, messaggio di protezione non confermata e assenza del pairing code nella risposta.
- Nessun middleware IP in memoria è stato introdotto: non sarebbe affidabile tra istanze e riavvii e potrebbe dare una falsa sensazione di sicurezza.

## Evidenza CI
GitHub Actions Backend tests [run 45119185491](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45119185491), commit `17a26100832138f37571f258ba9ee00fc393c0d0`: **SUCCESS — 115 passed, 1 warning in 1.52s**. Il job usa PostgreSQL effimero per i test.

## Stato e limiti
La regressione applicativa è verificata. La micro-fase 34 resta **In attesa di verifica** perché il criterio include anche rate limiting e abuso sul gateway/WAF reale. La micro-fase 12 resta bloccata: nessuna zona/dominio è stata autorizzata, nessuna regola esterna è stata creata e nessuna prova pubblica 429/Block è stata eseguita. Il gate `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED` deve rimanere false in ogni ambiente esposto finché il controllo condiviso non è applicato e testato.

Nessun account o servizio esterno creato; nessun provider attivato; nessun APK generato.
