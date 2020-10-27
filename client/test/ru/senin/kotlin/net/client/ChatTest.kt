package ru.senin.kotlin.net.client

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Assertions.*
import ru.senin.kotlin.net.*
import ru.senin.kotlin.net.server.ChatMessageListener
import kotlin.concurrent.thread

class TestListener : ChatMessageListener {
    val history: MutableList<Pair<String, String>> = mutableListOf()

    override fun messageReceived(userName: String, text: String) {
        history.add(Pair(userName, text))
    }
}

class ChatTest {
    private val listener = TestListener()
    private val userName = "loopa"
    private val localhost = "127.0.0.1"
    private val portA = 8080
    private val portB = 8080

    @BeforeEach
    fun clearHistory() {
        listener.history.clear()
    }

    @TestFactory
    fun `Test all protocols`(): Collection<DynamicTest> {
        val protocols = ClientFactory.supportedProtocols()
        val messages = listOf(
            "random message",
            "the second message",
            "so...",
            "sad...",
            "I write this %#@!^",
            "OK, let's check",
            "DEAD END..."
        )

        return protocols.map { protocol ->
            dynamicTest("Test $protocol chat") {
                listener.history.clear()
                val server = ServerFactory.create(protocol, localhost, portA)
                val client = ClientFactory.create(protocol, localhost, portB)
                server.setMessageListener(listener)

                val serversJob = thread {
                    server.start()
                }
                try {
                    runBlocking {
                        delay(100)
                        messages.forEach {
                            client.sendMessage(Message(userName, it))
                        }
                        delay(100)
                    }
                } finally {
                    server.stop()
                    client.close()
                    serversJob.join()
                }
                assertEquals(messages, listener.history.map {it.second})
                assertTrue(listener.history.all { it.first == userName })
            }
        }.toList()
    }

}