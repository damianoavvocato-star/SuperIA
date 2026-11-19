package it.superiaprivata.app.documents

/**
 * Deterministic paragraph-aware chunking shared by lexical and semantic retrieval.
 * Keeps chunks bounded and preserves source order for reliable citations.
 */
object LocalDocumentChunker {
    const val DEFAULT_MAX_CHARACTERS = 900

    fun chunk(text: String, maxCharacters: Int = DEFAULT_MAX_CHARACTERS): List<String> {
        require(maxCharacters > 0) { "La dimensione del blocco deve essere positiva." }
        val paragraphs = text.split(Regex("\\n[ \\t]*\\n"))
        val result = mutableListOf<String>()
        var current = StringBuilder()

        fun flush() {
            if (current.isNotEmpty()) {
                result += current.toString()
                current = StringBuilder()
            }
        }

        fun splitLongParagraph(paragraph: String) {
            var start = 0
            while (start < paragraph.length) {
                var end = minOf(start + maxCharacters, paragraph.length)
                if (end < paragraph.length) {
                    // Prefer a whitespace boundary, but fall back to a hard split
                    // when a single token is longer than the configured limit.
                    val boundary = paragraph.indexOfLastWhitespace(start, end - 1)
                    if (boundary > start) end = boundary + 1
                }
                result += paragraph.substring(start, end)
                start = end
            }
        }

        for (paragraph in paragraphs) {
            val normalized = paragraph.trim()
            if (normalized.isEmpty()) continue
            if (normalized.length > maxCharacters) {
                flush()
                splitLongParagraph(normalized)
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

    private fun String.indexOfLastWhitespace(start: Int, endInclusive: Int): Int {
        for (index in endInclusive downTo start) {
            if (this[index].isWhitespace()) return index
        }
        return -1
    }
}
