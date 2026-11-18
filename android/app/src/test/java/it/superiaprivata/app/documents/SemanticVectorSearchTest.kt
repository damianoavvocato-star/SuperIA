package it.superiaprivata.app.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SemanticVectorSearchTest {
    private fun chunk(title: String, vector: FloatArray) =
        SemanticChunkVector(title, 1, "testo $title", vector)

    @Test
    fun ranksByCosineSimilarityAndNormalizesMagnitude() {
        val chunks = listOf(
            chunk("opposto", floatArrayOf(-1f, 0f)),
            chunk("simile", floatArrayOf(10f, 0f)),
            chunk("ortogonale", floatArrayOf(0f, 1f))
        )
        val results = SemanticVectorSearch.search(floatArrayOf(1f, 0f), chunks)
        assertEquals("simile", results.first().first.documentTitle)
        assertEquals(1f, results.first().second, 0.0001f)
        assertEquals("ortogonale", results[1].first.documentTitle)
    }

    @Test
    fun ignoresVectorsWithDifferentDimensionsAndZeroNorm() {
        val chunks = listOf(
            chunk("dimensione-diversa", floatArrayOf(1f, 2f, 3f)),
            chunk("vettore-nullo", floatArrayOf(0f, 0f)),
            chunk("valido", floatArrayOf(1f, 0f))
        )
        val results = SemanticVectorSearch.search(floatArrayOf(1f, 0f), chunks)
        assertEquals(1, results.size)
        assertEquals("valido", results.single().first.documentTitle)
    }

    @Test
    fun respectsResultLimitAndMinimumScore() {
        val chunks = listOf(
            chunk("uno", floatArrayOf(1f, 0f)),
            chunk("due", floatArrayOf(0.8f, 0.6f)),
            chunk("tre", floatArrayOf(-1f, 0f))
        )
        val results = SemanticVectorSearch.search(
            queryEmbedding = floatArrayOf(1f, 0f),
            chunks = chunks,
            limit = 1,
            minimumScore = 0.5f
        )
        assertEquals(1, results.size)
        assertEquals("uno", results.single().first.documentTitle)
    }

    @Test
    fun rejectsInvalidQueryEmbeddingsAndArguments() {
        val chunks = emptyList<SemanticChunkVector>()
        assertTrue(runCatching {
            SemanticVectorSearch.search(floatArrayOf(Float.NaN), chunks)
        }.isFailure)
        assertTrue(runCatching {
            SemanticVectorSearch.search(floatArrayOf(1f), chunks, limit = 0)
        }.isFailure)
    }
}
