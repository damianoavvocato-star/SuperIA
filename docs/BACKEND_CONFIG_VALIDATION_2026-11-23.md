# Validazione configurazione backend — micro-fase 08

Data: 23 novembre 2026  
Commit codice: `01cb05d239cc91b70699752925b86ee3f44401bc`  
Commit test corretti: `a526d3fb69af368a16fe9a3120b7000eed6822e7`

## Modifica
Aggiunta in `services/api/app/providers/free_router.py` la funzione `validate_provider_environment`, invocata all'inizializzazione di `FreeOnlyRouter`. La validazione rifiuta configurazioni parziali o ambigue prima che possano essere usate per richieste ai provider.

Controlli:
- `SUPERIA_FREE_ONLY`, se presente, accetta soltanto `true` o `false`.
- La presenza di credenziali/modello/allowlist/account provider richiede `SUPERIA_FREE_ONLY=true`.
- Ogni provider configurato richiede attestazioni esplicite affirmative: `APPROVED_FREE`, `NO_PAYMENT_METHOD`, `HARD_STOP`, `NO_FIAT_BILLING`.
- `API_KEY`, `MODEL` e `FREE_MODEL_IDS` devono essere tutti presenti.
- Il modello attivo deve essere incluso nell'allowlist gratuita.
- OpenRouter accetta soltanto modelli con suffisso `:free`.
- Cloudflare richiede `ACCOUNT_ID`.
- I messaggi di errore indicano i nomi delle variabili mancanti, mai i valori dei segreti.

## Test aggiunti/aggiornati
Coperti valore globale non valido, credenziali senza modalità free-only, attestazioni mancanti, configurazione parziale, modello fuori allowlist, Cloudflare senza account ID e configurazione valida. I test preesistenti sono stati adattati al nuovo comportamento fail-closed: configurazioni non sicure ora sollevano un errore invece di essere silenziosamente ignorate.

## Evidenza CI
GitHub Actions Backend tests [run 634](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092312609), ID `45092312609`, commit `a526d3fb69af368a16fe9a3120b7000eed6822e7`: **SUCCESS**, `68 passed, 1 warning in 1.14s`, con servizio PostgreSQL effimero. I run 632 e 633 intermedi avevano rilevato aspettative di test non aggiornate; le aspettative sono state corrette e il run 634 sul commit finale è riuscito.

## Limiti
Questa validazione controlla la configurazione dichiarata nell'ambiente; non verifica in autonomia il contratto reale del provider, l'assenza effettiva di una carta nell'account o la correttezza commerciale delle attestazioni. Queste verifiche restano nella micro-fase 19. Non è stato configurato alcun provider reale e non sono stati creati account o servizi esterni. Nessun APK.
