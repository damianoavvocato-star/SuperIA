# Preparazione Vercel Hobby per SuperIA Privata

## Stato

Preparazione del codice soltanto. **Nessun account creato, nessun progetto importato e nessun deploy eseguito.** Vercel resta candidato non approvato finché non viene verificata la registrazione senza carta e l'uso del solo piano Hobby.

## Perché è candidato

- Vercel documenta il supporto diretto a FastAPI con Python Functions.
- Il piano Hobby è gratuito e destinato esclusivamente a progetti personali/non commerciali.
- La documentazione Vercel dice che i progetti Hobby vengono messi in pausa quando superano le quote gratuite; non è possibile acquistare overage rimanendo sul piano Hobby.
- Il runtime Python usa filesystem in sola lettura con spazio temporaneo `/tmp`: non trattare file locali come archivio persistente.
- Il piano Hobby include quote limitate (ad esempio 4 ore CPU attiva e 360 GB-ore memoria provisionata al mese, oltre a limiti di invocazioni e trasferimento). Le quote possono cambiare: ricontrollarle prima dell'attivazione.

## Requisiti inderogabili prima dell'eventuale attivazione

1. Registrazione e piano Hobby utilizzabili senza carta, PayPal o altro metodo di pagamento. La documentazione pubblica consultata non dimostra da sola che ogni percorso di registrazione sia senza carta: se viene richiesto un metodo di pagamento, fermarsi e non proseguire.
2. Selezionare direttamente Hobby gratuito: non avviare trial Pro, non attivare Pro, crediti acquistati, add-on, marketplace o servizi a consumo.
3. Nessun metodo di pagamento collegato e nessun upgrade automatico.
4. Confermare dall'account che il superamento delle quote Hobby sospenda il progetto senza addebiti. Non fare test intenzionali di consumo oltre soglia.
5. Accettare Hobby solo per l'uso personale/non commerciale previsto dai termini. Se l'app diventa commerciale o viene offerta come servizio, Vercel Hobby non è idoneo.
6. Prima di esporre l'API, impostare token segreto e chiavi provider esclusivamente come variabili d'ambiente server-side. Non inserirli nel repository o nell'APK.
7. Verificare il comportamento di `/health`, `/api/v1/capabilities`, `/api/v1/models` e `/api/v1/chat` in ambiente di prova. Il test deve includere provider senza crediti, 401/402/403/429 e sospensione per quota.
8. Il router mantiene cooldown e provider disabilitati in SQLite sotto `/tmp`, che è spazio temporaneo e non persistente tra istanze/cold start. Il provider router ora viene riutilizzato nella stessa istanza calda tramite cache, quindi i cooldown restano disponibili tra richieste servite dalla stessa istanza; possono però azzerarsi su cold start o su un'altra istanza. La conversazione utente resta salvata nell'app. Nessuno storage persistente esterno è stato aggiunto.

## Preparazione del repository

- Aggiunto `services/api/index.py`, entrypoint che esporta l'istanza FastAPI esistente.
- `services/api/app/main.py` ora riutilizza una singola istanza del router per ogni processo caldo tramite `lru_cache`, evitando di ricreare il router e il database ad ogni richiesta.
- Aggiunto test che verifica il riuso del router nella stessa istanza.
- Root Directory del progetto Vercel, se approvato: `services/api`.
- Il file `requirements.txt` esistente contiene FastAPI, Uvicorn e httpx.
- Non sono stati aggiunti segreti, credenziali, dipendenze a pagamento o servizi di storage.

## Passi manuali solo dopo verifica del piano

1. Accedere/registrarsi a Vercel senza aggiungere metodi di pagamento e verificare che il piano visualizzato sia Hobby.
2. Importare il repository GitHub `damianoavvocato-star/SuperIA-Privata`.
3. Impostare Root Directory su `services/api`.
4. Verificare che il runtime Python venga rilevato correttamente.
5. Configurare variabili d'ambiente server-side solo dopo aver validato l'account e la policy di quota.
6. Effettuare prima un deployment di prova, non collegare ancora l'APK di produzione.
7. Se compare una richiesta di pagamento, trial obbligatorio, carta o upgrade, interrompere e non confermare.

## Fonti ufficiali consultate

- FastAPI su Vercel: https://vercel.com/docs/frameworks/backend/fastapi
- Runtime Python: https://vercel.com/docs/functions/runtimes/python
- Piano Hobby e quote: https://vercel.com/docs/plans/hobby
- Limiti e comportamento al superamento delle quote: https://vercel.com/docs/plans
- Limiti delle Functions: https://vercel.com/docs/functions/limitations
- Termini, uso personale/non commerciale e pagamenti: https://vercel.com/legal/terms

## Decisione

Vercel resta il candidato tecnico prioritario per un'installazione personale: FastAPI è supportato e il piano Hobby viene sospeso al superamento delle quote, anziché consentire overage a pagamento. Non è ancora autorizzato alla produzione: resta obbligatoria la verifica pratica della registrazione senza carta, dell'account Hobby senza trial e dell'idoneità non commerciale. Nessun deploy è stato eseguito.
