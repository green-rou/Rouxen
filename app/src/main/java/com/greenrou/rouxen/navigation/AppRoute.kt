package com.greenrou.rouxen.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    object Home : AppRoute("home")
    object Settings : AppRoute("settings")
    object Apps : AppRoute("apps")
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

    object TrafficAppDetail : AppRoute("traffic_app/{uid}") {
        const val ARG_UID = "uid"
        fun createRoute(uid: Int) = "traffic_app/$uid"
    }

    object TrafficConnectionDetail : AppRoute("traffic_connection/{key}") {
        const val ARG_KEY = "key"
        fun createRoute(key: String) = "traffic_connection/${Uri.encode(key)}"
    }

    object Scan : AppRoute("scan/{url}") {
        const val ARG_URL = "url"
        fun createRoute(url: String): String = "scan/${Uri.encode(url)}"
    }
}
