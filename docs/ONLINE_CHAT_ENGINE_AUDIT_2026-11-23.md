# Motore chat online — audit micro-fasi 22–28

Data: 23 novembre 2026

## 22 — failover provider gratuito
Confermato: il router usa soltanto provider esplicitamente configurati e approvati. 429 applica cooldown e può passare al successivo provider free; 401/402/403 disabilita il provider; 408/5xx/rete applica cooldown. Nessun fallback locale o a pagamento. Già verificato dai test router.

## 23 — contratto messaggi e contesto
- Backend accetta 1–60 messaggi con ruoli `system`, `user`, `assistant`, contenuto massimo 32.000 caratteri per messaggio.
- Android ricostruisce contesto con cronologia recente, passaggi precedenti pertinenti, memoria e documenti, mantenendo etichette di ruolo nel prompt.
- Corretto un limite: il totale completo (prompt + memoria + documenti + cronologia + intestazioni) è ora limitato a 32.000 caratteri; se il messaggio nuovo occupa tutto il budget, il contesto viene omesso invece di far fallire la richiesta.
- Aggiunti test API per ruoli non validi, messaggi vuoti, 61 messaggi e contenuto oltre limite.
- Nota architetturale: l'Android invia il contesto ricostruito come singolo messaggio user con ruoli esplicitati nel testo, non ancora come array separato di turni user/assistant. Il backend supporta l'array multi-messaggio; una futura ottimizzazione può passare i turni separatamente.

## 24 — backend streaming-only
Rimosso l'endpoint non-streaming `POST /api/v1/chat`, il root web prototype, il file statico `index.html`, il modulo provider Ollama e il metodo Android `OnlineAssistantClient.send()`. L'unica API chat esposta è `POST /api/v1/chat/stream` (SSE). Aggiornati test e documentazione; le vecchie route rispondono 404.

## 25 — client Android SSE
Estratto il parser SSE in funzione testabile. Test coprono delta multipli, heartbeat comment, evento done, evento error dopo testo parziale, chiusura senza done, risposta vuota, escape JSON e payload malformati. La connessione HTTP viene chiusa in cancellazione; il Bearer è inviato solo all'endpoint associato.

## 26 — heartbeat, lease e disconnessione
Il backend invia heartbeat SSE e rinnova la lease durante stream silenziosi; chiude/cancella il task provider quando lo stream termina. Il client annulla la connessione HTTP sottostante. Il test backend verifica heartbeat durante provider silenzioso. Resta da verificare il comportamento di disconnessione su rete/telefono reale: micro-fase parziale fino al gate E2E.

## 27 — idempotenza e deduplicazione
Già verificati su PostgreSQL CI: request ID stabile per dispositivo, hash payload, replay risposta completata cifrata, 409 per ID riutilizzato con payload differente, lease/fencing per impedire a tentativi vecchi di completare richieste recuperate. Exactly-once assoluto non è garantito in crash tra risposta provider e persistenza.

## 28 — retry e ripresa
Android conserva request ID e richiesta pendente nella cronologia cifrata; retry riutilizza lo stesso ID. Il backend riproduce risposte completate e consente recupero dopo lease scaduta. Un'interruzione dopo token parziali non riprende il medesimo stream: conserva il testo parziale e consente un nuovo tentativo dopo il recupero lease. Test di processo Android terminato/riavviato su dispositivo reale ancora mancanti.

## Evidenze CI
- Backend [run 738](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45094905446), commit `90fd3efaaea800dbe89e942955c1fe571b080187`: **109 passed, 1 warning in 1.36s**, PostgreSQL effimero.
- Android [run 246](https://github.com/damianoavvocato-star/SuperIA/actions/runs/45095009850), commit `9a5579cc47eb278363163ecf19583c7b4d977aac`: **SUCCESS**, `testDebugUnitTest`, BUILD SUCCESSFUL in 39s. Nessun APK.

Stato: micro-fasi 22, 23, 24, 25 e 27 completate; 26 e 28 restano in attesa di test E2E su dispositivo e rete reali.
