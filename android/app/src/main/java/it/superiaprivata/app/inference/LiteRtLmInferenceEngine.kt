package it.superiaprivata.app.inference

import android.content.Context
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Real on-device LiteRT-LM adapter.
 *
 * The caller supplies a local model resolver and an explicit license gate.
 * No network or cloud fallback exists in this class.
 */
class LiteRtLmInferenceEngine(
    context: Context,
    private val modelFile: (ModelSpec) -> File?,
    private val isLicenseApproved: (String) -> Boolean
) : InferenceEngine, AutoCloseable {

    override val id: String = "litert-lm-android"

    private val appContext = context.applicationContext
    private val mutex = Mutex()
    private var activeModelId: String? = null
    private var engine: Engine? = null
    private var conversation: com.google.ai.edge.litertlm.Conversation? = null

    override fun checkAvailability(
        device: it.superiaprivata.app.device.DeviceProfile,
        model: ModelSpec
    ): RuntimeAvailability {
        if (!isLicenseApproved(model.licenseId)) {
            return RuntimeAvailability(
                RuntimeStatus.LICENSE_NOT_APPROVED,
                "La licenza del modello non è stata approvata."
            )
        }

        if (device.availableRamMb < model.minimumRamMb) {
            return RuntimeAvailability(
                RuntimeStatus.INSUFFICIENT_MEMORY,
                "Memoria RAM disponibile insufficiente per il requisito dichiarato."
            )
        }

        val file = modelFile(model)
            ?: return RuntimeAvailability(
                RuntimeStatus.MODEL_NOT_INSTALLED,
                "Il modello non è installato sul dispositivo."
            )

        if (!file.isFile || !file.canRead() || file.length() == 0L ||
            !file.extension.equals("litertlm", ignoreCase = true)
        ) {
            return RuntimeAvailability(
                RuntimeStatus.MODEL_NOT_INSTALLED,
                "File modello assente, non leggibile o in formato non supportato."
            )
        }

        return RuntimeAvailability(RuntimeStatus.READY, "Modello locale pronto per LiteRT-LM.")
    }

    override suspend fun generate(
        model: ModelSpec,
        request: InferenceRequest,
        onPartialText: (String) -> Unit
    ): InferenceResult = mutex.withLock {
        require(request.prompt.isNotBlank()) { "La richiesta non può essere vuota." }
        require(isLicenseApproved(model.licenseId)) {
            "La licenza del modello deve essere approvata prima dell'esecuzione."
        }

        val file = requireNotNull(modelFile(model)) {
            "Modello non installato."
        }
        require(file.isFile && file.canRead() && file.length() > 0L) {
            "File modello non valido o non leggibile."
        }
        require(file.extension.equals("litertlm", ignoreCase = true)) {
            "Formato modello non supportato da LiteRT-LM."
        }

        withContext(Dispatchers.IO) {
            ensureConversation(model, file)
            val output = StreamingTextAccumulator()
            val activeConversation = requireNotNull(conversation)
            activeConversation.sendMessageAsync(request.prompt).collect { message ->
                val partial = output.append(message.toString())
                withContext(Dispatchers.Main.immediate) {
                    onPartialText(partial)
                }
            }
            val generatedText = output.result().trim()
            check(generatedText.isNotEmpty()) {
                "Il runtime locale non ha restituito testo. Verifica il modello e riprova."
            }
            InferenceResult(
                text = generatedText,
                modelId = model.id
            )
        }
    }

    private suspend fun ensureConversation(model: ModelSpec, file: File) {
        if (activeModelId == model.id && engine != null && conversation != null) return

        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
        activeModelId = null

        val cache = File(appContext.cacheDir, "litert-lm").apply { mkdirs() }
        val createdEngine = Engine(
            EngineConfig(
                modelPath = file.absolutePath,
                backend = Backend.CPU(),
                cacheDir = cache.absolutePath
            )
        )
        try {
            createdEngine.initialize()
            val createdConversation = createdEngine.createConversation()

            engine = createdEngine
            conversation = createdConversation
            activeModelId = model.id
        } catch (error: Throwable) {
            // Do not leak native runtime resources when initialization fails.
            runCatching { createdEngine.close() }
            throw error
        }
    }

    override fun close() {
        conversation?.close()
        conversation = null
        engine?.close()
        engine = null
        activeModelId = null
    }
}
