package it.superiaprivata.app.documents

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class LocalDocumentContextBuilderTest {
    @Test
    fun automaticallyAddsOnlyRelevantLocalPassagesWithSourceLabels() {
        val documents = listOf(
            StoredDocument("1", "Contratto", "La durata del contratto è di dodici mesi.", File("1.enc")),
            StoredDocument("2", "Ricetta", "Preparare la pasta con pomodoro fresco.", File("2.enc"))
        )

        val context = LocalDocumentContextBuilder.build("durata contratto mesi", documents)

        assertTrue(context!!.contains("Contratto"))
        assertTrue(context.contains("dodici mesi"))
        assertTrue(!context.contains("pomodoro fresco"))
        assertTrue(context.contains("Fonte:"))
    }

    @Test
    fun returnsNullWhenThereIsNoRelevantDocumentOrManualSelection() {
        val documents = listOf(
            StoredDocument("1", "Appunti", "Testo sul diritto civile.", File("1.enc"))
        )
        assertNull(LocalDocumentContextBuilder.build("motore diesel", documents))
    }

    @Test
    fun preservesExplicitlySelectedContextWhenAutomaticSearchHasNoMatch() {
        val context = LocalDocumentContextBuilder.build(
            query = "motore diesel",
            documents = emptyList(),
            explicitlySelectedContext = "Documento scelto: clausola 4"
        )
        assertTrue(context!!.contains("clausola 4"))
    }

    @Test
    fun boundsCombinedContextLength() {
        val longText = "parola ".repeat(2_000)
        val documents = listOf(StoredDocument("1", "Documento", longText, File("1.enc")))
        val context = LocalDocumentContextBuilder.build(
            query = "parola",
            documents = documents,
            explicitlySelectedContext = longText
        )
        assertEquals(LocalDocumentContextBuilder.MAX_CONTEXT_CHARACTERS, context!!.length)
    }
}
