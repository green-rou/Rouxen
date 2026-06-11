package com.greenrou.rouxen.feature.traffic.data

import com.greenrou.rouxen.feature.traffic.model.ConnectionState
import com.greenrou.rouxen.feature.traffic.model.Protocol
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

data class ConnectionKey(
    val protocol: Protocol,
    val localPort: Int,
    val remoteAddress: String,
    val remotePort: Int,
)

class MutableTrafficConnection(
    val protocol: Protocol,
    val localPort: Int,
    val remoteAddress: InetAddress,
    val remotePort: Int,
    val uid: Int,
    @Volatile var state: ConnectionState,
) {
    val rxBytes = AtomicLong(0L)
    val txBytes = AtomicLong(0L)

    @Volatile var lastActivityMs: Long = System.currentTimeMillis()

    fun toImmutable(): TrafficConnection = TrafficConnection(
        protocol = protocol,
        localPort = localPort,
        remoteAddress = remoteAddress,
        remotePort = remotePort,
        uid = uid,
        rxBytes = rxBytes.get(),
        txBytes = txBytes.get(),
        lastActivityMs = lastActivityMs,
        state = state,
    )
}

class ConnectionRegistry {

    private val connections = ConcurrentHashMap<ConnectionKey, MutableTrafficConnection>()
    private val _connectionsFlow = MutableStateFlow<List<TrafficConnection>>(emptyList())
    val connectionsFlow: StateFlow<List<TrafficConnection>> = _connectionsFlow.asStateFlow()

    @Volatile private var lastPublishMs = 0L

    fun upsert(
        key: ConnectionKey,
        create: () -> MutableTrafficConnection,
        mutator: (MutableTrafficConnection) -> Unit,
    ): MutableTrafficConnection {
        var isNew = false
        val entry = connections.computeIfAbsent(key) { isNew = true; create() }
        mutator(entry)
        publish(force = isNew)
        return entry
    }

    fun remove(key: ConnectionKey) {
        if (connections.remove(key) != null) publish(force = true)
    }

    fun pruneInactive(now: Long, tcpGraceMs: Long, tcpIdleMs: Long, udpIdleMs: Long) {
        var removedAny = false
        connections.entries.removeIf { (key, conn) ->
            val idleMs = now - conn.lastActivityMs
            val timeout = if (key.protocol == Protocol.UDP) {
                udpIdleMs
            } else if (conn.state == ConnectionState.CLOSING || conn.state == ConnectionState.CLOSED) {
                tcpGraceMs
            } else {
                tcpIdleMs
            }
            (idleMs > timeout).also { if (it) removedAny = true }
        }
        if (removedAny) publish(force = true)
    }

    private fun publish(force: Boolean) {
        val now = System.currentTimeMillis()
        if (!force && now - lastPublishMs < PUBLISH_THROTTLE_MS) return
        lastPublishMs = now
        _connectionsFlow.value = connections.values.map { it.toImmutable() }
    }

    companion object {
        private const val PUBLISH_THROTTLE_MS = 250L
    }
}
