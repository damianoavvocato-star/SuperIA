package it.superiaprivata.app.chat

import org.junit.Assert.assertEquals
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
    @Test
    fun rejectsDuplicateMessageIds() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(
                    ChatLine(10L, "Primo messaggio", "user"),
                    ChatLine(10L, "Risposta", "assistant")
                )
            )
        }
    }

    @Test
    fun rejectsDuplicateRequestIds() {
        val requestId = "9b3f3a2e-5a90-4b9f-8f7e-6c0d4d5e2a11"
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(
                    ChatLine(11L, "Prima richiesta", "user", requestId = requestId),
                    ChatLine(12L, "Seconda richiesta", "user", requestId = requestId)
                )
            )
        }
    }

    @Test
    fun rejectsPendingAssistantMessages() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(ChatLine(13L, "Risposta", "assistant", pending = true))
            )
        }
    }

    @Test
    fun generatesUnusedMessageIdWhenNanoTimeCandidateAlreadyExists() {
        assertEquals(45L, nextChatLineId(listOf(42L, 43L, 44L), 42L))
        assertEquals(45L, nextChatLineId(listOf(42L, 43L, 44L), 45L))
    }

    @Test
    fun messageIdGenerationWrapsSafelyAtLongMaximum() {
        assertEquals(Long.MIN_VALUE, nextChatLineId(listOf(Long.MAX_VALUE), Long.MAX_VALUE))
    }

}
