package it.superiaprivata.app.chat

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class OnlineConnectionConfig(val endpoint: String, val token: String) {
    val isConfigured: Boolean get() = endpoint.isNotBlank() && token.isNotBlank()
}

class OnlineConnectionStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("superia_online_connection", Context.MODE_PRIVATE)

    fun load(): OnlineConnectionConfig {
        val encoded = prefs.getString(CONFIG_KEY, null) ?: return OnlineConnectionConfig("", "")
        return runCatching {
            val packed = Base64.decode(encoded, Base64.NO_WRAP)
            require(packed.size > IV_BYTES)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(TAG_BITS, packed.copyOfRange(0, IV_BYTES)))
            val clear = cipher.doFinal(packed.copyOfRange(IV_BYTES, packed.size))
            val json = JSONObject(String(clear, Charsets.UTF_8))
            OnlineConnectionConfig(json.optString("endpoint"), json.optString("token"))
        }.getOrDefault(OnlineConnectionConfig("", ""))
    }

    fun save(config: OnlineConnectionConfig) {
        val json = JSONObject().put("endpoint", config.endpoint.trim()).put("token", config.token)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val packed = cipher.iv + cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(CONFIG_KEY, Base64.encodeToString(packed, Base64.NO_WRAP)).commit()) {
            "Impossibile salvare in modo protetto la configurazione."
        }
    }

    fun clear() {
        check(prefs.edit().remove(CONFIG_KEY).commit()) { "Impossibile cancellare la configurazione." }
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance("AES", "AndroidKeyStore")
        generator.init(
            android.security.keystore.KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                android.security.keystore.KeyProperties.PURPOSE_ENCRYPT or
                    android.security.keystore.KeyProperties.PURPOSE_DECRYPT
            )
                .setKeySize(256)
                .setBlockModes(android.security.keystore.KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    private companion object {
        const val CONFIG_KEY = "encrypted_connection"
        const val KEY_ALIAS = "superia_online_config_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}

object OnlineAssistantClient {
    suspend fun send(config: OnlineConnectionConfig, prompt: String): String = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "Configura prima l'indirizzo HTTPS e il token del backend nelle Impostazioni." }
        require(prompt.isNotBlank()) { "La richiesta non può essere vuota." }

        val endpoint = config.endpoint.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere la conversazione è consentito soltanto un indirizzo HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null) { "Indirizzo backend non valido." }
        val chatUrl = URL("$endpoint/api/v1/chat")
        val connection = (chatUrl.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        try {
            val body = JSONObject().put(
                "messages",
                JSONArray().put(JSONObject().put("role", "user").put("content", prompt))
            ).toString().toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()
            if (status !in 200..299) {
                val detail = json?.optString("detail")?.takeIf { it.isNotBlank() }
                throw IllegalStateException(
                    detail ?: when (status) {
                        401 -> "Token backend non valido."
                        503 -> "Backend non configurato o nessun provider gratuito disponibile."
                        else -> "Errore del backend (HTTP $status)."
                    }
                )
            }
            val answer = json?.optString("content").orEmpty().trim()
            if (answer.isBlank()) throw IllegalStateException("Il backend ha restituito una risposta vuota.")
            answer
        } finally {
            connection.disconnect()
        }
    }
}
