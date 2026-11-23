package it.superiaprivata.app.chat

import android.content.Context
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatHistoryStoreInstrumentedTest {
    private lateinit var context: Context
    private lateinit var store: ChatHistoryStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        store = ChatHistoryStore(context)
        store.clear()
        store.clearProjectMemory()
        context.getSharedPreferences("superia_private_chat", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun persistsMoreThanOneHundredMessagesAndPendingState() {
        val expected = (0 until 160).map { index ->
            ChatLine(
                id = index.toLong(),
                text = "messaggio riservato $index",
                role = if (index == 159 || index % 2 == 0) "user" else "assistant",
                pending = index == 159
            )
        }

        store.save(expected)

        assertEquals(expected, ChatHistoryStore(context).load())
    }

    @Test
    fun recoversPreviousTranscriptWhenAtomicFileBackupIsTheOnlyCopy() {
        val expected = listOf(
            ChatLine(501L, "cronologia precedente da recuperare", "user"),
            ChatLine(502L, "risposta precedente", "assistant")
        )
        store.save(expected)

        val base = File(context.filesDir, "conversation_history.enc")
        val backup = File(base.absolutePath + ".bak")
        assertTrue("L'archivio principale deve esistere dopo il salvataggio", base.exists())
        assertTrue(base.copyTo(backup, overwrite = true).exists())
        assertTrue("Simula l'interruzione dopo il backup e prima del nuovo file", base.delete())

        assertEquals(expected, ChatHistoryStore(context).load())
        assertTrue("AtomicFile deve ripristinare il file principale", base.exists())
        assertFalse("Il backup recuperato deve essere consumato", backup.exists())
    }

    @Test
    fun explicitClearRemovesAtomicFileBackupsAndPreventsDataResurrection() {
        val transcript = listOf(ChatLine(601L, "da cancellare definitivamente", "user"))
        store.save(transcript)
        store.saveProjectMemory("memoria da cancellare definitivamente")

        val transcriptBase = File(context.filesDir, "conversation_history.enc")
        val memoryBase = File(context.filesDir, "project_memory.enc")
        val transcriptBackup = File(transcriptBase.absolutePath + ".bak")
        val memoryBackup = File(memoryBase.absolutePath + ".bak")
        transcriptBase.copyTo(transcriptBackup, overwrite = true)
        memoryBase.copyTo(memoryBackup, overwrite = true)

        store.clear()
        store.clearProjectMemory()

        assertFalse("La cancellazione cronologia deve rimuovere anche il backup", transcriptBackup.exists())
        assertFalse("La cancellazione memoria deve rimuovere anche il backup", memoryBackup.exists())
        assertTrue(ChatHistoryStore(context).load().isEmpty())
        assertEquals("", ChatHistoryStore(context).loadProjectMemory())
    }

    @Test
    fun transcriptIsEncryptedAtRest() {
        val secret = "testo-che-non-deve-essere-leggibile"
        store.save(listOf(ChatLine(1L, secret, "user")))

        val archive = File(context.filesDir, "conversation_history.enc")
        val raw = String(archive.readBytes(), StandardCharsets.ISO_8859_1)

        assertFalse(raw.contains(secret))
        assertEquals(secret, store.load().single().text)
    }

    @Test
    fun projectMemoryIsEncryptedAtRest() {
        val secret = "memoria-progetto-riservata"
        store.saveProjectMemory(secret)

        val archive = File(context.filesDir, "project_memory.enc")
        val raw = String(archive.readBytes(), StandardCharsets.ISO_8859_1)

        assertFalse(raw.contains(secret))
        assertEquals(secret, store.loadProjectMemory())
    }

    @Test
    fun corruptedProjectMemoryFailsClosedInsteadOfReturningGarbage() {
        store.clearProjectMemory()
        File(context.filesDir, "project_memory.enc").writeBytes(ByteArray(64) { (it * 3).toByte() })

        var failed = false
        try {
            store.loadProjectMemory()
        } catch (_: Exception) {
            failed = true
        }

        assertTrue("Una memoria di progetto alterata non deve essere accettata", failed)
    }

    @Test
    fun missingKeystoreKeyFailsClosedWithoutDestroyingEncryptedProjectMemory() {
        store.saveProjectMemory("memoria privata da preservare")
        val archive = File(context.filesDir, "project_memory.enc")
        val originalBytes = archive.readBytes()

        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry("superia_chat_history_aes")
        }

        var failed = false
        try {
            store.loadProjectMemory()
        } catch (_: Exception) {
            failed = true
        }

        assertTrue("La perdita della chiave non deve restituire memoria vuota", failed)
        assertNull(
            "La lettura non deve ricreare una chiave incompatibile",
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .getKey("superia_chat_history_aes", null)
        )
        assertArrayEquals("L'archivio cifrato deve restare intatto", originalBytes, archive.readBytes())
    }

    @Test
    fun recoversProjectMemoryWhenAtomicFileBackupIsTheOnlyCopy() {
        val expected = "memoria progetto precedente da recuperare"
        store.saveProjectMemory(expected)

        val base = File(context.filesDir, "project_memory.enc")
        val backup = File(base.absolutePath + ".bak")
        assertTrue("La memoria cifrata deve esistere dopo il salvataggio", base.exists())
        assertTrue(base.copyTo(backup, overwrite = true).exists())
        assertTrue("Simula l'interruzione dopo il backup e prima del nuovo file", base.delete())

        assertEquals(expected, ChatHistoryStore(context).loadProjectMemory())
        assertTrue("AtomicFile deve ripristinare il file principale", base.exists())
        assertFalse("Il backup recuperato deve essere consumato", backup.exists())
    }

    @Test
    fun projectMemoryCanBeSavedLoadedAndCleared() {
        store.saveProjectMemory("decisione privata del progetto")

        assertEquals("decisione privata del progetto", store.loadProjectMemory())

        store.clearProjectMemory()

        assertEquals("", store.loadProjectMemory())
    }

    @Test
    fun migratesLegacyEncryptedSharedPreferencesAndRemovesOldCopy() {
        store.save(emptyList()) // Creates the Android Keystore key used by the legacy format.
        store.clear()
        val legacyLine = ChatLine(7L, "messaggio legacy", "user", pending = true)
        val legacyJson = JSONArray().put(
            JSONObject()
                .put("id", legacyLine.id)
                .put("text", legacyLine.text)
                .put("role", legacyLine.role)
                .put("pending", legacyLine.pending)
        ).toString().toByteArray(StandardCharsets.UTF_8)

        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = keyStore.getKey("superia_chat_history_aes", null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val legacyPacked = cipher.iv + cipher.doFinal(legacyJson)
        val preferences = context.getSharedPreferences("superia_private_chat", Context.MODE_PRIVATE)
        assertTrue(
            preferences.edit()
                .putString("encrypted_history", Base64.encodeToString(legacyPacked, Base64.NO_WRAP))
                .commit()
        )

        assertEquals(listOf(legacyLine), store.load())
        assertNull(preferences.getString("encrypted_history", null))
        assertEquals(listOf(legacyLine), ChatHistoryStore(context).load())
    }

    @Test
    fun corruptedArchiveFailsClosedInsteadOfReturningGarbage() {
        store.clear()
        File(context.filesDir, "conversation_history.enc").writeBytes(ByteArray(64) { it.toByte() })

        var failed = false
        try {
            store.load()
        } catch (_: Exception) {
            failed = true
        }

        assertTrue("Un archivio alterato non deve essere accettato come cronologia valida", failed)
    }


    @Test
    fun missingKeystoreKeyFailsClosedWithoutDestroyingEncryptedArchive() {
        store.save(listOf(ChatLine(77L, "contenuto da preservare", "user")))
        val archive = File(context.filesDir, "conversation_history.enc")
        val originalBytes = archive.readBytes()

        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry("superia_chat_history_aes")
        }

        var failed = false
        try {
            store.load()
        } catch (_: Exception) {
            failed = true
        }

        assertTrue("La perdita della chiave non deve produrre una cronologia vuota o corrotta", failed)
        assertNull(
            "La lettura non deve creare silenziosamente una nuova chiave incompatibile",
            KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
                .getKey("superia_chat_history_aes", null)
        )
        assertArrayEquals("Il file cifrato originale deve restare intatto", originalBytes, archive.readBytes())
    }

    @Test
    fun concurrentStoreInstancesSerializeAtomicTranscriptWrites() {
        // Exercise the first-use race: both instances may need to create the Keystore key.
        KeyStore.getInstance("AndroidKeyStore").apply {
            load(null)
            deleteEntry("superia_chat_history_aes")
        }
        val firstStore = ChatHistoryStore(context)
        val secondStore = ChatHistoryStore(context)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        val firstTranscript = (0 until 80).map { ChatLine(it.toLong(), "prima-$it", "user") }
        val secondTranscript = (100 until 180).map { ChatLine(it.toLong(), "seconda-$it", "assistant") }
        try {
            val firstWrite = executor.submit {
                start.await()
                firstStore.save(firstTranscript)
            }
            val secondWrite = executor.submit {
                start.await()
                secondStore.save(secondTranscript)
            }
            start.countDown()
            firstWrite.get(30, TimeUnit.SECONDS)
            secondWrite.get(30, TimeUnit.SECONDS)
            val restored = ChatHistoryStore(context).load()
            assertTrue(restored == firstTranscript || restored == secondTranscript)
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun concurrentArchiveReadersObserveOnlyCompleteTranscripts() {
        val writer = ChatHistoryStore(context)
        val reader = ChatHistoryStore(context)
        val initial = listOf(ChatLine(1L, "iniziale", "user"))
        val replacement = (0 until 120).map {
            ChatLine(it.toLong(), "aggiornamento-$it", if (it % 2 == 0) "user" else "assistant")
        }
        writer.save(initial)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val write = executor.submit {
                start.await()
                repeat(20) { writer.save(replacement) }
            }
            val read = executor.submit {
                start.await()
                repeat(40) {
                    val snapshot = reader.load()
                    assertTrue(snapshot == initial || snapshot == replacement)
                }
            }
            start.countDown()
            write.get(30, TimeUnit.SECONDS)
            read.get(30, TimeUnit.SECONDS)
            assertEquals(replacement, reader.load())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun concurrentProjectMemoryReadsAndWritesRemainValid() {
        val writer = ChatHistoryStore(context)
        val reader = ChatHistoryStore(context)
        val first = "memoria iniziale"
        val replacement = "memoria aggiornata con contenuto completo"
        writer.saveProjectMemory(first)
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
        try {
            val write = executor.submit {
                start.await()
                repeat(20) { writer.saveProjectMemory(replacement) }
            }
            val read = executor.submit {
                start.await()
                repeat(40) {
                    val snapshot = reader.loadProjectMemory()
                    assertTrue(snapshot == first || snapshot == replacement)
                }
            }
            start.countDown()
            write.get(30, TimeUnit.SECONDS)
            read.get(30, TimeUnit.SECONDS)
            assertEquals(replacement, reader.loadProjectMemory())
        } finally {
            executor.shutdownNow()
        }
    }

    @Test
    fun pendingRequestIdSurvivesStoreRecreationForSafeRetry() {
        val requestId = "9b3f3a2e-5a90-4b9f-8f7e-6c0d4d5e2a11"
        val pending = ChatLine(
            id = 901L,
            text = "Continua la risposta interrotta",
            role = "user",
            pending = true,
            requestId = requestId,
            requestMessages = listOf(
                ChatPromptMessage("assistant", "Contesto precedente"),
                ChatPromptMessage("user", "Continua la risposta interrotta")
            )
        )
        store.save(listOf(pending))

        // A new store instance models the next app process after close/reopen.
        val reopenedStore = ChatHistoryStore(context)
        val restored = reopenedStore.load().single()

        assertEquals(requestId, restored.requestId)
        assertTrue(restored.pending)
        assertEquals(pending.text, restored.text)
        assertEquals(pending.id, restored.id)
        assertEquals(pending.requestMessages, restored.requestMessages)
    }

    @Test
    fun legacyPendingRequestWithoutIdRemainsReadableAndCanBeUpgraded() {
        // Ensure the device-bound key exists even if another test removed it first.
        store.save(emptyList())
        val legacyJson = JSONArray().put(
            JSONObject()
                .put("id", 902L)
                .put("text", "Richiesta precedente")
                .put("role", "user")
                .put("pending", true)
        ).toString().toByteArray(StandardCharsets.UTF_8)
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        val key = keyStore.getKey("superia_chat_history_aes", null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val packed = cipher.iv + cipher.doFinal(legacyJson)
        File(context.filesDir, "conversation_history.enc").writeBytes(packed)

        val restored = ChatHistoryStore(context).load().single()
        assertTrue(restored.pending)
        assertNull(restored.requestId)

        val upgradedId = "0f5e8f75-6e12-4b3b-a0f6-efce3e8a0d55"
        store.save(listOf(restored.copy(requestId = upgradedId)))
        assertEquals(upgradedId, ChatHistoryStore(context).load().single().requestId)
    }
}
