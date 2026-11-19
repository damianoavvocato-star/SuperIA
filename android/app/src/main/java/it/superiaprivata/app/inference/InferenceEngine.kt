package it.superiaprivata.app.inference

import it.superiaprivata.app.device.DeviceProfile

/**
 * Describes a model candidate without downloading or loading its weights.
 * License identifiers must resolve to a reviewed entry in the model registry.
 */
data class ModelSpec(
    val id: String,
    val displayName: String,
    val artifactFormat: String,
    val minimumRamMb: Long,
    val licenseId: String
)

data class InferenceRequest(
    val prompt: String,
    val systemInstruction: String? = null,
    val maxOutputTokens: Int = 1024
)

data class InferenceResult(
    val text: String,
    val modelId: String,
    val promptTokens: Int? = null,
    val generatedTokens: Int? = null
)

enum class RuntimeStatus {
    READY,
    RUNTIME_NOT_INSTALLED,
    MODEL_NOT_INSTALLED,
    DEVICE_UNSUPPORTED,
    INSUFFICIENT_MEMORY,
    LICENSE_NOT_APPROVED,
    TEMPORARILY_UNAVAILABLE
}

data class RuntimeAvailability(
    val status: RuntimeStatus,
    val detail: String
) {
    val isReady: Boolean get() = status == RuntimeStatus.READY
}

/**
 * Contract for real on-device inference providers.
 * Implementations must not silently fall back to cloud services.
 */
interface InferenceEngine {
    val id: String

    fun checkAvailability(
        device: DeviceProfile,
        model: ModelSpec
    ): RuntimeAvailability

    suspend fun generate(
        model: ModelSpec,
        request: InferenceRequest,
        onPartialText: (String) -> Unit = {}
    ): InferenceResult
}
