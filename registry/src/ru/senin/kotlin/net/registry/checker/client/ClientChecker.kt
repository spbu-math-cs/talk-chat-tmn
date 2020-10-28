package ru.senin.kotlin.net.registry.checker.client

import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress

interface CheckListener {
    fun startCheck(user: String)
    fun checkReceived(user: String, userAddress: UserAddress)
}

interface ClientChecker {
    fun check()
    fun close() {}
    fun setCheckListener(listener: CheckListener)
}

interface ClientCheckerFactory {
    fun create(userName: String, protocol: Protocol, host: String, port: Int) : ClientChecker
    fun supportedProtocols() : Set<Protocol>
}

class NotConnectedListener : IllegalStateException("Not connected listener!")
