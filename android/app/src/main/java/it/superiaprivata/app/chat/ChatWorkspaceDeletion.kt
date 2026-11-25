package it.superiaprivata.app.chat

import android.content.Context
import it.superiaprivata.app.documents.LocalDocumentRepository

/**
 * Fail-closed deletion for one non-legacy workspace.
 *
 * The index entry is removed last. A failed cleanup leaves the workspace visible
 * so the operation can be inspected/retried; user-selected source files and
 * external exports are outside the app-private directories and are never touched.
 */
internal object ChatWorkspaceDeletion {
    private val lock = Any()

    data class Result(
        val deletedWorkspaceId: String,
        val activeWorkspaceId: String
    )

    fun delete(context: Context, workspaceId: String): Result = synchronized(lock) {
        require(workspaceId != LEGACY_WORKSPACE_ID) {
            "La conversazione precedente protetta non può essere eliminata."
        }
        val appContext = context.applicationContext
        val workspaceStore = ChatWorkspaceStore(appContext)
        val workspace = workspaceStore.list(includeArchived = true)
            .firstOrNull { it.id == workspaceId }
            ?: error("La chat selezionata non esiste.")

        // Preflight every encrypted source before removing any data. Corruption or
        // a missing Keystore key aborts before deletion starts.
        val history = ChatHistoryStore(appContext, workspaceId)
        history.load()
        history.loadProjectMemory()
        val documents = LocalDocumentRepository(appContext, workspaceId)
        documents.list()

        // Ensure there will still be an active destination after deleting the
        // selected workspace. Creating an empty fallback is non-destructive.
        val fallback = workspaceStore.list(includeArchived = false)
            .firstOrNull { it.id != workspaceId }
            ?: workspaceStore.create("Nuova chat", ChatWorkspace.Kind.CHAT)

        // Delete only app-owned UUID-scoped archives. If any step fails, keep the
        // index record so the partial cleanup remains visible and retryable.
        documents.deleteAllWorkspaceDocuments()
        history.deleteWorkspaceData()
        workspaceStore.removeAfterDataCleanup(workspaceId)

        Result(
            deletedWorkspaceId = workspace.id,
            activeWorkspaceId = fallback.id
        )
    }
}
