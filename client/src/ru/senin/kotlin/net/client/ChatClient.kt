package ru.senin.kotlin.net.client

import ru.senin.kotlin.net.Message
import ru.senin.kotlin.net.Protocol

interface ChatClient {
    fun sendMessage(message: Message)
    fun close() {}
}

interface ChatClientFactory {
    fun create(protocol: Protocol, host: String, port: Int) : ChatClient
    fun supportedProtocols() : Set<Protocol>
}
