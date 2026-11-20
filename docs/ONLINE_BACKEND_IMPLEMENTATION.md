# Backend online — configurazione free-only

Il backend FastAPI usa ora il router FreeOnlyRouter. Il vecchio adapter Ollama resta nel repository per riferimento, ma non viene più selezionato dall'endpoint chat.

## Stato attuale

- Modalità richiesta: online e solo gratuita.
- Nessun fallback locale o a pagamento.
- Provider disabilitati per impostazione predefinita.
- Chat accetta la cronologia inviata dall'app e prova un provider alla volta.
- Su 429, 402, 403, 401, 408 o errore 5xx passa al provider gratuito successivo; non tenta upgrade, acquisti o top-up.
- Se non esiste un provider configurato, restituisce HTTP 503 e conserva la cronologia lato app.
- Le chiavi API sono lette solo dall'ambiente server e non devono essere incluse nell'APK, nei file versionati o nei log.

## Variabili server

Impostare SUPERIA_FREE_ONLY=true solo dopo aver controllato manualmente il profilo account di ciascun provider. Per ogni provider abilitato occorrono tutte queste attestazioni:

- SUPERIA_PROVIDER_GROQ_APPROVED_FREE=true
- SUPERIA_PROVIDER_GROQ_NO_PAYMENT_METHOD=true
- SUPERIA_PROVIDER_GROQ_HARD_STOP=true
- SUPERIA_PROVIDER_GROQ_API_KEY=<segreto server>
- SUPERIA_PROVIDER_GROQ_MODEL=<ID modello verificato gratuito>
- SUPERIA_PROVIDER_GROQ_FREE_MODEL_IDS=<lista esatta di ID gratuiti separati da virgola>

Per OpenRouter usare lo stesso schema OPENROUTER e selezionare esclusivamente un modello con suffisso :free. Il codice rifiuta qualsiasi modello OpenRouter senza tale suffisso.

Per Gemini, Cloudflare e SambaNova usare i prefissi GEMINI, CLOUDFLARE e SAMBANOVA. Cloudflare richiede inoltre SUPERIA_PROVIDER_CLOUDFLARE_ACCOUNT_ID.

Queste variabili non verificano da sole il conto provider: sono una conferma amministrativa che l'account non ha metodo di pagamento collegato, che il modello ha prezzo zero e che la quota esaurita causa un blocco. Se una di queste condizioni non è vera, non impostare le attestazioni e lasciare il provider disabilitato.

## API disponibili

- GET /health
- GET /api/v1/capabilities
- GET /api/v1/providers
- GET /api/v1/models
- POST /api/v1/chat

## Esempio richiesta chat

{
  "messages": [
    {"role": "user", "content": "Ciao"}
  ]
}

La risposta include content, model e provider. La conversazione resta indipendente dal provider: l'app deve continuare a inviare il contesto dalla propria cronologia cifrata, mantenendo lo stesso thread anche quando il router cambia fornitore.

## Limiti ancora aperti prima del rilascio

1. La allowlist è un insieme di candidati, non un'autorizzazione legale automatica: vanno verificati termini d'uso per app pubbliche/multiutente, privacy, retention e training.
2. Il piano gratuito di Google può usare i contenuti per migliorare i prodotti: non inviare documenti riservati finché non si approva questo rischio.
3. La registrazione di un account e l'assenza di carta devono essere provate manualmente per ogni fornitore; il backend non può interrogare in modo affidabile la dashboard di billing.
4. Il router non sostituisce un quota manager persistente: per ora si affida ai limiti/risposte del provider e applica fallback senza retry automatici ripetuti.
5. Il backend non è ancora pubblicato su un host. Non è stato collegato all'app Android e non esiste ancora un URL pubblico di servizio.
6. Prima del rilascio occorre aggiungere autenticazione app-server, limiti anti-abuso per utente, protezione DDoS, retention minima e una politica di gestione dei segreti.
7. L'hosting, il database, lo storage, il traffico e il dominio devono essere verificati come gratuiti; non attivare servizi con billing automatico.

## Verifiche automatiche

I test unitari del router controllano:
- provider disabilitato per default;
- rifiuto di modelli OpenRouter a pagamento;
- blocco se manca l'attestazione no-payment-method;
- passaggio da un provider che risponde 429 a un altro provider gratuito, senza fallback a pagamento.
