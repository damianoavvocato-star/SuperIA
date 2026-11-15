package it.superiaprivata.app.documents

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Xml
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream

data class ExtractedDocument(val title: String, val extension: String, val text: String)

class DocumentTextExtractor(context: Context) {
    private val appContext = context.applicationContext

    companion object {
        const val MAX_FILE_BYTES = 25L * 1024L * 1024L
        const val MAX_TEXT_CHARACTERS = 200_000
        private const val MAX_PDF_PAGES = 250
    }

    fun extract(uri: Uri): ExtractedDocument {
        val title = queryDisplayName(uri) ?: "documento"
        val extension = title.substringAfterLast('.', "").lowercase()
        require(extension in setOf("pdf", "txt", "csv", "docx")) {
            "Formato non supportato. Seleziona un PDF, DOCX, TXT o CSV."
        }
        val bytes = appContext.contentResolver.openInputStream(uri)?.use {
            readBounded(it, MAX_FILE_BYTES)
        } ?: throw IllegalArgumentException("Impossibile aprire il documento selezionato.")

        val extracted = when (extension) {
            "pdf" -> extractPdf(bytes)
            "docx" -> extractDocx(bytes)
            "txt", "csv" -> decodeText(bytes)
            else -> error("Formato non supportato.")
        }.trim()

        require(extracted.isNotBlank()) {
            if (extension == "pdf") "Il PDF non contiene testo estraibile. Potrebbe essere una scansione: OCR non ancora disponibile."
            else "Non è stato trovato testo leggibile nel documento."
        }
        require(extracted.length <= MAX_TEXT_CHARACTERS) {
            "Il documento supera il limite di $MAX_TEXT_CHARACTERS caratteri estraibili."
        }
        return ExtractedDocument(title, extension, extracted)
    }

    private fun extractPdf(bytes: ByteArray): String {
        PDFBoxResourceLoader.init(appContext)
        PDDocument.load(ByteArrayInputStream(bytes)).use { document ->
            require(document.numberOfPages <= MAX_PDF_PAGES) {
                "Il PDF supera il limite di $MAX_PDF_PAGES pagine."
            }
            return PDFTextStripper().getText(document).take(MAX_TEXT_CHARACTERS + 1)
        }
    }

    private fun extractDocx(bytes: ByteArray): String {
        var documentXml: ByteArray? = null
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                if (entry.name == "word/document.xml") {
                    documentXml = readBounded(zip, MAX_FILE_BYTES)
                    break
                }
                zip.closeEntry()
            }
        }
        val xml = documentXml ?: throw IllegalArgumentException("Il DOCX non contiene word/document.xml.")
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
        parser.setInput(ByteArrayInputStream(xml), "UTF-8")
        val output = StringBuilder()
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT && output.length <= MAX_TEXT_CHARACTERS) {
            when (event) {
                XmlPullParser.START_TAG -> when (parser.name) {
                    "tab" -> output.append('\t')
                    "br" -> output.append('\n')
                }
                XmlPullParser.TEXT -> if (parser.text.isNotBlank()) output.append(parser.text)
                XmlPullParser.END_TAG -> if (parser.name == "p") output.append('\n')
            }
            event = parser.next()
        }
        return output.toString().take(MAX_TEXT_CHARACTERS + 1)
    }

    private fun decodeText(bytes: ByteArray): String {
        return bytes.toString(Charsets.UTF_8).removePrefix("\uFEFF")
            .replace("\r\n", "\n").replace('\r', '\n')
            .take(MAX_TEXT_CHARACTERS + 1)
    }

    private fun queryDisplayName(uri: Uri): String? =
        appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else null
            }

    private fun readBounded(input: InputStream, limit: Long): ByteArray {
        val output = ByteArrayOutputStream()
        val buffer = ByteArray(8 * 1024)
        var total = 0L
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            total += count
            require(total <= limit) { "Il documento supera il limite di ${limit / (1024 * 1024)} MB." }
            output.write(buffer, 0, count)
        }
        return output.toByteArray()
    }
}
