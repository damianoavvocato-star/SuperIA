# Baseline gestione segreti — micro-fase 17

Data: 23 novembre 2026

## Misure implementate
- Esteso `.gitignore` per ignorare `.env`, `.env.*`, SQLite/database locali, chiavi private `*.pem/*.key/*.p12/*.pfx` e directory `secrets/`.
- Aggiunte eccezioni per mantenere versionabili soltanto i template `.env.example` e `.env.*.example`.
- Aggiunto `services/api/.env.example` con placeholder vuoti, ambiente development, provider disabilitati e pairing edge false. Non contiene credenziali reali.
- Aggiunto test che controlla che `provider_status()` e `list_models()` non restituiscano l'API key.
- Ricerca statica repository per pattern comuni `AIzaSy`, `sk_live_`, `sk-proj-`, `ghp_`, `xoxb-`, assegnazioni di `SUPERIA_CHAT_IDEMPOTENCY_KEY` e `SUPERIA_ADMIN_TOKEN`: nessun match nei risultati di code search.

## Evidenza CI
GitHub Actions Backend tests [run 683](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093635048), commit `cd7d3bf8d8996534d5abe8171362203ac6866d52`: **SUCCESS, 100 passed, 1 warning in 1.14s**, PostgreSQL effimero.

## Limiti e stato
Questa fase ha completato l'igiene del repository e la redazione delle diagnostiche. Non esiste ancora un secret manager di produzione perché il backend non è stato provisionato; le chiavi non sono state create o caricate. La rotazione operativa dei segreti e il controllo dei log di produzione richiedono un ambiente autorizzato. Stato complessivo della micro-fase: **bloccata dalla dipendenza esterna di deploy/secret manager**.

Nessuna chiave reale è stata generata, nessun account esterno creato, nessun APK.
