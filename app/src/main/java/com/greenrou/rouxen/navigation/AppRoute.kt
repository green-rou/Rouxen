package com.greenrou.rouxen.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    object Home : AppRoute("home")
    object Settings : AppRoute("settings")
    object SiteAnalyzerEntry : AppRoute("site_analyzer")
    object WifiScanner : AppRoute("wifi_scanner")
    object DeviceMonitor : AppRoute("device_monitor")
    object TrafficMonitor : AppRoute("traffic_monitor")

    object WifiNetworkDetail : AppRoute("wifi_detail/{bssid}") {
        const val ARG_BSSID = "bssid"
        fun createRoute(bssid: String) = "wifi_detail/${Uri.encode(bssid)}"
    }

    object BleDeviceDetail : AppRoute("ble_detail/{address}") {
        const val ARG_ADDRESS = "address"
        fun createRoute(address: String) = "ble_detail/${Uri.encode(address)}"
    }

    object Scan : AppRoute("scan/{url}") {
        const val ARG_URL = "url"
        fun createRoute(url: String): String = "scan/${Uri.encode(url)}"
    }
}
