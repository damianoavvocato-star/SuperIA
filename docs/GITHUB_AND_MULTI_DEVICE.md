# Integrazione GitHub e collegamento multi-dispositivo

## Obiettivo

GitHub è un'integrazione di prima classe di SuperIA: l'utente deve poter gestire repository e progetti software direttamente dall'app Android, chiedendo all'assistente di analizzare, pianificare, modificare e verificare lavori complessi. Il collegamento è facoltativo e revocabile; non rende pubblici i repository privati.

## Collegamento account e sicurezza

- Preferire una GitHub App con autorizzazioni granulari e installazione limitata ai repository scelti dall'utente.
- Per Android usare un flusso OAuth/device authorization nel browser di sistema, verificando i requisiti correnti di GitHub per il tipo di app scelto.
- Mai incorporare client secret, chiavi private GitHub App o token statici nel codice APK, nelle risorse o nel repository.
- Conservare token temporanei nel sistema di protezione Android (Keystore, con storage cifrato); scadenza, rinnovo, revoca e disconnessione espliciti.
- Richiedere solo i permessi necessari alla funzione attivata. Separare lettura e scrittura; chiedere permessi di Actions, amministrazione, deploy o sicurezza solo quando l'utente abilita quelle funzioni.
- Non inviare codice, issue o file a servizi AI esterni senza consenso specifico. L'analisi locale deve essere la modalità predefinita.
- Mostrare sempre repository, branch, file e diff coinvolti prima di scrivere. Richiedere conferma per push, merge, release, deploy, cancellazioni, modifica permessi o esecuzione di workflow che producono effetti esterni.
- Registrare localmente un audit delle azioni: chi le ha richieste, repository, operazione, esito e timestamp; non registrare token o segreti.

## Funzioni GitHub previste

1. Account e repository: elenco repository autorizzati, ricerca, metadati, branch, tag, commit e confronti.
2. Codice: navigazione albero file, ricerca, lettura, diff, creazione e modifica file, commit e push su branch dedicati.
3. Collaborazione: issue, commenti, label, milestone, assegnatari, pull request, review, richieste di revisione e merge con conferma.
4. Automazione: GitHub Actions, workflow, dispatch manuale, stato run, job, log e artefatti quando disponibili e autorizzati.
5. Distribuzione: release, tag, changelog, pacchetti e GitHub Pages dove supportati e autorizzati.
6. Pianificazione: GitHub Projects e milestone per roadmap, task e avanzamento.
7. Qualità e sicurezza: Dependabot alerts, code scanning, secret scanning e security advisories solo se il repository e il piano GitHub espongono tali funzioni e l'utente concede i permessi.
8. Discussione e documentazione: README, Wiki, Discussions e documentazione repository, quando abilitate.
9. Agente di sviluppo: analisi progetto, piano multi-step, generazione codice, test, correzione errori, revisione diff e pull request proposta.
10. Multi-repository: mappa dipendenze tra repository e coordinamento di modifiche correlate, senza operazioni cross-repo non approvate.

Non tutte le funzioni sono disponibili su ogni repository, piano GitHub o tipo di installazione. L'app deve mostrare chiaramente capacità non disponibili e non simulare esiti.

## Agente software e lavori complessi

Pipeline proposta:
1. L'utente sceglie repository, branch e obiettivo.
2. L'agente esamina struttura, README, issue, workflow, dipendenze e test pertinenti.
3. Produce piano e lista file da modificare.
4. Crea un branch di lavoro; non lavora direttamente su main per impostazione predefinita.
5. Modifica file in gruppi piccoli e mostra diff leggibile su smartphone.
6. Avvia test locali Android dove applicabili o GitHub Actions su richiesta.
7. Legge log, corregge gli errori e ripete entro un limite configurabile.
8. Presenta riepilogo, commit e pull request; merge e deploy restano sotto controllo dell'utente.

Le GitHub Actions usano runner GitHub e sono soggette a quote, limiti e condizioni del piano. Non sono inferenza illimitata né una garanzia di calcolo gratuito. Funzioni come Copilot, Codespaces, Actions minutes e storage possono richiedere piani o quote specifiche: non sono dipendenze obbligatorie di SuperIA.

## Collegamento tra più telefoni Android

- Ogni telefono può essere registrato come dispositivo autorizzato, con nome, capacità, RAM, SoC, acceleratori, spazio e stato batteria/temperatura.
- Il telefono più potente può essere scelto come nodo AI per modelli più grandi; quello da 6 GB resta controller e nodo per modelli leggeri/medi compatibili.
- Prima modalità: collegamento diretto sulla stessa rete Wi-Fi, pairing tramite QR, chiavi per dispositivo, TLS reciproco, autorizzazione revocabile e servizio foreground visibile durante l'elaborazione.
- L'utente seleziona quali modelli e file condividere. Nessuna sincronizzazione automatica dell'intero archivio.
- Coda lavori con stato, pausa, annullamento, timeout e ripresa sicura.
- Se i dispositivi non sono sulla stessa rete, non presumere che la connessione diretta funzioni. Eventuale relay/cloud sarà una funzione separata, opt-in, cifrata e mai obbligatoria.
- Android può sospendere processi in background: il nodo deve mostrare una notifica persistente durante i lavori lunghi e rispettare risparmio energetico e limiti termici.

## Fasi aggiornate

- Fase 1: fondamenta Kotlin/Compose, profilo dispositivo, pairing sicuro tra telefoni, autenticazione GitHub, repository browser e build debug.
- Fase 2: motore LLM on-device e instradamento dei lavori sul dispositivo più adatto.
- Fase 8: agente coding con lettura/modifica repository, branch, commit, PR e test Actions.
- Fase 10: agenti e workflow multi-step, con permessi granulari e conferme.
- Fase 14: audit sicurezza OAuth/token, pairing, isolamento e gestione segreti.

## Criteri di accettazione GitHub

- L'utente può collegare e scollegare GitHub senza inserire segreti in chiaro.
- Accesso limitato ai repository e permessi effettivamente necessari.
- Una modifica viene mostrata come diff e scritta su branch separato.
- Test Actions mostrano stato e log reali; nessun esito inventato.
- Operazioni distruttive, merge e deploy chiedono conferma.
- Test con repository pubblico e privato, token revocato, permessi insufficienti, rete assente e rate limit.
- Nessun token o contenuto privato finisce in log o telemetria.

## Stato

Questo documento definisce il requisito e il disegno tecnico. L'integrazione non è ancora implementata nell'app Android.


## Implementazione iniziale pairing (15 novembre 2026)

Nel codice Android sono stati aggiunti i primi mattoni crittografici:
- identità di installazione ECDSA P-256 generata e conservata in Android Keystore; la chiave privata non viene esportata;
- esportazione della sola chiave pubblica e firma di challenge;
- verifica delle firme ECDSA con chiave pubblica ricevuta;
- invito QR in JSON con nonce casuale da 256 bit e scadenza di cinque minuti;
- parser con limiti di dimensione, versione protocollo e controllo scadenza.

**Limite esplicito:** questi componenti non completano ancora il pairing. Non esistono ancora scanner QR, handshake di challenge reciproca, approvazione esplicita nell'interfaccia, TLS reciproco, discovery LAN, trasferimento lavori o revoca delle coppie. Un invito QR non autorizza da solo un dispositivo. Questi passaggi saranno implementati e testati prima di dichiarare il collegamento operativo.


### Schermata invito QR (prima integrazione Android)

La schermata principale ora può generare e mostrare un QR temporaneo usando la chiave pubblica dell'installazione e un nonce casuale. È stata aggiunta la libreria ZXing Core per codificare il payload QR. È inoltre presente un helper per fingerprint SHA-256 e verifica di firme ECDSA su challenge da 32 byte.

Questa versione è solo il lato generazione invito e gli strumenti crittografici iniziali: non contiene ancora lettura/scansione QR, trasporto di rete, handshake completo, archivio dei peer fidati o consenso reciproco. Il fingerprint deve essere confrontato/autenticato fuori banda e ciascun telefono deve approvare esplicitamente l'altro prima di trasferire dati.
