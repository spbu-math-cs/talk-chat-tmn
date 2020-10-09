package ru.senin.kotlin.net.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import ru.senin.kotlin.net.Message
import ru.senin.kotlin.net.WebSocketOptions

class WebSocketChatClient(private val host: String, private val port: Int) : BaseChatClient() {
    private val client = HttpClient(CIO).config { install(WebSockets) }
    private val messagesChannel = Channel<String>()
    private val job = GlobalScope.launch {
                // Client is always connected, messagesChannel is used to send message from sendMessage() method
                client.ws(HttpMethod.Get, host, port, WebSocketOptions.path) {
                    while (isActive) {
                        val content = messagesChannel.receive()
                        send(Frame.Text(content))
                    }
                }
            }

    override fun sendMessage(message: Message) {
        if (!job.isActive) {
            throw IllegalStateException("Client already closed")
        }
        val content = objectMapper.writeValueAsString(message)
        runBlocking {
            messagesChannel.send(content)
        }
    }

    override fun close() {
        runBlocking {
            messagesChannel.close()
            job.cancelAndJoin()
        }
    }
}
