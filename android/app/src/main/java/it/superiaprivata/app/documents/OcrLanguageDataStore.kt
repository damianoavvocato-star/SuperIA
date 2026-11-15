package it.superiaprivata.app.documents

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream

class OcrLanguageDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val dataDirectory = File(appContext.filesDir, "tesseract")
    private val tessdataDirectory = File(dataDirectory, "tessdata")
    private val italianDataFile = File(tessdataDirectory, "ita.traineddata")

    fun isItalianReady(): Boolean = italianDataFile.isFile && italianDataFile.length() > 0L

    fun importItalian(uri: Uri): Long {
        val displayName = appContext.contentResolver.query(
            uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) cursor.getString(index) else null
            } else null
        }.orEmpty()
        require(displayName.equals("ita.traineddata", ignoreCase = true)) {
            "Seleziona il file ita.traineddata di Tesseract."
        }
        check(tessdataDirectory.exists() || tessdataDirectory.mkdirs()) {
            "Impossibile creare la cartella privata per i dati OCR."
        }
        val temporary = File(tessdataDirectory, "ita.traineddata.tmp")
        val input = appContext.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Impossibile aprire il file OCR.")
        var total = 0L
        try {
            input.use { source ->
                FileOutputStream(temporary).use { target ->
                    val buffer = ByteArray(16 * 1024)
                    while (true) {
                        val count = source.read(buffer)
                        if (count < 0) break
                        total += count
                        require(total <= MAX_DATA_BYTES) { "Il file lingua OCR supera il limite di 40 MB." }
                        target.write(buffer, 0, count)
                    }
                    target.fd.sync()
                }
            }
            require(total > 0L) { "Il file lingua OCR è vuoto." }
            val backup = File(tessdataDirectory, "ita.traineddata.bak")
            if (backup.exists()) backup.delete()
            val hadPrevious = italianDataFile.exists()
            if (hadPrevious) check(italianDataFile.renameTo(backup)) {
                "Impossibile proteggere il dizionario OCR precedente."
            }
            if (!temporary.renameTo(italianDataFile)) {
                if (hadPrevious) backup.renameTo(italianDataFile)
                throw IllegalStateException("Impossibile salvare il file lingua OCR nell'area privata.")
            }
            backup.delete()
            return total
        } catch (error: Exception) {
            temporary.delete()
            throw error
        }
    }

    private companion object {
        const val MAX_DATA_BYTES = 40L * 1024L * 1024L
    }
}
