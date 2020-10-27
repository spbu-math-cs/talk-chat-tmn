package ru.senin.kotlin.net.registry

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import ru.senin.kotlin.net.UserInfo
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.*

fun Application.testModule() {

    (environment.config as MapApplicationConfig).apply {
        // define test environment here
    }
    module(testing = true)
}

class ApplicationTest {
    private val objectMapper = jacksonObjectMapper()
    private val testUserName = "pupkin"
    private val badUserName = "кек228"
    private val testHttpAddress = UserAddress(Protocol.HTTP, "127.0.0.1", 9999)
    private val testUpdAddress = UserAddress(Protocol.UDP, "127.0.0.1", 3002)
    private val userData = UserInfo(testUserName, testHttpAddress)
    private val badData = UserInfo(badUserName, testHttpAddress)

    @BeforeEach
    fun clearRegistry() {
        Registry.users.clear()
    }

    @Test
    fun `health endpoint`() {
        withTestApplication({ testModule() }) {
            handleRequest(HttpMethod.Get, "/v1/health").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("OK", response.content)
            }
        }
    }

    @Test
    fun `register user`(): Unit = withRegisteredTestUser { }

    @Test
    fun `registered user`() = withRegisteredTestUser {
        handleRequest(HttpMethod.Post, "/v1/users") {
            addHeader("Content-type", "application/json")
            setBody(objectMapper.writeValueAsString(userData))
        }.apply {
            assertEquals(HttpStatusCode.Conflict, response.status())
        }
    }

    @Test
    fun `bad username register`(): Unit = withTestApplication({ testModule() }) {
        handleRequest(HttpMethod.Post, "/v1/users") {
            addHeader("Content-type", "application/json")
            setBody(objectMapper.writeValueAsString(badData))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `change user`(): Unit = withRegisteredTestUser {
        handleRequest(HttpMethod.Put, "/v1/users/$testUserName") {
            addHeader("Content-type", "application/json")
            setBody(objectMapper.writeValueAsString(testUpdAddress))
        }.apply {
            assertEquals(HttpStatusCode.OK, response.status())
            val content = response.content ?: fail("No response content")
            val info = objectMapper.readValue<HashMap<String, String>>(content)

            assertNotNull(info["status"])
            assertEquals("ok", info["status"])
            assertEquals(Registry.users[testUserName], testUpdAddress)
        }
    }

    @Test
    fun `bad username change`(): Unit = withRegisteredTestUser {
        handleRequest(HttpMethod.Put, "/v1/users/$badUserName") {
            addHeader("Content-type", "application/json")
            setBody(objectMapper.writeValueAsString(testUpdAddress))
        }.apply {
            assertEquals(HttpStatusCode.BadRequest, response.status())
        }
    }

    @Test
    fun `list users`() = withRegisteredTestUser {
        handleRequest(HttpMethod.Get, "/v1/users").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            val content = response.content
            assertNotNull(content)
            val users: ConcurrentHashMap<String, UserAddress> = objectMapper.readValue(content)
            assertEquals(1, users.size)
            assertNotNull(users[testUserName])
            assertEquals(testHttpAddress, users[testUserName])
        }
    }

    @Test
    fun `delete user`() = withRegisteredTestUser {
        handleRequest(HttpMethod.Delete, "/v1/users/$testUserName").apply {
            assertEquals(HttpStatusCode.OK, response.status())
            val content = response.content
            assertNotNull(content)
            val info: HashMap<String, String> = objectMapper.readValue(content)
            assertNotNull(info["status"])
            assertEquals("ok", info["status"])
        }
    }

    private fun withRegisteredTestUser(block: TestApplicationEngine.() -> Unit) {
        withTestApplication({ testModule() }) {
            handleRequest {
                method = HttpMethod.Post
                uri = "/v1/users"
                addHeader("Content-type", "application/json")
                setBody(objectMapper.writeValueAsString(userData))
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val content = response.content ?: fail("No response content")
                val info = objectMapper.readValue<HashMap<String, String>>(content)

                assertNotNull(info["status"])
                assertEquals("ok", info["status"])

                this@withTestApplication.block()
            }
        }
    }
}