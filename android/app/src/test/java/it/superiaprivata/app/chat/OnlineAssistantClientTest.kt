package it.superiaprivata.app.chat

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test

class OnlineAssistantClientTest {
    @Test
    fun refusesPlainHttpEvenWhenTokenIsPresent() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("http://example.test", "test-token"),
                    "Ciao"
                )
            }
        }
    }

    @Test
    fun refusesEndpointWithQueryParameters() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("https://example.test?token=unsafe", "test-token"),
                    "Ciao"
                )
            }
        }
    }

    @Test
    fun refusesMissingCredentialsBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("https://example.test", ""),
                    "Ciao"
                )
            }
        }
    }

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
    fun refusesOversizedPromptBeforeOpeningConnection() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("https://example.test", "test-token"),
                    "x".repeat(32_001)
                )
            }
        }
    }

    @Test
    fun refusesEndpointContainingUserInfo() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("https://user:password@example.test", "test-token"),
                    "Ciao"
                )
            }
        }
    }

    @Test
    fun refusesEndpointContainingFragment() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.send(
                    OnlineConnectionConfig("https://example.test/#unsafe", "test-token"),
                    "Ciao"
                )
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

}
