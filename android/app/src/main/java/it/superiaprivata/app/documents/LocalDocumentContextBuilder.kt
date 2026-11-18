package it.superiaprivata.app.documents

/**
 * Builds a bounded, source-labelled context for one chat request.
 * Retrieval is lexical BM25 and runs fully offline; document text is untrusted content.
 */
object LocalDocumentContextBuilder {
    const val MAX_CONTEXT_CHARACTERS = 6_000
    const val MAX_RESULTS = 5

    fun build(
        query: String,
        documents: List<StoredDocument>,
        explicitlySelectedContext: String? = null
    ): String? {
        val retrieved = LocalDocumentRetriever.search(query, documents, limit = MAX_RESULTS)
        if (retrieved.isEmpty() && explicitlySelectedContext.isNullOrBlank()) return null

        return buildString {
            if (!explicitlySelectedContext.isNullOrBlank()) {
                appendLine("Estratto selezionato manualmente dall'utente:")
                appendLine(explicitlySelectedContext.trim())
                appendLine()
            }
            if (retrieved.isNotEmpty()) {
                appendLine("Passaggi recuperati automaticamente dall'archivio locale (ricerca BM25):")
                retrieved.forEach { chunk ->
                    appendLine("[Fonte: ${chunk.documentTitle}, blocco ${chunk.chunkNumber}]")
                    appendLine(chunk.text)
                    appendLine()
                }
            }
        }.trim().take(MAX_CONTEXT_CHARACTERS).ifBlank { null }
    }
}
