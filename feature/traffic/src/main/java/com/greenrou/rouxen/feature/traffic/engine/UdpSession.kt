package com.greenrou.rouxen.feature.traffic.engine

import android.net.VpnService
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicLong

class UdpSession(
    val localPort: Int,
    val remoteAddress: InetAddress,
    val remotePort: Int,
    val uid: Int,
) {
    val channel: DatagramChannel = DatagramChannel.open().apply { configureBlocking(false) }

    val rxBytes = AtomicLong(0L)
    val txBytes = AtomicLong(0L)

    @Volatile var lastActivityMs: Long = System.currentTimeMillis()

    fun connect(vpnService: VpnService) {
        vpnService.protect(channel.socket())
        channel.connect(InetSocketAddress(remoteAddress, remotePort))
    }

    fun close() {
        runCatching { channel.close() }
    }
}
