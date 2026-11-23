package it.superiaprivata.app.documents

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import java.io.File
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalDocumentRepositoryInstrumentedTest {
    private lateinit var context: Context
    private lateinit var directory: File
    private lateinit var repository: LocalDocumentRepository

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        directory = File(context.filesDir, "documents")
        directory.deleteRecursively()
        assertTrue(directory.mkdirs())
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry(KEY_ALIAS)
        }
        repository = LocalDocumentRepository(context)
    }

    @Test
    fun listsEncryptedDocumentUsingExistingKeystoreKey() {
        writeEncryptedDocument("document-1", "appunti.txt", "contenuto riservato")

        val restored = repository.list().single()

        assertEquals("appunti.txt", restored.title)
        assertEquals("contenuto riservato", restored.text)
    }

    @Test
    fun recoversDocumentWhenAtomicFileBackupIsTheOnlyCopy() {
        writeEncryptedDocument("document-2", "contratto.txt", "clausola da recuperare")
        val base = File(directory, "document-2.enc")
        val backup = File(base.absolutePath + ".bak")
        base.copyTo(backup, overwrite = true)
        assertTrue(base.delete())

        val restored = repository.list().single()

        assertEquals("clausola da recuperare", restored.text)
        assertTrue(base.exists())
        assertFalse(backup.exists())
    }

    @Test
    fun missingKeystoreKeyFailsClosedAndPreservesEncryptedDocuments() {
        writeEncryptedDocument("document-3", "privato.txt", "non sovrascrivere")
        val archive = File(directory, "document-3.enc")
        val original = archive.readBytes()
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry(KEY_ALIAS)
        }

        val result = runCatching { repository.list() }

        assertTrue("La perdita della chiave deve essere visibile come errore", result.isFailure)
        assertArrayEquals(original, archive.readBytes())
        assertNull(
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.getKey(KEY_ALIAS, null)
        )
    }

    @Test
    fun corruptedDocumentFailsClosedInsteadOfBeingSilentlyDropped() {
        val archive = File(directory, "document-4.enc")
        archive.writeBytes(ByteArray(64) { (it * 7).toByte() })

        val result = runCatching { repository.list() }

        assertTrue(result.isFailure)
        assertTrue("Il file corrotto deve restare disponibile per diagnosi o recupero", archive.exists())
    }

    @Test
    fun deleteRejectsFileOutsidePrivateDocumentDirectory() {
        val outside = File(context.filesDir, "must-not-delete.txt").apply { writeText("preserva") }
        val forged = StoredDocument("forged", "falso.txt", "testo", outside)

        assertTrue(runCatching { repository.delete(forged) }.isFailure)
        assertTrue(outside.exists())
    }

    private fun writeEncryptedDocument(id: String, title: String, text: String) {
        val key = getOrCreateKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val cleartext = "$title\ntxt\n\n$text".toByteArray(Charsets.UTF_8)
        val packed = cipher.iv + cipher.doFinal(cleartext)
        val target = AtomicFile(File(directory, "$id.enc"))
        var output: java.io.FileOutputStream? = null
        try {
            output = target.startWrite()
            output.write(packed)
            target.finishWrite(output)
        } catch (error: Exception) {
            if (output != null) target.failWrite(output)
            throw error
        }
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
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
        const val KEY_ALIAS = "superia_documents_aes"
    }
}
