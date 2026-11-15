package it.superiaprivata.app.chat

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ChatPromptBuilderTest {
    @Test fun returnsOnlyNewPromptWhenHistoryIsEmpty() {
        assertEquals("Ciao", ChatPromptBuilder.build(emptyList(), "  Ciao  "))
    }

    @Test fun includesRecentUserAndAssistantMessages() {
        val history = listOf(ChatLine(1L, "Mi chiamo Luca", "user"), ChatLine(2L, "Piacere, Luca", "assistant"))
        val prompt = ChatPromptBuilder.build(history, "Qual è il mio nome?")
        assertTrue(prompt.contains("Utente: Mi chiamo Luca"))
        assertTrue(prompt.contains("Assistente: Piacere, Luca"))
        assertTrue(prompt.endsWith("Qual è il mio nome?"))
    }

    @Test fun limitsHistoryToMostRecentMessages() {
        val history = listOf(ChatLine(1L, "Messaggio vecchio", "user"), ChatLine(2L, "Messaggio recente", "user"))
        val prompt = ChatPromptBuilder.build(history, "Continua", maxHistoryMessages = 1)
        assertTrue(!prompt.contains("Messaggio vecchio"))
        assertTrue(prompt.contains("Messaggio recente"))
    }

    @Test fun recallsOlderRelevantMessagesFromLongConversation() {
        val history = buildList {
            add(ChatLine(1L, "Il progetto si chiama SuperIA-Privata e deve restare offline.", "user"))
            repeat(20) { index -> add(ChatLine((index + 2).toLong(), "Discussione generica numero $index", "user")) }
        }
        val prompt = ChatPromptBuilder.build(history, "Quali erano i requisiti del progetto SuperIA-Privata?")
        assertTrue(prompt.contains("progetto si chiama SuperIA-Privata"))
        assertTrue(prompt.contains("Discussione generica numero 19"))
    }

    @Test fun includesEditableProjectMemorySummary() {
        val prompt = ChatPromptBuilder.build(
            emptyList(),
            "Continua il lavoro",
            memorySummary = "Obiettivo: app Android privata. Decisione: nessun cloud obbligatorio."
        )
        assertTrue(prompt.contains("Memoria di progetto salvata"))
        assertTrue(prompt.contains("nessun cloud obbligatorio"))
    }

    @Test fun includesBoundedDocumentContext() {
        val context = "Documento allegato: esempio\n" + "x".repeat(7000)
        val prompt = ChatPromptBuilder.build(emptyList(), "Riassumi", documentContext = context)
        assertTrue(prompt.contains("Documento allegato: esempio"))
        assertTrue(prompt.length < 6200)
        assertTrue(prompt.endsWith("Riassumi"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejectsBlankPrompt() {
        ChatPromptBuilder.build(emptyList(), "   ")
    }
}
