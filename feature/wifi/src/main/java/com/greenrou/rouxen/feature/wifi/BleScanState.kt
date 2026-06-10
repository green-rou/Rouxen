package com.greenrou.rouxen.feature.wifi

import com.greenrou.rouxen.feature.wifi.model.BleDevice

sealed class BleScanState {
    object Idle : BleScanState()
    data class Scanning(val devices: List<BleDevice> = emptyList()) : BleScanState()
    data class Success(val devices: List<BleDevice>) : BleScanState()
    data class Error(val message: String) : BleScanState()
}
