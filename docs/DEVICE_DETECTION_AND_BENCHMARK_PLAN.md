# Piano di rilevamento e benchmark dei dispositivi

Data: 15 novembre 2026

## Scopo

Rilevare le caratteristiche reali dei due telefoni destinati a SuperIA Privata e assegnare i carichi AI in base a misure ripetibili, non soltanto alla quantità di RAM o alle specifiche commerciali.

## Inventario dichiarato

| Dispositivo | RAM fisica | RAM virtuale | SoC candidato | Stato |
|---|---:|---:|---|---|
| Xiaomi Mi 10T | 6 GB | Non dichiarata | Qualcomm Snapdragon 865 | RAM e modello dichiarati dall'utente; Android/build da rilevare |
| realme C75 | 8 GB | Fino a 16 GB | MediaTek Helio G92 Max (ipotesi C75 4G) | RAM confermata dall'utente; variante e SoC da verificare sul dispositivo |

La RAM virtuale usa risorse di archiviazione e non equivale a RAM fisica. Non va sommata alla RAM fisica nei requisiti dei modelli.

## Dati da rilevare nell'app Android

Per ogni dispositivo, tramite API Android e controlli espliciti:
- modello commerciale, produttore e codice modello/build;
- versione Android, API level e patch di sicurezza;
- ABI supportate e architettura CPU;
- SoC/GPU dichiarati dal sistema, quando disponibili;
- memoria totale e memoria disponibile al momento del test;
- spazio interno libero;
- acceleratori effettivamente esposti al runtime ML;
- stato termico Android, batteria e stato di ricarica;
- supporto alle funzionalità richieste dalla specifica versione del runtime.

Non assumere che la presenza fisica di una GPU o NPU significhi che il runtime AI scelto possa utilizzarla.

## Protocollo benchmark

1. Riavviare il telefono e lasciarlo raffreddare; annotare temperatura ambiente e livello batteria.
2. Chiudere le app non necessarie, senza disabilitare componenti di sistema.
3. Misurare memoria disponibile e temperatura prima del test.
4. Eseguire lo stesso modello, stessa quantizzazione, stesso prompt e stesso runtime su ciascun dispositivo, se compatibile.
5. Registrare tempo di caricamento, token al secondo (quando applicabile), latenza al primo token, picco memoria, crash, temperatura e consumo batteria.
6. Ripetere il test almeno tre volte e annotare mediana e variabilità.
7. Ripetere dopo un carico prolungato per verificare il throttling termico.
8. Confrontare CPU e accelerazione GPU/NPU soltanto se il backend è ufficialmente supportato e il risultato è stabile.
9. Non tenere due LLM di grandi dimensioni caricati contemporaneamente.
10. Non eseguire benchmark pesanti durante l'uso quotidiano o con batteria surriscaldata.

## Profili iniziali, non ancora approvati

- **Leggero:** modelli compatti e quantizzati, contesto contenuto; primo candidato per entrambi i telefoni.
- **Bilanciato:** da abilitare solo dopo test di memoria e stabilità.
- **Massima qualità:** non abilitare automaticamente; richiede benchmark specifico e memoria libera sufficiente.

Nessun numero di parametri o modello specifico è approvato in questa fase: dimensione dei pesi, KV cache, runtime, contesto e memoria del sistema incidono sul fabbisogno reale.

## Regole di assegnazione tra telefoni

- Non sommare la RAM dei due telefoni come se fosse un unico pool.
- Ogni modello viene caricato ed eseguito su un singolo dispositivo, salvo un futuro progetto esplicito di inferenza distribuita e validata.
- Il telefono più veloce per un determinato carico diventa il nodo preferito per quel carico; non assegnare il ruolo sulla sola base dei GB di RAM.
- Il secondo telefono può servire per attività indipendenti, elaborazioni leggere o come nodo remoto autorizzato.
- Il pairing richiede consenso, autenticazione reciproca, cifratura, revoca e trasferimento esplicito dei soli dati necessari.

## Stato

- [x] Xiaomi Mi 10T: 6 GB RAM dichiarati dall'utente.
- [x] realme C75: 8 GB RAM fisica confermati dall'utente.
- [x] realme C75: fino a 16 GB RAM virtuale confermati dall'utente; esclusi dal conteggio fisico.
- [ ] Codice modello e variante C75 verificati direttamente.
- [ ] Versioni Android e patch rilevate su entrambi i dispositivi.
- [ ] Memoria interna libera rilevata.
- [ ] Benchmark comparativo reale completato.
- [ ] Modelli e quantizzazioni approvati per dispositivo.

## Fonti tecniche

- Specifiche ufficiali realme C75: https://www.realme.com/global/realme-c75/specs
- Specifiche realme Italia: https://www.realme.com/it/realme-c75
- I valori commerciali sono riferimenti iniziali; le caratteristiche effettive del dispositivo devono essere rilevate sul telefono.
