package com.greenrou.rouxen.feature.wifi.model

data class WifiConnectionInfo(
    val bssid: String,
    val linkSpeedMbps: Int,
    val rxLinkSpeedMbps: Int?,
    val ipAddress: String,
)
