package it.superiaprivata.app.chat

import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import it.superiaprivata.app.documents.DocumentTextExtractor
import it.superiaprivata.app.documents.LocalDocumentContextBuilder
import it.superiaprivata.app.documents.LocalDocumentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Composable
fun AssistantChatPanel(documentContext: String? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyStore = remember(context) { ChatHistoryStore(context) }
    val initialHistory = remember(historyStore) {
        runCatching {
            historyStore.clearLegacyLocalHistoryOnce()
            historyStore.load()
        }
    }
    val messages = remember(initialHistory) {
        mutableStateListOf<ChatLine>().apply {
            addAll(initialHistory.getOrDefault(emptyList()))
        }
    }
    val connectionStore = remember(context) { OnlineConnectionStore(context) }
    val savedConnection = remember(connectionStore) { connectionStore.load() }
    var backendEndpoint by remember { mutableStateOf(savedConnection.endpoint) }
    var pairedEndpoint by remember { mutableStateOf(savedConnection.endpoint) }
    var backendToken by remember { mutableStateOf(savedConnection.token) }
    var pairingCode by rememberSaveable { mutableStateOf("") }
    var connectionStatus by remember { mutableStateOf(if (savedConnection.isConfigured) "Configurazione backend caricata in modo protetto." else "Collega un backend HTTPS per iniziare.") }
    var checkingBackend by remember { mutableStateOf(false) }
    var exitDiagnostic by remember(context) { mutableStateOf(RuntimeExitDiagnostics.read(context)) }
    var generating by remember { mutableStateOf(false) }
    var draft by remember { mutableStateOf("") }
    var projectMemory by remember(historyStore) { mutableStateOf(runCatching { historyStore.loadProjectMemory() }.getOrDefault("")) }
    var memorySaved by remember { mutableStateOf(false) }
    var savingMemory by remember { mutableStateOf(false) }
    var memoryError by remember { mutableStateOf<String?>(null) }
    var storageError by remember { mutableStateOf(initialHistory.exceptionOrNull()?.message) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var generationError by remember { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var attachedDocumentContext by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    var importingDocument by remember { mutableStateOf(false) }
    val onlineConfigured = pairedEndpoint.isNotBlank() && backendToken.isNotBlank()
    val messageListState = rememberLazyListState()

    LaunchedEffect(messages.size, messages.lastOrNull()?.text) {
        if (messages.isNotEmpty()) messageListState.animateScrollToItem(messages.lastIndex)
    }

    suspend fun buildRequestMessages(
        history: List<ChatLine>,
        request: String,
        explicitlySelectedContext: String?
    ): List<ChatPromptMessage> {
        val storedDocuments = withContext(Dispatchers.IO) {
            LocalDocumentRepository(context).list()
        }
        val combinedContext = listOfNotNull(explicitlySelectedContext, attachedDocumentContext)
            .filter { it.isNotBlank() }.joinToString("\n\n")
        val retrievedContext = withContext(Dispatchers.Default) {
            LocalDocumentContextBuilder.build(request, storedDocuments, combinedContext.ifBlank { null })
        }
        return ChatPromptBuilder.buildMessages(
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

    val exportConversationPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            val snapshot = messages.toList()
            scope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        val output = context.contentResolver.openOutputStream(uri)
                            ?: error("Impossibile aprire il file di destinazione.")
                        output.bufferedWriter(Charsets.UTF_8).use { writer ->
                            snapshot.forEach { line ->
                                writer.append(if (line.role == "assistant") "[SuperIA]" else "[Utente]")
                                writer.append("\n")
                                writer.append(line.text)
                                writer.append("\n\n")
                            }
                        }
                    }
                    exportStatus = "Cronologia esportata nella posizione scelta. La copia salvata è in chiaro."
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Exception) {
                    exportStatus = error.message ?: "Esportazione cronologia non riuscita."
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
                            onClick = { exportConversationPicker.launch("superia-conversazione.txt") }
                        ) { Text("Esporta") }
                        TextButton(
                            enabled = !generating,
                            onClick = {
                                runCatching { historyStore.clear() }
                                    .onSuccess { messages.clear(); storageError = null; exportStatus = null }
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
                value = pairingCode,
                onValueChange = { pairingCode = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Codice di associazione") },
                placeholder = { Text("Codice fornito per collegare questo dispositivo") },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = !generating && !checkingBackend && backendEndpoint.startsWith("https://") && pairingCode.length >= 16,
                        onClick = {
                            scope.launch {
                                checkingBackend = true
                                connectionStatus = "Associazione sicura del dispositivo in corso…"
                                try {
                                    val paired = OnlineAssistantClient.pair(
                                        backendEndpoint.trim(),
                                        pairingCode,
                                        android.os.Build.MODEL ?: "Dispositivo Android"
                                    )
                                    connectionStore.save(OnlineConnectionConfig(backendEndpoint.trim(), paired.accessToken, paired.deviceId))
                                    pairedEndpoint = backendEndpoint.trim()
                                    backendToken = paired.accessToken
                                    pairingCode = ""
                                    connectionStatus = "Dispositivo associato (ID ${paired.deviceId}). Token individuale salvato cifrato; scadenza tra 90 giorni."
                                    generationError = null
                                } catch (error: Exception) {
                                    connectionStatus = error.message ?: "Associazione non riuscita."
                                } finally {
                                    checkingBackend = false
                                }
                            }
                        }
                    ) { Text(if (checkingBackend) "Associazione…" else "Associa dispositivo") }
                    OutlinedButton(
                        enabled = !generating && !checkingBackend && onlineConfigured,
                        onClick = {
                            scope.launch {
                                checkingBackend = true
                                connectionStatus = "Verifica del backend associato in corso…"
                                try {
                                    connectionStatus = OnlineAssistantClient.checkOnlineReadiness(
                                        OnlineConnectionConfig(pairedEndpoint, backendToken)
                                    )
                                } catch (error: Exception) {
                                    connectionStatus = error.message ?: "Verifica del backend non riuscita."
                                } finally {
                                    checkingBackend = false
                                }
                            }
                        }
                    ) { Text(if (checkingBackend) "Verifica…" else "Verifica backend") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = !generating && !checkingBackend && onlineConfigured,
                        onClick = {
                            scope.launch {
                                checkingBackend = true
                                connectionStatus = "Rinnovo sicuro del token in corso…"
                                try {
                                    val rotated = OnlineAssistantClient.rotate(
                                        OnlineConnectionConfig(pairedEndpoint, backendToken, savedConnection.deviceId)
                                    )
                                    val updated = OnlineConnectionConfig(pairedEndpoint, rotated.accessToken, rotated.deviceId)
                                    connectionStore.save(updated)
                                    backendToken = rotated.accessToken
                                    connectionStatus = "Token rinnovato. Il token precedente è stato invalidato."
                                } catch (error: Exception) {
                                    connectionStatus = error.message ?: "Rinnovo del token non riuscito."
                                } finally {
                                    checkingBackend = false
                                }
                            }
                        }
                    ) { Text(if (checkingBackend) "Rinnovo…" else "Rinnova token") }
                    TextButton(
                        enabled = !generating && !checkingBackend && onlineConfigured,
                        onClick = {
                            scope.launch {
                                checkingBackend = true
                                connectionStatus = "Revoca del dispositivo sul server in corso…"
                                try {
                                    OnlineAssistantClient.revoke(
                                        OnlineConnectionConfig(pairedEndpoint, backendToken, savedConnection.deviceId)
                                    )
                                    connectionStore.clear()
                                    backendToken = ""
                                    pairedEndpoint = ""
                                    connectionStatus = "Dispositivo revocato sul server e disconnesso."
                                } catch (error: Exception) {
                                    connectionStatus = error.message ?: "Revoca non riuscita; credenziali locali conservate."
                                } finally {
                                    checkingBackend = false
                                }
                            }
                        }
                    ) { Text("Revoca e disconnetti") }
                }
            }
            Text(connectionStatus, style = MaterialTheme.typography.bodySmall)
            Text(
                "La chiave viene conservata cifrata con Android Keystore. Usa soltanto un backend HTTPS fidato. Non inserire qui le chiavi dei fornitori IA: sono segreti che devono restare sul server.",
                style = MaterialTheme.typography.bodySmall
            )

            Text("Memoria di progetto", style = MaterialTheme.typography.titleSmall)
            Text(
                "Testo modificabile che SuperIA può usare come contesto nelle richieste future. Resta cifrato sul dispositivo e viene inviato solo come parte del contesto della chat.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedTextField(
                value = projectMemory,
                onValueChange = { projectMemory = it; memorySaved = false },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Memoria personale di progetto") },
                minLines = 3,
                maxLines = 5
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    enabled = !savingMemory && !generating,
                    onClick = {
                        scope.launch {
                            savingMemory = true
                            memoryError = null
                            try {
                                withContext(Dispatchers.IO) { historyStore.saveProjectMemory(projectMemory) }
                                memorySaved = true
                            } catch (error: Exception) {
                                memoryError = error.message ?: "Salvataggio memoria non riuscito."
                            } finally {
                                savingMemory = false
                            }
                        }
                    }
                ) { Text(if (savingMemory) "Salvataggio…" else "Salva memoria cifrata") }
                TextButton(
                    enabled = !savingMemory && !generating && projectMemory.isNotBlank(),
                    onClick = {
                        scope.launch {
                            savingMemory = true
                            try {
                                withContext(Dispatchers.IO) { historyStore.clearProjectMemory() }
                                projectMemory = ""
                                memorySaved = false
                                memoryError = null
                            } catch (error: Exception) {
                                memoryError = error.message ?: "Cancellazione memoria non riuscita."
                            } finally {
                                savingMemory = false
                            }
                        }
                    }
                ) { Text("Cancella memoria") }
            }
            if (memorySaved) Text("Memoria salvata cifrata sul dispositivo.", style = MaterialTheme.typography.bodySmall)
            memoryError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }

            }

            messages.lastOrNull { it.pending }?.let { pendingLine ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Richiesta non completata", style = MaterialTheme.typography.titleSmall)
                        Text("L'app è stata chiusa o la richiesta online si è interrotta prima della risposta. Puoi riprovare senza reinserire il messaggio.")
                        OutlinedButton(
                            enabled = onlineConfigured && initialHistory.isSuccess && !generating,
                            onClick = {
                                scope.launch {
                                    generating = true
                                    generationError = null
                                    try {
                                        val pendingIndexBefore = messages.indexOfFirst { it.id == pendingLine.id }
                                        val staleAssistant = messages.drop((pendingIndexBefore + 1).coerceAtLeast(0))
                                            .lastOrNull { it.role == "assistant" }
                                        if (staleAssistant?.text.isNullOrBlank()) {
                                            staleAssistant?.let { messages.remove(it) }
                                        }
                                        val retryRequestId = pendingLine.requestId ?: java.util.UUID.randomUUID().toString()
                                        val pendingPosition = messages.indexOfFirst { it.id == pendingLine.id }
                                        if (pendingPosition >= 0) {
                                            messages[pendingPosition] = pendingLine.copy(requestId = retryRequestId)
                                            historyStore.save(messages.toList())
                                        }
                                        val contextHistory = messages.filterNot { it.pending || it.id == staleAssistant?.id }
                                        val retryMessages = buildRequestMessages(
                                            contextHistory,
                                            pendingLine.text,
                                            documentContext
                                        )
                                        val assistantLine = staleAssistant?.takeIf { it.text.isNotBlank() }
                                            ?: ChatLine(nextChatLineId(messages.map { it.id }, System.nanoTime()), "", "assistant").also { messages.add(it) }
                                        var receivedNewOutput = false
                                        val streamPersister = StreamHistoryPersister(historyStore, scope)
                                        OnlineAssistantClient.stream(
                                            OnlineConnectionConfig(pairedEndpoint, backendToken),
                                            retryMessages,
                                            requestId = retryRequestId
                                        ) { piece ->
                                            val index = messages.indexOfFirst { it.id == assistantLine.id }
                                            if (index >= 0) {
                                                val previous = messages[index].text
                                                messages[index] = messages[index].copy(
                                                    text = if (receivedNewOutput) previous + piece else piece
                                                )
                                                receivedNewOutput = true
                                                streamPersister.onDelta(messages.toList())
                                            }
                                        }
                                        val pendingIndex = messages.indexOfFirst { it.id == pendingLine.id }
                                        if (pendingIndex >= 0) messages[pendingIndex] = messages[pendingIndex].copy(pending = false)
                                        streamPersister.flush(messages.toList())
                                        storageError = null
                                    } catch (error: CancellationException) {
                                        runCatching { historyStore.save(messages.toList()) }
                                        throw error
                                    } catch (error: OutOfMemoryError) {
                                        generationError = "Memoria insufficiente durante la preparazione della richiesta. Riprova."
                                        runCatching { historyStore.save(messages.toList()) }
                                    } catch (error: Exception) {
                                        generationError = error.message ?: "Ripresa della richiesta non riuscita."
                                        runCatching { historyStore.save(messages.toList()) }
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
                    enabled = draft.isNotBlank() && onlineConfigured && initialHistory.isSuccess && !generating && messages.none { it.pending },
                    onClick = {
                        val request = draft.trim()
                        if (request.isNotEmpty()) {
                            val contextHistory = messages.toList()
                            val userLine = ChatLine(
                                nextChatLineId(messages.map { it.id }, System.nanoTime()), request, "user",
                                pending = true,
                                requestId = java.util.UUID.randomUUID().toString()
                            )
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
                                    val messagesWithHistory = buildRequestMessages(contextHistory, request, documentContext)
                                    val assistantLine = ChatLine(nextChatLineId(messages.map { it.id }, System.nanoTime()), "", "assistant")
                                    messages.add(assistantLine)
                                    val streamPersister = StreamHistoryPersister(historyStore, scope)
                                    OnlineAssistantClient.stream(
                                        OnlineConnectionConfig(pairedEndpoint, backendToken),
                                        messagesWithHistory,
                                        requestId = userLine.requestId ?: java.util.UUID.randomUUID().toString()
                                    ) { piece ->
                                        val index = messages.indexOfFirst { it.id == assistantLine.id }
                                        if (index >= 0) {
                                            messages[index] = messages[index].copy(text = messages[index].text + piece)
                                            streamPersister.onDelta(messages.toList())
                                        }
                                    }
                                    val pendingIndex = messages.indexOfFirst { it.id == userLine.id }
                                    if (pendingIndex >= 0) messages[pendingIndex] = userLine.copy(pending = false)
                                    streamPersister.flush(messages.toList())
                                } catch (error: CancellationException) {
                                    runCatching { historyStore.save(messages.toList()) }
                                    throw error
                                } catch (error: OutOfMemoryError) {
                                    generationError = "Memoria insufficiente durante l'inferenza. Chiudi le altre app e riprova."
                                    runCatching { historyStore.save(messages.toList()) }
                                } catch (error: Exception) {
                                    generationError = error.message ?: "Errore durante la richiesta online."
                                    runCatching { historyStore.save(messages.toList()) }
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
            exportStatus?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
            storageError?.let {
                Text("Problema con la cronologia protetta: $it", color = MaterialTheme.colorScheme.error)
            }
            generationError?.let {
                Text("Errore AI: $it", color = MaterialTheme.colorScheme.error)
            }
            if (!onlineConfigured) {
                Text(
                    "Configura un backend HTTPS e associa questo dispositivo per usare la chat online.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}


/**
 * Periodically snapshots streaming output without writing once per token.
 * Version checks prevent an older background write from overwriting a newer snapshot.
 */
private class StreamHistoryPersister(
    private val store: ChatHistoryStore,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    private val mutex = Mutex()
    private var lastSnapshotAt = 0L
    private var nextVersion = 0L
    private var persistedVersion = 0L

    fun onDelta(messages: List<ChatLine>) {
        val now = SystemClock.elapsedRealtime()
        if (now - lastSnapshotAt < 900L) return
        lastSnapshotAt = now
        schedule(messages.toList())
    }

    suspend fun flush(messages: List<ChatLine>) {
        val snapshot = messages.toList()
        val version = synchronized(this) { ++nextVersion }
        withContext(Dispatchers.IO) {
            mutex.withLock {
                if (version > persistedVersion) {
                    store.save(snapshot)
                    persistedVersion = version
                }
            }
        }
    }

    private fun schedule(snapshot: List<ChatLine>) {
        val version = synchronized(this) { ++nextVersion }
        scope.launch(Dispatchers.IO) {
            mutex.withLock {
                if (version > persistedVersion) {
                    try {
                        store.save(snapshot)
                        persistedVersion = version
                    } catch (_: Exception) {
                        // A later snapshot or the final flush will retry persistence.
                    }
                }
            }
        }
    }
}
