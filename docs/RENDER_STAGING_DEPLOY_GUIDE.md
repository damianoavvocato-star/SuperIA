# Preparazione deploy staging Render — micro-fase 14

Stato: **Bloccata da autorizzazione esterna**. Il blueprint è preparato, ma non è stato collegato alcun account e non è stato creato alcun servizio.

## Blueprint
Il file `render.yaml` prepara un servizio web Python in regione Frankfurt:
- root directory `services/api`
- build: installazione requirements
- avvio Uvicorn con la porta fornita da Render
- health check pubblico `/health`
- piano Free
- auto-deploy disattivato
- `SUPERIA_FREE_ONLY=false`
- `SUPERIA_PAIRING_EDGE_RATE_LIMIT_CONFIRMED=false`
- database e segreti come variabili `sync: false`, mai committati nel repository.

## Prima del collegamento
1. L'utente deve autorizzare esplicitamente l'account Render e l'accesso GitHub al solo repository SuperIA.
2. Creare il servizio dal blueprint e verificare che il piano selezionato sia Free.
3. Non aggiungere alcun metodo di pagamento e non effettuare upgrade.
4. Creare il database Aiven Free soltanto dopo consenso separato; selezionare Free e non aggiungere payment method. Verificare nel pannello disponibilità della connessione e TLS.
5. Inserire `SUPERIA_DATABASE_URL` e i segreti solo nel pannello come variabili segrete; non inviarli in chat o committarli.
6. Mantenere `SUPERIA_FREE_ONLY=false` e pairing edge false. Nessun provider AI reale viene abilitato in questa fase.
7. Eseguire deploy manuale e verificare `/health`, log senza segreti e assenza di SQLite/file temporanei per lo stato condiviso.

## Cosa non è stato fatto
- Nessun account Render/Aiven creato.
- Nessuna autorizzazione GitHub App concessa.
- Nessun servizio o database provisionato.
- Nessun segreto inserito.
- Nessun deploy eseguito.
- Nessun APK generato.

La micro-fase 14 non può essere completata finché l'utente non autorizza l'accesso ai servizi esterni e finché il deploy non viene verificato. La selezione documentale di staging è in [FREE_STAGING_HOST_SELECTION_2026-11-23.md](FREE_STAGING_HOST_SELECTION_2026-11-23.md).
