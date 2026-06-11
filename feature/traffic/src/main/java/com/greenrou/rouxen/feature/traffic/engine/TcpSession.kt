package com.greenrou.rouxen.feature.traffic.engine

import android.net.VpnService
import java.net.InetAddress
import java.net.InetSocketAddress
import java.nio.channels.SocketChannel
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random

class TcpSession(
    val localPort: Int,
    val remoteAddress: InetAddress,
    val remotePort: Int,
    val clientIsn: Long,
    val uid: Int,
) {
    val ourIsn: Long = Random.nextInt().toLong() and SEQ_MASK

    val channel: SocketChannel = SocketChannel.open().apply { configureBlocking(false) }

    @Volatile var state: TcpState = TcpState.SYN_RECEIVED

    @Volatile var connected: Boolean = false

    val bytesFromClient = AtomicLong(0L)

    val bytesToClient = AtomicLong(0L)

    val rxBytes = AtomicLong(0L)
    val txBytes = AtomicLong(0L)

    @Volatile var lastActivityMs: Long = System.currentTimeMillis()

    @Volatile var closeAtMs: Long = 0L

    val expectedClientSeq: Long get() = seq(clientIsn + 1 + bytesFromClient.get())

    val nextOurSeq: Long get() = seq(ourIsn + 1 + bytesToClient.get())

    fun connect(vpnService: VpnService) {
        vpnService.protect(channel.socket())
        channel.connect(InetSocketAddress(remoteAddress, remotePort))
    }

    fun close() {
        runCatching { channel.close() }
    }

    companion object {
        const val SEQ_MASK = 0xFFFFFFFFL

        fun seq(value: Long): Long = value and SEQ_MASK
    }
}
