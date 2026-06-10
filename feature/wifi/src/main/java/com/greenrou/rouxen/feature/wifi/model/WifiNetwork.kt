package com.greenrou.rouxen.feature.wifi.model

data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val capabilities: String,
)

val WifiNetwork.level: Int
    get() = when {
        rssi >= -55 -> 4
        rssi >= -66 -> 3
        rssi >= -77 -> 2
        rssi >= -88 -> 1
        else -> 0
    }

val WifiNetwork.frequencyBand: String
    get() = when {
        frequency < 3000 -> "2.4 GHz"
        frequency < 5925 -> "5 GHz"
        else -> "6 GHz"
    }

val WifiNetwork.securityType: String
    get() = when {
        "WPA3" in capabilities -> "WPA3"
        "WPA2" in capabilities -> "WPA2"
        "WPA" in capabilities -> "WPA"
        "WEP" in capabilities -> "WEP"
        else -> "Open"
    }
