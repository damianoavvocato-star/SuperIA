package it.superiaprivata.app.chat

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** Metadata for one independent conversation or project. */
internal const val LEGACY_WORKSPACE_ID = "00000000-0000-4000-8000-000000000001"

data class ChatWorkspace(
    val id: String,
    val title: String,
    val kind: Kind,
    val createdAtMillis: Long,
    val updatedAtMillis: Long,
    val archived: Boolean = false
) {
    enum class Kind { CHAT, PROJECT }
}

/**
 * Encrypted, atomic index for independent chats/projects.
 *
 * The stable legacy workspace owns the pre-multi-chat transcript after the
 * verified, non-destructive ChatWorkspaceMigration. This index never deletes
 * transcript or document data; deletion is handled by a separate coordinator.
 */
class ChatWorkspaceStore(context: Context) {
    private val appContext = context.applicationContext
    private val index = AtomicFile(File(appContext.filesDir, INDEX_FILE))

    fun list(includeArchived: Boolean = true): List<ChatWorkspace> =
        synchronized(LOCK) {
            val entries = readIndex()
            (if (includeArchived) entries else entries.filterNot { it.archived })
                .sortedWith(compareByDescending<ChatWorkspace> { it.updatedAtMillis }.thenBy { it.id })
        }

    /** Stable entry that owns conversations already present before multi-chat activation. */
    fun ensureLegacyWorkspace(): ChatWorkspace = synchronized(LOCK) {
        val existing = readIndex().firstOrNull { it.id == LEGACY_WORKSPACE_ID }
        if (existing != null) return@synchronized existing
        val now = System.currentTimeMillis()
        val legacy = ChatWorkspace(
            id = LEGACY_WORKSPACE_ID,
            title = "Conversazione precedente",
            kind = ChatWorkspace.Kind.CHAT,
            createdAtMillis = now,
            updatedAtMillis = now
        )
        writeIndex(readIndex() + legacy)
        legacy
    }

    fun create(title: String, kind: ChatWorkspace.Kind = ChatWorkspace.Kind.CHAT): ChatWorkspace =
        synchronized(LOCK) {
            val cleanTitle = normalizeTitle(title)
            val now = System.currentTimeMillis()
            val workspace = ChatWorkspace(
                id = UUID.randomUUID().toString(),
                title = cleanTitle,
                kind = kind,
                createdAtMillis = now,
                updatedAtMillis = now
            )
            writeIndex(readIndex() + workspace)
            workspace
        }

    fun rename(id: String, title: String): ChatWorkspace = update(id) {
        it.copy(title = normalizeTitle(title), updatedAtMillis = System.currentTimeMillis())
    }

    fun setArchived(id: String, archived: Boolean): ChatWorkspace = update(id) {
        it.copy(archived = archived, updatedAtMillis = System.currentTimeMillis())
    }

    /** Remove an entry only after its workspace-owned data has been cleaned successfully. */
    internal fun removeAfterDataCleanup(id: String) = synchronized(LOCK) {
        requireValidId(id)
        val entries = readIndex()
        check(entries.any { it.id == id }) { "La chat selezionata non esiste." }
        writeIndex(entries.filterNot { it.id == id })
        check(readIndex().none { it.id == id }) {
            "Verifica della rimozione della chat dall'indice non riuscita."
        }
    }

    private fun update(id: String, transform: (ChatWorkspace) -> ChatWorkspace): ChatWorkspace =
        synchronized(LOCK) {
            requireValidId(id)
            var changed: ChatWorkspace? = null
            val entries = readIndex().map { entry ->
                if (entry.id == id) transform(entry).also { changed = it } else entry
            }
            val result = checkNotNull(changed) { "La chat selezionata non esiste." }
            writeIndex(entries)
            result
        }

    private fun readIndex(): List<ChatWorkspace> {
        val packed = try {
            index.openRead().use { it.readBytes() }
        } catch (_: java.io.FileNotFoundException) {
            return emptyList()
        }
        val cleartext = decrypt(packed)
        val array = JSONArray(String(cleartext, Charsets.UTF_8))
        val entries = (0 until array.length()).map { position ->
            val item = array.getJSONObject(position)
            val kind = ChatWorkspace.Kind.valueOf(item.getString("kind"))
            ChatWorkspace(
                id = item.getString("id").also(::requireValidId),
                title = normalizeTitle(item.getString("title")),
                kind = kind,
                createdAtMillis = item.getLong("createdAtMillis"),
                updatedAtMillis = item.getLong("updatedAtMillis"),
                archived = item.optBoolean("archived", false)
            )
        }
        require(entries.map { it.id }.distinct().size == entries.size) {
            "L'indice chat contiene identificativi duplicati."
        }
        return entries
    }

    private fun writeIndex(entries: List<ChatWorkspace>) {
        require(entries.map { it.id }.distinct().size == entries.size) {
            "Non è possibile salvare chat con identificativi duplicati."
        }
        val array = JSONArray()
        entries.forEach { entry ->
            requireValidId(entry.id)
            array.put(
                JSONObject()
                    .put("id", entry.id)
                    .put("title", normalizeTitle(entry.title))
                    .put("kind", entry.kind.name)
                    .put("createdAtMillis", entry.createdAtMillis)
                    .put("updatedAtMillis", entry.updatedAtMillis)
                    .put("archived", entry.archived)
            )
        }
        val encrypted = encrypt(array.toString().toByteArray(Charsets.UTF_8))
        var output: java.io.FileOutputStream? = null
        try {
            output = index.startWrite()
            output.write(encrypted)
            index.finishWrite(output)
        } catch (error: Exception) {
            if (output != null) index.failWrite(output)
            throw error
        }
    }

    private fun encrypt(cleartext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        return cipher.iv + cipher.doFinal(cleartext)
    }

    private fun decrypt(packed: ByteArray): ByteArray {
        require(packed.size > IV_BYTES) { "Indice chat non valido." }
        val iv = packed.copyOfRange(0, IV_BYTES)
        val ciphertext = packed.copyOfRange(IV_BYTES, packed.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getExistingKey(), GCMParameterSpec(TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun getExistingKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as? SecretKey
            ?: throw IllegalStateException("La chiave Keystore dell'indice chat non è disponibile.")
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

    private fun normalizeTitle(value: String): String =
        value.trim().replace(Regex("\\s+"), " ").also {
            require(it.isNotBlank() && it.length <= MAX_TITLE_LENGTH) {
                "Il titolo deve contenere da 1 a $MAX_TITLE_LENGTH caratteri."
            }
        }

    private fun requireValidId(value: String) {
        UUID.fromString(value)
    }

    private companion object {
        const val INDEX_FILE = "chat_workspaces.enc"
        const val ANDROID_KEY_STORE = "AndroidKeyStore"
        const val KEY_ALIAS = "superia_workspace_index_aes"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
        const val MAX_TITLE_LENGTH = 80
        val LOCK = Any()
    }
}
