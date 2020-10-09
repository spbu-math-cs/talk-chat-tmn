package ru.senin.kotlin.net.server

import ru.senin.kotlin.net.Protocol

interface ChatMessageListener {
    fun messageReceived(userName: String, text: String)
}

interface ChatServer {
    fun start()
    fun stop()
    fun setMessageListener(listener: ChatMessageListener)
}

interface ChatServerFactory {
    fun create(protocol: Protocol, host: String, port: Int) : ChatServer
}

