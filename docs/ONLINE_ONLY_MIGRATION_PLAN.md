# Migrazione SuperIA a online-only

Aggiornato: 20 novembre 2026

## Decisione vincolante

La versione finale di SuperIA esegue l'inferenza esclusivamente online. Gemma 3 1B e qualsiasi runtime locale sono componenti legacy di transizione: non vanno cancellati dal telefono né rimossi dalla build stabile finché il nuovo percorso online non è stato verificato end-to-end.

## Regole economiche inderogabili

- Sono ammessi crediti gratuiti interni al servizio (crediti, token, punti o unità), anche se assegnati una tantum o periodicamente.
- Sono esclusi depositi, ricariche, acquisti, addebiti e saldi in valuta (euro, dollari, dinari o qualsiasi altra moneta).
- Non collegare carte o metodi di pagamento e non attivare prove che possano convertirsi automaticamente in piani a pagamento.
- Quando la quota gratuita termina, il router passa al prossimo provider eleggibile; se tutti sono esauriti, sospende la risposta e conserva la conversazione senza spendere denaro.
- Non creare account multipli né ruotare chiavi per aggirare quote o condizioni.
- Ogni provider resta disabilitato finché non sono verificati account, modello gratuito esatto, termini d'uso, privacy, disponibilità in Italia e hard-stop a quota zero.

## Stato tecnico verificato nel repository

- Esiste un prototipo FastAPI con router fail-closed per Gemini, Groq, OpenRouter, Cloudflare Workers AI e SambaNova.
- Il router richiede attestazioni esplicite per gratuità, assenza di metodo di pagamento, hard-stop e assenza di billing in valuta.
- OpenRouter accetta solo ID modello con suffisso `:free`.
- Sono presenti test unitari per provider disabilitati per default, rifiuto di modelli OpenRouter non gratuiti, failover su 429, controlli di billing e persistenza SQLite di cooldown/disabilitazioni tra istanze del router.
- Il backend non è pubblicato e non è collegato all'app Android.
- Il router ora salva cooldown e disabilitazioni in SQLite, così sopravvivono alla ricreazione dell'oggetto e al riavvio del processo se il file DB risiede su storage persistente. Il default `/tmp/superia-router-state.sqlite3` è adatto ai test, non a un hosting effimero: in produzione va impostato `SUPERIA_ROUTER_STATE_DB` su un volume persistente.
- Il prototipo non fornisce ancora autenticazione, quote per utente o protezioni anti-abuso.
- Il controllo combinato CI per il commit documentale più recente non ha restituito status checks: non viene dichiarato un nuovo esito CI.

## Fasi e criteri di completamento

1. **Rendere coerenti architettura e documentazione** — in corso. Il README ora distingue la build locale legacy dal prodotto finale online-only.
2. **Rendere robusto il router** — persistenza SQLite dei cooldown e dei provider disabilitati implementata con test dedicati; restano gestione quota più precisa, rate limit per utente, telemetria minima e test d'integrazione. In hosting il DB deve stare su volume persistente.
3. **Verificare i provider** — audit ufficiale aggiornato, modello per modello e account per account. I crediti gratuiti non monetari sono ammessi; i servizi con ricariche/addebiti in valuta sono esclusi.
4. **Preparare backend sicuro** — autenticazione, rate limit, protezione segreti, minimizzazione log e retention. Non pubblicare finché hosting e database non sono verificati a costo zero senza carta né billing automatico.
5. **Collegare Android** — client HTTPS verso il backend, chiavi provider solo sul server, cronologia cifrata mantenuta sul dispositivo e contesto inviato a ogni richiesta.
6. **Collaudare** — conversazione multi-turno, failover tra provider, quota esaurita, indisponibilità rete, riavvio backend, privacy, documenti e recupero cronologia.
7. **Rimuovere Gemma** — solo dopo test end-to-end superati e una nuova build Android installata e verificata. Prima di quel gate, mantenere disponibile la build legacy.

## Nota sui fornitori

La lista nel documento `FREE_PROVIDER_BILLING_AUDIT.md` è una lista di candidati e servizi da verificare, non l'elenco di provider già attivi. Lovable ed Emergent possono essere mantenuti come strumenti/piattaforme con crediti gratuiti per le rispettive funzioni, ma non sono automaticamente endpoint API di chat e non vanno conteggiati come motori di inferenza senza un'API ufficiale compatibile e condizioni verificate.
