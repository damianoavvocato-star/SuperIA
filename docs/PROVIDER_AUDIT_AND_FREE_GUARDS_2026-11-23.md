# Provider audit e guard-rail free-only — micro-fasi 19–21

Data: 23 novembre 2026

## Micro-fase 19 — audit provider
Rapporto pubblico aggiornato: [AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md](AI_PROVIDER_FREE_TIER_AUDIT_2026-11-23.md).
- GroqCloud: candidato condizionato a account Free senza carta e ZDR verificato.
- Cloudflare Workers AI: candidato prioritario solo modelli con quota Free, hard-stop e nessun metodo di pagamento.
- OpenRouter: non approvato per chat privata perché i provider a valle hanno policy dati differenti e il piano Free non offre Data Policy-Based Routing.
- Gemini: non approvato per chat privata perché i prompt Free sono usati per migliorare i prodotti Google.
- SambaNova: escluso perché il piano Free richiede metodo di pagamento e acquisto crediti.

Aggiunta attestazione distinta `SUPERIA_PROVIDER_<ID>_DATA_POLICY_APPROVED=true`, oltre a free-only, no-payment-method, hard-stop e no-fiat-billing. Il default resta false. SambaNova è rimosso dall'ordine router e le sue variabili vengono rifiutate.

**Stato micro-fase 19: bloccata** fino a verifica nel pannello account no-card, hard-stop e ZDR per Groq/Cloudflare. Nessun provider è stato abilitato.

## Micro-fase 20 — attivazione primo provider reale
Non eseguita: nessun account provider o API key è stato creato. La fase richiede autorizzazione esplicita per creare/collegare l'account e verificare nel pannello che non esistano carta o billing. Non inserire chiavi in repository o chat. Stato bloccato da consenso e account esterno.

## Micro-fase 21 — guard-rail free-only
Verificati:
- modalità globale `SUPERIA_FREE_ONLY=true` obbligatoria quando si configura un provider;
- attestazioni economiche e `DATA_POLICY_APPROVED` tutte esplicite;
- API key, model ID e allowlist obbligatori; modello attivo deve appartenere all'allowlist;
- OpenRouter accetta soltanto ID `:free`;
- Cloudflare richiede account ID;
- SambaNova escluso e rifiutato;
- HTTP 429 applica cooldown e consente failover soltanto verso provider approvati gratuiti;
- HTTP 401/402/403 disabilita persistentemente il provider;
- HTTP 408/5xx/rete applica cooldown;
- lo stream non cambia provider dopo il primo token; errore successivo interrompe la risposta e non concatena un altro modello;
- se non esiste provider gratuito disponibile, il router si ferma con `NoFreeProviderAvailable`, senza fallback locale o a pagamento.

Test di routing, allowlist, privacy approval, esclusione SambaNova, cooldown, disable e stream. GitHub Actions Backend tests [run 705](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45094107321), commit `75ec9a3d132b5927308eee8e4d677b63d79d30b0`: **SUCCESS, 104 passed, 1 warning in 2.01s**, PostgreSQL effimero.

Nessuna chiave provider è attiva e nessuna richiesta è stata inviata a un provider reale.
