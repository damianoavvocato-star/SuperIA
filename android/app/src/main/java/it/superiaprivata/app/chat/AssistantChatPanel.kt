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
import androidx.compose.material3.AlertDialog
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
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Composable
fun AssistantChatPanel(
    documentContext: String? = null,
    documentContextWorkspaceId: String? = null,
    modifier: Modifier = Modifier,
    onWorkspaceChanged: (String) -> Unit = {},
    onWorkspaceDeleted: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val migrationResult = remember(context) {
        runCatching { ChatWorkspaceMigration.migrate(context) }
    }
    val workspaceStore = remember(context) { ChatWorkspaceStore(context) }
    val workspaceSelection = remember(context) {
        context.getSharedPreferences("superia_workspace_selection", android.content.Context.MODE_PRIVATE)
    }
    var selectedWorkspaceId by rememberSaveable {
        mutableStateOf(workspaceSelection.getString("active_workspace_id", LEGACY_WORKSPACE_ID) ?: LEGACY_WORKSPACE_ID)
    }
    var workspaces by remember(workspaceStore, migrationResult) {
        mutableStateOf(runCatching { workspaceStore.list(includeArchived = true) }.getOrDefault(emptyList()))
    }
    var workspaceTitleDraft by rememberSaveable { mutableStateOf("") }
    var renameWorkspace by rememberSaveable { mutableStateOf(false) }
    var showArchivedWorkspaces by rememberSaveable { mutableStateOf(false) }
    var confirmWorkspaceDeletion by rememberSaveable { mutableStateOf(false) }
    var deletingWorkspace by remember { mutableStateOf(false) }
    LaunchedEffect(selectedWorkspaceId) {
        workspaceSelection.edit().putString("active_workspace_id", selectedWorkspaceId).apply()
        onWorkspaceChanged(selectedWorkspaceId)
    }
    LaunchedEffect(workspaces) {
        if (workspaces.isNotEmpty() && workspaces.none { it.id == selectedWorkspaceId }) {
            selectedWorkspaceId = workspaces.firstOrNull { !it.archived }?.id
                ?: workspaces.first().id
        }
    }
    val activeWorkspace = workspaces.firstOrNull { it.id == selectedWorkspaceId }
    val historyStore = remember(context, selectedWorkspaceId) {
        ChatHistoryStore(context, selectedWorkspaceId)
    }
    val initialHistory = remember(historyStore, migrationResult) {
        migrationResult.fold(
            onSuccess = { runCatching { historyStore.load() } },
            onFailure = { Result.failure(it) }
        )
    }
    val messages = remember(historyStore, initialHistory) {
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
    val initialProjectMemory = remember(historyStore) {
        runCatching { historyStore.loadProjectMemory() }
    }
    var projectMemory by remember(historyStore, initialProjectMemory) {
        mutableStateOf(initialProjectMemory.getOrDefault(""))
    }
    var memorySaved by remember { mutableStateOf(false) }
    var savingMemory by remember { mutableStateOf(false) }
    var memoryError by remember(historyStore, initialProjectMemory) {
        mutableStateOf(initialProjectMemory.exceptionOrNull()?.message)
    }
    var storageError by remember { mutableStateOf(initialHistory.exceptionOrNull()?.message) }
    var exportStatus by remember { mutableStateOf<String?>(null) }
    var generationError by remember { mutableStateOf<String?>(null) }
    var showSettings by rememberSaveable { mutableStateOf(false) }
    var attachedDocumentContext by remember { mutableStateOf<String?>(null) }
    var attachmentStatus by remember { mutableStateOf<String?>(null) }
    var importingDocument by remember { mutableStateOf(false) }
    val onlineConfigured = pairedEndpoint.isNotBlank() && backendToken.isNotBlank()
    val workspaceDocumentContext = documentContext.takeIf {
        documentContextWorkspaceId == selectedWorkspaceId
    }
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
            LocalDocumentRepository(context, selectedWorkspaceId).list()
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
                    val repository = LocalDocumentRepository(context, selectedWorkspaceId)
                    val stored = withContext(Dispatchers.IO) {
                        repository.import(uri, DocumentTextExtractor(context))
                    }
                    attachedDocumentContext = "Documento allegato alla conversazione: ${stored.title}\nContenuto estratto:\n${stored.text.take(6000)}"
                    attachmentStatus = "Allegato pronto: ${stored.title}. Il file è conservato cifrato in questo progetto."
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
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("SuperIA", style = MaterialTheme.typography.titleLarge)
                    Text(
                        activeWorkspace?.title ?: "Conversazione",
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    if (messages.isNotEmpty()) {
                        TextButton(
                            enabled = !generating,
                            onClick = { exportConversationPicker.launch("superia-conversazione.txt") }
                        ) { Text("Esporta") }
                        TextButton(
                            enabled = !generating,
                            onClick = {
                                scope.launch {
                                    try {
                                        withContext(Dispatchers.IO) { historyStore.clear() }
                                        messages.clear()
                                        storageError = null
                                        exportStatus = null
                                    } catch (error: Exception) {
                                        storageError = error.message ?: "Errore nella cancellazione"
                                    }
                                }
                            }
                        ) { Text("Svuota") }
                    }
                    OutlinedButton(onClick = { showSettings = !showSettings }) {
                        Text(if (showSettings) "Chiudi" else "Impostazioni")
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                OutlinedButton(
                    enabled = !generating && !importingDocument,
                    onClick = {
                        runCatching {
                            val created = workspaceStore.create("Nuova chat", ChatWorkspace.Kind.CHAT)
                            workspaces = workspaceStore.list(includeArchived = true)
                            selectedWorkspaceId = created.id
                            attachedDocumentContext = null
                            attachmentStatus = null
                            generationError = null
                            exportStatus = null
                            draft = ""
                        }.onFailure { storageError = it.message ?: "Creazione chat non riuscita." }
                    }
                ) { Text("＋ Nuova chat") }
                OutlinedButton(
                    enabled = !generating && !importingDocument,
                    onClick = {
                        runCatching {
                            val created = workspaceStore.create("Nuovo progetto", ChatWorkspace.Kind.PROJECT)
                            workspaces = workspaceStore.list(includeArchived = true)
                            selectedWorkspaceId = created.id
                            attachedDocumentContext = null
                            attachmentStatus = null
                            generationError = null
                            exportStatus = null
                            draft = ""
                        }.onFailure { storageError = it.message ?: "Creazione progetto non riuscita." }
                    }
                ) { Text("＋ Progetto") }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    enabled = !generating && !importingDocument,
                    onClick = { showArchivedWorkspaces = !showArchivedWorkspaces }
                ) { Text(if (showArchivedWorkspaces) "Mostra attive" else "Archiviate") }
            }
            val visibleWorkspaces = workspaces.filter { it.archived == showArchivedWorkspaces }
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(visibleWorkspaces, key = { it.id }) { workspace ->
                    OutlinedButton(
                        enabled = !generating && !importingDocument && workspace.id != selectedWorkspaceId,
                        onClick = {
                            selectedWorkspaceId = workspace.id
                            attachedDocumentContext = null
                            attachmentStatus = null
                            generationError = null
                            exportStatus = null
                            draft = ""
                            storageError = null
                            memorySaved = false
                            renameWorkspace = false
                        }
                    ) {
                        Text(
                            (if (workspace.kind == ChatWorkspace.Kind.PROJECT) "▣ " else "▤ ") +
                                workspace.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                TextButton(
                    enabled = !generating && activeWorkspace != null,
                    onClick = {
                        workspaceTitleDraft = activeWorkspace?.title.orEmpty()
                        renameWorkspace = !renameWorkspace
                    }
                ) { Text(if (renameWorkspace) "Annulla" else "Rinomina") }
                if (activeWorkspace?.archived == true) {
                    TextButton(
                        enabled = !generating && !importingDocument,
                        onClick = {
                            runCatching {
                                workspaceStore.setArchived(selectedWorkspaceId, false)
                                workspaces = workspaceStore.list(includeArchived = true)
                                showArchivedWorkspaces = false
                            }.onFailure { storageError = it.message ?: "Ripristino chat non riuscito." }
                        }
                    ) { Text("Ripristina") }
                } else {
                    TextButton(
                        enabled = !generating && !importingDocument &&
                            activeWorkspace != null && selectedWorkspaceId != LEGACY_WORKSPACE_ID,
                        onClick = {
                            runCatching {
                                workspaceStore.setArchived(selectedWorkspaceId, true)
                                workspaces = workspaceStore.list(includeArchived = true)
                                val next = workspaces.firstOrNull { !it.archived }
                                if (next != null) selectedWorkspaceId = next.id
                                attachedDocumentContext = null
                                attachmentStatus = null
                                renameWorkspace = false
                            }.onFailure { storageError = it.message ?: "Archiviazione non riuscita." }
                        }
                    ) { Text("Archivia") }
                }
                TextButton(
                    enabled = !generating && !importingDocument && !savingMemory &&
                        !deletingWorkspace && activeWorkspace != null,
                    onClick = { confirmWorkspaceDeletion = true }
                ) { Text("Elimina") }
            }
            if (renameWorkspace) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = workspaceTitleDraft,
                        onValueChange = { workspaceTitleDraft = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Nome chat/progetto") },
                        singleLine = true
                    )
                    OutlinedButton(
                        enabled = !generating && workspaceTitleDraft.isNotBlank(),
                        onClick = {
                            runCatching {
                                workspaceStore.rename(selectedWorkspaceId, workspaceTitleDraft)
                                workspaces = workspaceStore.list(includeArchived = true)
                                renameWorkspace = false
                            }.onFailure { storageError = it.message ?: "Rinomina non riuscita." }
                        }
                    ) { Text("Salva") }
                }
            }
            if (confirmWorkspaceDeletion) {
                AlertDialog(
                    onDismissRequest = {
                        if (!deletingWorkspace) confirmWorkspaceDeletion = false
                    },
                    title = { Text("Eliminare definitivamente?") },
                    text = {
                        Text(
                            "Verranno eliminati cronologia, memoria e documenti cifrati di «${activeWorkspace?.title ?: "questa chat"}». " +
                                "L'operazione non si può annullare. File esportati in altre app e contenuti esterni non verranno toccati."
                        )
                    },
                    confirmButton = {
                        TextButton(
                            enabled = !deletingWorkspace,
                            onClick = {
                                val deletingId = selectedWorkspaceId
                                deletingWorkspace = true
                                scope.launch {
                                    try {
                                        val result = withContext(Dispatchers.IO) {
                                            ChatWorkspaceDeletion.delete(context, deletingId)
                                        }
                                        onWorkspaceDeleted(deletingId)
                                        selectedWorkspaceId = result.activeWorkspaceId
                                        workspaces = workspaceStore.list(includeArchived = true)
                                        attachedDocumentContext = null
                                        attachmentStatus = null
                                        generationError = null
                                        storageError = null
                                        exportStatus = null
                                        draft = ""
                                        renameWorkspace = false
                                        showArchivedWorkspaces = false
                                        confirmWorkspaceDeletion = false
                                    } catch (error: Exception) {
                                        // A failed cleanup may already have created the fallback workspace.
                                        // Refresh the index so the user can see it and retry safely.
                                        workspaces = runCatching {
                                            workspaceStore.list(includeArchived = true)
                                        }.getOrDefault(workspaces)
                                        storageError = error.message ?: "Eliminazione non completata. I dati non rimossi restano associati alla chat."
                                    } finally {
                                        deletingWorkspace = false
                                    }
                                }
                            }
                        ) { Text(if (deletingWorkspace) "Eliminazione…" else "Elimina definitivamente") }
                    },
                    dismissButton = {
                        TextButton(
                            enabled = !deletingWorkspace,
                            onClick = { confirmWorkspaceDeletion = false }
                        ) { Text("Annulla") }
                    }
                )
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
            Text("Ogni chat e progetto conserva cronologia, memoria e documenti separati e cifrati sul dispositivo. Per rispondere, la richiesta viene inviata al backend HTTPS configurato.", style = MaterialTheme.typography.bodyMedium)
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
                                    val streamPersister = StreamHistoryPersister(historyStore, scope)
                                    try {
                                        val pendingIndexBefore = messages.indexOfFirst { it.id == pendingLine.id }
                                        val staleAssistant = messages.drop((pendingIndexBefore + 1).coerceAtLeast(0))
                                            .lastOrNull { it.role == "assistant" }
                                        if (staleAssistant?.text.isNullOrBlank()) {
                                            staleAssistant?.let { messages.remove(it) }
                                        }
                                        val retryRequestId = requestIdForRetry(pendingLine.requestId)
                                        val retryMessages = pendingLine.requestMessages ?: buildRequestMessages(
                                            messages.filterNot { it.pending || it.id == staleAssistant?.id },
                                            pendingLine.text,
                                            workspaceDocumentContext
                                        )
                                        val pendingPosition = messages.indexOfFirst { it.id == pendingLine.id }
                                        if (pendingPosition >= 0) {
                                            messages[pendingPosition] = pendingLine.copy(
                                                requestId = retryRequestId,
                                                requestMessages = retryMessages
                                            )
                                            // Freeze the exact provider payload before retrying. Reusing the
                                            // same request ID with changed memory/documents would conflict server-side.
                                            persistChatHistory(historyStore, messages)
                                        }
                                        val assistantLine = staleAssistant?.takeIf { it.text.isNotBlank() }
                                            ?: ChatLine(nextChatLineId(messages.map { it.id }, System.nanoTime()), "", "assistant").also { messages.add(it) }
                                        // Persist the assistant placeholder before opening the network stream.
                                        // Recovery after process death then sees a consistent transcript.
                                        persistChatHistory(historyStore, messages)
                                        var receivedNewOutput = false
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
                                        if (pendingIndex >= 0) messages[pendingIndex] = messages[pendingIndex].copy(pending = false, requestMessages = null)
                                        streamPersister.flush(messages.toList())
                                        storageError = null
                                    } catch (error: CancellationException) {
                                        runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
                                        throw error
                                    } catch (error: OutOfMemoryError) {
                                        generationError = "Memoria insufficiente durante la preparazione della richiesta. Riprova."
                                        runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
                                    } catch (error: Exception) {
                                        generationError = error.message ?: "Ripresa della richiesta non riuscita."
                                        runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
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
                            draft = ""
                            scope.launch {
                                generating = true
                                generationError = null
                                val streamPersister = StreamHistoryPersister(historyStore, scope)
                                try {
                                    persistChatHistory(historyStore, messages)
                                    storageError = null
                                    val messagesWithHistory = buildRequestMessages(contextHistory, request, workspaceDocumentContext)
                                    val payloadPendingIndex = messages.indexOfFirst { it.id == userLine.id }
                                    if (payloadPendingIndex >= 0) {
                                        messages[payloadPendingIndex] = userLine.copy(requestMessages = messagesWithHistory)
                                        // Store the exact encrypted request before any provider/network activity.
                                        persistChatHistory(historyStore, messages)
                                    }
                                    val assistantLine = ChatLine(nextChatLineId(messages.map { it.id }, System.nanoTime()), "", "assistant")
                                    messages.add(assistantLine)
                                    // Save the user pending state and assistant placeholder before network I/O.
                                    persistChatHistory(historyStore, messages)
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
                                    if (pendingIndex >= 0) messages[pendingIndex] = userLine.copy(pending = false, requestMessages = null)
                                    streamPersister.flush(messages.toList())
                                } catch (error: CancellationException) {
                                    runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
                                    throw error
                                } catch (error: OutOfMemoryError) {
                                    generationError = "Memoria insufficiente durante l'inferenza. Chiudi le altre app e riprova."
                                    runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
                                } catch (error: Exception) {
                                    generationError = error.message ?: "Errore durante la richiesta online."
                                    runCatching { withContext(NonCancellable) { streamPersister.flush(messages) } }
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
 * Copy Compose state on the caller context before switching to IO. Reading a
 * SnapshotStateList from a worker thread can race with UI mutations.
 */
private suspend fun persistChatHistory(store: ChatHistoryStore, messages: List<ChatLine>) {
    val snapshot = messages.toList()
    withContext(Dispatchers.IO) { store.save(snapshot) }
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
