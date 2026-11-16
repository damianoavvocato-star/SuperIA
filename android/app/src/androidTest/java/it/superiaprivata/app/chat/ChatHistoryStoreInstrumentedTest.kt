package it.superiaprivata.app.chat

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    }

    @Test
    fun persistsMoreThanOneHundredMessagesAndPendingState() {
        val expected = (0 until 160).map { index ->
            ChatLine(
                id = index.toLong(),
                text = "messaggio riservato $index",
                role = if (index % 2 == 0) "user" else "assistant",
                pending = index == 159
            )
        }

        store.save(expected)

        assertEquals(expected, ChatHistoryStore(context).load())
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
    fun projectMemoryCanBeSavedLoadedAndCleared() {
        store.saveProjectMemory("decisione privata del progetto")

        assertEquals("decisione privata del progetto", store.loadProjectMemory())

        store.clearProjectMemory()

        assertEquals("", store.loadProjectMemory())
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
}
