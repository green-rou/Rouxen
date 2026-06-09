package com.greenrou.rouxen.core.network.traceroute

data class TracerouteHop(
    val hop: Int,
    val host: String?,
    val latencyMs: Long?,
)
