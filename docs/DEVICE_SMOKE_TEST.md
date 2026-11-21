# Collaudo pratico Android — SuperIA

Data di preparazione: 16 novembre 2026

Questo protocollo completa la CI: i test su emulatore non sostituiscono le prove sul Realme C75 e sullo Xiaomi Mi 10T. Compilazione e test strumentali CI sono passati sulla run [43890341130](https://github.com/damianoavvocato-star/SuperIA/actions/runs/43890341130), commit `a82cf5ca255ebcb9b619d773768adb97b87facee`.

## 1. Preparazione dell'APK

1. Aprire la [run Android 43890341130](https://github.com/damianoavvocato-star/SuperIA/actions/runs/43890341130).
2. Nella sezione **Artifacts**, scaricare `superia-privata-debug`.
3. Estrarre lo ZIP e individuare il file APK.
4. Trasferire/installare l'APK sul telefono. Android potrebbe chiedere di autorizzare temporaneamente l'installazione da quella sorgente.
5. Se esiste già un'installazione dell'app, non disinstallarla per tentare un aggiornamento: la disinstallazione può cancellare i dati privati e la chiave Keystore. Installare l'APK sopra la versione esistente, se Android lo consente; altrimenti fermarsi e chiedere indicazioni prima di rimuovere l'app.

L'artefatto è una build **Debug**, non una release firmata per distribuzione. Il modello non è incluso nell'APK.

## 2. Importazione del modello

1. Aprire la pagina del modello [Gemma 3 1B IT — LiteRT Community](https://huggingface.co/litert-community/Gemma3-1B-IT).
2. Accedere a Hugging Face e accettare personalmente gli eventuali termini richiesti dal distributore.
3. Scaricare la variante `gemma3-1b-it-int4.litertlm` indicata nel catalogo dell'app (dimensione indicativa circa 584 MB).
4. Nell'app aprire i termini Gemma, leggerli e selezionare la casella di accettazione.
5. Usare **Importa modello dal telefono** e selezionare il file scaricato.
6. Attendere il completamento. Non interrompere l'importazione e non cancellare il file originale finché l'app non segnala che il modello è pronto.

L'importazione verifica estensione, dimensione massima e SHA-256 del file copiato; il calcolo SHA-256 è un controllo d'integrità della copia, non una certificazione che il modello sia autentico o compatibile. Il runtime effettua la verifica effettiva quando tenta di caricare il modello.

## 3. Prova minima di inferenza

Eseguire prima con il telefono fresco, con almeno 2 GB di RAM disponibile secondo la soglia preliminare dell'app e spazio libero sufficiente.

1. Verificare che l'app mostri **Modello installato e pronto per l'inferenza locale**.
2. Inviare: `Rispondi esattamente: TEST LOCALE RIUSCITO.`
3. Attendere la risposta e controllare che il testo appaia progressivamente.
4. Inviare una seconda richiesta: `Qual era la frase che ti ho chiesto di ripetere?`
5. Verificare che la risposta usi il contesto della conversazione.
6. Dopo che il modello è stato importato, attivare la modalità aereo e ripetere una richiesta breve. La generazione deve continuare a funzionare senza rete.

Se il modello non si carica, l'app si chiude, il telefono diventa eccessivamente caldo o Android segnala memoria insufficiente, interrompere la prova e annotare il messaggio: non insistere con tentativi ripetuti.

## 4. Persistenza e ripresa

1. Inviare una richiesta breve e attendere la risposta completa.
2. Chiudere normalmente l'app e riaprirla: verificare che la conversazione sia presente.
3. Riavviare il telefono e riaprire l'app: verificare nuovamente la cronologia.
4. Per provare una richiesta pendente, inviare una richiesta solo dopo aver salvato dati non importanti e interrompere l'app durante la generazione. Alla riapertura verificare se compare **Richiesta non completata** e se il pulsante **Riprendi richiesta** funziona.
5. Non usare conversazioni contenenti dati riservati durante queste prove.

La ripresa può ripetere la richiesta: il comportamento attuale è at-least-once e non garantisce esecuzione esattamente una volta.

## 5. Scheda di misurazione per ciascun telefono

Compilare separatamente per Realme C75 e Xiaomi Mi 10T.

| Misura | Risultato |
|---|---|
| Modello/codice dispositivo | |
| Versione Android e API | |
| RAM totale e disponibile prima del test | |
| Spazio libero prima dell'importazione | |
| Importazione modello completata | Sì / No |
| Prima risposta generata | Sì / No |
| Streaming progressivo visibile | Sì / No |
| Secondo turno con contesto corretto | Sì / No |
| Generazione in modalità aereo | Sì / No |
| Cronologia dopo chiusura/riapertura | Sì / No |
| Cronologia dopo riavvio | Sì / No |
| Tempo di caricamento modello | |
| Tempo alla prima risposta | |
| Temperatura/stato termico prima e dopo | |
| Batteria prima e dopo | |
| Errori, crash o messaggi mostrati | |

Ripetere la prova breve almeno tre volte per dispositivo, lasciando raffreddare il telefono tra i test. Non confrontare velocità ottenute con condizioni molto diverse.

## 6. Criteri per chiudere il collaudo

- [ ] Realme C75: importazione e inferenza Gemma riuscite.
- [ ] Realme C75: streaming, secondo turno, modalità aereo e persistenza verificati.
- [ ] Realme C75: riavvio e ripresa richiesta pendente verificati.
- [ ] Xiaomi Mi 10T: importazione e inferenza Gemma riuscite.
- [ ] Xiaomi Mi 10T: streaming, secondo turno, modalità aereo e persistenza verificati.
- [ ] Xiaomi Mi 10T: riavvio e ripresa richiesta pendente verificati.
- [ ] Benchmark ripetuti e problemi residui documentati.

**Stato iniziale:** nessuna delle prove fisiche sopra è dichiarata completata. La Fase 2 resta aperta finché non vengono registrati risultati reali su entrambi i dispositivi.
