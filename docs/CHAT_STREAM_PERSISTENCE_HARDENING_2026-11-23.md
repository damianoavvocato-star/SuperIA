# Verifica persistenza chat durante lo streaming — 23 novembre 2026

## Modifiche verificate

- Commit `ec03c7d329f6c5945e7e7d29735ae674372f5ea1`: corretto il test della generazione ID. Con gli ID 42, 43 e 44 già occupati, il candidato 42 deve produrre 45; la funzione di produzione era corretta, l'aspettativa del test era errata.
- Commit `12fe5d19bbc58d7c1f4f39a2ff747a4093d1a347`: il normale invio salva la cronologia con messaggio utente pending e segnaposto assistant prima di iniziare la richiesta di rete. Nel percorso di ripresa è stato rimosso un salvataggio duplicato; resta un singolo salvataggio del segnaposto prima dello streaming.
- La persistenza dei delta continua a usare snapshot cifrati periodici e una coda serializzata con versioni, in modo che uno snapshot precedente non sovrascriva uno più recente.

## CI sul commit 12fe5d1

- Android unit tests: [run 45158387411](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387411) — SUCCESS, `gradle --no-daemon testDebugUnitTest`, BUILD SUCCESSFUL in 38s.
- Backend tests: [run 45158387265](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45158387265) — SUCCESS, **116 passed, 1 warning in 1.57s**.
- Il workflow Android esegue soltanto `testDebugUnitTest`; non esegue assembleDebug/assembleRelease e non produce APK.

## Stato e limiti

Questa verifica conferma i test automatici e il percorso di salvataggio preventivo nel codice. Non dimostra ancora il recupero dopo terminazione forzata del processo, riavvio del telefono, sospensione Android o perdita reale della rete. Queste prove richiedono un dispositivo/emulatore e il backend online reale; le micro-fasi 26, 28 e 29 restano quindi in attesa di verifica pratica secondo il piano. Il gate 36 resta bloccato e non è autorizzata alcuna build APK.

## Prossima verifica tecnica

Eseguire test strumentali su dispositivo/emulatore per: invio e chiusura forzata prima del primo delta; chiusura durante delta; riapertura e retry con lo stesso request ID; risposta già completata/replay; rete interrotta e ripristinata. Verificare inoltre che l'archivio AES-GCM rimanga leggibile dopo ogni interruzione.
