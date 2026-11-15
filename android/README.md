# SuperIA Privata — Android

Prima base nativa Kotlin + Jetpack Compose.

## Requisiti di build
- Android Studio con JDK 17
- Android SDK Platform 35
- Gradle 8.11.1 o compatibile con Android Gradle Plugin 8.9.2

## Apertura
Aprire questa cartella (`android/`) in Android Studio e attendere la sincronizzazione Gradle. Collegare un dispositivo Android con debug USB abilitato o usare un emulatore.

## Funzioni presenti
- Dashboard Compose iniziale.
- Lettura di produttore/modello, versione Android/API, RAM totale e disponibile esposte da Android, ABI CPU, spazio libero nell'area dati e stato termico.
- Pulsante per aggiornare il profilo dispositivo.

## Limiti noti
- La RAM totale letta da `ActivityManager.MemoryInfo` è quella esposta dal sistema; il valore può differire dalla RAM commerciale dichiarata e non identifica separatamente la RAM virtuale del produttore.
- Non legge IMEI, seriali, account o identificatori persistenti.
- Non misura ancora CPU/GPU/NPU, prestazioni LLM, consumo batteria o throttling nel tempo.
- Non contiene ancora pairing, GitHub OAuth, runtime AI o APK verificato.
- Nessuna autorizzazione speciale è richiesta in questa prima schermata.

## Prossimi incrementi
Test unitari del profilo, inventario acceleratori supportati, benchmark controllato e pairing sicuro.
