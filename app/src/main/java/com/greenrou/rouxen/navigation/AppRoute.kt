package com.greenrou.rouxen.navigation

import android.net.Uri

sealed class AppRoute(val route: String) {
    object Home : AppRoute("home")
    object History : AppRoute("history")

    object Scan : AppRoute("scan/{url}") {
        const val ARG_URL = "url"
        fun createRoute(url: String): String = "scan/${Uri.encode(url)}"
    }

    object ScanDetail : AppRoute("scan_detail/{id}") {
        const val ARG_ID = "id"
        fun createRoute(id: Long): String = "scan_detail/$id"
    }
}
