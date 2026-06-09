package com.greenrou.rouxen.feature.wifi.model

data class BleDevice(
    val name: String,
    val address: String,
    val rssi: Int,
    val manufacturerId: Int?,
)

val BleDevice.level: Int
    get() = when {
        rssi >= -50 -> 4
        rssi >= -65 -> 3
        rssi >= -75 -> 2
        rssi >= -85 -> 1
        else -> 0
    }

val BleDevice.distanceLabel: String
    get() = when {
        rssi >= -50 -> "Immediate"
        rssi >= -70 -> "Near"
        rssi >= -85 -> "Far"
        else -> "Very far"
    }
