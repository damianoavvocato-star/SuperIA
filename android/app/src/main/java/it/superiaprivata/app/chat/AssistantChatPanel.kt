package it.superiaprivata.app.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import it.superiaprivata.app.device.DeviceProfileReader
import it.superiaprivata.app.documents.DocumentTextExtractor
import it.superiaprivata.app.documents.LocalDocumentContextBuilder
import it.superiaprivata.app.documents.LocalDocumentRepository
import it.superiaprivata.app.inference.InferenceRequest
import it.superiaprivata.app.inference.LiteRtLmInferenceEngine
import it.superiaprivata.app.inference.LocalModelCatalog
import it.superiaprivata.app.inference.LocalModelInstaller
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun AssistantChatPanel(documentContext: String? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyStore = remember(context) { ChatHistoryStore(context) }
    val initialHistory = remember(historyStore) { runCatching { historyStore.load() } }
    val messages = remember(initialHistory) {
        mutableStateListOf<ChatLine>().apply {
            addAll(initialHistory.getOrDefault(emptyList()))
        }
    }
    val connectionStore = remember(context) { OnlineConnectionStore(context) }
    val savedConnection = remember(connectionStore) { connectionStore.load() }
    var backendEndpoint by remember { mutableStateOf(savedConnection.endpoint) }
    var backendToken by remember { mutableStateOf(savedConnection.token) }
    var connectionStatus by remember { mutableStateOf(if (savedConnection.isConfigured) "Configurazione backend caricata in modo protetto." else "Collega un backend HTTPS per iniziare.") }
    var exitDiagnostic by remember(context) { mutableStateOf(RuntimeExitDiagnostics.read(context)) }
    var generating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var projectMemory by remember(historyStore) { mutableStateOf(runCatching { historyStore.loadProjectMemory() }.getOrDefault("")) }
    var memorySaved by remember { mutableStateOf(false) }
    var memoryError by remember { mutableStateOf<String?>(null) }
    var storageError by remember { mutableStateOf(initialHistory.exceptionOrNull()?.message) }
    var generationError by remember { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var attachedDocumentContext by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    var importingDocument by remember { mutableStateOf(false) }
    val onlineConfigured = backendEndpoint.isNotBlank() && backendToken.isNotBlank()
    val messageListState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) messageListState.animateScrollToItem(messages.lastIndex)
    }

    suspend fun buildRequestPrompt(
        history: List<ChatLine>,
        request: String,
        explicitlySelectedContext: String?
    ): String {
        val storedDocuments = withContext(Dispatchers.IO) {
            LocalDocumentRepository(context).list()
        }
        val combinedContext = listOfNotNull(explicitlySelectedContext, attachedDocumentContext)
            .filter { it.isNotBlank() }.joinToString("\n\n")
        val retrievedContext = withContext(Dispatchers.Default) {
            LocalDocumentContextBuilder.build(request, storedDocuments, combinedContext.ifBlank { null })
        }
        return ChatPromptBuilder.build(
            history,
            request,
            documentContext = retrievedContext,
            memorySummary = projectMemory
        )
    }
    val documentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                importingDocument = true
                attachmentStatus = "Importazione documento in corso…"
                try {
                    val repository = LocalDocumentRepository(context)
                    val stored = withContext(Dispatchers.IO) {
                        repository.import(uri, DocumentTextExtractor(context))
                    }
                    attachedDocumentContext = "Documento allegato alla conversazione: ${stored.title}\nContenuto estratto:\n${stored.text.take(6000)}"
                    attachmentStatus = "Allegato pronto: ${stored.title}. Il file è conservato cifrato e sarà consultabile anche nelle prossime chat."
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    attachmentStatus = error.message ?: "Importazione documento non riuscita."
                } finally {
                    importingDocument = false
                }
            }
        }
    }



    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chat", style = MaterialTheme.typography.titleLarge)
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        TextButton(
                            enabled = !generating,
                            onClick = {
                                runCatching { historyStore.clear() }
                                    .onSuccess { messages.clear(); storageError = null }
                                    .onFailure { storageError = it.message ?: "Errore nella cancellazione" }
                            }
                        ) { Text("Svuota") }
                    }
                    OutlinedButton(onClick = { showSettings = !showSettings }) {
                        Text(if (showSettings) "Chiudi" else "Impostazioni")
                    }
                }
            }
            if (showSettings) {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Diagnostica ultima chiusura", style = MaterialTheme.typography.titleSmall)
                    Text(exitDiagnostic, style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(
                        onClick = { exitDiagnostic = RuntimeExitDiagnostics.read(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Aggiorna diagnostica") }
                }
            }
            }
            if (showSettings) {
            Text("Una conversazione persistente: la cronologia resta cifrata sul dispositivo. Per rispondere, la richiesta viene inviata al backend HTTPS configurato.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = backendEndpoint,
                onValueChange = { backendEndpoint = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Indirizzo backend HTTPS") },
                placeholder = { Text("https://tuo-backend.example") },
                singleLine = true
            )
            OutlinedTextField(
                value = backendToken,
                onValueChange = { backendToken = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Token di accesso backend") },
                placeholder = { Text("Token fornito dall'amministratore del backend") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = !generating && backendEndpoint.startsWith("https://") && backendToken.isNotBlank(),
                    onClick = {
                        runCatching {
                            connectionStore.save(OnlineConnectionConfig(backendEndpoint.trim(), backendToken))
                        }.onSuccess {
                            connectionStatus = "Configurazione salvata cifrata sul dispositivo."
                            generationError = null
                        }.onFailure {
                            connectionStatus = it.message ?: "Salvataggio non riuscito."
                        }
                    }
                ) { Text("Salva collegamento") }
                TextButton(
                    enabled = !generating,
                    onClick = {
                        runCatching { connectionStore.clear() }
                            .onSuccess {
                                backendEndpoint = ""
                                backendToken = ""
                                connectionStatus = "Collegamento rimosso."
                            }
                            .onFailure { connectionStatus = it.message ?: "Rimozione non riuscita." }
                    }
                ) { Text("Rimuovi") }
            }
            Text(connectionStatus, style = MaterialTheme.typography.bodySmall)
            Text(
                "La chiave viene conservata cifrata con Android Keystore. Usa soltanto un backend HTTPS fidato. Non inserire qui le chiavi dei fornitori IA: sono segreti che devono restare sul server.",
                style = MaterialTheme.typography.bodySmall
            )

            }

            messages.lastOrNull { it.pending }?.let { pendingLine ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Richiesta non completata", style = MaterialTheme.typography.titleSmall)
                        Text("L'app è stata chiusa o l'inferenza si è interrotta prima della risposta. Puoi riprovare senza reinserire il messaggio.")
                        OutlinedButton(
                            enabled = onlineConfigured && !generating,
                            onClick = {
                                val contextHistory = messages.filterNot { it.pending }
                                scope.launch {
                                    generating = true
                                    generationError = null
                                    try {
                                        val retryPrompt = buildRequestPrompt(
                                            contextHistory,
                                            pendingLine.text,
                                            documentContext
                                        )
                                        val result = OnlineAssistantClient.send(
                                            OnlineConnectionConfig(backendEndpoint.trim(), backendToken),
                                            retryPrompt
                                        )
                                        val pendingIndex = messages.indexOfFirst { it.id == pendingLine.id }
                                        if (pendingIndex >= 0) messages[pendingIndex] = pendingLine.copy(pending = false)
                                        messages.add(ChatLine(System.nanoTime(), result, "assistant"))
                                        historyStore.save(messages.toList())
                                        storageError = null
                                    } catch (error: CancellationException) {
                                        
                                        throw error
                                    } catch (error: OutOfMemoryError) {
                                        messages.removeAll { it.id == Long.MIN_VALUE }
                                            generationError = "Memoria insufficiente durante l'inferenza. Chiudi le altre app e riprova."
                                    } catch (error: Exception) {
                                        messages.removeAll { it.id == Long.MIN_VALUE }
                                        generationError = error.message ?: "Ripresa della richiesta non riuscita."
                                    } finally {
                                        generating = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Riprendi richiesta") }
                    }
                }
            }

            if (messages.isNotEmpty()) {
                LazyColumn(
                    state = messageListState,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    items(messages, key = { it.id }) { line ->
                        Surface(
                            color = if (line.role == "assistant")
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
                                Text(
                                    if (line.role == "assistant") "SuperIA" else "Tu",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                val expanded = remember(line.id) { mutableStateOf(false) }
                                MarkdownMessageText(
                                    markdown = line.text,
                                    maxLines = if (expanded.value) Int.MAX_VALUE else 16,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable { expanded.value = !expanded.value }
                                )
                            }
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text("Ciao, sono SuperIA", style = MaterialTheme.typography.headlineSmall)
                    Text("Scrivi un messaggio oppure usa + per allegare un documento.", style = MaterialTheme.typography.bodyMedium)
                }
            }

            attachmentStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                IconButton(
                    enabled = !importingDocument && !generating,
                    onClick = { documentPicker.launch(arrayOf("application/pdf", "text/plain", "text/csv", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Allega documento")
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Scrivi a SuperIA…") },
                    maxLines = 3
                )
                IconButton(
                    enabled = draft.isNotBlank() && onlineConfigured && !generating && messages.none { it.pending },
                    onClick = {
                        val request = draft.trim()
                        if (request.isNotEmpty()) {
                            val contextHistory = messages.toList()
                            val userLine = ChatLine(System.nanoTime(), request, "user", pending = true)
                            messages.add(userLine)
                            val saved = runCatching { historyStore.save(messages.toList()) }
                            if (saved.isFailure) {
                                messages.remove(userLine)
                                storageError = saved.exceptionOrNull()?.message ?: "Errore nel salvataggio"
                                return@IconButton
                            }
                            storageError = null
                            draft = ""
                            scope.launch {
                                generating = true
                                generationError = null
                                try {
                                    val promptWithHistory = buildRequestPrompt(contextHistory, request, documentContext)
                                    val result = OnlineAssistantClient.send(
                                        OnlineConnectionConfig(backendEndpoint.trim(), backendToken),
                                        promptWithHistory
                                    )
                                    val pendingIndex = messages.indexOfFirst { it.id == userLine.id }
                                    if (pendingIndex >= 0) messages[pendingIndex] = userLine.copy(pending = false)
                                    messages.add(ChatLine(System.nanoTime(), result, "assistant"))
                                    historyStore.save(messages.toList())
                                } catch (error: CancellationException) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    throw error
                                } catch (error: OutOfMemoryError) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    generationError = "Memoria insufficiente durante l'inferenza. Chiudi le altre app e riprova."
                                } catch (error: Exception) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    generationError = error.message ?: "Errore durante la richiesta online."
                                } finally {
                                    generating = false
                                }
                            }
                        }
                    }
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Invia messaggio")
                }
            }
            storageError?.let {
                Text("Problema con la cronologia protetta: $it", color = MaterialTheme.colorScheme.error)
            }
            generationError?.let {
                Text("Errore AI: $it", color = MaterialTheme.colorScheme.error)
            }
            if (!onlineConfigured) {
                Text(
                    "Configura un backend HTTPS e il token nelle Impostazioni per usare la chat online.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
