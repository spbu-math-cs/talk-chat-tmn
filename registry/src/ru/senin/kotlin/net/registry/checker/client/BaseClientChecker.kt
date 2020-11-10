package ru.senin.kotlin.net.registry.checker.client

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

abstract class BaseClientChecker(var listener: CheckListener) : ClientChecker {
    val objectMapper = jacksonObjectMapper()
}