package it.superiaprivata.app.documents

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredDocument(val id: String, val title: String, val text: String, val file: File)

/** Stores extracted document text encrypted at rest with a non-exportable Android Keystore key. */
class LocalDocumentRepository(context: Context) {
    private val directory = File(context.applicationContext.filesDir, "documents")

    init {
        check(directory.exists() || directory.mkdirs()) { "Impossibile creare l'archivio documenti privato." }
    }

    fun import(uri: Uri, extractor: DocumentTextExtractor): StoredDocument {
        val extracted = extractor.extract(uri)
        val existingFiles = directory.listFiles { candidate -> candidate.isFile && candidate.extension == "enc" }.orEmpty()
        require(existingFiles.size < MAX_DOCUMENTS) {
            "Archivio pieno: il limite è $MAX_DOCUMENTS documenti. Elimina un file prima di importarne altri."
        }
        val cleartext = "${extracted.title}\n${extracted.extension}\n\n${extracted.text}".toByteArray(Charsets.UTF_8)
        val currentBytes = existingFiles.sumOf { it.length() }
        require(currentBytes + cleartext.size + GCM_IV_BYTES + 16 <= MAX_TOTAL_BYTES) {
            "Archivio documenti pieno: limite complessivo 200 MB. Elimina alcuni documenti."
        }
        val id = "${System.currentTimeMillis()}_${existingFiles.size}"
        val file = File(directory, "$id.enc")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        file.writeBytes(cipher.iv + cipher.doFinal(cleartext))
        return StoredDocument(id, extracted.title, extracted.text, file)
    }

    fun list(): List<StoredDocument> = directory.listFiles { file ->
        file.isFile && file.extension == "enc"
    }.orEmpty().sortedByDescending { it.lastModified() }.mapNotNull { file ->
        runCatching {
            val packed = file.readBytes()
            require(packed.size > GCM_IV_BYTES) { "Archivio documento non valido." }
            val iv = packed.copyOfRange(0, GCM_IV_BYTES)
            val ciphertext = packed.copyOfRange(GCM_IV_BYTES, packed.size)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            val cleartext = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
            val lines = cleartext.split("\n\n", limit = 2)
            val metadata = lines.first().lines()
            val title = metadata.firstOrNull().orEmpty()
            val text = lines.getOrNull(1).orEmpty()
            StoredDocument(file.nameWithoutExtension, title, text, file)
        }.getOrNull()
    }

    fun delete(document: StoredDocument) {
        check(document.file.delete()) { "Impossibile eliminare il documento dall'archivio." }
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
    }
}
