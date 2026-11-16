package it.superiaprivata.app.chat

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Reads Android retained process-exit records for native crashes and low-memory kills. */
object RuntimeExitDiagnostics {
    fun read(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return "Android non espone lo storico delle chiusure su questa versione."
        }
        return runCatching {
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val latest = manager.getHistoricalProcessExitReasons(context.packageName, 0, 5).firstOrNull()
                ?: return "Nessuna chiusura anomala precedente registrata da Android."
            val reason = when (latest.reason) {
                3 -> "Terminazione per memoria insufficiente"
                4 -> "Arresto anomalo Java/Kotlin"
                5 -> "Arresto anomalo del codice nativo"
                6 -> "Applicazione bloccata (ANR)"
                9 -> "Uso eccessivo di risorse"
                10 -> "Chiusura richiesta dall'utente"
                2 -> "Processo terminato da un segnale di sistema"
                else -> "Chiusura con codice Android " + latest.reason
            }
            val time = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY).format(Date(latest.timestamp))
            buildString {
                append(reason)
                append(" · ")
                append(time)
                latest.description?.takeIf { it.isNotBlank() }?.let { append("\nDettaglio: " + it.take(500)) }
                append("\nCodice uscita: " + latest.status)
            }
        }.getOrElse { error ->
            "Diagnostica non disponibile: " + error.javaClass.simpleName + ": " + error.message.orEmpty()
        }
    }
}
