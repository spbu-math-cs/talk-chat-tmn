package ru.senin.kotlin.net.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

abstract class BaseChatClient : ChatClient {
    protected val objectMapper = jacksonObjectMapper()
}
