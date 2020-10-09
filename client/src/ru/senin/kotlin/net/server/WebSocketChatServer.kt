package ru.senin.kotlin.net.server

import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import ru.senin.kotlin.net.WebSocketOptions
import java.time.Duration

class WebSocketChatServer(host: String, port: Int) : NettyChatServer(host, port) {

    override fun configureModule(): Application.() -> Unit = {
        install(WebSockets) {
            pingPeriod = Duration.ofSeconds(15)
            timeout = Duration.ofSeconds(15)
            maxFrameSize = Long.MAX_VALUE
            masking = false
        }

        routing {
            webSocket(WebSocketOptions.path) {
                // TODO: implement processing loop
            }
        }
    }

}
