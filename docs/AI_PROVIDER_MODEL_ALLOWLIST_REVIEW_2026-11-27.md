# Revisione allowlist modelli gratuiti — 27 novembre 2026

## Obiettivo

Ridurre il rischio che una configurazione errata o troppo permissiva abiliti un modello a pagamento in SuperIA. Il router ora applica allowlist compilate nel codice per Gemini API, GroqCloud e Cohere, sia durante la validazione iniziale sia al momento della selezione runtime. Le dichiarazioni ambientali dell'operatore non possono ampliare queste allowlist.

## Modelli ammessi dal router

| Provider | Model ID ammessi | Evidenza pubblica |
|---|---|---|
| Gemini API | `gemini-3.8-flash`, `gemini-3.1-flash-lite`, `gemini-3-flash-preview` | La pagina ufficiale prezzi Gemini indica input/output Free of charge per questi modelli nel Free tier. |
| GroqCloud | `openai/gpt-oss-120b`, `openai/gpt-oss-20b`, `qwen/qwen3.8-27b` | La pagina ufficiale Rate Limits espone per il Free Plan limiti per modello, inclusi questi tre ID. |
| Cohere | `command-r7b-12-2024` | Cohere documenta Trial API keys gratuite e limitate; il router ammette un solo ID chat già usato e verificato nell'integrazione SuperIA, in modo conservativo. |

## Limiti e condizioni da non confondere

- Gemini Free tier: accesso limitato ai modelli, quote limitate; Google dichiara che i contenuti del Free tier possono essere usati per migliorare i prodotti. Questo è stato accettato dall'utente, ma deve restare dichiarato nell'informativa di SuperIA.
- Groq: le quote sono a livello organizzazione e variano per modello; il superamento dei limiti produce risposte di rate limiting. La verifica del piano e dell'assenza di un metodo di pagamento deve essere eseguita sul singolo account prima dell'attivazione.
- Cohere: le Trial keys sono gratuite ma limitate a 1.000 chiamate API al mese; per Chat la documentazione indica 20 richieste/minuto per modello. La disponibilità del Trial key senza carta e l'assenza di billing devono essere verificate nel pannello dell'account prima dell'attivazione.
- La presenza di un modello in questa allowlist non equivale all'attivazione del provider, non dimostra l'idoneità dell'account individuale e non autorizza l'inserimento di chiavi.
- Nessun account, token API, metodo di pagamento, servizio esterno o deployment è stato creato o modificato da questa revisione.

## Controlli implementati

- Allowlist esplicite per Gemini, Groq e Cohere.
- Rifiuto in fase di configurazione di modelli fuori lista.
- Nuova verifica runtime della allowlist prima di restituire una configurazione provider.
- Test positivi per i modelli ammessi e test negativi per modelli paid/non allowlisted.
- Restano in vigore tutte le attestazioni fail-closed: `APPROVED_FREE`, `NO_PAYMENT_METHOD`, `HARD_STOP`, `NO_FIAT_BILLING`, `DATA_POLICY_APPROVED`.

## Fonti ufficiali

- Gemini API pricing: https://ai.google.dev/gemini-api/docs/pricing
- Groq supported models: https://console.groq.com/docs/models
- Groq rate limits (Free Plan): https://console.groq.com/docs/rate-limits
- Cohere API keys and rate limits: https://docs.cohere.com/docs/rate-limits
- Cohere FAQ: https://docs.cohere.com/docs/cohere-faqs

## Stato

Revisione del codice e test aggiunti su `main`. L'esito CI del commit finale deve essere verificato separatamente. La verifica account-level e il test end-to-end con credenziali reali restano bloccati fino all'autorizzazione esplicita dell'utente.
