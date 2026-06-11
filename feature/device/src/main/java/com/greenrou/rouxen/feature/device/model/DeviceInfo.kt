package com.greenrou.rouxen.feature.device.model

data class DeviceInfo(
    val model: String,
    val manufacturer: String,
    val androidVersion: String,
    val sdkLevel: Int,
    val thermalStatus: String,
)
