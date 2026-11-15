# Architettura

- Client: interfaccia web di sviluppo; Flutter previsto per Android e desktop.
- API: FastAPI versionata sotto /api/v1.
- AI Gateway: Ollama locale come prima integrazione; provider remoti non abilitati implicitamente.
- Moduli futuri: immagini, video, audio, coding, ricerca e agenti.
- Dati: conversazioni e memoria personale salvate dal browser in localStorage; nessuna persistenza lato backend.

## Memoria locale
- L'utente gestisce un blocco di testo personale nell'interfaccia.
- Il testo resta nel browser e viene aggiunto come messaggio di contesto alle richieste chat.
- Il contenuto non addestra né modifica i pesi del modello e non è condiviso tra dispositivi.
- Il contesto è limitato a 3000 caratteri e può essere cancellato dall'utente.

## Vincoli
- Default bind su 127.0.0.1.
- Timeout espliciti per chiamate al modello.
- Validazione input e dimensione richieste.
- Nessuna chiave API nel repository.
- Operazioni agentiche e distruttive soggette ad approvazione.
