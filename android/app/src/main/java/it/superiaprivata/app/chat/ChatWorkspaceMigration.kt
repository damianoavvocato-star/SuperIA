package it.superiaprivata.app.chat

import android.content.Context

/**
 * One-time, restart-safe migration of the pre-multi-chat conversation into a
 * stable workspace. The source archive is retained as a recovery copy.
 */
internal object ChatWorkspaceMigration {
    private const val PREFERENCES = "superia_workspace_migration"
    private const val COMPLETE_KEY = "legacy_workspace_archive_v1"
    private val lock = Any()

    fun migrate(context: Context): ChatWorkspace = synchronized(lock) {
        val appContext = context.applicationContext
        val workspaces = ChatWorkspaceStore(appContext)
        val legacyWorkspace = workspaces.ensureLegacyWorkspace()
        val preferences = appContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

        if (preferences.getBoolean(COMPLETE_KEY, false)) {
            return@synchronized legacyWorkspace
        }

        // Copy encrypted bytes, then decrypt/read back both destination archives.
        // No legacy file or document is removed by this operation.
        ChatHistoryStore(appContext).migrateToWorkspace(legacyWorkspace.id)

        check(preferences.edit().putBoolean(COMPLETE_KEY, true).commit()) {
            "Impossibile registrare il completamento della migrazione multi-chat."
        }
        check(preferences.getBoolean(COMPLETE_KEY, false)) {
            "Il marker di migrazione non è persistente."
        }
        legacyWorkspace
    }
}
