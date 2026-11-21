# Android signing and final APK release — SuperIA

## Stato corrente: rilascio bloccato

Su richiesta dell'utente, non generare, assemblare, firmare o caricare APK intermedi. Il workflow Android corrente deve eseguire esclusivamente i test unitari e non deve contenere passaggi di assemblaggio APK.

La configurazione della firma descritta in questo documento è documentazione preparatoria per l'eventuale APK definitivo. Non attivare ora segreti di firma, workflow di release o upload di artefatti.

## Gate obbligatorio prima della firma

Una build definitiva potrà essere autorizzata soltanto dopo che tutte le funzioni necessarie alla chat online saranno implementate e verificate end-to-end: backend HTTPS, database persistente, rate limiting reale, pairing/autenticazione/revoca, provider gratuiti verificati, streaming, failover, hard-stop quote, continuità conversazionale, gestione errori, privacy e collaudo su dispositivi reali.

## Identità Android

- Application ID attuale: it.superiaprivata.app.
- Il nome visibile del prodotto è SuperIA.
- Non modificare applicationId, namespace o package Kotlin senza un piano di migrazione e autorizzazione esplicita: una modifica può interrompere aggiornamenti, firma, dati locali e identità installata.

## Regole per il futuro APK definitivo

- Usare una chiave di firma persistente e conservarne un backup offline sicuro.
- Non inserire keystore, password, token o chiavi provider nel repository.
- Conservare le credenziali di firma esclusivamente nei GitHub Actions Secrets solo quando il gate APK sarà stato superato e la release autorizzata.
- Verificare firma, application ID, versionCode, aggiornamento in-place e integrità dell'artefatto prima di consegnare l'APK.
- Non produrre APK multipli o intermedi: una sola build finale dopo il completamento della chat online.