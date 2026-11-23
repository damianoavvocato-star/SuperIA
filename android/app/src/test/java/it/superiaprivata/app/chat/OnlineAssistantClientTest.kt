package it.superiaprivata.app.chat

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
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

        """.trimIndent().replace("\\n", "\n")
        val answer = OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) { deltas += it }
        assertEquals("Ciao mondo", answer)
        assertEquals(listOf("Ciao ", "mondo"), deltas)
    }

    @Test
    fun sseParserSurfacesErrorAfterPreservingReceivedDeltas() {
        val deltas = mutableListOf<String>()
        val stream = """
            event: delta
            data: {"text":"testo parziale"}

            event: error
            data: {"message":"Provider interrotto"}

        """.trimIndent()
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
        """.trimIndent()
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
    fun sseParserRejectsEmptyDoneEvent() {
        val stream = """
            event: done
            data: {}
        """.trimIndent()
        val error = assertThrows(IllegalStateException::class.java) {
            runBlocking {
                OnlineAssistantClient.consumeSseEvents(stream.reader().buffered()) {}
            }
        }
        assertEquals("Il backend ha restituito una risposta vuota.", error.message)
    }


}
