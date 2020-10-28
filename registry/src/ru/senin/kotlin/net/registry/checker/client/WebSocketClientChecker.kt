package ru.senin.kotlin.net.registry.checker.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import ru.senin.kotlin.net.UserInfo
import ru.senin.kotlin.net.WebSocketOptions

class WebSocketClientChecker(
    private val user: UserInfo
) : BaseClientChecker() {
    private val client = HttpClient(CIO).config { install(WebSockets) }

    override fun check() {
        listener?.startCheck(user.name) ?: throw NotConnectedListener()
        var checked = true
        runBlocking {
            try {
                client.ws(HttpMethod.Get, user.address.host, user.address.port, WebSocketOptions.path) {}
            } catch (e: Exception) {
                checked = false
            }
        }
        if (checked)
            listener?.checkReceived(user)
    }

}