package com.greenrou.rouxen.feature.traffic.model

data class ConnectedDevice(
    val ipAddress: String,
    val macAddress: String,
    val vendor: String?,
    val hostname: String?,
)
