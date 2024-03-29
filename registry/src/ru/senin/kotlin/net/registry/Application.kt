package ru.senin.kotlin.net.registry

import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.netty.*
import org.slf4j.event.Level
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import ru.senin.kotlin.net.UserInfo
import ru.senin.kotlin.net.checkUserName
import ru.senin.kotlin.net.registry.checker.client.*
import java.lang.Thread.sleep
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

class RegistryListener : CheckListener {
    override fun startCheck(user: UserInfo) {
        println("Start checking ${user.name}")
    }

    override fun checkReceived(user: UserInfo) {
        println("Checked ${user.name}")
    }

    override fun checkFailed(user: UserInfo) {
        println("Check failed ${user.name}")
        Registry.users.remove(user.name)
    }
}

class CheckerFactory : ClientCheckerFactory {
    override fun create(user: UserInfo): ClientChecker =
        when (user.address.protocol) {
            Protocol.HTTP -> HttpClientChecker(user, Registry.registryListener)
            Protocol.UDP -> UdpClientChecker(user, Registry.registryListener)
            Protocol.WEBSOCKET -> WebSocketClientChecker(user, Registry.registryListener)
        }

    override fun supportedProtocols(): Set<Protocol> = setOf(Protocol.HTTP, Protocol.UDP, Protocol.WEBSOCKET)
}

fun main(args: Array<String>) {
    thread {
        while (true) {
            sleep(1000 * 60 * 2)
            println("Start requests...")
            Registry.users.toList().forEach { (userName, userAddress) ->
                val checker = CheckerFactory().create(UserInfo(userName, userAddress))
                try {
                    checker.check()
                } catch (e: Exception) {
                    println(e.message)
                } finally {
                    checker.close()
                }
            }
            println("Finish requests...")
        }
    }
    EngineMain.main(args)
}

object Registry {
    val users = ConcurrentHashMap<String, UserAddress>()
    val registryListener = RegistryListener()
}

@Suppress("UNUSED_PARAMETER")
@JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        exception<IllegalArgumentException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "invalid argument")
        }
        exception<UserAlreadyRegisteredException> { cause ->
            call.respond(HttpStatusCode.Conflict, cause.message ?: "user already registered")
        }
        exception<IllegalUserNameException> { cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "illegal user name")
        }
    }
    routing {
        get("/v1/health") {
            call.respondText("OK", contentType = ContentType.Text.Plain)
        }

        post("/v1/users") {
            val user = call.receive<UserInfo>()
            val userAddresses = Registry.users[user.name]
            checkUserName(user.name) ?: throw IllegalUserNameException()
            if (userAddresses != null) {
                throw UserAlreadyRegisteredException()
            }
            Registry.users[user.name] = user.address
            call.respond(mapOf("status" to "ok"))
        }

        get("/v1/users") {
            call.respond(HttpStatusCode.OK, Registry.users)
        }

        put("/v1/users/{name}") {
            val userName = call.parameters["name"] as String
            val userAddress = call.receive<UserAddress>()
            checkUserName(userName) ?: throw IllegalUserNameException()
            Registry.users[userName] = userAddress
            call.respond(mapOf("status" to "ok"))
        }

        delete("/v1/users/{user}") {
            val userName = call.parameters["user"]
            Registry.users.remove(userName)
            call.respond(HttpStatusCode.OK, mapOf("status" to "ok"))
        }
    }
}

class UserAlreadyRegisteredException : RuntimeException("User already registered")
class IllegalUserNameException : RuntimeException("Illegal user name")