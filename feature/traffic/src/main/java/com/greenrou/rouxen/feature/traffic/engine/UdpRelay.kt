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

class UdpRelay(
    private val vpnService: VpnService,
    private val registry: ConnectionRegistry,
) {
    private val sessions = ConcurrentHashMap<ConnectionKey, UdpSession>()
    private val connectivityManager = vpnService.getSystemService(ConnectivityManager::class.java)!!

    private val readBuffer = ByteBuffer.allocate(MAX_DATAGRAM_SIZE)

    fun onTunPacket(ip: Ipv4Packet, udp: UdpHeader) {
        val key = ConnectionKey(Protocol.UDP, udp.sourcePort, ip.destinationAddress.hostAddress, udp.destinationPort)
        val data = udp.data()

        val session = sessions.getOrPut(key) {
            UdpSession(udp.sourcePort, ip.destinationAddress, udp.destinationPort, resolveUid(ip, udp))
                .apply { connect(vpnService) }
        }

        try {
            session.channel.write(ByteBuffer.wrap(data))
        } catch (_: Exception) {
            sessions.remove(key)
            session.close()
            registry.remove(key)
            return
        }
        session.txBytes.addAndGet(data.size.toLong())
        session.lastActivityMs = System.currentTimeMillis()

        registry.upsert(key, create = { newConnection(session) }) { conn ->
            conn.txBytes.addAndGet(data.size.toLong())
            conn.lastActivityMs = session.lastActivityMs
            conn.state = ConnectionState.ACTIVE
        }
    }

    fun pollIncoming(tunWriter: (ByteArray) -> Unit) {
        for ((key, session) in sessions) {
            readBuffer.clear()
            val read = try {
                session.channel.read(readBuffer)
            } catch (_: Exception) {
                -1
            }
            if (read <= 0) continue

            readBuffer.flip()
            val data = ByteArray(read)
            readBuffer.get(data)

            session.rxBytes.addAndGet(read.toLong())
            session.lastActivityMs = System.currentTimeMillis()

            val datagram = UdpHeader.build(session.remotePort, session.localPort, data)
            tunWriter(Ipv4Packet.buildPacket(session.remoteAddress, TunAddress.ADDRESS, Ipv4Packet.PROTOCOL_UDP, datagram))

            registry.upsert(key, create = { newConnection(session) }) { conn ->
                conn.rxBytes.addAndGet(read.toLong())
                conn.lastActivityMs = session.lastActivityMs
                conn.state = ConnectionState.ACTIVE
            }
        }
    }

    fun pruneIdle(now: Long) {
        sessions.entries.removeIf { (key, session) ->
            val idle = now - session.lastActivityMs > UDP_IDLE_TIMEOUT_MS
            if (idle) {
                session.close()
                registry.remove(key)
            }
            idle
        }
    }

    private fun newConnection(session: UdpSession) = MutableTrafficConnection(
        protocol = Protocol.UDP,
        localPort = session.localPort,
        remoteAddress = session.remoteAddress,
        remotePort = session.remotePort,
        uid = session.uid,
        state = ConnectionState.ACTIVE,
    )

    private fun resolveUid(ip: Ipv4Packet, udp: UdpHeader): Int {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return TrafficConnection.UID_UNKNOWN
        return try {
            connectivityManager.getConnectionOwnerUid(
                Ipv4Packet.PROTOCOL_UDP,
                InetSocketAddress(ip.sourceAddress, udp.sourcePort),
                InetSocketAddress(ip.destinationAddress, udp.destinationPort),
            )
        } catch (_: Exception) {
            TrafficConnection.UID_UNKNOWN
        }
    }

    companion object {
        private const val MAX_DATAGRAM_SIZE = 1500
        private const val UDP_IDLE_TIMEOUT_MS = 60_000L
    }
}
