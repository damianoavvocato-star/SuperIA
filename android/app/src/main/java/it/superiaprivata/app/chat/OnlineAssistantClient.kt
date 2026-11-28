package it.superiaprivata.app.chat

import android.content.Context
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.DisposableHandle
import kotlinx.coroutines.Job
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.withContext

data class OnlineConnectionConfig(val endpoint: String, val token: String, val deviceId: String = "") {
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
            OnlineConnectionConfig(json.optString("endpoint"), json.optString("token"), json.optString("deviceId"))
        }.getOrDefault(OnlineConnectionConfig("", ""))
    }

    fun save(config: OnlineConnectionConfig) {
        val json = JSONObject().put("endpoint", config.endpoint.trim()).put("token", config.token).put("deviceId", config.deviceId)
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

data class PairingResult(val accessToken: String, val deviceId: String, val expiresAtEpochSeconds: Long)

object OnlineAssistantClient {
    suspend fun pair(endpointValue: String, pairingCode: String, deviceName: String): PairingResult = withContext(Dispatchers.IO) {
        require(pairingCode.length >= 16) { "Il codice di associazione deve contenere almeno 16 caratteri." }
        val endpoint = endpointValue.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere l'associazione è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido."
        }
        val connection = (URL("$endpoint/api/v1/device/pair").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 15_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val body = JSONObject().put("pairing_code", pairingCode)
                .put("device_name", deviceName.take(80))
                .toString().toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()
            if (status !in 200..299) {
                val detail = json?.optString("detail")?.takeIf { it.isNotBlank() }
                throw IllegalStateException(detail ?: "Associazione non riuscita (HTTP $status).")
            }
            val token = json?.optString("access_token").orEmpty()
            val deviceId = json?.optString("device_id").orEmpty()
            val expiresAt = json?.optLong("expires_at") ?: 0L
            require(token.isNotBlank() && deviceId.isNotBlank() && expiresAt > 0L) {
                "Il backend ha restituito dati di associazione incompleti."
            }
            PairingResult(token, deviceId, expiresAt)
        } finally {
            connection.disconnect()
        }
    }
    suspend fun rotate(config: OnlineConnectionConfig): PairingResult = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "Associa prima questo dispositivo al backend." }
        val endpoint = config.endpoint.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere il rinnovo è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido."
        }
        val connection = (URL("$endpoint/api/v1/device/rotate").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()
            if (status !in 200..299) {
                val detail = json?.optString("detail")?.takeIf { it.isNotBlank() }
                throw IllegalStateException(detail ?: "Rinnovo credenziali non riuscito (HTTP $status).")
            }
            val token = json?.optString("access_token").orEmpty()
            val deviceId = json?.optString("device_id").orEmpty()
            val expiresAt = json?.optLong("expires_at") ?: 0L
            require(token.isNotBlank() && deviceId.isNotBlank() && expiresAt > 0L) {
                "Il backend ha restituito dati di rinnovo incompleti."
            }
            PairingResult(token, deviceId, expiresAt)
        } finally {
            connection.disconnect()
        }
    }

    suspend fun revoke(config: OnlineConnectionConfig) = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "Associa prima questo dispositivo al backend." }
        val endpoint = config.endpoint.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere la revoca è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido."
        }
        val connection = (URL("$endpoint/api/v1/device/revoke-self").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val raw = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(raw) }.getOrNull()
            if (status !in 200..299 || json?.optBoolean("revoked", false) != true) {
                val detail = json?.optString("detail")?.takeIf { it.isNotBlank() }
                throw IllegalStateException(detail ?: "Revoca credenziali non riuscita (HTTP $status).")
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun checkHealth(endpointValue: String): String = withContext(Dispatchers.IO) {
        val endpoint = endpointValue.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere la connessione è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido."
        }
        val connection = (URL("$endpoint/health").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = false
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val raw = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            }
            require(status in 200..299) { "Backend non raggiungibile (HTTP $status)." }
            val health = JSONObject(raw)
            require(health.optString("status") == "ok") { "Il servizio non segnala uno stato operativo." }
            "Backend raggiungibile. La verifica non controlla ancora token o provider."
        } finally {
            connection.disconnect()
        }
    }

    suspend fun checkOnlineReadiness(config: OnlineConnectionConfig): String = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "Associa prima questo dispositivo al backend." }
        val endpoint = config.endpoint.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere la connessione è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido."
        }
        val connection = (URL("$endpoint/api/v1/providers").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            instanceFollowRedirects = false
            connectTimeout = 10_000
            readTimeout = 15_000
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Accept", "application/json")
        }
        try {
            val status = connection.responseCode
            val raw = if (status in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            }
            if (status == 401) throw IllegalStateException("Token dispositivo non valido o scaduto: associa nuovamente il dispositivo.")
            if (status !in 200..299) throw IllegalStateException("Verifica online non riuscita (HTTP $status).")
            val payload = JSONObject(raw)
            val providers = payload.optJSONArray("providers") ?: JSONArray()
            val configured = mutableListOf<String>()
            val available = mutableListOf<String>()
            val waiting = mutableListOf<String>()
            for (index in 0 until providers.length()) {
                val item = providers.optJSONObject(index) ?: continue
                val id = item.optString("id").takeIf { it.isNotBlank() } ?: continue
                if (item.optBoolean("enabled", false)) configured += id
                if (item.optBoolean("available", false)) available += id
                if (item.optString("state") == "cooldown") {
                    waiting += "$id (${item.optInt("cooldown_seconds")} s)"
                }
            }
            when {
                available.isNotEmpty() ->
                    "Backend autenticato. Provider configurati senza blocchi locali: ${available.joinToString(", ")}. Configurati: ${configured.joinToString(", ")}. La quota gratuita effettiva viene verificata solo al primo invio."
                configured.isEmpty() ->
                    "Backend raggiungibile e token valido, ma nessun provider gratuito è configurato."
                waiting.isNotEmpty() ->
                    "Backend autenticato, ma i provider gratuiti sono temporaneamente in pausa: ${waiting.joinToString(", ")}."
                else ->
                    "Backend autenticato, ma i provider configurati risultano disabilitati. Nessun fallback a pagamento verrà usato."
            }
        } finally {
            connection.disconnect()
        }
    }

    suspend fun stream(
        config: OnlineConnectionConfig,
        prompt: String,
        requestId: String = UUID.randomUUID().toString(),
        onDelta: suspend (String) -> Unit,
    ): String = stream(
        config,
        listOf(ChatPromptMessage("user", prompt)),
        requestId,
        onDelta
    )

    suspend fun stream(
        config: OnlineConnectionConfig,
        messages: List<ChatPromptMessage>,
        requestId: String = UUID.randomUUID().toString(),
        onDelta: suspend (String) -> Unit,
    ): String = streamInternal(
        config, messages, requestId,
        { url -> url.openConnection() as HttpURLConnection },
        onDelta
    )

    internal suspend fun streamInternal(
        config: OnlineConnectionConfig,
        messages: List<ChatPromptMessage>,
        requestId: String,
        connectionFactory: (URL) -> HttpURLConnection,
        onDelta: suspend (String) -> Unit,
    ): String = withContext(Dispatchers.IO) {
        require(config.isConfigured) { "Configura prima l'indirizzo HTTPS e il token del backend." }
        require(messages.isNotEmpty() && messages.size <= 60) {
            "La conversazione deve contenere da 1 a 60 messaggi."
        }
        require(messages.all { it.role in setOf("system", "user", "assistant") && it.content.isNotBlank() }) {
            "La conversazione contiene un ruolo o un messaggio non valido."
        }
        require(messages.all { it.content.length <= ChatPromptBuilder.MAX_TOTAL_PROMPT_CHARACTERS } &&
            messages.sumOf { it.content.length } <= ChatPromptBuilder.MAX_TOTAL_PROMPT_CHARACTERS) {
            "La conversazione supera il limite complessivo di 32.000 caratteri."
        }
        require(runCatching { UUID.fromString(requestId) }.isSuccess) {
            "Identificativo della richiesta non valido. Riapri la chat e riprova."
        }

        val endpoint = config.endpoint.trim().trimEnd('/')
        val base = URL(endpoint)
        require(base.protocol.equals("https", ignoreCase = true)) {
            "Per proteggere la conversazione è consentito soltanto HTTPS."
        }
        require(base.host.isNotBlank() && base.userInfo == null && base.query == null && base.ref == null) {
            "Indirizzo backend non valido: non usare credenziali, parametri o frammenti nell'URL."
        }
        val connection = connectionFactory(URL("$endpoint/api/v1/chat/stream")).apply {
            requestMethod = "POST"
            instanceFollowRedirects = false
            connectTimeout = 15_000
            readTimeout = 120_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer ${config.token}")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "text/event-stream")
        }
        val cancellationHandle = registerDisconnectOnCancellation(currentCoroutineContext().job, connection)
        try {
            val messageArray = JSONArray()
            messages.forEach { message ->
                messageArray.put(JSONObject().put("role", message.role).put("content", message.content))
            }
            val body = JSONObject()
                .put("request_id", requestId)
                .put("messages", messageArray)
                .toString().toByteArray(Charsets.UTF_8)
            connection.outputStream.use { it.write(body) }
            val status = connection.responseCode
            if (status !in 200..299) {
                val raw = connection.errorStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                val detail = runCatching { JSONObject(raw).optString("detail") }.getOrNull()
                val message = detail?.takeIf { it.isNotBlank() } ?: when (status) {
                    401 -> "Token dispositivo non valido o scaduto: associa nuovamente il dispositivo."
                    409 -> "La richiesta risulta già in elaborazione oppure l'identificativo è in conflitto. Attendi e riprendi la stessa richiesta."
                    429 -> "Troppe richieste in poco tempo. Attendi prima di riprovare."
                    503 -> "Servizio temporaneamente non disponibile o nessun provider gratuito pronto. Nessun servizio a pagamento verrà usato."
                    in 500..599 -> "Errore temporaneo del server (HTTP $status). La richiesta resta salvata per il recupero."
                    else -> "Errore del backend (HTTP $status)."
                }
                throw IllegalStateException(message)
            }

            try {
                connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                    consumeSseEvents(reader) { piece ->
                        withContext(kotlinx.coroutines.Dispatchers.Main) { onDelta(piece) }
                    }
                }
            } catch (failure: Throwable) {
                // disconnect() può sbloccare readLine() con EOF prima che la cancellazione
                // raggiunga il dispatcher: in quel caso deve prevalere la cancellazione.
                if (!currentCoroutineContext().isActive) {
                    throw CancellationException("Streaming annullato.", failure)
                }
                throw failure
            }
        } finally {
            cancellationHandle.dispose()
            connection.disconnect()
        }
    }




internal fun extractJsonStringField(payload: String, field: String): String? {
    val key = "\"${field}\""
    var index = payload.indexOf(key)
    if (index < 0) return null
    index += key.length
    while (index < payload.length && payload[index].isWhitespace()) index++
    if (index >= payload.length || payload[index] != ':') return null
    index++
    while (index < payload.length && payload[index].isWhitespace()) index++
    if (index >= payload.length || payload[index] != '"') return null
    index++

    val result = StringBuilder()
    while (index < payload.length) {
        val character = payload[index++]
        when {
            character == '"' -> return result.toString()
            character == '\\' -> {
                if (index >= payload.length) return null
                when (payload[index++]) {
                    '"' -> result.append('"')
                    '\\' -> result.append('\\')
                    '/' -> result.append('/')
                    'b' -> result.append('\b')
                    'f' -> result.append('\u000C')
                    'n' -> result.append('\n')
                    'r' -> result.append('\r')
                    't' -> result.append('\t')
                    'u' -> {
                        if (index + 4 > payload.length) return null
                        val code = payload.substring(index, index + 4).toIntOrNull(16) ?: return null
                        result.append(code.toChar())
                        index += 4
                    }
                    else -> return null
                }
            }
            else -> result.append(character)
        }
    }
    return null
}

internal suspend fun consumeSseEvents(
    reader: java.io.BufferedReader,
    onDelta: suspend (String) -> Unit,
): String {
    val answer = StringBuilder()
    var eventName = ""
    val eventData = StringBuilder()
    var hasEventData = false
    var completed = false

    suspend fun dispatchEvent() {
        if (!hasEventData) {
            eventName = ""
            return
        }
        val data = eventData.toString()
        when (eventName) {
            "delta" -> {
                val payload = runCatching { JSONObject(data) }.getOrElse {
                    throw IllegalStateException("Il backend ha inviato un frammento SSE non valido.")
                }
                val piece = payload.opt("text") as? String
                    ?: throw IllegalStateException("Il backend ha inviato un frammento SSE senza testo valido.")
                if (piece.isNotEmpty()) {
                    answer.append(piece)
                    onDelta(piece)
                }
            }
            "done" -> {
                runCatching { JSONObject(data) }.getOrElse {
                    throw IllegalStateException("Il backend ha inviato un evento di completamento non valido.")
                }
                completed = true
            }
            "error" -> {
                val payload = runCatching { JSONObject(data) }.getOrElse {
                    throw IllegalStateException("Il backend ha inviato un errore SSE non valido.")
                }
                val message = payload.opt("message") as? String
                throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "Il flusso online si è interrotto.")
            }
        }
        eventName = ""
        eventData.clear()
        hasEventData = false
    }

    while (true) {
        val line = reader.readLine() ?: break
        when {
            line.isEmpty() -> {
                dispatchEvent()
                if (completed) break
            }
            line.startsWith(":") -> Unit
            line.startsWith("event:") -> eventName = line.substringAfter("event:").trim()
            line.startsWith("data:") -> {
                if (hasEventData) eventData.append('\n')
                val value = line.substringAfter("data:")
                eventData.append(if (value.startsWith(" ")) value.drop(1) else value)
                hasEventData = true
            }
        }
    }
    // Per SSE un evento non terminato da una riga vuota non viene dispatchato a EOF.
    if (!completed) throw IllegalStateException(
        if (answer.isNotEmpty()) "Connessione interrotta: il testo parziale è stato conservato. Puoi riprendere la richiesta."
        else "Il backend ha chiuso il flusso senza completare la risposta."
    )
    if (answer.isBlank()) throw IllegalStateException("Il backend ha restituito una risposta vuota.")
    return answer.toString()
}

}


/** Disconnects a blocking HTTP read as soon as its owning coroutine is cancelled. */
@OptIn(InternalCoroutinesApi::class)
internal fun registerDisconnectOnCancellation(job: Job, connection: HttpURLConnection): DisposableHandle =
    job.invokeOnCompletion(onCancelling = true, invokeImmediately = true) { cause ->
        if (cause is CancellationException) connection.disconnect()
    }
