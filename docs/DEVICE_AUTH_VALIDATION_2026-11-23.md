# Autenticazione individuale dispositivi — micro-fase 09

Data: 23 novembre 2026  
Commit test: `54dd8a2f507e79fb00ebbe0e74e24dde7ad543d3`

## Codice verificato
`services/api/app/device_auth.py`:
- Token casuale emesso con `secrets.token_urlsafe(32)`.
- Il server conserva SHA-256 del token, non il token in chiaro.
- Ogni dispositivo riceve un UUID dedicato; il database impone unicità su token hash e device ID.
- Scadenza impostata a 90 giorni.
- Validazione rifiuta token assente, scaduto o revocato.
- La funzione di autenticazione restituisce l'ID dispositivo e non espone l'hash.
- Revoca aggiorna soltanto il dispositivo indicato; gli errori del database PostgreSQL non attivano fallback SQLite.

## Test aggiunti
- Verifica scadenza esatta al TTL configurato, inclusa invalidazione al timestamp di scadenza.
- Verifica isolamento: revocare un dispositivo non invalida il token di un altro.
- Suite preesistente: ciclo emissione/validazione/revoca, persistenza del solo hash, concorrenza di validazione/revoca, PostgreSQL e fail-closed in caso di indisponibilità database.

## Evidenza CI
GitHub Actions Backend tests [run 638](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092450472), ID `45092450472`, commit `54dd8a2f507e79fb00ebbe0e74e24dde7ad543d3`: **SUCCESS, 70 passed, 1 warning in 1.08s**, con PostgreSQL effimero.

## Limiti
L'autenticazione individuale è coperta dalla suite automatica. Restano da completare pairing su backend pubblico, rate limiting effettivo al gateway/WAF, test dispositivo reale e procedure operative di rotazione credenziali. Nessun APK.
