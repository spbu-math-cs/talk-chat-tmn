package ru.senin.kotlin.net.registry.checker.client

import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserInfo

interface CheckListener {
    fun startCheck(user: UserInfo)
    fun checkReceived(user: UserInfo)
    fun checkFailed(user: UserInfo)
}

interface ClientChecker {
    fun check()
    fun close() {}
}

interface ClientCheckerFactory {
    fun create(user: UserInfo) : ClientChecker
    fun supportedProtocols() : Set<Protocol>
}
