package it.superiaprivata.app.inference

/**
 * Accumulates the delta chunks emitted by LiteRT-LM's streaming Flow.
 * LiteRT-LM sends each latest chunk, not a cumulative snapshot.
 */
internal class StreamingTextAccumulator {
    private val output = StringBuilder()

    fun append(chunk: String): String {
        output.append(chunk)
        return output.toString()
    }

    fun result(): String = output.toString()
}
