# SuperIA Privata — Android

Prima base nativa Kotlin + Jetpack Compose.

## Requisiti di build
- Android Studio con JDK 17
- Android SDK Platform 35
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2

## Apertura
Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle. Collegare un dispositivo Android con debug USB abilitato o usare un emulatore.

## Funzioni presenti
- Dashboard Compose iniziale e pannello chat conversazionale.
- Inserimento e visualizzazione dei messaggi dell'utente.
- Cronologia chat locale cifrata con AES-GCM; chiave AES-256 non esportabile custodita in Android Keystore; pulsante per cancellare la cronologia.
- Lettura di produttore/modello, versione Android/API, RAM totale e disponibile esposte da Android, ABI CPU, spazio libero nell'area dati e stato termico.
- Pulsante per aggiornare il profilo dispositivo.

## Limiti noti
- La RAM totale letta da `ActivityManager.MemoryInfo` è quella esposta dal sistema; il valore può differire dalla RAM commerciale dichiarata e non identifica separatamente la RAM virtuale del produttore.
- Non legge IMEI, seriali, account o identificatori persistenti.
- Non misura ancora CPU/GPU/NPU, prestazioni LLM, consumo batteria o throttling nel tempo.
- La chat Android non è ancora collegata a un runtime AI: l'invio memorizza il testo, non genera risposte.
- Non contiene ancora pairing completo, GitHub OAuth o runtime AI locale integrato.
- La cronologia è cifrata a riposo; non è sincronizzata tra telefoni e non sostituisce un backup cifrato.
- Nessuna autorizzazione speciale è richiesta in questa prima schermata.

## Prossimi incrementi
Collegamento a un runtime AI locale Android, allegati con selettore documenti, test strumentali per archivio cifrato, inventario acceleratori supportati, benchmark controllato e completamento del pairing sicuro.
