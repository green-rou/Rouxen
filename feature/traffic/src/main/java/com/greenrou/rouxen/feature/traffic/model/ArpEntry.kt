package com.greenrou.rouxen.feature.traffic.model

data class ArpEntry(
    val ipAddress: String,
    val macAddress: String,
    val device: String,
)
