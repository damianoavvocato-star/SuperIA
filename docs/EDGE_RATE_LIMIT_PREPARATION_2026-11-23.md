# Preparazione rate limiting gateway — micro-fase 12

Data: 23 novembre 2026  
Stato: **Bloccata da dipendenza esterna**.

## Configurazione candidata verificata
Cloudflare WAF Free documenta una sola rate-limiting rule per zona. Il piano Free consente di usare il path nella condizione, contare per IP e applicare un periodo di 10 secondi con mitigazione di 10 secondi. La regola candidata per SuperIA è:
- espressione: `http.request.uri.path eq "/api/v1/device/pair"`
- caratteristica di conteggio: IP
- soglia iniziale proposta: 5 richieste ogni 10 secondi
- azione: Block
- mitigazione: 10 secondi
- nessuna challenge interattiva, perché il client è un'app Android.

## Limiti documentati
Cloudflare avverte che i contatori possono aggiornarsi con ritardo di alcuni secondi e che richieste eccedenti possono comunque raggiungere l'origine prima dell'azione. La regola Free è quindi una protezione di base, non una garanzia di conteggio matematicamente esatto. Il Rate Limiting API dei Workers è anch'esso approssimativo e per-colocation: non è stato considerato sostituto di un WAF con regola IP.

## Blocco reale
Per applicare una regola WAF alla rotta occorre una zona Cloudflare che controlli il dominio del backend e instradi il traffico attraverso il proxy Cloudflare. Al momento:
- nessun dominio/zone Cloudflare del progetto è stato indicato o autorizzato;
- nessun account o servizio Cloudflare è stato creato;
- non esiste ancora un backend pubblico;
- il flag `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED` resta **false/non configurato** e non deve essere impostato a true sulla sola base di questo documento.

La micro-fase resta bloccata fino a dominio/zone disponibili, regola realmente creata e test esterno che dimostri 429/Block, finestra, recupero e IP distinti. Non esporre il pairing pubblico prima di allora.

## Riferimenti ufficiali
- https://developers.cloudflare.com/waf/ — Free include una rate limiting rule.
- https://developers.cloudflare.com/waf/rate-limiting-rules/ — caratteristiche e limiti del piano.
- https://developers.cloudflare.com/waf/rate-limiting-rules/parameters/ — IP, periodi e mitigazione.
- https://developers.cloudflare.com/workers/runtime-apis/bindings/rate-limit/ — limiti del binding rate limit Workers.
