# Revoca e rotazione credenziali dispositivo — micro-fase 11

Data: 23 novembre 2026

## Backend
- Aggiunto `POST /api/v1/device/rotate`, accessibile soltanto con il token attualmente valido.
- La rotazione genera un nuovo token casuale, mantiene lo stesso device ID e aggiorna l'hash SHA-256 salvato nel database; il token precedente smette immediatamente di autenticarsi.
- La rotazione usa compare-and-swap sul token hash presentato. Due tentativi concorrenti con lo stesso token non possono entrambi ottenere una credenziale valida: soltanto il primo aggiornamento riesce.
- Token scaduti o revocati non possono essere ruotati.
- Aggiunto `POST /api/v1/device/revoke-self`, autenticato con il token del dispositivo, per revocare immediatamente il dispositivo corrente.
- La revoca amministrativa esistente resta disponibile e separata.

## Android
- `OnlineAssistantClient.rotate()` rinnova il token via HTTPS e valida la risposta.
- `OnlineAssistantClient.revoke()` revoca il dispositivo sul server prima di cancellare il token locale.
- Impostazioni chat: pulsanti “Rinnova token” e “Revoca e disconnetti”.
- L'endpoint del dispositivo associato viene conservato separatamente dal campo modificabile per inserire un nuovo backend. Chat, readiness, rinnovo e revoca usano sempre l'endpoint associato, evitando di inviare il Bearer token a un host digitato successivamente.
- Test unitari aggiunti per rifiuto HTTP e credenziali mancanti nelle operazioni di rotazione/revoca.

## Evidenze CI
- Backend [run 661](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45093088810), commit `a75ea1ebdd538a709891c7cc5ff1b35333892424`: **SUCCESS, 83 passed, 1 warning in 1.05s**, PostgreSQL effimero.
- Android [run 229](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45092957056), commit `2e2a9475f80e27569c3d4fba57894651ce5e04c1`: **SUCCESS**, `testDebugUnitTest`, **BUILD SUCCESSFUL in 36s**. Nessun task APK.

## Limiti
I workflow automatici verificano la logica backend e la compilazione/test unitari Android. Non sostituiscono il test su telefono reale, né il collaudo contro backend pubblico; questi restano nelle fasi infrastrutturali e nella fase 35. Nessun APK creato.
