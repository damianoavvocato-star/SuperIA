package it.superiaprivata.app.chat

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Eseguito in due invocazioni separate dal workflow:
 * 1) writePendingStateForProcessRestart
 * 2) force-stop del package applicativo tramite adb
 * 3) verifyPendingStateAfterProcessRestart
 *
 * Non cancellare lo store in @Before: la seconda invocazione deve leggere
 * esattamente l'archivio cifrato prodotto dalla prima.
 */
@RunWith(AndroidJUnit4::class)
class ProcessRestartRecoveryInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test
    fun writePendingStateForProcessRestart() {
        val store = ChatHistoryStore(context)
        store.clear()
        store.clearProjectMemory()

        val requestId = "2b18d5c7-4a91-4d5e-9f03-8c6a1b7d2e40"
        val pending = ChatLine(
            id = 19001L,
            text = "Verifica recupero dopo riavvio del processo",
            role = "user",
            pending = true,
            requestId = requestId,
            requestMessages = listOf(
                ChatPromptMessage("assistant", "Contesto persistito prima dell'interruzione"),
                ChatPromptMessage("user", "Verifica recupero dopo riavvio del processo")
            )
        )

        store.save(listOf(pending))
        store.saveProjectMemory("memoria da conservare dopo process death")
        assertEquals(requestId, store.load().single().requestId)
    }

    @Test
    fun verifyPendingStateAfterProcessRestart() {
        val store = ChatHistoryStore(context)
        val restored = store.load().single()

        assertTrue("La richiesta deve restare pendente dopo il riavvio", restored.pending)
        assertEquals(19001L, restored.id)
        assertEquals("2b18d5c7-4a91-4d5e-9f03-8c6a1b7d2e40", restored.requestId)
        assertEquals(
            "Verifica recupero dopo riavvio del processo",
            restored.text
        )
        assertEquals(
            listOf(
                ChatPromptMessage("assistant", "Contesto persistito prima dell'interruzione"),
                ChatPromptMessage("user", "Verifica recupero dopo riavvio del processo")
            ),
            restored.requestMessages
        )
        assertEquals("memoria da conservare dopo process death", store.loadProjectMemory())
    }
}
