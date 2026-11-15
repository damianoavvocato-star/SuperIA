package it.superiaprivata.app.inference

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

data class ModelImportResult(
    val file: File,
    val sizeBytes: Long,
    val sha256: String
)

/**
 * Imports a user-selected model into app-private storage.
 * No external storage permission or network access is needed.
 */
class LocalModelInstaller(private val context: Context) {

    suspend fun importModel(
        uri: Uri,
        artifact: LocalModelCatalog.LocalArtifact = LocalModelCatalog.gemma3_1b_it_int4
    ): ModelImportResult = withContext(Dispatchers.IO) {
        val displayName = queryDisplayName(uri)
        require(displayName.endsWith(".litertlm", ignoreCase = true)) {
            "Seleziona un file con estensione .litertlm."
        }

        val directory = LocalModelCatalog.modelDirectory(context)
        val target = LocalModelCatalog.modelFile(context, artifact)
        val temporary = File(directory, "${artifact.fileName}.importing")
        val digest = MessageDigest.getInstance("SHA-256")
        var totalBytes = 0L

        try {
            val input = requireNotNull(context.contentResolver.openInputStream(uri)) {
                "Impossibile aprire il file selezionato."
            }
            input.use { source ->
                FileOutputStream(temporary).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    while (true) {
                        val read = source.read(buffer)
                        if (read < 0) break
                        totalBytes += read
                        require(totalBytes <= MAX_MODEL_BYTES) {
                            "Il file supera il limite di sicurezza di 2 GB."
                        }
                        digest.update(buffer, 0, read)
                        output.write(buffer, 0, read)
                    }
                    output.fd.sync()
                }
            }

            require(totalBytes > 0L) { "Il file selezionato è vuoto." }

            // Prefer an atomic replacement. If the filesystem does not support it,
            // use a backup/restore path so a failed replacement preserves the old model.
            try {
                Files.move(
                    temporary.toPath(),
                    target.toPath(),
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING
                )
            } catch (_: AtomicMoveNotSupportedException) {
                replaceWithBackup(temporary, target)
            } catch (_: UnsupportedOperationException) {
                replaceWithBackup(temporary, target)
            }

            ModelImportResult(
                file = target,
                sizeBytes = totalBytes,
                sha256 = digest.digest().joinToString("") { "%02x".format(it) }
            )
        } catch (error: Throwable) {
            temporary.delete()
            throw error
        }
    }


    private fun replaceWithBackup(temporary: File, target: File) {
        val backup = File(target.parentFile, "${target.name}.backup")

        // Recover a previous import interrupted after moving the old target aside.
        if (backup.exists() && !target.exists()) {
            Files.move(backup.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } else if (backup.exists()) {
            backup.delete()
        }

        val hadPreviousModel = target.exists()
        if (hadPreviousModel) {
            Files.move(target.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        try {
            Files.move(temporary.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
            backup.delete()
        } catch (failure: Throwable) {
            target.delete()
            if (backup.exists()) {
                try {
                    Files.move(backup.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING)
                } catch (restoreFailure: Throwable) {
                    failure.addSuppressed(restoreFailure)
                }
            }
            throw failure
        }
    }

    private fun queryDisplayName(uri: Uri): String {
        context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index).orEmpty()
            }
        }
        return uri.lastPathSegment.orEmpty()
    }

    private companion object {
        const val MAX_MODEL_BYTES = 2L * 1024L * 1024L * 1024L
    }
}
