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
    fun healthCheckAlsoRejectsPlainHttp() {
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                OnlineAssistantClient.checkHealth("http://example.test")
            }
        }
    }
}