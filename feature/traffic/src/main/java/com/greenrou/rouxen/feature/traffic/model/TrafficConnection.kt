package com.greenrou.rouxen.feature.traffic.model

import java.net.InetAddress

enum class Protocol { TCP, UDP }

enum class ConnectionState { ESTABLISHED, CLOSING, CLOSED, ACTIVE, IDLE }

data class TrafficConnection(
    val protocol: Protocol,
    val localPort: Int,
    val remoteAddress: InetAddress,
    val remotePort: Int,
    val uid: Int,
    val rxBytes: Long,
    val txBytes: Long,
    val lastActivityMs: Long,
    val state: ConnectionState,
) {
    companion object {
        const val UID_UNKNOWN = -1
    }
}
