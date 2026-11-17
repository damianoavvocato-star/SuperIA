package it.superiaprivata.app.chat

import android.content.Context
import android.os.Process
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/** Persists the uncaught Java/Kotlin stack trace before Android terminates the process. */
object CrashLogStore {
    private val installed = AtomicBoolean(false)

    fun install(context: Context) {
        if (!installed.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, error ->
            runCatching {
                val trace = StringWriter().also { writer ->
                    PrintWriter(writer).use { it.println(error.stackTraceToString()) }
                }.toString()
                val timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.ITALY).format(Date())
                File(appContext.filesDir, CRASH_FILE).writeText(
                    "Errore Java/Kotlin · $timestamp\nThread: ${thread.name}\nClasse: ${error.javaClass.name}\nMessaggio: ${error.message.orEmpty()}\n\n$trace",
                    Charsets.UTF_8
                )
            }
            if (previous != null) {
                previous.uncaughtException(thread, error)
            } else {
                Process.killProcess(Process.myPid())
                kotlin.system.exitProcess(10)
            }
        }
    }

    fun read(context: Context): String? = runCatching {
        File(context.applicationContext.filesDir, CRASH_FILE)
            .takeIf { it.isFile && it.length() > 0L }
            ?.readText(Charsets.UTF_8)
            ?.takeLast(MAX_CHARS)
    }.getOrNull()

    private const val CRASH_FILE = "last_java_crash.txt"
    private const val MAX_CHARS = 12000
}
