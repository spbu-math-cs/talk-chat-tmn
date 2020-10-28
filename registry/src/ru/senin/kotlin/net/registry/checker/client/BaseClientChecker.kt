package ru.senin.kotlin.net.registry.checker.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

abstract class BaseClientChecker : ClientChecker {
    val objectMapper = jacksonObjectMapper()
    var listener: CheckListener? = null

    override fun setCheckListener(listener: CheckListener) {
        this.listener = listener
    }
}