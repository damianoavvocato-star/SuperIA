package it.superiaprivata.app.inference

import it.superiaprivata.app.device.DeviceProfile

data class EngineCandidate(
    val engine: InferenceEngine,
    val availability: RuntimeAvailability
)

/**
 * Explicit registry: only injected, reviewed engines can be selected.
 * The default registry is intentionally empty until a real Android runtime
 * and a model/license combination pass compatibility checks.
 */
class InferenceEngineRegistry(
    private val engines: List<InferenceEngine> = emptyList()
) {
    fun inspect(device: DeviceProfile, model: ModelSpec): List<EngineCandidate> =
        engines.map { engine ->
            EngineCandidate(engine, engine.checkAvailability(device, model))
        }

    fun select(device: DeviceProfile, model: ModelSpec): InferenceEngine? =
        inspect(device, model).firstOrNull { it.availability.isReady }?.engine

    fun hasReadyEngine(device: DeviceProfile, model: ModelSpec): Boolean =
        select(device, model) != null
}
