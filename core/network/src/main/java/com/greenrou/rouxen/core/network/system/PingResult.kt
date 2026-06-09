package com.greenrou.rouxen.core.network.system

data class PingResult(
    val host: String,
    val isReachable: Boolean,
    val latencyMs: Long,
)
