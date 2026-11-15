package it.superiaprivata.app.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalDocumentRetrieverTest {
    @Test
    fun ranksRelevantDocumentAboveUnrelatedDocument() {
        val documents = listOf(
            StoredDocument("1", "Cucina", "Ricetta di pasta con pomodoro e basilico.", File("1.enc")),
            StoredDocument("2", "Motore", "Il motore diesel usa carburante e pistoni.", File("2.enc"))
        )
        val results = LocalDocumentRetriever.search("pasta pomodoro", documents)
        assertEquals("Cucina", results.first().documentTitle)
        assertTrue(results.first().score > 0.0)
    }

    @Test
    fun returnsNoResultsForEmptyOrUnmatchedQuery() {
        val documents = listOf(
            StoredDocument("1", "Appunti", "Testo sul diritto civile.", File("1.enc"))
        )
        assertTrue(LocalDocumentRetriever.search("   ", documents).isEmpty())
        assertTrue(LocalDocumentRetriever.search("zzqvxx", documents).isEmpty())
    }

    @Test
    fun limitsNumberOfReturnedChunks() {
        val text = (1..12).joinToString("\\n\\n") { "Capitolo $it contiene parole comuni e ricerca locale." }
        val documents = listOf(StoredDocument("1", "Libro", text, File("1.enc")))
        assertTrue(LocalDocumentRetriever.search("ricerca locale", documents, limit = 3).size <= 3)
    }
}
