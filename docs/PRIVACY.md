# Privacy e sicurezza

Prototipo locale, non piattaforma certificata.
- Avvio documentato su localhost.
- Il provider comunica con Ollama all'endpoint configurato, predefinito localhost.
- Nessun provider cloud o fallback cloud.
- Il backend non salva le conversazioni.
- La cronologia dell'interfaccia web viene salvata nel localStorage del browser sul dispositivo corrente.
- I dati nel localStorage non sono cifrati dall'app: chi può accedere allo stesso profilo browser/dispositivo potrebbe leggerli. Non salvare informazioni altamente riservate su dispositivi condivisi.
- Le conversazioni possono essere eliminate dalla barra laterale o svuotate dall'interfaccia; cancellare i dati del sito dal browser rimuove anche la cronologia locale.
- L'esportazione genera un file di testo scaricato dall'utente.
- Non esporre il server su Internet senza autenticazione e revisione di sicurezza.
- Verificare licenze e condizioni d'uso dei modelli scaricati separatamente.
