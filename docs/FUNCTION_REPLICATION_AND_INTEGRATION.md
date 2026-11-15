# Acquisizione, replica e integrazione delle funzioni AI

## Obiettivo
SuperIA Privata è un'app Android personale, non commerciale, per un solo utente. L'obiettivo è offrire in un unico ambiente il maggior numero possibile di capacità presenti nei 182 sistemi del catalogo, includendo funzioni nuove che l'utente potrà chiedere di aggiungere.

L'uso personale/non commerciale è un requisito di prodotto, ma non annulla automaticamente copyright, licenze software, termini dei servizi, diritti sui dataset o restrizioni sui pesi dei modelli. Non si deve quindi copiare indiscriminatamente codice proprietario, APK, chiavi, pesi riservati, contenuti protetti o aggirare accessi e pagamenti.

## Quattro modalità di acquisizione

1. **Integrazione diretta**: collegare librerie, modelli e applicazioni open source o con licenza compatibile, rispettando attribuzioni, obblighi di distribuzione e condizioni d'uso.
2. **Replica funzionale indipendente**: studiare il comportamento osservabile e progettare un'implementazione originale equivalente, senza copiare codice o asset protetti e senza aggirare misure di accesso.
3. **Connettore ufficiale**: usare API, OAuth, webhook o interfacce documentate del fornitore. Deve essere opzionale, con chiara indicazione di Internet, limiti, costi e dati trasmessi.
4. **Importazione di estensioni**: installare plugin o moduli da sorgenti selezionate, con manifest, firma/checksum, licenza, permessi minimi, sandbox e possibilità di rimozione.

## Scheda obbligatoria per ogni funzione

Ogni richiesta nuova deve essere trasformata in una scheda con:
- nome della funzione e risultato atteso;
- sistema di riferimento e comportamento osservabile da replicare;
- alternativa open source o implementazione originale;
- repository e provenienza dei componenti;
- licenza del codice, del modello, dei pesi e dei dati, verificati separatamente;
- uso personale consentito, eventuali condizioni e attribuzioni;
- esecuzione offline/online, dati inviati e permessi;
- requisiti Android, RAM, spazio, acceleratori e dispositivo consigliato;
- dipendenze, dimensione download e checksum;
- test automatici e prova end-to-end;
- stato: da analizzare, licenza verificata, prototipo, integrato, testato, limitato o non disponibile.

Una licenza open source del codice non implica automaticamente una licenza aperta dei pesi, dei dataset o dei contenuti di addestramento.

## Regole per i moduli e plugin
- Non eseguire APK o codice importato alla cieca.
- Non chiedere o memorizzare password, token o chiavi in chiaro.
- Separare permessi di lettura, scrittura, rete, file, microfono, fotocamera e accessibilità.
- Nessuna esfiltrazione di file o conversazioni verso servizi esterni senza consenso specifico.
- Mostrare fonte, licenza, versione, permessi e dimensione prima dell'installazione.
- Aggiornamenti verificati e reversibili; rimozione completa dei dati del modulo su richiesta.
- Operazioni esterne o distruttive sempre soggette a conferma.
- Se una funzione non è legalmente o tecnicamente integrabile, spiegare il motivo e proporre un equivalente consentito.

## Clonazione e compatibilità
Il termine "clonare" nell'app indica creare un modulo indipendente che riproduce una capacità o un flusso di lavoro, non duplicare un servizio proprietario o impersonarne l'identità. Nomi, marchi, UI, codice, prompt interni, dataset e asset di terzi non vanno copiati senza autorizzazione. L'app può offrire un catalogo di capacità equivalenti e consentire all'utente di scegliere il motore locale o connettore autorizzato.

## Priorità d'implementazione
1. Catalogare le funzioni dei 182 sistemi e raggruppare le capacità sovrapposte.
2. Dare priorità a implementazioni offline leggere e componenti open source verificati.
3. Creare un sistema plugin/moduli Android con interfacce stabili e permessi granulari.
4. Aggiungere connettori ufficiali opzionali per le funzioni non sostenibili localmente.
5. Misurare prestazioni sul telefono da 6 GB e sul secondo dispositivo Realme, dopo averne rilevato il modello esatto.
6. Pubblicare nell'app lo stato reale di ogni capacità: disponibile, parziale, online, sperimentale o non supportata.

## Criterio di completamento
Una capacità non è "clonata" o completata solo perché compare nel catalogo. Deve essere implementata in un modulo Android, avere licenze e provenienza registrate, test pertinenti, prova su dispositivo e limiti dichiarati.
