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
    fun acceptsLargeTranscriptWithPendingUserMessageAtEnd() {
        val transcript = (0 until 160).map { index ->
            ChatLine(
                id = index.toLong(),
                text = "messaggio $index",
                role = if (index == 159 || index % 2 == 0) "user" else "assistant",
                pending = index == 159
            )
        }

        validatePersistedChatLines(transcript)
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
    fun retryPreservesExistingRequestIdAcrossAttempts() {
        val original = "9b3f3a2e-5a90-4b9f-8f7e-6c0d4d5e2a11"

        assertEquals(original, requestIdForRetry(original))
        assertEquals(original, requestIdForRetry(requestIdForRetry(original)))
    }

    @Test
    fun retryCreatesValidRequestIdForLegacyPendingMessage() {
        val generated = requestIdForRetry(null)

        assertEquals(generated, requestIdForRetry(generated))
        org.junit.Assert.assertTrue(runCatching { java.util.UUID.fromString(generated) }.isSuccess)
    }

    @Test
    fun retryFirstDeltaReplacesStalePartialOutput() {
        assertEquals("Risposta nuova", mergeRetryStreamDelta("Risposta parziale vecchia", "Risposta nuova", false))
    }

    @Test
    fun retrySubsequentDeltasAppendToNewOutput() {
        val first = mergeRetryStreamDelta("Risposta parziale vecchia", "Nuova ", false)
        assertEquals("Nuova risposta", mergeRetryStreamDelta(first, "risposta", true))
    }

    @Test
    fun acceptsFrozenPayloadOnlyWhenItMatchesThePendingUserMessage() {
        val payload = listOf(
            ChatPromptMessage("assistant", "Risposta precedente"),
            ChatPromptMessage("user", "Domanda pendente")
        )
        validatePersistedChatLines(
            listOf(
                ChatLine(
                    id = 1L,
                    text = "Domanda pendente",
                    role = "user",
                    pending = true,
                    requestId = "9b3f3a2e-5a90-4b9f-8f7e-6c0d4d5e2a11",
                    requestMessages = payload
                )
            )
        )
    }

    @Test
    fun rejectsFrozenPayloadWhenItDoesNotMatchPendingUserMessage() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(
                    ChatLine(
                        id = 1L,
                        text = "Domanda modificata",
                        role = "user",
                        pending = true,
                        requestMessages = listOf(ChatPromptMessage("user", "Domanda originale"))
                    )
                )
            )
        }
    }

    @Test
    fun rejectsFrozenPayloadOnCompletedMessage() {
        assertThrows(IllegalArgumentException::class.java) {
            validatePersistedChatLines(
                listOf(
                    ChatLine(
                        id = 1L,
                        text = "Domanda",
                        role = "user",
                        pending = false,
                        requestMessages = listOf(ChatPromptMessage("user", "Domanda"))
                    )
                )
            )
        }
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

    @Test
    fun messageIdGenerationSkipsOccupiedIdAfterOverflow() {
        assertEquals(
            Long.MIN_VALUE + 1L,
            nextChatLineId(listOf(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE)
        )
    }

}
