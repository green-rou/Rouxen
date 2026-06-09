package com.greenrou.rouxen.core.network.system

data class PortResult(
    val port: Int,
    val isOpen: Boolean,
    val serviceName: String,
)
