package ru.senin.kotlin.net.client

import ru.senin.kotlin.net.HttpApi
import ru.senin.kotlin.net.Message

class HttpChatClient(host: String, port: Int) : BaseChatClient() {
    private val httpApi: HttpApi = TODO("Create HttpApi Retrofit implementation with base url http://$host:$port")

    override fun sendMessage(message: Message) {
        val response = httpApi.sendMessage(message).execute()
        if (!response.isSuccessful) {
            println("${response.code()} ${response.message()}}")
        }
    }
}
