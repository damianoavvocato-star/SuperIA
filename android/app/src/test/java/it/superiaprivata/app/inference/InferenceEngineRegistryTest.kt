package it.superiaprivata.app.inference

import it.superiaprivata.app.device.DeviceProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class InferenceEngineRegistryTest {
    private val device = DeviceProfile(
        manufacturer = "Test",
        modelName = "Test device",
        androidVersion = "15",
        apiLevel = 35,
        totalRamMb = 8192,
        availableRamMb = 4096,
        supportedAbis = listOf("arm64-v8a"),
        freeStorageGb = 20.0,
        thermalStatus = "normal"
    )
    private val model = ModelSpec(
        id = "test-model",
        displayName = "Test model",
        artifactFormat = "test",
        minimumRamMb = 2048,
        licenseId = "test-license"
    )

    @Test
    fun emptyRegistryDoesNotClaimInferenceIsAvailable() {
        val registry = InferenceEngineRegistry()
        assertTrue(registry.inspect(device, model).isEmpty())
        assertNull(registry.select(device, model))
        assertFalse(registry.hasReadyEngine(device, model))
    }

    @Test
    fun registrySelectsOnlyReadyEngines() {
        val unavailable = StubEngine(
            "not-ready",
            RuntimeAvailability(RuntimeStatus.MODEL_NOT_INSTALLED, "Model missing")
        )
        val ready = StubEngine(
            "ready",
            RuntimeAvailability(RuntimeStatus.READY, "Ready")
        )
        val registry = InferenceEngineRegistry(listOf(unavailable, ready))

        assertEquals("ready", registry.select(device, model)?.id)
        assertTrue(registry.hasReadyEngine(device, model))
    }

    private class StubEngine(
        override val id: String,
        private val result: RuntimeAvailability
    ) : InferenceEngine {
        override fun checkAvailability(
            device: DeviceProfile,
            model: ModelSpec
        ): RuntimeAvailability = result

        override suspend fun generate(
            model: ModelSpec,
            request: InferenceRequest,
            onPartialText: (String) -> Unit
        ): InferenceResult {
            onPartialText("test")
            return InferenceResult("test", model.id)
        }
    }
}
