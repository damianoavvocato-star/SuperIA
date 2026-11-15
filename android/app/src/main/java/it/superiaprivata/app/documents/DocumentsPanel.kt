package it.superiaprivata.app.documents

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
    val documents = remember(repository) {
        mutableStateListOf<StoredDocument>().apply { addAll(repository.list()) }
    }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var importing by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<String?>(null) }
    var selectedContext by remember { mutableStateOf<String?>(null) }

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

    val selected = documents.firstOrNull { it.id == selectedId }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Documenti e conoscenza personale", style = MaterialTheme.typography.titleLarge)
            Text("Importa PDF, DOCX, TXT o CSV. Il testo viene estratto sul telefono e conservato cifrato nell'area privata dell'app.")
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
                "Limiti: massimo 25 MB per file, 250 pagine PDF e 200.000 caratteri estratti. I PDF scansiti richiedono OCR, non ancora incluso. La ricerca semantica/RAG multi-documento è uno step successivo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
