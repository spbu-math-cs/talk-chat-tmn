package ru.senin.kotlin.net.registry.checker.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import ru.senin.kotlin.net.UserInfo
import java.net.InetSocketAddress
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

class UdpClientChecker(
    private val user: UserInfo,
    listener: CheckListener
) : BaseClientChecker(listener) {

    companion object {
        private val socket: BoundDatagramSocket =
            aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind(InetSocketAddress(0))
        private val usersId = ConcurrentHashMap<String, UserInfo>()
    }

    private var job: Job? = null

    private fun getRandomId(): String {
        return UUID.randomUUID().toString()
    }

    private fun startJob() {
        runBlocking {
            job = GlobalScope.launch {
                while (isActive) {
                    val datagram = socket.incoming.receive()
                    val reader = datagram.packet.readerUTF8()
                    launch {
                        try {
                            val id = reader.readText()
                            usersId[id]?.let {
                                listener.checkReceived(it)
                            }
                        } catch (e: CancellationException) {
                            throw IllegalStateException("Canceled during message processing: ${e.message}", e)
                        } catch (e: Throwable) {
                            throw java.lang.IllegalStateException("Error during message processing: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }

    override fun check() {
        listener.startCheck(user.name)
        val id = getRandomId()
        usersId[id] = user
        runBlocking {
            job ?: startJob()
            socket.outgoing.send(Datagram(buildPacket {
                writeText("id-checker $id")
            }, InetSocketAddress(user.address.host, user.address.port)))
        }
    }

    override fun close() {
        runBlocking {
            job?.cancelAndJoin()
            job = null
        }
    }

}