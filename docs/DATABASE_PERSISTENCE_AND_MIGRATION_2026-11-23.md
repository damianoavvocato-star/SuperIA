# Persistenza PostgreSQL e migrazione — micro-fasi 15–16

Data: 23 novembre 2026

## Micro-fase 15 — guardia PostgreSQL persistente
Aggiunto `validate_deployment_database_url` in `services/api/app/database.py`:
- development/test possono usare SQLite locale;
- staging/production richiedono URL PostgreSQL;
- `SUPERIA_DATABASE_URL` mancante o SQLite in staging/production viene rifiutato con HTTP 503;
- `SUPERIA_ROUTER_STATE_DB` e il percorso effettivamente selezionato per lo stato router devono anch'essi essere PostgreSQL in ambienti deployati;
- nessun fallback silenzioso a `/tmp` o SQLite per autenticazione e stato provider.

Test API, router e adapter coprono URL PostgreSQL, SQLite locale, database mancante, staging/production e router state temporaneo.
Evidenza GitHub Actions [Backend tests run 678](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093503278): **95 passed, 1 warning in 1.38s**, PostgreSQL effimero.

**Blocco residuo:** nessun database Aiven/Neon è stato creato o collegato. La selezione Aiven per staging richiede consenso e provisioning espliciti; quindi la micro-fase 15 resta bloccata sul database persistente reale. La guardia codice è completata, non la configurazione del servizio.

## Micro-fase 16 — migrazione e integrità
La migrazione `migrate_sqlite_to_database` è verificata per:
- copia dei token e dello stato provider;
- conteggi sorgente/destinazione;
- SHA-256 canonico dei record;
- destinazione non vuota rifiutata;
- tabella device_tokens mancante rifiutata;
- rollback completo quando una tabella di destinazione incompatibile provoca un errore dopo l'inizio della copia;
- sorgente senza provider_state gestita come tabella vuota;
- integrazione PostgreSQL con schema isolato, verifica token e pulizia schema.

Aggiunti test per rollback e tabella provider_state opzionale.
Evidenza GitHub Actions [Backend tests run 680](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093532739): **99 passed, 1 warning in 1.57s**, PostgreSQL effimero.

Nessuna migrazione di dati utente è stata eseguita: non esiste ancora un database esterno autorizzato. Nessun APK.
