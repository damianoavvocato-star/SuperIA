package it.superiaprivata.app.documents

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredDocument(val id: String, val title: String, val text: String, val file: File)

/** Stores extracted document text encrypted at rest with a non-exportable Android Keystore key. */
class LocalDocumentRepository(context: Context, workspaceId: String? = null) {
    private val appFilesDir = context.applicationContext.filesDir
    private val directory = File(
        appFilesDir,
        if (workspaceId == null) {
            "documents"
        } else {
            "documents/${UUID.fromString(workspaceId)}"
        }
    )

    init {
        check(directory.exists() || directory.mkdirs()) { "Impossibile creare l'archivio documenti privato." }
    }

    fun import(uri: Uri, extractor: DocumentTextExtractor): StoredDocument {
        val extracted = extractor.extract(uri)
        val existingFiles = documentFiles()
        require(existingFiles.size < MAX_DOCUMENTS) {
            "Archivio pieno: il limite è $MAX_DOCUMENTS documenti. Elimina un file prima di importarne altri."
        }
        val cleartext = "${extracted.title}\n${extracted.extension}\n\n${extracted.text}".toByteArray(Charsets.UTF_8)
        val currentBytes = existingFiles.sumOf { file ->
            maxOf(file.length(), File(file.absolutePath + ".bak").length())
        }
        require(currentBytes + cleartext.size + GCM_IV_BYTES + 16 <= MAX_TOTAL_BYTES) {
            "Archivio documenti pieno: limite complessivo 200 MB. Elimina alcuni documenti."
        }
        val id = UUID.randomUUID().toString()
        val file = File(directory, "$id.enc")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val archiveKey = if (existingFiles.isEmpty()) getOrCreateKey() else getExistingKey()
        cipher.init(Cipher.ENCRYPT_MODE, archiveKey)
        val encrypted = cipher.iv + cipher.doFinal(cleartext)
        writeAtomically(file, encrypted)
        return StoredDocument(id, extracted.title, extracted.text, file)
    }

    /**
     * Reads every known archive through AtomicFile so a backup-only write is recovered.
     * Any unreadable archive fails the whole operation: never hide a document or replace
     * a missing Keystore key with a new key that cannot decrypt existing data.
     */
    fun list(): List<StoredDocument> = documentFiles().sortedByDescending { it.lastModified() }.map { file ->
        try {
            val packed = AtomicFile(file).openRead().use { it.readBytes() }
            require(packed.size > GCM_IV_BYTES) { "Archivio documento non valido." }
            val iv = packed.copyOfRange(0, GCM_IV_BYTES)
            val ciphertext = packed.copyOfRange(GCM_IV_BYTES, packed.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getExistingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            val cleartext = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
            val parts = cleartext.split("\n\n", limit = 2)
            val metadata = parts.first().lines()
            val title = metadata.firstOrNull().orEmpty()
            val extension = metadata.getOrNull(1).orEmpty()
            val text = parts.getOrNull(1).orEmpty()
            require(title.isNotBlank() && extension in SUPPORTED_EXTENSIONS && text.isNotBlank()) {
                "Metadati del documento non validi."
            }
            StoredDocument(file.name.removeSuffix(".enc"), title, text, file)
        } catch (error: Exception) {
            throw IllegalStateException(
                "Impossibile leggere il documento cifrato ${file.name}. I dati sono stati preservati; non eliminarli o reimportarli prima di aver verificato la chiave e l'archivio.",
                error
            )
        }
    }

    fun delete(document: StoredDocument) {
        val canonicalDirectory = directory.canonicalFile
        val canonicalFile = document.file.canonicalFile
        require(canonicalFile.parentFile == canonicalDirectory && canonicalFile.name == "${document.id}.enc") {
            "Il documento selezionato non appartiene all'archivio privato."
        }
        AtomicFile(canonicalFile).delete()
    }

    /**
     * Removes only this workspace's encrypted imported-document archives.
     * Unknown files make the operation fail closed; source documents selected
     * through Android's picker and user-created exports are never touched.
     */
    fun deleteAllWorkspaceDocuments() {
        check(directory.name.matches(Regex("[0-9a-fA-F-]{36}"))) {
            "La raccolta documenti legacy non può essere eliminata come progetto."
        }
        val canonicalDirectory = directory.canonicalFile
        require(canonicalDirectory.parentFile?.canonicalFile ==
            File(appFilesDir, "documents").canonicalFile) {
            "Percorso archivio documenti non valido."
        }
        val candidates = directory.listFiles().orEmpty()
        require(candidates.all { file ->
            file.isFile && (file.name.endsWith(".enc") || file.name.endsWith(".enc.bak"))
        }) {
            "La cartella del progetto contiene file non riconosciuti: nessun file è stato eliminato."
        }
        candidates.filter { it.name.endsWith(".enc") }.forEach { file ->
            AtomicFile(file).delete()
        }
        check(directory.listFiles().orEmpty().isEmpty()) {
            "Non è stato possibile rimuovere tutti gli archivi documenti del progetto."
        }
        check(directory.delete() || !directory.exists()) {
            "Impossibile rimuovere la cartella documenti del progetto."
        }
    }

    private fun documentFiles(): List<File> {
        val candidates = directory.listFiles().orEmpty()
        return candidates.asSequence()
            .filter { it.isFile }
            .mapNotNull { candidate ->
                when {
                    candidate.name.endsWith(".enc.bak") -> File(candidate.absolutePath.removeSuffix(".bak"))
                    candidate.name.endsWith(".enc") -> candidate
                    else -> null
                }
            }
            .distinctBy { it.absolutePath }
            .filter { it.exists() || File(it.absolutePath + ".bak").exists() }
            .toList()
    }

    private fun writeAtomically(file: File, encrypted: ByteArray) {
        val atomic = AtomicFile(file)
        var output: java.io.FileOutputStream? = null
        try {
            output = atomic.startWrite()
            output.write(encrypted)
            atomic.finishWrite(output)
        } catch (error: Exception) {
            if (output != null) atomic.failWrite(output)
            throw error
        }
    }

    private fun getExistingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            ?: throw IllegalStateException("La chiave Keystore dei documenti non è disponibile.")
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "superia_documents_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        const val MAX_DOCUMENTS = 50
        const val MAX_TOTAL_BYTES = 200L * 1024L * 1024L
        val SUPPORTED_EXTENSIONS = setOf("pdf", "docx", "txt", "csv")
    }
}
