package it.superiaprivata.app.documents

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DocumentsPanel(onContextChanged: (String?) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = remember(context) { LocalDocumentRepository(context) }
    val extractor = remember(context) { DocumentTextExtractor(context) }
    val ocrStore = remember(context) { OcrLanguageDataStore(context) }
    val documents = remember(repository) {
        mutableStateListOf<StoredDocument>().apply { addAll(repository.list()) }
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var selectedContext by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<RetrievedChunk>>(emptyList()) }
    var ocrReady by remember(ocrStore) { mutableStateOf(ocrStore.isItalianReady()) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            importing = true
            status = "Estrazione locale in corso…"
            try {
                val stored = withContext(Dispatchers.IO) { repository.import(uri, extractor) }
                documents.removeAll { it.id == stored.id }
                documents.add(0, stored)
                selectedId = stored.id
                status = "Documento importato nell'archivio privato del telefono."
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                status = error.message ?: "Impossibile leggere il documento."
            } finally {
                importing = false
            }
        }
    }

    val ocrPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) scope.launch {
            status = "Importazione del dizionario OCR…"
            try {
                val bytes = withContext(Dispatchers.IO) { ocrStore.importItalian(uri) }
                ocrReady = true
                status = "Dizionario italiano OCR importato: ${bytes / (1024 * 1024)} MB."
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                status = error.message ?: "Importazione del dizionario OCR non riuscita."
            }
        }
    }

    val selected = documents.firstOrNull { it.id == selectedId }
    val exportPicker = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) { uri ->
        val document = selected
        if (uri != null && document != null) scope.launch {
            status = "Esportazione del testo estratto…"
            try {
                withContext(Dispatchers.IO) {
                    val output = context.contentResolver.openOutputStream(uri)
                        ?: error("Impossibile aprire il file di destinazione.")
                    output.use { it.write(document.text.toByteArray(Charsets.UTF_8)) }
                }
                status = "Testo estratto esportato nella posizione scelta. La copia salvata è in chiaro."
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                status = error.message ?: "Esportazione non riuscita."
            }
        }
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Documenti e conoscenza personale", style = MaterialTheme.typography.titleLarge)
            Text("Importa PDF, DOCX, TXT o CSV. Il testo viene estratto sul telefono e conservato cifrato nell'area privata dell'app.")
            Text(
                if (ocrReady) "OCR italiano offline: pronto"
                else "OCR italiano offline: importa il file ita.traineddata per leggere i PDF scansiti.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(
                onClick = { ocrPicker.launch(arrayOf("*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (ocrReady) "Sostituisci dizionario OCR italiano" else "Importa dizionario OCR italiano") }
            OutlinedButton(
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/tesseract-ocr/tessdata_fast/blob/4.1.0/ita.traineddata"))
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Apri dizionario italiano ufficiale") }
            Button(
                enabled = !importing,
                onClick = { picker.launch(arrayOf("application/pdf", "text/plain", "text/csv", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (importing) "Importazione…" else "Importa un documento") }

            if (documents.isNotEmpty()) {
                Text("Archivio locale · ${documents.size} documenti", style = MaterialTheme.typography.titleSmall)
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(documents, key = { it.id }) { document ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { selectedId = document.id }.padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(document.title, style = MaterialTheme.typography.bodyMedium)
                                Text("${document.text.length} caratteri estratti", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(if (document.id == selectedId) "Selezionato" else "Apri", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            if (documents.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Cerca in tutti i documenti") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedButton(
                    enabled = searchQuery.isNotBlank(),
                    onClick = {
                        val snapshot = documents.toList()
                        scope.launch {
                            searchResults = withContext(Dispatchers.Default) {
                                LocalDocumentRetriever.search(searchQuery, snapshot, limit = 5)
                            }
                            status = if (searchResults.isEmpty()) "Nessun passaggio pertinente trovato."
                            else "${searchResults.size} passaggi pertinenti trovati con ricerca locale."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cerca nell'archivio") }
                searchResults.forEach { result ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(
                            "${result.documentTitle} · blocco ${result.chunkNumber}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(result.text.take(360), style = MaterialTheme.typography.bodySmall)
                    }
                }
                if (searchResults.isNotEmpty()) {
                    Button(
                        onClick = {
                            selectedContext = buildString {
                                appendLine("Risultati recuperati dall'archivio documentale locale tramite BM25:")
                                searchResults.forEach { result ->
                                    appendLine("[Fonte: ${result.documentTitle}, blocco ${result.chunkNumber}]")
                                    appendLine(result.text)
                                    appendLine()
                                }
                            }.take(6000)
                            onContextChanged(selectedContext)
                            status = "Passaggi documentali aggiunti al contesto della chat."
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Usa i risultati nella chat AI") }
                }
            }

            selected?.let { document ->
                Text("Anteprima: ${document.title}", style = MaterialTheme.typography.titleSmall)
                Text(document.text.take(1200) + if (document.text.length > 1200) "…" else "", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = {
                        selectedContext = "Documento allegato: ${document.title}\nContenuto estratto (estratto limitato a 6.000 caratteri):\n" + document.text.take(6000)
                        onContextChanged(selectedContext)
                        status = "Documento aggiunto al contesto della prossima richiesta chat."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Usa nella chat AI") }
                OutlinedButton(
                    onClick = {
                        val suggestedName = document.title.substringBeforeLast('.', document.title)
                            .replace(Regex("[^A-Za-z0-9._-]"), "_") + ".txt"
                        exportPicker.launch(suggestedName)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Esporta testo estratto") }
                OutlinedButton(
                    onClick = {
                        runCatching { repository.delete(document) }
                            .onSuccess {
                                documents.remove(document)
                                selectedId = null
                                selectedContext = null
                                onContextChanged(null)
                                status = "Documento eliminato dall'archivio locale."
                            }
                            .onFailure { status = it.message }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Elimina documento") }
            }
            if (selectedContext != null) {
                OutlinedButton(
                    onClick = {
                        selectedContext = null
                        onContextChanged(null)
                        status = "Contesto documento rimosso dalla chat."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Rimuovi contesto dalla chat") }
            }
            status?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            Text(
                "Limiti: massimo 25 MB per file, 250 pagine PDF, 200.000 caratteri estratti, 50 documenti e 200 MB complessivi. I PDF scansiti usano Tesseract offline se è stato importato ita.traineddata; l'OCR è limitato alle prime 20 pagine. La ricerca semantica basata su embeddings è uno step successivo; la ricerca testuale locale BM25 su più documenti è disponibile.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
