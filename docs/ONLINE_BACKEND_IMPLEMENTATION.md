# Backend online — configurazione free-only

Il backend FastAPI usa il router `FreeOnlyRouter`. La versione finale della chat è online-only e free-only; nessun fallback locale o a pagamento è consentito.

## Stato attuale

- Provider disabilitati per impostazione predefinita.
- Il router prova un provider gratuito verificato alla volta.
- Su quota/limite temporaneo (429) applica un cooldown; su credenziali, billing o permessi incompatibili (401/402/403) disabilita il provider.
- Se non esiste un provider idoneo, restituisce HTTP 503 senza tentare acquisti, upgrade o top-up.
- Le chiavi provider restano esclusivamente nell'ambiente server.
- Le API applicative richiedono ora un token Bearer server-side. `/health` resta pubblico per il controllo di disponibilità.

## Autenticazione API

Prima di esporre il backend, impostare un segreto lungo e casuale nell'ambiente server:

```bash
SUPERIA_API_ACCESS_TOKEN=<segreto-casuale-lungo>
```

Le rotte `/api/v1/capabilities`, `/api/v1/providers`, `/api/v1/models` e `/api/v1/chat` rifiutano richieste senza token valido. Se il token server non è configurato, le rotte falliscono in modo chiuso con HTTP 503. Non inserire il token nel repository, nell'APK o nei log. La schermata chat Android ora permette una configurazione manuale temporanea di endpoint HTTPS e Bearer token, salvati cifrati con Android Keystore. Il pairing che rilascia token individuali, revoca e rotazione non è ancora implementato: non distribuire l'app con un token condiviso incorporato.

Questa autenticazione a token singolo è una protezione iniziale per un backend privato, non sostituisce account per utente, revoca/rotazione dei token, rate limiting, quote per utente o protezione DDoS.

## Variabili provider

Impostare `SUPERIA_FREE_ONLY=true` solo dopo aver verificato manualmente le condizioni dell'account. Per ogni provider occorrono tutte le attestazioni:

- `SUPERIA_PROVIDER_GROQ_APPROVED_FREE=true`
- `SUPERIA_PROVIDER_GROQ_NO_PAYMENT_METHOD=true`
- `SUPERIA_PROVIDER_GROQ_HARD_STOP=true`
- `SUPERIA_PROVIDER_GROQ_NO_FIAT_BILLING=true`
- `SUPERIA_PROVIDER_GROQ_API_KEY=<segreto server>`
- `SUPERIA_PROVIDER_GROQ_MODEL=<ID modello verificato gratuito>`
- `SUPERIA_PROVIDER_GROQ_FREE_MODEL_IDS=<ID esatti separati da virgola>`

Per OpenRouter usare il prefisso `OPENROUTER` e solo modelli con suffisso `:free`. Per Gemini, Cloudflare e SambaNova usare i rispettivi prefissi. Cloudflare richiede anche `SUPERIA_PROVIDER_CLOUDFLARE_ACCOUNT_ID`.

Sono ammessi crediti gratuiti interni al servizio (token, punti o crediti), anche se espressi con unità nominali; sono esclusi carta/metodo di pagamento, denaro reale, ricariche, acquisti, addebiti automatici e conversioni a piani a pagamento. La quota deve arrestarsi a zero. Le attestazioni ambientali non verificano da sole il conto: un amministratore deve controllare le condizioni reali e lasciare il provider disabilitato in caso di dubbio.

## API

- `GET /health` — pubblico, solo stato del servizio.
- `GET /api/v1/capabilities` — Bearer richiesto.
- `GET /api/v1/providers` — Bearer richiesto.
- `GET /api/v1/models` — Bearer richiesto.
- `POST /api/v1/chat` — Bearer richiesto.

Esempio di richiesta autenticata:

```http
Authorization: Bearer <SUPERIA_API_ACCESS_TOKEN>
Content-Type: application/json
```

```json
{
  "messages": [
    {"role": "user", "content": "Ciao"}
  ]
}
```

La risposta include `content`, `model` e `provider`. La conversazione resta indipendente dal provider: l'app invierà il contesto necessario ricostruito dalla cronologia cifrata locale.

## Blocchi prima del rilascio

1. Verificare per ogni provider termini, privacy, retention, uso dei dati e idoneità per il caso d'uso.
2. Confermare manualmente assenza di carta e possibilità di hard-stop per ogni account.
3. Implementare pairing/app authentication con token individuali, revoca e rotazione; la configurazione manuale attuale è soltanto un ponte di sviluppo.
4. Aggiungere rate limiting e quote per utente, limiti di dimensione e protezione anti-abuso.
5. Scegliere hosting e storage solo dopo aver verificato che siano gratuiti senza carta, billing automatico o rischio di addebiti.
6. Pubblicare il backend su HTTPS e completare il collaudo del collegamento Android; non esiste ancora un endpoint pubblico configurato.
7. Eseguire test backend e collaudo end-to-end su dispositivi Android reali.

Il piano gratuito di alcuni provider può prevedere trattamento dei prompt per migliorare i prodotti: non inviare dati riservati finché la relativa policy non è stata verificata e approvata.

## Test automatici

I test coprono provider disabilitati per default, rifiuto di modelli OpenRouter non free, attestazioni di pagamento obbligatorie, failover su 429, persistenza del cooldown/disabilitazione e autenticazione Bearer delle API.
