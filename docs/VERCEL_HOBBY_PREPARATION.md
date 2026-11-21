# Preparazione Vercel Hobby per SuperIA Privata

## Stato

Preparazione del codice soltanto. **Nessun account creato, nessun progetto importato e nessun deploy eseguito.** Vercel resta candidato non approvato finché non viene verificata la registrazione senza carta e l'uso del solo piano Hobby.

## Perché è candidato

- Vercel documenta il supporto diretto a FastAPI con Python Functions.
- Il piano Hobby è gratuito e destinato esclusivamente a progetti personali/non commerciali.
- Quando un account Hobby supera le quote gratuite, la documentazione dice che il progetto viene messo in pausa; non è previsto il pagamento di consumi extra sul piano Hobby.
- Il runtime Python usa filesystem in sola lettura con spazio temporaneo `/tmp`: non trattare file locali come archivio persistente.

## Requisiti inderogabili prima dell'eventuale attivazione

1. Registrazione e piano Hobby utilizzabili senza carta, PayPal o altro metodo di pagamento. Se viene richiesto un metodo di pagamento, fermarsi e non proseguire.
2. Selezionare direttamente Hobby gratuito: non avviare trial Pro, non attivare Pro, crediti acquistati, add-on, marketplace o servizi a consumo.
3. Nessun metodo di pagamento collegato e nessun upgrade automatico.
4. Confermare dall'account che il superamento di ogni quota Hobby sospenda il progetto senza addebiti.
5. Accettare Hobby solo per l'uso personale/non commerciale previsto dai termini. Se l'app diventa commerciale o viene offerta come servizio, Vercel Hobby non è idoneo.
6. Prima di esporre l'API, impostare token segreto e chiavi provider esclusivamente come variabili d'ambiente server-side. Non inserirli nel repository o nell'APK.
7. Verificare il comportamento di `/health`, `/api/v1/capabilities`, `/api/v1/models` e `/api/v1/chat` in ambiente di prova. Il test deve includere provider senza crediti, 401/402/403/429 e sospensione per quota.
8. Il router usa SQLite sotto `/tmp` come stato temporaneo: su un runtime serverless non è garantita la persistenza tra istanze o cold start. La conversazione utente resta salvata nell'app; cooldown e disabilitazioni provider devono essere considerati temporanei finché non viene adottato uno storage persistente gratuito e verificato.

## Preparazione del repository

- Aggiunto `services/api/index.py`, entrypoint riconosciuto da Vercel, che esporta l'istanza FastAPI già esistente.
- Root Directory del progetto Vercel, se approvato: `services/api`.
- Il file `requirements.txt` esistente resta nella stessa directory e contiene FastAPI, Uvicorn e httpx.
- Non sono stati aggiunti segreti, credenziali, dipendenze a pagamento o servizi di storage.

## Passi manuali solo dopo verifica del piano

1. Accedere/registrarsi a Vercel senza aggiungere metodi di pagamento e verificare che il piano visualizzato sia Hobby.
2. Importare il repository GitHub `damianoavvocato-star/SuperIA-Privata`.
3. Impostare Root Directory su `services/api`.
4. Verificare che il framework venga rilevato come FastAPI/Python.
5. Configurare variabili d'ambiente server-side solo dopo aver validato l'account e la policy di quota.
6. Effettuare prima un deployment di prova, non collegare ancora l'APK di produzione.
7. Se compare una richiesta di pagamento, trial obbligatorio, carta o upgrade, interrompere e non confermare.

## Fonti ufficiali consultate

- FastAPI su Vercel: https://vercel.com/docs/frameworks/backend/fastapi
- Piano Hobby: https://vercel.com/docs/plans/hobby
- Limiti e pausa automatica dei piani: https://vercel.com/docs/plans
- Runtime Python e filesystem temporaneo: https://vercel.com/docs/functions/runtimes
- Termini, uso personale/non commerciale e pagamenti: https://vercel.com/legal/terms

## Decisione

Vercel è tecnicamente compatibile e ha un comportamento documentato di pausa al superamento delle quote Hobby, quindi è il candidato più promettente finora per l'uso personale. Non è ancora autorizzato alla produzione: resta obbligatoria la verifica manuale dell'assenza di carta e dell'account Hobby senza trial o servizi a pagamento.
