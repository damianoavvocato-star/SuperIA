# Protezione quote gratuite: gestione HTTP 429 — 27 novembre 2026

## Regola di sicurezza

Il router SuperIA disabilita ora in modo persistente qualsiasi provider che risponda HTTP 429, sia nella chat streaming sia nella chat non-streaming.

Motivo: un 429 può indicare un limite al minuto, una quota giornaliera di richieste, una quota di token o un limite di capacità dell'account. Non tutti i provider espongono un header di reset affidabile e uniforme. Un cooldown breve seguito da un nuovo tentativo potrebbe quindi riprovare una quota giornaliera già esaurita.

## Comportamento

- Il provider che restituisce 429 viene escluso e il suo stato viene salvato nel database di stato.
- Il router può continuare con un altro provider già configurato e autorizzato, sempre prima di aver emesso il primo token.
- Non vengono selezionati modelli a pagamento come fallback.
- Il provider disabilitato non viene riattivato automaticamente: serve una verifica manuale e una procedura esplicita di riabilitazione.
- Gli errori 401, 402 e 403 continuano a disabilitare il provider; gli errori transitori 408 e 5xx restano soggetti a cooldown breve.

## Test

Il test di regressione verifica che un 429 ricevuto da Groq venga memorizzato come stato disabilitato e resti tale dopo la ricreazione del router, mentre il failover usa esclusivamente un secondo provider configurato come gratuito.

## Limite noto

La regola è intenzionalmente conservativa: anche un 429 dovuto soltanto a un limite RPM temporaneo disabilita il provider fino a verifica manuale. Questo può ridurre la disponibilità, ma evita di ripetere automaticamente chiamate quando la quota gratuita potrebbe essere esaurita.

## Nessuna attivazione esterna

La modifica non crea account, chiavi API, metodi di pagamento, servizi esterni o deployment. Non è una verifica delle quote effettive dei singoli account provider.
