package it.superiaprivata.app.inference

import org.junit.Assert.assertEquals
import org.junit.Test

class StreamingTextAccumulatorTest {
    @Test
    fun appendsDeltaChunksInOrderAndReturnsCumulativeText() {
        val accumulator = StreamingTextAccumulator()

        assertEquals("C", accumulator.append("C"))
        assertEquals("Ciao", accumulator.append("iao"))
        assertEquals("Ciao!", accumulator.append("!"))
        assertEquals("Ciao!", accumulator.result())
    }

    @Test
    fun emptyChunksDoNotChangeAccumulatedText() {
        val accumulator = StreamingTextAccumulator()

        assertEquals("", accumulator.append(""))
        assertEquals("test", accumulator.append("test"))
        assertEquals("test", accumulator.append(""))
    }
}
