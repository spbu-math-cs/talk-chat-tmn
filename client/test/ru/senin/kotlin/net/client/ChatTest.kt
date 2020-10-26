package ru.senin.kotlin.net.client

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
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
    private val port = 8080

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
                val server = ServerFactory.create(protocol, localhost, port)
                val client = ClientFactory.create(protocol, localhost, port)

                server.setMessageListener(listener)

                val serversJob = thread {
                    server.start()
                }
                try {
                    messages.forEach {
                        client.sendMessage(Message(userName, it))
                    }
                } finally {
                    server.stop()
                    serversJob.join()
                }
                assertEquals(messages, listener.history.map { it.second })
                assertTrue(listener.history.all { it.first == userName })
            }
        }.toList()
    }

}