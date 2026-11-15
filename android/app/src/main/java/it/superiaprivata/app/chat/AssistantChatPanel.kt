package it.superiaprivata.app.chat

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import it.superiaprivata.app.device.DeviceProfileReader
import it.superiaprivata.app.inference.InferenceRequest
import it.superiaprivata.app.inference.LiteRtLmInferenceEngine
import it.superiaprivata.app.inference.LocalModelCatalog
import it.superiaprivata.app.inference.LocalModelInstaller
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun AssistantChatPanel(documentContext: String? = null) {
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
    var licenseAccepted by rememberSaveable { mutableStateOf(false) }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Parla con SuperIA", style = MaterialTheme.typography.titleLarge)
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
                            onCheckedChange = { licenseAccepted = it }
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
                                val retryPrompt = ChatPromptBuilder.build(
                                    contextHistory,
                                    pendingLine.text,
                                    documentContext = documentContext,
                                    memorySummary = projectMemory
                                )
                                scope.launch {
                                    generating = true
                                    generationError = null
                                    try {
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
                                        throw error
                                    } catch (error: Exception) {
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
                    modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
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
                                Text(line.text, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                OutlinedButton(
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
            }

            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Scrivi cosa vuoi che faccia…") },
                placeholder = { Text("Es. Riassumi questo documento") },
                minLines = 2,
                maxLines = 5
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    enabled = draft.isNotBlank() && availability.isReady && licenseAccepted && !generating && messages.none { it.pending },
                    onClick = {
                        val request = draft.trim()
                        if (request.isNotEmpty()) {
                            // The native LiteRT-LM conversation is in memory only.
                            // Rebuild a bounded context from the encrypted transcript.
                            val promptWithHistory = ChatPromptBuilder.build(messages.toList(), request, documentContext = documentContext, memorySummary = projectMemory)
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
                                    throw error
                                } catch (error: Exception) {
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
                "L'inferenza viene eseguita localmente via LiteRT-LM CPU. Nessun fallback cloud. La compatibilità e la velocità sui telefoni reali devono ancora essere verificate.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
