package it.superiaprivata.app.chat

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.HttpURLConnection
import java.net.URL
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OnlineAssistantClientTest {
    @Test
    fun streamingRefusesPlainHttpBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("http://example.test", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRequiresDeviceCredentials() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", ""),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsOversizedPromptBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    "x".repeat(32_001)
                ) {}
            }
        }
    }


    @Test
    fun streamingRejectsInvalidRequestIdBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    "Ciao",
                    requestId = "not-a-uuid"
                ) {}
            }
        }
    }

    @Test
    fun healthCheckAlsoRejectsPlainHttp() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.checkHealth("http://example.test")
            }
        }
    }

    @Test
    fun authenticatedReadinessRejectsPlainHttp() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.checkOnlineReadiness(
                    OnlineConnectionConfig("http://example.test", "test-token")
                )
            }
        }
    }

    @Test
    fun authenticatedReadinessRequiresDeviceToken() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.checkOnlineReadiness(
                    OnlineConnectionConfig("https://example.test", "")
                )
            }
        }
    }

    @Test
    fun streamingRejectsEndpointWithQueryBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test?token=unsafe", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsEndpointWithUserInfoBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://user:password@example.test", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsEndpointWithFragmentBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test/#unsafe", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsBlankPromptBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    "   "
                ) {}
            }
        }
    }


    @Test
    fun tokenRotationRefusesPlainHttpBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.rotate(
                    OnlineConnectionConfig("http://example.test", "device-token")
                )
            }
        }
    }

    @Test
    fun tokenRotationRequiresCurrentDeviceToken() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.rotate(
                    OnlineConnectionConfig("https://example.test", "")
                )
            }
        }
    }

    @Test
    fun selfRevocationRefusesPlainHttpBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.revoke(
                    OnlineConnectionConfig("http://example.test", "device-token")
                )
            }
        }
    }

    @Test
    fun selfRevocationRequiresCurrentDeviceToken() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.revoke(
                    OnlineConnectionConfig("https://example.test", "")
                )
            }
        }
    }


    @Test
    fun tokenRotationRejectsEndpointWithQueryBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.rotate(
                    OnlineConnectionConfig("https://example.test?token=unsafe", "device-token")
                )
            }
        }
    }

    @Test
    fun selfRevocationRejectsEndpointWithUserInfoBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.revoke(
                    OnlineConnectionConfig("https://user:password@example.test", "device-token")
                )
            }
        }
    }

    @Test
    fun streamingRejectsEndpointWithQueryParameters() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test?token=unsafe", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsEndpointContainingUserInfo() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://user:password@example.test", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsEndpointContainingFragment() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test/#unsafe", "test-token"),
                    "Ciao"
                ) {}
            }
        }
    }



    @Test
    fun sseParserCollectsDeltasAndIgnoresHeartbeatComments() = runBlocking {
        val deltas = mutableListOf<String>()
        val stream = """
            : keep-alive

            event: delta
            data: {"text":"Ciao "}

            : keep-alive

            event: delta
            data: {"text":"mondo"}

            event: done
            data: {"provider":"fake","model":"fake-model"}

        """.trimIndent() + "\n\n"
        val answer = OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
        assertEquals("Ciao mondo", answer)
        assertEquals(listOf("Ciao ", "mondo"), deltas)
    }

    @Test
    fun sseParserCombinesMultipleDataLinesIntoOneEvent() = runBlocking {
        val deltas = mutableListOf<String>()
        val stream = """
            event: delta
            data: {"text":
            data: "Ciao mondo"}

            event: done
            data: {"provider":"fake"}

        """.trimIndent() + "\n\n"
        val answer = OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
        assertEquals("Ciao mondo", answer)
        assertEquals(listOf("Ciao mondo"), deltas)
    }

    @Test
    fun sseParserDoesNotDispatchUnterminatedEventAtEof() {
        val deltas = mutableListOf<String>()
        val stream = """
            event: delta
            data: {"text":"non ancora completo"}
        """.trimIndent()
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
            }
        }
        assertEquals(emptyList<String>(), deltas)
        assertEquals(
            "Il backend ha chiuso il flusso senza completare la risposta.",
            error.message
        )
    }

    @Test
    fun sseParserSurfacesErrorAfterPreservingReceivedDeltas() {
        val deltas = mutableListOf<String>()
        val stream = """
            event: delta
            data: {"text":"testo parziale"}

            event: error
            data: {"message":"Provider interrotto"}

        """.trimIndent() + "\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
            }
        }
        assertEquals(listOf("testo parziale"), deltas)
        assertEquals("Provider interrotto", error.message)
    }

    @Test
    fun sseParserRejectsEndOfStreamWithoutDoneAndKeepsPartialOutput() {
        val stream = """
            event: delta
            data: {"text":"parziale"}
        """.trimIndent() + "\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals(
            "Connessione interrotta: il testo parziale è stato conservato. Puoi riprendere la richiesta.",
            error.message
        )
    }

    @Test
    fun sseParserAcceptsCrLfLineEndings() = runBlocking {
        val stream = "event: delta\r\ndata: {\"text\":\"CRLF\"}\r\n\r\nevent: done\r\ndata: {}\r\n\r\n"
        val deltas = mutableListOf<String>()
        val answer = OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
        assertEquals("CRLF", answer)
        assertEquals(listOf("CRLF"), deltas)
    }

    @Test
    fun sseParserIgnoresUnknownFieldsAndEventTypes() = runBlocking {
        val stream = "id: 42\nretry: 1500\nx-custom: ignored\nevent: telemetry\ndata: {\"status\":\"working\"}\n\nevent: delta\ndata: {\"text\":\"ok\"}\n\nevent: done\ndata: {}\n\n"
        val deltas = mutableListOf<String>()
        val answer = OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
        assertEquals("ok", answer)
        assertEquals(listOf("ok"), deltas)
    }

    @Test
    fun sseParserDoesNotIgnoreExplicitlyEmptyDoneData() {
        val stream = "event: done\ndata:\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha inviato un evento di completamento non valido.", error.message)
    }

    @Test
    fun sseParserRejectsEmptyDoneEvent() {
        val stream = """
            event: done
            data: {}
        """.trimIndent() + "\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha restituito una risposta vuota.", error.message)
    }



    @Test
    fun sseParserKeepsEarlierDeltaWhenFollowingEventIsMalformed() {
        val deltas = mutableListOf<String>()
        val stream = """
            event: delta
            data: {"text":"prima parte"}

            event: delta
            data: {"text":}

        """.trimIndent() + "\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
            }
        }
        assertEquals(listOf("prima parte"), deltas)
        assertEquals("Il backend ha inviato un frammento SSE non valido.", error.message)
    }

    @Test
    fun sseParserRejectsMalformedDeltaInsteadOfSilentlyDroppingIt() {
        val stream = """
            event: delta
            data: {"text":}

            event: done
            data: {"provider":"fake"}

        """.trimIndent()
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha inviato un frammento SSE non valido.", error.message)
    }

    @Test
    fun sseParserRejectsDeltaWithoutStringText() {
        val stream = """
            event: delta
            data: {"text":42}

            event: done
            data: {}

        """.trimIndent()
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha inviato un frammento SSE senza testo valido.", error.message)
    }

    @Test
    fun sseParserRejectsMalformedCompletionEvent() {
        val stream = """
            event: delta
            data: {"text":"risposta"}

            event: done
            data: {invalid}

        """.trimIndent() + "\n\n"
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha inviato un evento di completamento non valido.", error.message)
    }

    @Test
    fun sseJsonFieldParserUnescapesControlCharactersAndUnicode() {
        val value = OnlineAssistantClient.extractJsonStringField(
            """{"text":"riga\n\"citazione\" \u00E8"}""",
            "text"
        )
        assertEquals("riga\n\"citazione\" è", value)
    }

    @Test
    fun sseJsonFieldParserRejectsMalformedOrMissingFields() {
        assertEquals(null, OnlineAssistantClient.extractJsonStringField("""{"text":}""", "text"))
        assertEquals(null, OnlineAssistantClient.extractJsonStringField("""{"other":"value"}""", "text"))
    }



    @Test
    fun streamingRejectsUnsupportedStructuredMessageRoleBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    listOf(ChatPromptMessage("tool", "Risultato non ammesso"))
                ) {}
            }
        }
    }

    @Test
    fun streamingRejectsStructuredConversationOverTotalCharacterLimit() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.stream(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    listOf(
                        ChatPromptMessage("user", "x".repeat(20_000)),
                        ChatPromptMessage("assistant", "y".repeat(12_001))
                    )
                ) {}
            }
        }
    }


    @Test
    fun cancellingStreamCoroutineDisconnectsBlockingHttpConnection() {
        var disconnected = false
        val connection = object : HttpURLConnection(URL("https://example.test")) {
            override fun disconnect() { disconnected = true }
            override fun usingProxy(): Boolean = false
            override fun connect() {}
        }
        val job = Job()
        val handle = registerDisconnectOnCancellation(job, connection)

        job.cancel()

        assertTrue("La connessione deve essere chiusa quando si annulla lo stream", disconnected)
        handle.dispose()
    }

    @Test
    fun normalStreamCompletionDoesNotDisconnectFromCancellationHook() {
        var disconnected = false
        val connection = object : HttpURLConnection(URL("https://example.test")) {
            override fun disconnect() { disconnected = true }
            override fun usingProxy(): Boolean = false
            override fun connect() {}
        }
        val job = Job()
        val handle = registerDisconnectOnCancellation(job, connection)

        job.complete()

        assertEquals(false, disconnected)
        handle.dispose()
    }


    @Test
    fun cancellingStreamWhileSseReadIsBlockedDisconnectsConnection() = runBlocking {
        val readStarted = CountDownLatch(1)
        val disconnectSignal = CountDownLatch(1)
        val disconnected = AtomicBoolean(false)
        val fakeConnection = object : HttpURLConnection(URL("https://example.test/api/v1/chat/stream")) {
            override fun connect() {}
            override fun usingProxy(): Boolean = false
            override fun disconnect() {
                disconnected.set(true)
                disconnectSignal.countDown()
            }
            override fun getOutputStream() = ByteArrayOutputStream()
            override fun getResponseCode(): Int = 200
            override fun getInputStream(): InputStream = object : InputStream() {
                override fun read(): Int {
                    readStarted.countDown()
                    disconnectSignal.await()
                    return -1
                }
            }
        }

        val task = async(Dispatchers.Default) {
            OnlineAssistantClient.streamInternal(
                OnlineConnectionConfig("https://example.test", "test-token"),
                listOf(ChatPromptMessage("user", "Ciao")),
                java.util.UUID.randomUUID().toString(),
                { fakeConnection }
            ) {}
        }

        assertTrue("Lo stream deve entrare nella lettura SSE", readStarted.await(3, TimeUnit.SECONDS))
        task.cancel()
        assertTrue("La cancellazione deve sbloccare subito la lettura SSE", disconnectSignal.await(2, TimeUnit.SECONDS))
        task.cancelAndJoin()
        assertTrue("La connessione deve risultare disconnessa", disconnected.get())
    }

}
