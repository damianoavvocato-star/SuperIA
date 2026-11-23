# Diagnostica readiness — micro-fase 18

Data: 23 novembre 2026

## Endpoint
Aggiunto `GET /api/v1/readiness`, protetto dal token amministrativo `SUPERIA_ADMIN_TOKEN`. Non usa il token dispositivo e quindi può essere usato anche quando il database di autenticazione non è disponibile.

Restituisce soltanto stati sintetici, senza URL, host, nomi provider, token, API key o dettagli di eccezioni:
- `process`: processo applicativo raggiungibile;
- `database`: connessione PostgreSQL/SQLite locale disponibile o unavailable;
- `device_auth`: schema `device_tokens` presente o mancante;
- `provider_configuration`: configured, none_configured, invalid_or_unavailable oppure not_checked.

HTTP 200 indica database e schema auth pronti; HTTP 503 indica stato degraded. Nessun segreto viene incluso nel corpo della risposta.

## Distinzione dagli endpoint esistenti
- `GET /health` resta pubblico e controlla soltanto che il processo risponda.
- `GET /api/v1/readiness` è diagnostica amministrativa e controlla database, schema auth e configurazione provider.
- `GET /api/v1/providers` resta endpoint autenticato per lo stato dei provider, senza chiavi.

## Test
Aggiunti test per readiness completa con database disponibile/provider assenti, database PostgreSQL mancante in staging, risposta degradata e redazione del token amministrativo. I test verificano anche che l'assenza di provider non venga confusa con un database non pronto.

## Evidenza CI
GitHub Actions Backend tests [run 691](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093774332), commit `1f6172f32832af73a571e739e61145fd1b12aa3f`: **SUCCESS, 102 passed, 1 warning in 1.19s**, PostgreSQL effimero.

## Limiti
Il test usa database CI e non prova un database Aiven reale o un deploy pubblico. La diagnostica è pronta nel codice, ma per usarla in staging serve il provisioning e il secret admin. Nessun APK.
