package it.superiaprivata.app.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPromptBuilderTest {

    @Test
    fun returnsOnlyNewPromptWhenHistoryIsEmpty() {
        assertEquals(
            "Ciao",
            ChatPromptBuilder.build(emptyList(), "  Ciao  ")
        )
    }

    @Test
    fun includesRecentUserAndAssistantMessages() {
        val history = listOf(
            ChatLine(1L, "Mi chiamo Luca", "user"),
            ChatLine(2L, "Piacere, Luca", "assistant")
        )

        val prompt = ChatPromptBuilder.build(history, "Qual è il mio nome?")

        assertTrue(prompt.contains("Utente: Mi chiamo Luca"))
        assertTrue(prompt.contains("Assistente: Piacere, Luca"))
        assertTrue(prompt.endsWith("Qual è il mio nome?"))
    }

    @Test
    fun limitsHistoryToMostRecentMessages() {
        val history = listOf(
            ChatLine(1L, "Messaggio vecchio", "user"),
            ChatLine(2L, "Messaggio recente", "user")
        )

        val prompt = ChatPromptBuilder.build(history, "Continua", maxHistoryMessages = 1)

        assertTrue(!prompt.contains("Messaggio vecchio"))
        assertTrue(prompt.contains("Messaggio recente"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankPrompt() {
        ChatPromptBuilder.build(emptyList(), "   ")
    }
}
