package com.greenrou.rouxen.feature.traffic.data

import com.greenrou.rouxen.feature.traffic.model.ConnectionState
import com.greenrou.rouxen.feature.traffic.model.Protocol
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.InetAddress

class ConnectionRegistryTest {

    private fun sampleConnection(
        protocol: Protocol = Protocol.UDP,
        localPort: Int = 1234,
        remoteAddress: String = "8.8.8.8",
        remotePort: Int = 53,
        state: ConnectionState = ConnectionState.ACTIVE,
        lastActivityMs: Long = System.currentTimeMillis(),
    ): MutableTrafficConnection = MutableTrafficConnection(
        protocol = protocol,
        localPort = localPort,
        remoteAddress = InetAddress.getByName(remoteAddress),
        remotePort = remotePort,
        uid = 1000,
        state = state,
    ).apply { this.lastActivityMs = lastActivityMs }

    @Test
    fun `upsert publishes immediately for a new key`() {
        val registry = ConnectionRegistry()
        val key = ConnectionKey(Protocol.UDP, 1234, "8.8.8.8", 53)

        registry.upsert(key, create = { sampleConnection() }) { it.txBytes.addAndGet(100) }

        val snapshot = registry.connectionsFlow.value
        assertEquals(1, snapshot.size)
        assertEquals(100L, snapshot[0].txBytes)
    }

    @Test
    fun `rapid upserts on existing key throttle publish but still mutate underlying counters`() {
        val registry = ConnectionRegistry()
        val key = ConnectionKey(Protocol.UDP, 1234, "8.8.8.8", 53)

        val entry = registry.upsert(key, create = { sampleConnection() }) { it.txBytes.addAndGet(100) }
        registry.upsert(key, create = { sampleConnection() }) { it.txBytes.addAndGet(50) }

        assertEquals(150L, entry.txBytes.get())
        assertEquals(100L, registry.connectionsFlow.value[0].txBytes)
    }

    @Test
    fun `remove publishes immediately`() {
        val registry = ConnectionRegistry()
        val key = ConnectionKey(Protocol.TCP, 1234, "1.1.1.1", 443)
        registry.upsert(
            key,
            create = { sampleConnection(protocol = Protocol.TCP, remoteAddress = "1.1.1.1", remotePort = 443) },
            mutator = {},
        )
        assertEquals(1, registry.connectionsFlow.value.size)

        registry.remove(key)
        assertEquals(0, registry.connectionsFlow.value.size)
    }

    @Test
    fun `pruneInactive removes idle UDP and stale closed TCP entries`() {
        val registry = ConnectionRegistry()
        val now = 1_000_000L

        val udpKey = ConnectionKey(Protocol.UDP, 1, "8.8.8.8", 53)
        val tcpClosedKey = ConnectionKey(Protocol.TCP, 2, "1.1.1.1", 443)
        val tcpAliveKey = ConnectionKey(Protocol.TCP, 3, "2.2.2.2", 443)

        registry.upsert(
            udpKey,
            create = { sampleConnection(Protocol.UDP, 1, "8.8.8.8", 53, lastActivityMs = now - 70_000) },
            mutator = {},
        )
        registry.upsert(
            tcpClosedKey,
            create = {
                sampleConnection(
                    Protocol.TCP, 2, "1.1.1.1", 443,
                    state = ConnectionState.CLOSED, lastActivityMs = now - 10_000,
                )
            },
            mutator = {},
        )
        registry.upsert(
            tcpAliveKey,
            create = {
                sampleConnection(
                    Protocol.TCP, 3, "2.2.2.2", 443,
                    state = ConnectionState.ESTABLISHED, lastActivityMs = now - 10_000,
                )
            },
            mutator = {},
        )

        registry.pruneInactive(now, tcpGraceMs = 5_000, tcpIdleMs = 300_000, udpIdleMs = 60_000)

        val remainingPorts = registry.connectionsFlow.value.map { it.localPort }.toSet()
        assertEquals(setOf(3), remainingPorts)
    }
}
