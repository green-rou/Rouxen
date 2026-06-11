package com.greenrou.rouxen.feature.device.model

data class BatteryStats(
    val level: Int,
    val status: String,
    val health: String,
    val temperatureCelsius: Float,
    val voltageMv: Int,
    val technology: String,
    val isCharging: Boolean,
    val chargePlug: String,
    val capacityMah: Int?,
)
