package it.superiaprivata.app.chat

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class ChatLine(val id: Long, val text: String, val role: String = "user")

/**
 * Persistent, append-by-replacement conversation archive.
 * The complete transcript is compressed and encrypted with AES-GCM; the key is
 * non-exportable and held by Android Keystore. AtomicFile protects the previous
 * archive if the process is interrupted during a write.
 */
class ChatHistoryStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "superia_private_chat", Context.MODE_PRIVATE
    )
    private val archive = AtomicFile(File(context.applicationContext.filesDir, "conversation_history.enc"))

    fun load(): List<ChatLine> {
        if (archive.baseFile.exists()) {
            val packed = archive.openRead().use { it.readBytes() }
            val jsonBytes = decrypt(packed)
            val json = runCatching {
                GZIPInputStream(ByteArrayInputStream(jsonBytes)).use { it.readBytes() }
            }.getOrElse { jsonBytes }
            return decode(String(json, Charsets.UTF_8))
        }

        // One-time migration from the previous encrypted SharedPreferences archive.
        val legacy = preferences.getString(HISTORY_KEY, null) ?: return emptyList()
        val packed = Base64.decode(legacy, Base64.NO_WRAP)
        val migrated = decode(String(decrypt(packed), Charsets.UTF_8))
        save(migrated)
        check(preferences.edit().remove(HISTORY_KEY).commit()) {
            "Cronologia migrata, ma impossibile rimuovere la copia precedente."
        }
        return migrated
    }

    fun save(messages: List<ChatLine>) {
        val json = JSONArray()
        messages.forEach { line ->
            json.put(JSONObject().put("id", line.id).put("text", line.text).put("role", line.role))
        }
        val compressed = ByteArrayOutputStream().also { output ->
            GZIPOutputStream(output).use { gzip ->
                gzip.write(json.toString().toByteArray(Charsets.UTF_8))
            }
        }.toByteArray()
        val encrypted = encrypt(compressed)
        var stream: java.io.FileOutputStream? = null
        try {
            stream = archive.startWrite()
            stream.write(encrypted)
            archive.finishWrite(stream)
        } catch (error: Exception) {
            if (stream != null) archive.failWrite(stream)
            throw error
        }
    }

    fun clear() {
        archive.delete()
        check(preferences.edit().remove(HISTORY_KEY).commit()) {
            "Impossibile cancellare la cronologia"
        }
    }

    private fun decode(json: String): List<ChatLine> {
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ChatLine(item.getLong("id"), item.getString("text"), item.optString("role", "user"))
        }
    }

    private fun encrypt(cleartext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv + cipher.doFinal(cleartext)
    }

    private fun decrypt(packed: ByteArray): ByteArray {
        require(packed.size > GCM_IV_BYTES) { "Archivio chat non valido" }
        val iv = packed.copyOfRange(0, GCM_IV_BYTES)
        val ciphertext = packed.copyOfRange(GCM_IV_BYTES, packed.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
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
        const val KEY_ALIAS = "superia_chat_history_aes"
        const val HISTORY_KEY = "encrypted_history"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
    }
}
