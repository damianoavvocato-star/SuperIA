# Privacy e sicurezza

Prototipo locale, non piattaforma certificata.
- Avvio documentato su localhost.
- Il provider comunica con Ollama all'endpoint configurato, predefinito localhost.
- Nessun provider cloud o fallback cloud.
- Il backend non salva conversazioni o memoria.
- Cronologia e memoria personale sono conservate nel localStorage del browser sul dispositivo corrente.
- La memoria personale è testo inserito e controllato dall'utente; viene aggiunta al contesto delle richieste chat. Può influenzare le risposte, ma non è una memoria verificata o un sistema di apprendimento del modello.
- Il localStorage non è cifrato dall'app: chi può accedere allo stesso profilo browser/dispositivo potrebbe leggerlo. Non salvare informazioni altamente riservate su dispositivi condivisi.
- Le conversazioni possono essere eliminate dalla barra laterale o svuotate; la memoria personale si può modificare o cancellare dal relativo campo. Cancellare i dati del sito dal browser rimuove anche i dati locali.
- L'esportazione genera un file di testo scaricato dall'utente.
- Non esporre il server su Internet senza autenticazione e revisione di sicurezza.
- Verificare licenze e condizioni d'uso dei modelli scaricati separatamente.
