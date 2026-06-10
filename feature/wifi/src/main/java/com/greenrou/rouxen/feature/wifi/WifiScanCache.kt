package com.greenrou.rouxen.feature.wifi

import com.greenrou.rouxen.feature.wifi.model.BleDevice
import com.greenrou.rouxen.feature.wifi.model.WifiNetwork

object WifiScanCache {
    var wifiNetworks: List<WifiNetwork> = emptyList()
    var bleDevices: List<BleDevice> = emptyList()
}
