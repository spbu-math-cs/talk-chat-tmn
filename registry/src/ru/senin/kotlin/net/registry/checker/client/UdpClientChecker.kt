package ru.senin.kotlin.net.registry.checker.client

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import ru.senin.kotlin.net.Protocol
import ru.senin.kotlin.net.UserAddress
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

class UdpClientChecker(
    private val userName: String,
    private val host: String,
    private val port: Int
) : BaseClientChecker() {

    companion object {
        private val socket: BoundDatagramSocket =
            aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind(InetSocketAddress(0))
        private val usersId = ConcurrentHashMap<String, String>()
    }

    private var job: Job? = null

    private fun getRandomId(): String {
        TODO()
    }

    private fun startJob() {
        runBlocking {
            job = GlobalScope.launch {
                while (isActive) {
                    val datagram = socket.receive()
                    val reader = datagram.packet.readerUTF8()
                    launch {
                        try {
                            val id = reader.readText()
                            usersId[id]?.let {
                                listener?.checkReceived(it, UserAddress(Protocol.UDP, host, port))
                                    ?: throw NotConnectedListener()
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
        listener?.startCheck(userName) ?: NotConnectedListener()
        val id = getRandomId()
        usersId[id] = userName
        runBlocking {
            job ?: startJob()

            socket.outgoing.send(Datagram(buildPacket {
                writeText(id)
            }, InetSocketAddress(host, port)))
        }
    }

    override fun close() {
        runBlocking {
            job?.cancelAndJoin()
            job = null
        }
    }

}