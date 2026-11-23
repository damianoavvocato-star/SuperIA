# Pairing sicuro — micro-fase 10

Data: 23 novembre 2026  
Commit test: `a0f08ad51a890ebdd0992fe65d7ae281d2ef6462`

## Controlli esaminati
- Il pairing è esposto soltanto su `POST /api/v1/device/pair`.
- Richiede `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=true`; in assenza del gate restituisce HTTP 503 prima di verificare il codice.
- Il segreto è server-side (`SUPERIA_PAIRING_CODE`), confrontato con `hmac.compare_digest`; se manca, la configurazione fallisce chiusa.
- Il payload Pydantic impone pairing code tra 16 e 256 caratteri e device name tra 1 e 80 caratteri.
- Il pairing rilascia un token Bearer individuale, device UUID e scadenza; non restituisce il codice pairing.
- L'app Android usa HTTPS, disabilita redirect, non persiste il codice pairing e lo svuota dopo il successo.

## Test aggiunti
- Segreto pairing server mancante → HTTP 503.
- Pairing code troppo corto/lungo e device name vuoto/lungo → HTTP 422.
- Risposta contiene esclusivamente `access_token`, `device_id`, `expires_at`, `token_type`; non contiene il codice pairing.
- Test preesistenti coprono codice errato, emissione token, autenticazione, revoca e gate edge non confermato.

## Evidenza CI
GitHub Actions Backend tests [run 639](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092525104), ID `45092525104`, commit `a0f08ad51a890ebdd0992fe65d7ae281d2ef6462`: **SUCCESS, 76 passed, 1 warning in 1.26s**, con PostgreSQL effimero.

## Limite di sicurezza non risolto qui
Il flag `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED` è soltanto un gate dichiarativo. Non implementa né prova un limite IP reale. Non abilitare il pairing pubblico finché il gateway/WAF non applica e dimostra il rate limiting; questo resta la micro-fase 12. Nessun backend pubblico o provider reale è stato attivato. Nessun APK.
