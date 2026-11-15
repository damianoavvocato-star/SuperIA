package it.superiaprivata.app.documents

import android.content.Context
import android.net.Uri
import java.io.File

data class StoredDocument(val id: String, val title: String, val text: String, val file: File)

class LocalDocumentRepository(context: Context) {
    private val directory = File(context.applicationContext.filesDir, "documents")

    init {
        check(directory.exists() || directory.mkdirs()) { "Impossibile creare l'archivio documenti privato." }
    }

    fun import(uri: Uri, extractor: DocumentTextExtractor): StoredDocument {
        val extracted = extractor.extract(uri)
        val id = "${System.currentTimeMillis()}_${directory.listFiles()?.size ?: 0}"
        val file = File(directory, "$id.txt")
        file.writeText("# ${extracted.title}\n# Formato: ${extracted.extension}\n\n${extracted.text}", Charsets.UTF_8)
        return StoredDocument(id, extracted.title, extracted.text, file)
    }

    fun list(): List<StoredDocument> = directory.listFiles { file ->
        file.isFile && file.extension == "txt"
    }.orEmpty().sortedByDescending { it.lastModified() }.mapNotNull { file ->
        runCatching {
            val content = file.readText(Charsets.UTF_8)
            val title = content.lineSequence().firstOrNull()?.removePrefix("# ") ?: file.name
            val text = content.substringAfter("\n\n", "")
            StoredDocument(file.nameWithoutExtension, title, text, file)
        }.getOrNull()
    }

    fun delete(document: StoredDocument) {
        check(document.file.delete()) { "Impossibile eliminare il documento dall'archivio." }
    }
}
