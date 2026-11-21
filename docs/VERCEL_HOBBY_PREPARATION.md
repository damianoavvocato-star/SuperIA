# Preparazione Vercel Hobby per SuperIA Privata

## Stato
Preparazione del codice soltanto. **Nessun account creato, nessun progetto importato e nessun deploy eseguito.** Vercel resta candidato non approvato finché non viene verificata la registrazione senza carta e l'uso del solo piano Hobby.

## Requisiti hosting
- Vercel documenta il supporto diretto a FastAPI con Python Functions.
- Hobby è gratuito ma destinato esclusivamente a progetti personali/non commerciali.
- I progetti Hobby vengono messi in pausa al superamento delle quote gratuite; non è possibile acquistare overage restando su Hobby.
- Il runtime Python usa filesystem read-only e /tmp temporaneo: nessun database SQLite locale va considerato persistente.
- Prima dell'attivazione verificare registrazione senza carta, nessun trial Pro, nessun metodo di pagamento collegato e sospensione senza addebiti al superamento delle quote. Se una di queste condizioni non è confermata, non attivare.

## Sicurezza backend: associazione dispositivi
Il backend usa token casuali individuali per dispositivo, non un unico token condiviso:
- POST /api/v1/device/pair associa un dispositivo usando SUPERIA_PAIRING_CODE e restituisce un token Bearer casuale valido 90 giorni.
- Il database conserva solo l'hash SHA-256 del token, non il token in chiaro.
- POST /api/v1/device/revoke revoca un dispositivo usando SUPERIA_ADMIN_TOKEN e il suo device_id.
- Tutte le route API protette verificano token attivo, non scaduto e non revocato.
- Se mancano configurazioni o archivio auth, l'accesso fallisce in modo chiuso (503); non viene usato il vecchio token condiviso.
- I token sono individuali e revocabili; il codice pairing e il token amministratore devono essere segreti casuali forti e restare esclusivamente nelle variabili d'ambiente server-side.
- La connessione deve essere HTTPS. Non inserire pairing code, token amministratore o token dispositivo nel repository, nei log o nell'APK.

### Variabili server-side richieste
- SUPERIA_AUTH_DB: percorso del database SQLite persistente che contiene gli hash dei token e lo stato di revoca.
- SUPERIA_PAIRING_CODE: segreto di associazione iniziale, lungo e casuale.
- SUPERIA_ADMIN_TOKEN: segreto amministratore distinto, lungo e casuale, necessario per revocare i dispositivi.
- SUPERIA_API_ACCESS_TOKEN non è più usato: il vecchio token condiviso è stato rimosso dall'autenticazione.

### Limite importante per Vercel
Il database auth è SQLite. Il filesystem /tmp di Vercel è effimero e non condiviso tra istanze: non configurare SUPERIA_AUTH_DB su /tmp in produzione, perché la revoca potrebbe non essere visibile dopo cold start o su istanze diverse. Prima del deploy serve uno storage persistente e condiviso, con piano gratuito verificato senza carta e senza overage; in mancanza, non pubblicare il backend. La conversazione resta salvata cifrata nell'app, non in questo database.

La route di pairing richiede inoltre una protezione anti-abuso a livello gateway/WAF (limite richieste per IP). La limitazione in-process non è sufficiente in un ambiente serverless multi-istanza: non esporre pubblicamente il pairing finché non è configurato questo controllo.

## Limite richieste
Le API rifiutano body superiori a 2 MiB, inclusi i body in streaming senza Content-Length. Il limite è una protezione aggiuntiva, non sostituisce rate limiting, WAF o protezione DDoS del provider hosting.

## Passi prima di un deployment
1. Verificare account Vercel Hobby senza carta e senza trial/upgrade.
2. Verificare uso personale/non commerciale.
3. Trovare e approvare uno storage persistente condiviso senza carta e senza pagamenti/overage.
4. Configurare i tre segreti server-side e lo storage auth persistente; non usare /tmp.
5. Configurare rate limit per IP su pairing e revoca.
6. Eseguire test di pairing, token scaduto, token revocato, database indisponibile, 401/403/429 e provider senza crediti.
7. Integrare la schermata Android di associazione; il client attuale usa ancora il vecchio campo token manuale e non è ancora compatibile con il nuovo endpoint di pairing.
8. Non collegare l'APK di produzione finché tutti i gate non sono superati.

## Preparazione tecnica
- services/api/index.py esporta l'app FastAPI esistente.
- Root Directory Vercel prevista: services/api.
- Il router provider viene riutilizzato nella stessa istanza calda; cooldown e cache router restano comunque best-effort in ambiente serverless.
- Nessun account, deploy, segreto, credenziale provider o metodo di pagamento è stato creato o inserito.

## Fonti ufficiali
- FastAPI su Vercel: https://vercel.com/docs/frameworks/backend/fastapi
- Runtime Python: https://vercel.com/docs/functions/runtimes/python
- Piano Hobby: https://vercel.com/docs/plans/hobby
- Quote e pausa: https://vercel.com/docs/plans
- Limiti Functions: https://vercel.com/docs/functions/limitations
- Termini: https://vercel.com/legal/terms

## Decisione
Vercel resta candidato tecnico, non approvato per produzione. Mancano ancora verifica senza carta, storage persistente condiviso e rate limiting gateway. Nessun deploy è stato eseguito.