package com.greenrou.rouxen.feature.wifi

sealed class BleGattState {
    object Disconnected : BleGattState()
    object Connecting : BleGattState()
    data class Connected(val services: List<GattService>) : BleGattState()
    data class Error(val message: String) : BleGattState()
}

data class GattService(
    val uuid: String,
    val name: String,
    val characteristics: List<GattCharacteristic>,
)

data class GattCharacteristic(
    val uuid: String,
    val name: String,
    val properties: Set<String>,
    val value: String?,
)
