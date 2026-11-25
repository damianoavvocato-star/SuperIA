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
import java.util.UUID

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Generate a stable transcript key without colliding with restored or in-memory messages. */
internal fun nextChatLineId(existingIds: Collection<Long>, candidate: Long): Long {
    val used = existingIds.toHashSet()
    var id = candidate
    while (id in used) {
        id = if (id == Long.MAX_VALUE) Long.MIN_VALUE else id + 1L
    }
    return id
}

/** Reuse the durable id on retry; create one only for legacy pending entries. */
internal fun requestIdForRetry(existingRequestId: String?): String {
    if (existingRequestId != null) {
        UUID.fromString(existingRequestId)
        return existingRequestId
    }
    return UUID.randomUUID().toString()
}

data class ChatLine(
    val id: Long,
    val text: String,
    val role: String = "user",
    val pending: Boolean = false,
    val requestId: String? = null,
    val requestMessages: List<ChatPromptMessage>? = null
)

/** Reject malformed transcript entries before they can be persisted or shown to the prompt builder. */
internal fun validatePersistedChatLines(messages: List<ChatLine>) {
    val messageIds = HashSet<Long>(messages.size)
    val requestIds = HashSet<String>()
    messages.forEach { line ->
        require(line.role == "user" || line.role == "assistant") {
            "La cronologia contiene un ruolo non supportato."
        }
        require(messageIds.add(line.id)) {
            "La cronologia contiene identificativi messaggio duplicati."
        }
        if (line.pending) {
            require(line.role == "user") {
                "Solo un messaggio utente può essere in attesa."
            }
        }
        line.requestMessages?.let { requestMessages ->
            require(line.pending) { "Il payload di retry può esistere soltanto per una richiesta pendente." }
            require(requestMessages.isNotEmpty() && requestMessages.size <= 60) {
                "Il payload della richiesta pendente non è valido."
            }
            require(requestMessages.all { it.role in setOf("system", "user", "assistant") && it.content.isNotBlank() }) {
                "Il payload della richiesta pendente contiene messaggi non validi."
            }
            require(requestMessages.sumOf { it.content.length } <= ChatPromptBuilder.MAX_TOTAL_PROMPT_CHARACTERS) {
                "Il payload della richiesta pendente supera il limite consentito."
            }
            require(requestMessages.last().role == "user" && requestMessages.last().content == line.text.trim()) {
                "Il payload della richiesta pendente non corrisponde al messaggio utente."
            }
        }
        line.requestId?.let { value ->
            require(runCatching { UUID.fromString(value) }.isSuccess) {
                "La cronologia contiene un identificativo richiesta non valido."
            }
            require(requestIds.add(value)) {
                "La cronologia contiene identificativi richiesta duplicati."
            }
        }
    }
}

/**
 * Persistent, append-by-replacement conversation archive.
 * The complete transcript is compressed and encrypted with AES-GCM; the key is
 * non-exportable and held by Android Keystore. AtomicFile protects the previous
 * archive if the process is interrupted during a write.
 */
class ChatHistoryStore(context: Context, workspaceId: String? = null) {
    private val appContext = context.applicationContext
    private val workspaceSuffix = workspaceId?.also { UUID.fromString(it) }?.let { "_$it" }.orEmpty()
    private val isLegacyStore = workspaceId == null
    private val preferences = appContext.getSharedPreferences(
        if (isLegacyStore) "superia_private_chat" else "superia_private_chat$workspaceSuffix",
        Context.MODE_PRIVATE
    )
    // Null preserves the exact legacy filenames; a UUID gives each workspace
    // independent AtomicFiles while retaining the existing device Keystore key.
    private val archive = AtomicFile(
        File(appContext.filesDir, if (isLegacyStore) "conversation_history.enc" else "conversation_history$workspaceSuffix.enc")
    )
    private val memoryArchive = AtomicFile(
        File(appContext.filesDir, if (isLegacyStore) "project_memory.enc" else "project_memory$workspaceSuffix.enc")
    )

    /**
     * One-time migration: discard conversations produced by the retired on-device model.
     * The encrypted archive remains available for new online conversations.
     */
    fun clearLegacyLocalHistoryOnce() = synchronized(ARCHIVE_WRITE_LOCK) {
        check(isLegacyStore) { "La pulizia legacy è consentita soltanto allo store storico." }
        if (preferences.getBoolean(ONLINE_ONLY_MIGRATION_KEY, false)) return@synchronized
        archive.delete()
        val legacyModels = File(appContext.filesDir, "models")
        check(!legacyModels.exists() || legacyModels.deleteRecursively()) {
            "Impossibile rimuovere i file del vecchio modello locale."
        }
        val legacyRuntimeCache = File(appContext.cacheDir, "litert-lm")
        check(!legacyRuntimeCache.exists() || legacyRuntimeCache.deleteRecursively()) {
            "Impossibile rimuovere la cache del vecchio runtime locale."
        }
        check(
            preferences.edit()
                .remove(HISTORY_KEY)
                .putBoolean(ONLINE_ONLY_MIGRATION_KEY, true)
                .commit()
        ) { "Impossibile completare la migrazione della cronologia online." }
    }

    /**
     * Copies the legacy encrypted archives into a workspace without deleting or
     * modifying the source. Existing destinations are accepted only when their
     * decrypted contents exactly match the legacy source.
     */
    fun migrateToWorkspace(workspaceId: String) = synchronized(ARCHIVE_WRITE_LOCK) {
        check(isLegacyStore) { "La migrazione deve partire esclusivamente dall'archivio legacy." }
        val destination = ChatHistoryStore(appContext, workspaceId)

        // Normalize the former encrypted SharedPreferences format into the
        // legacy AtomicFile first; load() removes its old key only after save succeeds.
        val expectedTranscript = load()
        val expectedMemory = loadProjectMemory()

        val sourceTranscriptBytes = readPackedArchiveOrNull(archive)
        if (sourceTranscriptBytes != null) {
            val existing = readPackedArchiveOrNull(destination.archive)
            if (existing == null) {
                destination.writeAtomically(destination.archive, sourceTranscriptBytes)
            } else {
                check(destination.load() == expectedTranscript) {
                    "La destinazione legacy contiene una cronologia diversa: migrazione interrotta senza sovrascrivere dati."
                }
            }
            check(destination.load() == expectedTranscript) {
                "Verifica della cronologia migrata non riuscita."
            }
        } else {
            check(destination.load().isEmpty()) {
                "Esiste una cronologia destinazione ma non una sorgente legacy verificabile."
            }
        }

        val sourceMemoryBytes = readPackedArchiveOrNull(memoryArchive)
        if (sourceMemoryBytes != null) {
            val existing = readPackedArchiveOrNull(destination.memoryArchive)
            if (existing == null) {
                destination.writeAtomically(destination.memoryArchive, sourceMemoryBytes)
            } else {
                check(destination.loadProjectMemory() == expectedMemory) {
                    "La destinazione legacy contiene una memoria diversa: migrazione interrotta senza sovrascrivere dati."
                }
            }
            check(destination.loadProjectMemory() == expectedMemory) {
                "Verifica della memoria migrata non riuscita."
            }
        } else {
            check(destination.loadProjectMemory().isBlank()) {
                "Esiste una memoria destinazione ma non una sorgente legacy verificabile."
            }
        }
    }

    private fun readPackedArchiveOrNull(target: AtomicFile): ByteArray? = try {
        target.openRead().use { it.readBytes() }
    } catch (_: java.io.FileNotFoundException) {
        null
    }

    fun load(): List<ChatLine> = synchronized(ARCHIVE_WRITE_LOCK) {
        // openRead() performs AtomicFile recovery before reading. Do not gate it
        // on baseFile.exists(): after an interrupted replacement the valid
        // transcript may still be present only in AtomicFile's .bak file.
        val packedArchive = try {
            archive.openRead().use { it.readBytes() }
        } catch (_: java.io.FileNotFoundException) {
            null
        }
        if (packedArchive != null) {
            val jsonBytes = decrypt(packedArchive)
            val json = runCatching {
                GZIPInputStream(ByteArrayInputStream(jsonBytes)).use { it.readBytes() }
            }.getOrElse { jsonBytes }
            return@synchronized decode(String(json, Charsets.UTF_8))
        }

        // One-time migration from the previous encrypted SharedPreferences archive.
        val legacy = preferences.getString(HISTORY_KEY, null) ?: return@synchronized emptyList()
        val packed = Base64.decode(legacy, Base64.NO_WRAP)
        val migrated = decode(String(decrypt(packed), Charsets.UTF_8))
        save(migrated)
        check(load() == migrated) {
            "Verifica dell'archivio AtomicFile migrato non riuscita; la copia SharedPreferences è stata conservata."
        }
        check(preferences.edit().remove(HISTORY_KEY).commit()) {
            "Cronologia migrata, ma impossibile rimuovere la copia precedente."
        }
        migrated
    }

    fun save(messages: List<ChatLine>) = synchronized(ARCHIVE_WRITE_LOCK) {
        validatePersistedChatLines(messages)
        val json = JSONArray()
        messages.forEach { line ->
            val item = JSONObject()
                .put("id", line.id)
                .put("text", line.text)
                .put("role", line.role)
                .put("pending", line.pending)
                .put("requestId", line.requestId)
            line.requestMessages?.let { requestMessages ->
                val payload = JSONArray()
                requestMessages.forEach { message ->
                    payload.put(JSONObject().put("role", message.role).put("content", message.content))
                }
                item.put("requestMessages", payload)
            }
            json.put(item)
        }
        val compressed = ByteArrayOutputStream().also { output ->
            GZIPOutputStream(output).use { gzip ->
                gzip.write(json.toString().toByteArray(Charsets.UTF_8))
            }
        }.toByteArray()
        writeAtomically(archive, encrypt(compressed))
    }

    /** User-editable project memory, encrypted with the same device-bound key. */
    fun loadProjectMemory(): String = synchronized(ARCHIVE_WRITE_LOCK) {
        // Match transcript recovery: openRead() must be allowed to restore a
        // backup-only AtomicFile after process death during replacement.
        val packedMemory = try {
            memoryArchive.openRead().use { it.readBytes() }
        } catch (_: java.io.FileNotFoundException) {
            null
        }
        packedMemory?.let { String(decrypt(it), Charsets.UTF_8) } ?: ""
    }

    fun saveProjectMemory(memory: String) = synchronized(ARCHIVE_WRITE_LOCK) {
        writeAtomically(memoryArchive, encrypt(memory.trim().toByteArray(Charsets.UTF_8)))
    }

    fun clearProjectMemory() = synchronized(ARCHIVE_WRITE_LOCK) {
        memoryArchive.delete()
    }

    /**
     * Permanently removes only this UUID workspace's encrypted transcript,
     * memory, AtomicFile backups and preferences. Legacy storage is never eligible.
     */
    /**
     * Remove pre-migration duplicate archives after the migration marker proves
     * that the initial copy was verified. The source may be older than the live
     * workspace after later messages, so equality is not required at deletion time.
     */
    internal fun deleteLegacySourceCopiesAfterVerifiedMigration(workspaceId: String) =
        synchronized(ARCHIVE_WRITE_LOCK) {
            check(isLegacyStore) { "La pulizia deve partire esclusivamente dagli archivi legacy." }
            check(workspaceId == LEGACY_WORKSPACE_ID) { "Workspace legacy non valido." }
            check(ChatWorkspaceMigration.isComplete(appContext)) {
                "La migrazione legacy non è stata verificata: sorgenti conservate."
            }
            archive.delete()
            memoryArchive.delete()
            check(preferences.edit().clear().commit()) {
                "Impossibile rimuovere le preferenze della copia legacy."
            }
            check(
                !archive.baseFile.exists() &&
                    !File(archive.baseFile.path + ".bak").exists() &&
                    !memoryArchive.baseFile.exists() &&
                    !File(memoryArchive.baseFile.path + ".bak").exists() &&
                    preferences.all.isEmpty()
            ) {
                "Sono rimaste copie legacy della cronologia o della memoria."
            }
        }

    internal fun deleteWorkspaceData() = synchronized(ARCHIVE_WRITE_LOCK) {
        check(!isLegacyStore) { "L'archivio storico non può essere eliminato da questa operazione." }
        archive.delete()
        memoryArchive.delete()
        check(preferences.edit().clear().commit()) {
            "Impossibile rimuovere le preferenze private del progetto."
        }
        check(!archive.baseFile.exists() && !File(archive.baseFile.path + ".bak").exists()) {
            "La cronologia del progetto non è stata rimossa completamente."
        }
        check(!memoryArchive.baseFile.exists() && !File(memoryArchive.baseFile.path + ".bak").exists()) {
            "La memoria del progetto non è stata rimossa completamente."
        }
        check(preferences.all.isEmpty()) {
            "Sono rimasti dati nelle preferenze private del progetto."
        }
    }

    private fun writeAtomically(target: AtomicFile, bytes: ByteArray) = synchronized(ARCHIVE_WRITE_LOCK) {
        var stream: java.io.FileOutputStream? = null
        try {
            stream = target.startWrite()
            stream.write(bytes)
            target.finishWrite(stream)
        } catch (error: Exception) {
            if (stream != null) target.failWrite(stream)
            throw error
        }
    }

    fun clear() = synchronized(ARCHIVE_WRITE_LOCK) {
        archive.delete()
        check(preferences.edit().remove(HISTORY_KEY).commit()) {
            "Impossibile cancellare la cronologia"
        }
    }

    private fun decode(json: String): List<ChatLine> {
        val array = JSONArray(json)
        val messages = (0 until array.length()).map { index ->
            val item = array.getJSONObject(index)
            val requestMessages = item.optJSONArray("requestMessages")?.let { payload ->
                (0 until payload.length()).map { messageIndex ->
                    val message = payload.getJSONObject(messageIndex)
                    ChatPromptMessage(message.getString("role"), message.getString("content"))
                }
            }
            ChatLine(
                item.getLong("id"),
                item.getString("text"),
                item.optString("role", "user"),
                item.optBoolean("pending", false),
                item.optString("requestId").takeIf { it.isNotBlank() },
                requestMessages
            )
        }
        validatePersistedChatLines(messages)
        return messages
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
        cipher.init(Cipher.DECRYPT_MODE, getExistingKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    /** Decryption must never silently replace a missing key with a new, unrelated key. */
    private fun getExistingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            ?: throw IllegalStateException("La chiave Keystore della cronologia non è disponibile.")
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
        const val ONLINE_ONLY_MIGRATION_KEY = "online_only_history_migration_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_IV_BYTES = 12
        const val GCM_TAG_BITS = 128
        val ARCHIVE_WRITE_LOCK = Any()
    }
}
