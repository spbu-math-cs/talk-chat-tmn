package ru.senin.kotlin.net.registry.checker.client

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import ru.senin.kotlin.net.UserInfo
import ru.senin.kotlin.net.registry.checker.HttpApi

class HttpClientChecker(
    private val user: UserInfo
) : BaseClientChecker() {
    private val httpApi = Retrofit.Builder()
        .baseUrl("http://${user.address.host}:${user.address.port}")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build().create(HttpApi::class.java)

    override fun check() {
        listener?.startCheck(user.name) ?: throw NotConnectedListener()
        try {
            val response = httpApi.check().execute()
            if (response.isSuccessful)
                listener?.checkReceived(user) ?: throw NotConnectedListener()
        } catch (e: Exception) { }
    }

}