package it.superiaprivata.app.chat

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class ChatLine(val id: Long, val text: String, val role: String = "user")

/**
 * Stores the local chat history encrypted with an AES-256 key held by Android Keystore.
 * The key is non-exportable; only ciphertext and the GCM IV are written to app-private prefs.
 */
class ChatHistoryStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "superia_private_chat", Context.MODE_PRIVATE
    )

    fun load(): List<ChatLine> {
        val encoded = preferences.getString(HISTORY_KEY, null) ?: return emptyList()
        val packed = Base64.decode(encoded, Base64.NO_WRAP)
        require(packed.size > GCM_IV_BYTES) { "Archivio chat non valido" }
        val iv = packed.copyOfRange(0, GCM_IV_BYTES)
        val ciphertext = packed.copyOfRange(GCM_IV_BYTES, packed.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        val json = String(cipher.doFinal(ciphertext), Charsets.UTF_8)
        val array = JSONArray(json)
        return (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            ChatLine(item.getLong("id"), item.getString("text"), item.optString("role", "user"))
        }
    }

    fun save(messages: List<ChatLine>) {
        val array = JSONArray()
        messages.takeLast(MAX_MESSAGES).forEach { line ->
            array.put(JSONObject().put("id", line.id).put("text", line.text).put("role", line.role))
        }
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(array.toString().toByteArray(Charsets.UTF_8))
        val packed = cipher.iv + encrypted
        check(preferences.edit().putString(HISTORY_KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).commit()) {
            "Impossibile salvare la cronologia"
        }
    }

    fun clear() {
        check(preferences.edit().remove(HISTORY_KEY).commit()) {
            "Impossibile cancellare la cronologia"
        }
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
        const val MAX_MESSAGES = 100
    }
}
