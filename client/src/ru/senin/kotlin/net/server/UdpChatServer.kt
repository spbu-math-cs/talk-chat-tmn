package ru.senin.kotlin.net.server

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

class UdpChatServer(private val host: String, private val port: Int) : BaseChatServer() {
    private var serverJob : Job? = null

    override fun start() {
        runBlocking {
            serverJob?.let {
                if (it.isActive) {
                    return@runBlocking
                }
            }
            serverJob = launch {
                val datagramSocket = aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind(InetSocketAddress(host, port))
                println("Socket bound: ${datagramSocket.localAddress}")
                while (isActive) {
                    val datagram = datagramSocket.receive()
                    val reader = datagram.packet.readerUTF8()

                    launch {
                        try {
                            while (true) {
                                val line = reader.readText()
                                println("${datagram.address}: $line")
                            }
                            // DONE: implement datagram processing
                        }
                        catch (e: CancellationException) {
                            log.debug( "Canceled during message processing: ${e.message}", e)
                        }
                        catch (e: Throwable) {
                            log.error( "Error during message processing: ${e.message}", e)
                        }
                    }
                }
            }
        }
    }

    override fun stop() {
        runBlocking {
            serverJob?.cancelAndJoin()
            serverJob = null
        }
    }

    companion object {
        val log: Logger = LoggerFactory.getLogger("udp-server")
    }
}