package it.superiaprivata.app.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalDocumentChunkerTest {
    @Test
    fun combinesParagraphsWithoutExceedingLimit() {
        val chunks = LocalDocumentChunker.chunk("Primo paragrafo.\n\nSecondo paragrafo.", maxCharacters = 40)
        assertEquals(listOf("Primo paragrafo.\n\nSecondo paragrafo."), chunks)
    }

    @Test
    fun splitsLongParagraphIntoBoundedChunksInSourceOrder() {
        val source = "a".repeat(25)
        val chunks = LocalDocumentChunker.chunk(source, maxCharacters = 10)
        assertEquals(listOf("a".repeat(10), "a".repeat(10), "a".repeat(5)), chunks)
        assertEquals(source, chunks.joinToString(""))
    }

    @Test
    fun prefersWordBoundariesAndPreservesEveryCharacter() {
        val source = "alpha beta gamma delta"
        val chunks = LocalDocumentChunker.chunk(source, maxCharacters = 10)
        assertTrue(chunks.all { it.length <= 10 })
        assertEquals(source, chunks.joinToString(""))
        assertEquals(listOf("alpha ", "beta ", "gamma ", "delta"), chunks)
    }

    @Test
    fun ignoresEmptyParagraphsAndRejectsInvalidLimit() {
        assertEquals(listOf("testo"), LocalDocumentChunker.chunk("  \n\n testo \n\n"))
        assertTrue(runCatching { LocalDocumentChunker.chunk("testo", 0) }.isFailure)
    }
}
