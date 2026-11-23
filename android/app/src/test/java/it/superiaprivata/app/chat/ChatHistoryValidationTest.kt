package it.superiaprivata.app.chat

import org.junit.Assert.assertThrows
import org.junit.Test

class ChatHistoryValidationTest {
    @Test
    fun acceptsOnlySupportedPersistedConversationRoles() {
        validatePersistedChatLines(
            listOf(
                ChatLine(1L, "Ciao", "user"),
                ChatLine(2L, "Salve", "assistant")
            )
        )
    }

    @Test
    fun rejectsUnexpectedPersistedRole() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(listOf(ChatLine(1L, "tool output", "tool")))
        }
    }

    @Test
    fun rejectsMalformedPersistedRequestId() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(ChatLine(1L, "Riprendi", "user", pending = true, requestId = "not-a-uuid"))
            )
        }
    }

    @Test
    fun acceptsLegacyPendingMessageWithoutRequestId() {
        validatePersistedChatLines(
            listOf(ChatLine(1L, "Richiesta precedente", "user", pending = true))
        )
    }
}
