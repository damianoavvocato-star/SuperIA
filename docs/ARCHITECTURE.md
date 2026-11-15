# Architettura

- Client: interfaccia web di sviluppo; Flutter previsto per Android e desktop.
- API: FastAPI versionata sotto /api/v1.
- AI Gateway: Ollama locale come prima integrazione; provider remoti non abilitati implicitamente.
- Moduli futuri: immagini, video, audio, coding, ricerca e agenti.
- Dati: nessuna persistenza delle conversazioni in questa versione.

## Vincoli
- Default bind su 127.0.0.1.
- Timeout espliciti per chiamate al modello.
- Validazione input e dimensione richieste.
- Nessuna chiave API nel repository.
- Operazioni agentiche e distruttive soggette ad approvazione.
