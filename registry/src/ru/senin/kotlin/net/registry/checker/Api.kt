package ru.senin.kotlin.net.registry.checker

import retrofit2.Call
import retrofit2.http.GET
import ru.senin.kotlin.net.HttpOptions

interface CheckerHttpApi {
    @GET(HttpOptions.healthCheckPath)
    fun check(): Call<Map<String, String>>
}