package it.superiaprivata.app.chat

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatWorkspaceStoreInstrumentedTest {
    private lateinit var context: Context
    private lateinit var store: ChatWorkspaceStore

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val file = File(context.filesDir, "chat_workspaces.enc")
        file.delete()
        File(file.absolutePath + ".bak").delete()
        store = ChatWorkspaceStore(context)
    }

    @Test
    fun createsIndependentChatAndProjectEntriesAndPersistsThem() {
        val chat = store.create("Videogioco")
        val project = store.create("Montaggio video", ChatWorkspace.Kind.PROJECT)

        val restored = ChatWorkspaceStore(context).list()

        assertEquals(2, restored.size)
        assertTrue(restored.any { it.id == chat.id && it.kind == ChatWorkspace.Kind.CHAT })
        assertTrue(restored.any { it.id == project.id && it.kind == ChatWorkspace.Kind.PROJECT })
        assertTrue(chat.id != project.id)
    }

    @Test
    fun renameAndArchiveAffectOnlySelectedWorkspace() {
        val first = store.create("Prima chat")
        val second = store.create("Seconda chat")

        store.rename(first.id, "Gioco strategico")
        store.setArchived(first.id, true)

        assertEquals("Gioco strategico", store.list().single { it.id == first.id }.title)
        assertTrue(store.list().single { it.id == first.id }.archived)
        assertFalse(store.list(includeArchived = false).any { it.id == first.id })
        assertTrue(store.list(includeArchived = false).any { it.id == second.id })
    }

    @Test
    fun workspaceIndexIsEncryptedAtRest() {
        val secretTitle = "Titolo progetto riservato"
        store.create(secretTitle)

        val raw = String(
            File(context.filesDir, "chat_workspaces.enc").readBytes(),
            Charsets.ISO_8859_1
        )

        assertFalse(raw.contains(secretTitle))
        assertEquals(secretTitle, store.list().single().title)
    }

    @Test
    fun corruptedIndexFailsClosedInsteadOfReturningAnEmptyWorkspaceList() {
        File(context.filesDir, "chat_workspaces.enc").writeBytes(ByteArray(64) { (it * 7).toByte() })

        var failed = false
        try {
            store.list()
        } catch (_: Exception) {
            failed = true
        }

        assertTrue("Un indice alterato non deve essere interpretato come archivio vuoto", failed)
    }

    @Test
    fun invalidTitleAndUnknownWorkspaceAreRejected() {
        var blankRejected = false
        try {
            store.create("   ")
        } catch (_: IllegalArgumentException) {
            blankRejected = true
        }
        assertTrue(blankRejected)

        var missingRejected = false
        try {
            store.rename(java.util.UUID.randomUUID().toString(), "Nuovo titolo")
        } catch (_: IllegalStateException) {
            missingRejected = true
        }
        assertTrue(missingRejected)
    }
}
