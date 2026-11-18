package it.superiaprivata.app.documents

/**
 * Deterministic paragraph-aware chunking shared by lexical and semantic retrieval.
 * Keeps chunks bounded and preserves source order for reliable citations.
 */
object LocalDocumentChunker {
    const val DEFAULT_MAX_CHARACTERS = 900

    fun chunk(text: String, maxCharacters: Int = DEFAULT_MAX_CHARACTERS): List<String> {
        require(maxCharacters > 0) { "La dimensione del blocco deve essere positiva." }
        val paragraphs = text.split(Regex("\\n\\s*\\n"))
        val result = mutableListOf<String>()
        var current = StringBuilder()

        fun flush() {
            if (current.isNotEmpty()) {
                result += current.toString()
                current = StringBuilder()
            }
        }

        for (paragraph in paragraphs) {
            val normalized = paragraph.trim()
            if (normalized.isEmpty()) continue
            if (normalized.length > maxCharacters) {
                flush()
                normalized.chunked(maxCharacters).forEach(result::add)
            } else {
                if (current.length + normalized.length + 2 > maxCharacters && current.isNotEmpty()) {
                    flush()
                }
                if (current.isNotEmpty()) current.append("\\n\\n")
                current.append(normalized)
            }
        }
        flush()
        return result
    }
}
