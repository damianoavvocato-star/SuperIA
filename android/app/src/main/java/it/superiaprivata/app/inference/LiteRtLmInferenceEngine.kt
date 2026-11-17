package it.superiaprivata.app.inference

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.ai.edge.litertlm.Backend
import com.google.ai.edge.litertlm.Engine
import com.google.ai.edge.litertlm.EngineConfig
import com.google.ai.edge.litertlm.MessageCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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
    private val mainHandler = Handler(Looper.getMainLooper())
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
        require(request.maxOutputTokens > 0) { "Il limite dei token di output deve essere positivo." }
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
            val outputLock = Any()
            val activeConversation = requireNotNull(conversation)

            // Use LiteRT-LM's callback API directly. Its Flow overload internally
            // creates a callbackFlow and calls SendChannel.close() from the JNI
            // completion thread; affected published AARs contain an incompatible
            // synthetic call that crashes with NoSuchMethodError on Android.
            val generatedText = suspendCancellableCoroutine { continuation ->
                activeConversation.sendMessageAsync(
                    request.prompt,
                    object : MessageCallback {
                        override fun onMessage(message: com.google.ai.edge.litertlm.Message) {
                            if (!continuation.isActive) return
                            val partial = synchronized(outputLock) {
                                output.append(message.toString())
                            }
                            mainHandler.post {
                                if (continuation.isActive) onPartialText(partial)
                            }
                        }

                        override fun onDone() {
                            if (!continuation.isActive) return
                            val result = synchronized(outputLock) { output.result().trim() }
                            continuation.resume(result)
                        }

                        override fun onError(error: Throwable) {
                            if (continuation.isActive) continuation.resumeWithException(error)
                        }
                    },
                    maxOutputToken = request.maxOutputTokens
                )
            }.also { result ->
                check(result.isNotEmpty()) {
                    "Il runtime locale non ha restituito testo. Verifica il modello e riprova."
                }
            }

            InferenceResult(
                text = generatedText,
                modelId = model.id
            )
        }
    }

    private suspend fun ensureConversation(model: ModelSpec, file: File) {
        // ChatPromptBuilder already reconstructs the bounded transcript from the
        // encrypted archive. Start each turn with a fresh conversation so the
        // same history is not appended twice (once by LiteRT-LM's in-memory
        // conversation and once inside the rebuilt prompt).
        if (activeModelId == model.id && engine != null) {
            conversation?.close()
            conversation = requireNotNull(engine).createConversation()
            return
        }

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
