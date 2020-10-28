package ru.senin.kotlin.net.registry.checker.client

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import ru.senin.kotlin.net.registry.checker.HttpApi

class HttpClientChecker(
    private val userName: String,
    private val host: String,
    private val port: Int
) : BaseClientChecker() {
    private val httpApi = Retrofit.Builder()
        .baseUrl("http://$host:$port")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build().create(HttpApi::class.java)

    override fun check() {
        listener?.startCheck(userName) ?: throw NotConnectedListener()
        try {
            val response = httpApi.check().execute()
            if (response.isSuccessful)
                listener?.checkReceived(userName, UserAddress(Protocol.HTTP, host, port)) ?: throw NotConnectedListener()
        } catch (e : Exception) { }
    }

}