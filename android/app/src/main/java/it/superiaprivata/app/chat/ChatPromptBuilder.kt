package it.superiaprivata.app.chat

/**
 * Rebuilds a bounded conversational context from the encrypted transcript.
 * LiteRT-LM conversations are in-memory, so this keeps recent context available
 * after the app recreates its runtime.
 */
object ChatPromptBuilder {
    const val DEFAULT_HISTORY_MESSAGES = 12
    const val MAX_HISTORY_CHARACTERS = 6_000

    fun build(
        history: List<ChatLine>,
        newPrompt: String,
        maxHistoryMessages: Int = DEFAULT_HISTORY_MESSAGES
    ): String {
        val prompt = newPrompt.trim()
        require(prompt.isNotEmpty()) { "La richiesta non può essere vuota." }
        require(maxHistoryMessages > 0) { "Il numero di messaggi di contesto deve essere positivo." }

        val transcript = history
            .takeLast(maxHistoryMessages)
            .joinToString("\n") { line ->
                val speaker = if (line.role == "assistant") "Assistente" else "Utente"
                "$speaker: ${line.text}"
            }
            .takeLast(MAX_HISTORY_CHARACTERS)

        if (transcript.isBlank()) return prompt

        return buildString {
            appendLine("Cronologia recente della conversazione (solo contesto):")
            appendLine(transcript)
            appendLine()
            appendLine("Nuovo messaggio dell'utente:")
            append(prompt)
        }
    }
}
