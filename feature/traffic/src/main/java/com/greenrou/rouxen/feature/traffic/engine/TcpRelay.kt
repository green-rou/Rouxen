package com.greenrou.rouxen.feature.traffic.engine

import android.net.ConnectivityManager
import android.net.VpnService
import android.os.Build
import com.greenrou.rouxen.feature.traffic.data.ConnectionKey
import com.greenrou.rouxen.feature.traffic.data.ConnectionRegistry
import com.greenrou.rouxen.feature.traffic.data.MutableTrafficConnection
import com.greenrou.rouxen.feature.traffic.model.ConnectionState
import com.greenrou.rouxen.feature.traffic.model.Protocol
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class TcpRelay(
    private val vpnService: VpnService,
    private val registry: ConnectionRegistry,
) {
    private val sessions = ConcurrentHashMap<ConnectionKey, TcpSession>()
    private val connectivityManager = vpnService.getSystemService(ConnectivityManager::class.java)!!

    private val readBuffer = ByteBuffer.allocate(MAX_SEGMENT_SIZE)

    fun onTunPacket(ip: Ipv4Packet, tcp: TcpHeader, tunWriter: (ByteArray) -> Unit) {
        val key = ConnectionKey(Protocol.TCP, tcp.sourcePort, ip.destinationAddress.hostAddress, tcp.destinationPort)

        if (tcp.rst) {
            sessions.remove(key)?.let { it.close(); registry.remove(key) }
            return
        }

        var session = sessions[key]

        if (session == null) {
            if (!(tcp.syn && !tcp.ack)) return

            session = TcpSession(
                localPort = tcp.sourcePort,
                remoteAddress = ip.destinationAddress,
                remotePort = tcp.destinationPort,
                clientIsn = tcp.sequenceNumber,
                uid = resolveUid(ip, tcp),
            )
            try {
                session.connect(vpnService)
            } catch (_: Exception) {
                sendSegment(session, TcpHeader.FLAG_RST or TcpHeader.FLAG_ACK, tunWriter = tunWriter)
                return
            }
            sessions[key] = session
            registry.upsert(key, create = { newConnection(session) }) {}
            return
        }

        session.lastActivityMs = System.currentTimeMillis()

        if (tcp.syn) return

        when (session.state) {
            TcpState.SYN_RECEIVED -> {
                if (tcp.ack && tcp.ackNumber == TcpSession.seq(session.ourIsn + 1)) {
                    session.state = TcpState.ESTABLISHED
                    handleData(session, key, tcp, tunWriter)
                    if (tcp.fin) handleClientFin(session, tunWriter)
                }
            }
            TcpState.ESTABLISHED -> {
                handleData(session, key, tcp, tunWriter)
                if (tcp.fin) handleClientFin(session, tunWriter)
            }
            TcpState.FIN_WAIT_1 -> {
                if (tcp.fin) {
                    ackClientFin(session, tunWriter)
                    session.state = TcpState.TIME_WAIT
                    session.closeAtMs = System.currentTimeMillis() + TCP_CLOSE_GRACE_MS
                } else if (tcp.ack && tcp.ackNumber == session.nextOurSeq) {
                    session.state = TcpState.FIN_WAIT_2
                }
            }
            TcpState.FIN_WAIT_2 -> {
                if (tcp.fin) {
                    ackClientFin(session, tunWriter)
                    session.state = TcpState.TIME_WAIT
                    session.closeAtMs = System.currentTimeMillis() + TCP_CLOSE_GRACE_MS
                }
            }
            TcpState.LAST_ACK -> {
                if (tcp.ack && tcp.ackNumber == session.nextOurSeq) {
                    session.state = TcpState.CLOSED
                }
            }
            TcpState.CLOSE_WAIT, TcpState.TIME_WAIT, TcpState.CLOSED -> Unit
        }

        updateRegistryState(key, session)
    }

    fun pollSockets(tunWriter: (ByteArray) -> Unit) {
        val now = System.currentTimeMillis()
        for ((key, session) in sessions) {
            if (!session.connected) {
                try {
                    if (session.channel.finishConnect()) {
                        session.connected = true
                        session.lastActivityMs = now
                        sendSynAck(session, tunWriter)
                    }
                } catch (_: Exception) {
                    sendSegment(session, TcpHeader.FLAG_RST or TcpHeader.FLAG_ACK, tunWriter = tunWriter)
                    teardown(session, key)
                }
                continue
            }

            if (session.state != TcpState.ESTABLISHED &&
                session.state != TcpState.SYN_RECEIVED &&
                session.state != TcpState.CLOSE_WAIT
            ) {
                continue
            }

            readBuffer.clear()
            val read = try {
                session.channel.read(readBuffer)
            } catch (_: Exception) {
                sendSegment(session, TcpHeader.FLAG_RST or TcpHeader.FLAG_ACK, tunWriter = tunWriter)
                teardown(session, key)
                continue
            }

            when {
                read > 0 -> {
                    readBuffer.flip()
                    val data = ByteArray(read)
                    readBuffer.get(data)

                    sendSegment(session, TcpHeader.FLAG_ACK or TcpHeader.FLAG_PSH, data = data, tunWriter = tunWriter)
                    session.bytesToClient.addAndGet(read.toLong())
                    session.rxBytes.addAndGet(read.toLong())
                    session.lastActivityMs = now

                    registry.upsert(key, create = { newConnection(session) }) { conn ->
                        conn.rxBytes.addAndGet(read.toLong())
                        conn.lastActivityMs = now
                    }
                }
                read == -1 -> {
                    when (session.state) {
                        TcpState.ESTABLISHED -> sendOurFin(session, TcpState.FIN_WAIT_1, tunWriter)
                        TcpState.CLOSE_WAIT -> sendOurFin(session, TcpState.LAST_ACK, tunWriter)
                        TcpState.SYN_RECEIVED -> {
                            sendSegment(session, TcpHeader.FLAG_RST or TcpHeader.FLAG_ACK, tunWriter = tunWriter)
                            teardown(session, key)
                            continue
                        }
                        else -> Unit
                    }
                    session.lastActivityMs = now
                    updateRegistryState(key, session)
                }
            }
        }
    }

    fun pruneInactive(now: Long) {
        sessions.entries.removeIf { (key, session) ->
            val terminal = session.state == TcpState.CLOSED ||
                (session.state == TcpState.TIME_WAIT && now >= session.closeAtMs)
            val idle = now - session.lastActivityMs > TCP_IDLE_TIMEOUT_MS
            (terminal || idle).also { remove ->
                if (remove) {
                    session.close()
                    registry.remove(key)
                }
            }
        }
    }

    private fun handleData(session: TcpSession, key: ConnectionKey, tcp: TcpHeader, tunWriter: (ByteArray) -> Unit) {
        val data = tcp.data()
        if (data.isEmpty()) return
        if (tcp.sequenceNumber != session.expectedClientSeq) return

        val written = try {
            session.channel.write(ByteBuffer.wrap(data))
        } catch (_: Exception) {
            teardown(session, key)
            return
        }
        if (written <= 0) return

        session.bytesFromClient.addAndGet(written.toLong())
        session.txBytes.addAndGet(written.toLong())
        sendSegment(session, TcpHeader.FLAG_ACK, tunWriter = tunWriter)
    }

    private fun handleClientFin(session: TcpSession, tunWriter: (ByteArray) -> Unit) {
        ackClientFin(session, tunWriter)
        session.state = TcpState.CLOSE_WAIT
        runCatching { session.channel.shutdownOutput() }
    }

    private fun ackClientFin(session: TcpSession, tunWriter: (ByteArray) -> Unit) {
        val ack = TcpSession.seq(session.expectedClientSeq + 1)
        sendSegment(session, TcpHeader.FLAG_ACK, ackOverride = ack, tunWriter = tunWriter)
        session.bytesFromClient.incrementAndGet()
    }

    private fun sendOurFin(session: TcpSession, next: TcpState, tunWriter: (ByteArray) -> Unit) {
        sendSegment(session, TcpHeader.FLAG_FIN or TcpHeader.FLAG_ACK, tunWriter = tunWriter)
        session.bytesToClient.incrementAndGet()
        session.state = next
    }

    private fun sendSynAck(session: TcpSession, tunWriter: (ByteArray) -> Unit) {
        val segment = TcpHeader.build(
            sourcePort = session.remotePort,
            destinationPort = session.localPort,
            sequenceNumber = session.ourIsn,
            ackNumber = TcpSession.seq(session.clientIsn + 1),
            flags = TcpHeader.FLAG_SYN or TcpHeader.FLAG_ACK,
            window = TCP_WINDOW_SIZE,
            sourceAddress = session.remoteAddress,
            destinationAddress = TunAddress.ADDRESS,
        )
        tunWriter(Ipv4Packet.buildPacket(session.remoteAddress, TunAddress.ADDRESS, Ipv4Packet.PROTOCOL_TCP, segment))
    }

    private fun sendSegment(
        session: TcpSession,
        flags: Int,
        data: ByteArray = ByteArray(0),
        ackOverride: Long? = null,
        tunWriter: (ByteArray) -> Unit,
    ) {
        val segment = TcpHeader.build(
            sourcePort = session.remotePort,
            destinationPort = session.localPort,
            sequenceNumber = session.nextOurSeq,
            ackNumber = ackOverride ?: session.expectedClientSeq,
            flags = flags,
            window = TCP_WINDOW_SIZE,
            sourceAddress = session.remoteAddress,
            destinationAddress = TunAddress.ADDRESS,
            data = data,
        )
        tunWriter(Ipv4Packet.buildPacket(session.remoteAddress, TunAddress.ADDRESS, Ipv4Packet.PROTOCOL_TCP, segment))
    }

    private fun teardown(session: TcpSession, key: ConnectionKey) {
        sessions.remove(key)
        session.close()
        registry.remove(key)
    }

    private fun updateRegistryState(key: ConnectionKey, session: TcpSession) {
        registry.upsert(key, create = { newConnection(session) }) { conn ->
            conn.lastActivityMs = session.lastActivityMs
            conn.state = mapState(session.state)
        }
    }

    private fun newConnection(session: TcpSession) = MutableTrafficConnection(
        protocol = Protocol.TCP,
        localPort = session.localPort,
        remoteAddress = session.remoteAddress,
        remotePort = session.remotePort,
        uid = session.uid,
        state = mapState(session.state),
    )

    private fun mapState(state: TcpState): ConnectionState = when (state) {
        TcpState.SYN_RECEIVED -> ConnectionState.ACTIVE
        TcpState.ESTABLISHED -> ConnectionState.ESTABLISHED
        TcpState.FIN_WAIT_1, TcpState.FIN_WAIT_2, TcpState.CLOSE_WAIT, TcpState.LAST_ACK -> ConnectionState.CLOSING
        TcpState.TIME_WAIT, TcpState.CLOSED -> ConnectionState.CLOSED
    }

    private fun resolveUid(ip: Ipv4Packet, tcp: TcpHeader): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return TrafficConnection.UID_UNKNOWN
        return try {
            connectivityManager.getConnectionOwnerUid(
                Ipv4Packet.PROTOCOL_TCP,
                InetSocketAddress(ip.sourceAddress, tcp.sourcePort),
                InetSocketAddress(ip.destinationAddress, tcp.destinationPort),
            )
        } catch (_: Exception) {
            TrafficConnection.UID_UNKNOWN
        }
    }

    companion object {
        private const val MAX_SEGMENT_SIZE = 1460
        private const val TCP_WINDOW_SIZE = 65535
        private const val TCP_CLOSE_GRACE_MS = 5_000L

        private const val TCP_IDLE_TIMEOUT_MS = 5 * 60_000L
    }
}
