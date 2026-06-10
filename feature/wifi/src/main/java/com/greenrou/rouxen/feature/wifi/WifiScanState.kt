package com.greenrou.rouxen.feature.wifi

import com.greenrou.rouxen.feature.wifi.model.WifiNetwork

sealed class WifiScanState {
    object Idle : WifiScanState()
    object Scanning : WifiScanState()
    data class Success(val networks: List<WifiNetwork>) : WifiScanState()
    data class Error(val message: String) : WifiScanState()
}
