package ru.senin.kotlin.net.server

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.senin.kotlin.net.Message
import java.net.InetSocketAddress

class UdpChatServer(private val host: String, private val port: Int) : BaseChatServer() {
    private var serverJob: Job? = null

    override fun start() {
        runBlocking {
            serverJob?.let {
                if (it.isActive) {
                    return@runBlocking
                }
            }
            serverJob = launch {
                val datagramSocket =
                    aSocket(ActorSelectorManager(Dispatchers.IO)).udp().bind(InetSocketAddress(host, port))
                while (isActive) {
                    val datagram = datagramSocket.incoming.receive()
                    val text = datagram.packet.readText()

                    launch {
                        try {
                            if (text.startsWith("id-checker")) {
                                datagramSocket.outgoing.send(Datagram(buildPacket {
                                    writeText(text.substringAfter(" "))
                                }, datagram.address))
                            } else {
                                val content: Message = objectMapper.readValue(text)
                                listener?.messageReceived(content.user, content.text)
                            }
                        } catch (e: CancellationException) {
                            log.debug("Canceled during message processing: ${e.message}", e)
                        } catch (e: Throwable) {
                            log.error("Error during message processing: ${e.message}", e)
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