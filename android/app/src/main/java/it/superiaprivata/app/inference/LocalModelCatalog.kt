package it.superiaprivata.app.inference

import android.content.Context
import java.io.File

/**
 * Reviewed starter catalog. Model weights are not bundled in the APK.
 * The user must obtain the artifact and accept its upstream terms separately.
 */
object LocalModelCatalog {
    const val GEMMA_LICENSE_ID = "gemma-terms-of-use"

    data class LocalArtifact(
        val spec: ModelSpec,
        val fileName: String,
        val approximateDownloadBytes: Long,
        val sourceUrl: String,
        val licenseUrl: String
    )

    val gemma3_1b_it_int4 = LocalArtifact(
        spec = ModelSpec(
            id = "gemma3-1b-it-int4",
            displayName = "Gemma 3 1B IT (INT4)",
            artifactFormat = "litertlm",
            minimumRamMb = 2048,
            licenseId = GEMMA_LICENSE_ID
        ),
        fileName = "gemma3-1b-it-int4.litertlm",
        approximateDownloadBytes = 584_000_000L,
        sourceUrl = "https://huggingface.co/litert-community/Gemma3-1B-IT",
        licenseUrl = "https://ai.google.dev/gemma/terms"
    )

    fun modelDirectory(context: Context): File =
        File(context.filesDir, "models").apply { mkdirs() }

    fun modelFile(context: Context, artifact: LocalArtifact = gemma3_1b_it_int4): File =
        File(modelDirectory(context), artifact.fileName)
}
