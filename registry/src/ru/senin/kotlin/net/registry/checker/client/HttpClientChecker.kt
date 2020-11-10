package ru.senin.kotlin.net.registry.checker.client

import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import ru.senin.kotlin.net.UserInfo
import ru.senin.kotlin.net.registry.checker.CheckerHttpApi

class HttpClientChecker(
    private val user: UserInfo,
    listener: CheckListener
) : BaseClientChecker(listener) {
    private val httpApi = Retrofit.Builder()
        .baseUrl("http://${user.address.host}:${user.address.port}")
        .addConverterFactory(JacksonConverterFactory.create(objectMapper))
        .build().create(CheckerHttpApi::class.java)

    override fun check() {
        listener.startCheck(user)
        try {
            val response = httpApi.check().execute()
            if (response.isSuccessful)
                listener.checkReceived(user)
            else
                listener.checkFailed(user)
        } catch (e: Exception) {
            listener.checkFailed(user)
        }
    }

}