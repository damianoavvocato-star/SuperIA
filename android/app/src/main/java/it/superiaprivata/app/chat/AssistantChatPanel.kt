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
    val artifact = LocalModelCatalog.gemma3_1b_it_int4
    val modelFile = remember(context) { LocalModelCatalog.modelFile(context, artifact) }
    val installer = remember(context) { LocalModelInstaller(context) }
    val deviceProfile = remember(context) { DeviceProfileReader(context).read() }
    var exitDiagnostic by remember(context) { mutableStateOf(RuntimeExitDiagnostics.read(context)) }
    var licenseAccepted by remember(context) {
        mutableStateOf(context.getSharedPreferences("superia_model_consent", 0)
            .getBoolean(artifact.spec.licenseId, false))
    }
    var modelInstalled by remember(modelFile) { mutableStateOf(modelFile.isFile && modelFile.length() > 0L) }
    var importing by remember { mutableStateOf(false) }
    var generating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var projectMemory by remember(historyStore) { mutableStateOf(runCatching { historyStore.loadProjectMemory() }.getOrDefault("")) }
    var memorySaved by remember { mutableStateOf(false) }
    var memoryError by remember { mutableStateOf<String?>(null) }
    var storageError by remember { mutableStateOf(initialHistory.exceptionOrNull()?.message) }
    var modelMessage by remember { mutableStateOf<String?>(null) }
    var generationError by remember { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var attachedDocumentContext by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    var importingDocument by remember { mutableStateOf(false) }
    val messageListState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) messageListState.animateScrollToItem(messages.lastIndex)
    }

    val engine = remember(context) {
        LiteRtLmInferenceEngine(
            context = context,
            modelFile = { requested ->
                if (requested.id == artifact.spec.id) modelFile else null
            },
            isLicenseApproved = { licenseAccepted }
        )
    }
    DisposableEffect(engine) {
        onDispose { engine.close() }
    }

    val availability = engine.checkAvailability(deviceProfile, artifact.spec)

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

    val modelPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                importing = true
                modelMessage = "Importazione del modello in corso…"
                try {
                    val result = installer.importModel(uri, artifact)
                    modelInstalled = true
                    modelMessage = "Modello importato: ${result.sizeBytes / (1024 * 1024)} MB · SHA-256 ${result.sha256.take(16)}…"
                    generationError = null
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    modelMessage = error.message ?: "Importazione non riuscita."
                } finally {
                    importing = false
                }
            }
        }
    }

    Card(
        modifier = modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Chat", style = MaterialTheme.typography.titleLarge)
                OutlinedButton(onClick = { showSettings = !showSettings }) {
                    Text(if (showSettings) "Nascondi opzioni" else "Impostazioni")
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
            Text("Una conversazione persistente: la cronologia viene conservata sul dispositivo anche quando chiudi l'app.", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(
                value = projectMemory,
                onValueChange = { projectMemory = it; memorySaved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Memoria del progetto") },
                placeholder = { Text("Decisioni, obiettivi e informazioni da ricordare nelle prossime sessioni…") },
                minLines = 2,
                maxLines = 5
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = {
                    runCatching { historyStore.saveProjectMemory(projectMemory) }
                        .onSuccess { memorySaved = true; memoryError = null }
                        .onFailure { memoryError = it.message ?: "Impossibile salvare la memoria." }
                }) { Text(if (memorySaved) "Memoria salvata" else "Salva memoria") }
            }
            memoryError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Text(
                "Assistente locale sul telefono. Il modello non è incluso nell'APK: va scaricato e importato una sola volta.",
                style = MaterialTheme.typography.bodyMedium
            )

            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(artifact.spec.displayName, style = MaterialTheme.typography.titleMedium)
                    Text("File richiesto: ${artifact.fileName}")
                    Text("Dimensione indicativa: circa 584 MB. Richiede almeno 2 GB di RAM disponibile secondo la soglia iniziale.")
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(artifact.sourceUrl))
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Apri pagina ufficiale del modello") }
                    OutlinedButton(
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse(artifact.licenseUrl))
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Leggi i termini di licenza Gemma") }
                    Row {
                        Checkbox(
                            checked = licenseAccepted,
                            onCheckedChange = {
                                licenseAccepted = it
                                context.getSharedPreferences("superia_model_consent", 0).edit()
                                    .putBoolean(artifact.spec.licenseId, it).apply()
                            }
                        )
                        Text(
                            "Ho letto e accetto i termini Gemma per questo modello.",
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                    OutlinedButton(
                        enabled = licenseAccepted && !importing && !generating,
                        onClick = { modelPicker.launch(arrayOf("*/*")) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (importing) "Importazione…" else "Importa modello dal telefono")
                    }
                    Text(
                        when {
                            !licenseAccepted -> "Prima di importare o usare il modello, leggi e accetta i termini."
                            modelInstalled && availability.isReady -> "Modello installato e pronto per l'inferenza locale."
                            modelInstalled -> availability.detail
                            else -> "Modello non ancora installato. Scaricalo dalla pagina ufficiale dopo aver accettato le condizioni su Hugging Face."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (licenseAccepted && modelInstalled && availability.isReady)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    modelMessage?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            }

            messages.lastOrNull { it.pending }?.let { pendingLine ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Richiesta non completata", style = MaterialTheme.typography.titleSmall)
                        Text("L'app è stata chiusa o l'inferenza si è interrotta prima della risposta. Puoi riprovare senza reinserire il messaggio.")
                        OutlinedButton(
                            enabled = availability.isReady && licenseAccepted && !generating,
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
                                        val streamingId = Long.MIN_VALUE
                                        messages.removeAll { it.id == streamingId }
                                        messages.add(ChatLine(streamingId, "", "assistant"))
                                        val result = engine.generate(
                                            artifact.spec,
                                            InferenceRequest(prompt = retryPrompt),
                                            onPartialText = { partial ->
                                                val streamingIndex = messages.indexOfFirst { it.id == streamingId }
                                                if (streamingIndex >= 0) {
                                                    messages[streamingIndex] = ChatLine(streamingId, partial, "assistant")
                                                }
                                            }
                                        )
                                        messages.removeAll { it.id == streamingId }
                                        val pendingIndex = messages.indexOfFirst { it.id == pendingLine.id }
                                        if (pendingIndex >= 0) messages[pendingIndex] = pendingLine.copy(pending = false)
                                        messages.add(ChatLine(System.nanoTime(), result.text, "assistant"))
                                        historyStore.save(messages.toList())
                                        storageError = null
                                    } catch (error: CancellationException) {
                                        messages.removeAll { it.id == Long.MIN_VALUE }
                                        throw error
                                    } catch (error: OutOfMemoryError) {
                                        messages.removeAll { it.id == Long.MIN_VALUE }
                                        runCatching { engine.close() }
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
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(messages, key = { it.id }) { line ->
                        Surface(
                            color = if (line.role == "assistant")
                                MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    if (line.role == "assistant") "SuperIA" else "Tu",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                val expanded = remember(line.id) { mutableStateOf(false) }
                                Text(
                                    line.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = if (expanded.value) Int.MAX_VALUE else 16,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.clickable { expanded.value = !expanded.value }
                                )
                            }
                        }
                    }
                }
                OutlinedButton(
                    enabled = !generating,
                    onClick = {
                        runCatching { historyStore.clear() }
                            .onSuccess {
                                messages.clear()
                                storageError = null
                            }
                            .onFailure { storageError = it.message ?: "Errore nella cancellazione" }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Cancella cronologia") }
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedButton(
                    enabled = !importingDocument && !generating,
                    onClick = { documentPicker.launch(arrayOf("application/pdf", "text/plain", "text/csv", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")) }
                ) { Text(if (importingDocument) "…" else "+") }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Scrivi a SuperIA…") },
                    singleLine = true
                )
                Button(
                    enabled = draft.isNotBlank() && availability.isReady && licenseAccepted && !generating && messages.none { it.pending },
                    onClick = {
                        val request = draft.trim()
                        if (request.isNotEmpty()) {
                            // Capture history before appending the new pending user turn.
                            val contextHistory = messages.toList()
                            val userLine = ChatLine(System.nanoTime(), request, "user", pending = true)
                            messages.add(userLine)
                            val saved = runCatching { historyStore.save(messages.toList()) }
                            if (saved.isFailure) {
                                messages.remove(userLine)
                                storageError = saved.exceptionOrNull()?.message ?: "Errore nel salvataggio"
                                return@Button
                            }
                            storageError = null
                            draft = ""

                            scope.launch {
                                generating = true
                                generationError = null
                                try {
                                    val promptWithHistory = buildRequestPrompt(
                                        contextHistory,
                                        request,
                                        documentContext
                                    )
                                    val streamingId = Long.MIN_VALUE
                                    messages.removeAll { it.id == streamingId }
                                    messages.add(ChatLine(streamingId, "", "assistant"))
                                    val result = engine.generate(
                                        artifact.spec,
                                        InferenceRequest(prompt = promptWithHistory),
                                        onPartialText = { partial ->
                                            val streamingIndex = messages.indexOfFirst { it.id == streamingId }
                                            if (streamingIndex >= 0) {
                                                messages[streamingIndex] = ChatLine(streamingId, partial, "assistant")
                                            }
                                        }
                                    )
                                    messages.removeAll { it.id == streamingId }
                                    val pendingIndex = messages.indexOfFirst { it.id == userLine.id }
                                    if (pendingIndex >= 0) messages[pendingIndex] = userLine.copy(pending = false)
                                    messages.add(ChatLine(System.nanoTime(), result.text, "assistant"))
                                    historyStore.save(messages.toList())
                                } catch (error: CancellationException) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    throw error
                                } catch (error: OutOfMemoryError) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    runCatching { engine.close() }
                                    generationError = "Memoria insufficiente durante l'inferenza. Chiudi le altre app e riprova."
                                } catch (error: Exception) {
                                    messages.removeAll { it.id == Long.MIN_VALUE }
                                    generationError = error.message ?: "Errore durante l'inferenza locale."
                                } finally {
                                    generating = false
                                }
                            }
                        }
                    }
                ) { Text(if (generating) "Sto pensando…" else "Invia") }
            }
            storageError?.let {
                Text("Problema con la cronologia protetta: $it", color = MaterialTheme.colorScheme.error)
            }
            generationError?.let {
                Text("Errore AI: $it", color = MaterialTheme.colorScheme.error)
            }
            Text(
                when {
                    !licenseAccepted -> "Per inviare, accetta prima i termini della licenza del modello nelle Impostazioni."
                    !availability.isReady -> availability.detail
                    else -> "Inferenza locale tramite LiteRT-LM su CPU. Nessun fallback cloud. Le prestazioni dipendono dal modello, dalla RAM disponibile e dalla temperatura del dispositivo."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
