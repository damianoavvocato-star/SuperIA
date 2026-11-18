package it.superiaprivata.app.documents

import kotlin.math.ln

data class RetrievedChunk(
    val documentTitle: String,
    val chunkNumber: Int,
    val text: String,
    val score: Double
)

/** Small offline BM25 index over locally decrypted document text. */
object LocalDocumentRetriever {
    private const val BM25_K1 = 1.2
    private const val BM25_B = 0.75

    fun search(query: String, documents: List<StoredDocument>, limit: Int = 5): List<RetrievedChunk> {
        require(limit > 0) { "Il numero di risultati deve essere positivo." }
        val queryTerms = tokenize(query).distinct()
        if (queryTerms.isEmpty() || documents.isEmpty()) return emptyList()

        val chunks = documents.flatMap { document ->
            LocalDocumentChunker.chunk(document.text).mapIndexed { index, text ->
                Chunk(document.title, index + 1, text, tokenize(text))
            }
        }
        if (chunks.isEmpty()) return emptyList()

        val documentFrequency = queryTerms.associateWith { term ->
            chunks.count { term in it.terms }
        }
        val averageLength = chunks.map { it.terms.size }.average().coerceAtLeast(1.0)

        return chunks.map { chunk ->
            var score = 0.0
            for (term in queryTerms) {
                val tf = chunk.terms.count { it == term }
                if (tf == 0) continue
                val df = documentFrequency[term] ?: 0
                val idf = ln(1.0 + (chunks.size - df + 0.5) / (df + 0.5))
                val lengthNorm = 1.0 - BM25_B + BM25_B * chunk.terms.size / averageLength
                score += idf * (tf * (BM25_K1 + 1.0)) / (tf + BM25_K1 * lengthNorm)
            }
            RetrievedChunk(chunk.title, chunk.number, chunk.text, score)
        }.filter { it.score > 0.0 }
            .sortedByDescending { it.score }
            .take(limit)
    }

    private fun tokenize(text: String): List<String> =
        Regex("[\\p{L}\\p{N}]{2,}").findAll(text.lowercase())
            .map { it.value }
            .toList()

    private data class Chunk(val title: String, val number: Int, val text: String, val terms: List<String>)
}
