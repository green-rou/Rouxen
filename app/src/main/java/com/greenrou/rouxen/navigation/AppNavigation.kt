package com.greenrou.rouxen.navigation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.greenrou.rouxen.feature.history.db.ScanResultEntity
import com.greenrou.rouxen.feature.history.domain.ExportScanUseCase
import com.greenrou.rouxen.feature.history.domain.SaveScanUseCase
import com.greenrou.rouxen.feature.history.domain.ScanExportModel
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.feature.apps.AppsScreen
import com.greenrou.rouxen.feature.device.DeviceMonitorScreen
import com.greenrou.rouxen.feature.dns.DnsScreen
import com.greenrou.rouxen.feature.home.HomeScreen
import com.greenrou.rouxen.feature.home.SiteAnalyzerEntryScreen
import com.greenrou.rouxen.feature.map.MapScreen
import com.greenrou.rouxen.feature.ping.PingScreen
import com.greenrou.rouxen.feature.scan.ScanScreen
import com.greenrou.rouxen.feature.scan.ScanTab
import com.greenrou.rouxen.feature.settings.SettingsScreen
import com.greenrou.rouxen.feature.ssl.HeadersScreen
import com.greenrou.rouxen.feature.ssl.SslScreen
import com.greenrou.rouxen.feature.traceroute.TracerouteScreen
import com.greenrou.rouxen.feature.traffic.AppDetailScreen
import com.greenrou.rouxen.feature.traffic.ConnectionDetailScreen
import com.greenrou.rouxen.feature.traffic.TrafficMonitorScreen
import com.greenrou.rouxen.feature.whois.WhoisScreen
import com.greenrou.rouxen.feature.wifi.BleDeviceDetailScreen
import com.greenrou.rouxen.feature.wifi.WifiNetworkDetailScreen
import com.greenrou.rouxen.feature.wifi.WifiScannerScreen
import com.greenrou.rouxen.navigation.scan.AllScreen

private val topLevelRoutes = setOf(
    AppRoute.Home.route,
    AppRoute.Apps.route,
    AppRoute.Settings.route,
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
        containerColor = RouxenColors.Background,
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                BottomNavBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoute.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoute.Home.route) {
                HomeScreen(
                    onSiteAnalyzer = { navController.navigate(AppRoute.SiteAnalyzerEntry.route) },
                    onWifiScanner = { navController.navigate(AppRoute.WifiScanner.route) },
                    onDeviceMonitor = { navController.navigate(AppRoute.DeviceMonitor.route) },
                    onTrafficMonitor = { navController.navigate(AppRoute.TrafficMonitor.route) },
                )
            }

            composable(AppRoute.Apps.route) {
                AppsScreen()
            }

            composable(AppRoute.DeviceMonitor.route) {
                DeviceMonitorScreen(onBack = { navController.popBackStack() })
            }

            composable(AppRoute.TrafficMonitor.route) {
                TrafficMonitorScreen(
                    onBack = { navController.popBackStack() },
                    onAppClick = { uid ->
                        navController.navigate(AppRoute.TrafficAppDetail.createRoute(uid))
                    },
                    onConnectionClick = { key ->
                        navController.navigate(AppRoute.TrafficConnectionDetail.createRoute(key))
                    },
                )
            }

            composable(
                route = AppRoute.TrafficAppDetail.route,
                arguments = listOf(
                    navArgument(AppRoute.TrafficAppDetail.ARG_UID) { type = NavType.IntType }
                ),
            ) { backStackEntry ->
                val uid = backStackEntry.arguments?.getInt(AppRoute.TrafficAppDetail.ARG_UID) ?: 0
                AppDetailScreen(
                    uid = uid,
                    onBack = { navController.popBackStack() },
                    onConnectionClick = { key ->
                        navController.navigate(AppRoute.TrafficConnectionDetail.createRoute(key))
                    },
                )
            }

            composable(
                route = AppRoute.TrafficConnectionDetail.route,
                arguments = listOf(
                    navArgument(AppRoute.TrafficConnectionDetail.ARG_KEY) { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val key = Uri.decode(
                    backStackEntry.arguments?.getString(AppRoute.TrafficConnectionDetail.ARG_KEY) ?: ""
                )
                ConnectionDetailScreen(connectionKey = key, onBack = { navController.popBackStack() })
            }

            composable(AppRoute.Settings.route) {
                SettingsScreen()
            }

            composable(AppRoute.SiteAnalyzerEntry.route) {
                SiteAnalyzerEntryScreen(
                    onBack = { navController.popBackStack() },
                    onAnalyze = { url ->
                        navController.navigate(AppRoute.Scan.createRoute(url))
                    },
                )
            }

            composable(AppRoute.WifiScanner.route) {
                WifiScannerScreen(
                    onBack = { navController.popBackStack() },
                    onNetworkClick = { bssid ->
                        navController.navigate(AppRoute.WifiNetworkDetail.createRoute(bssid))
                    },
                    onDeviceClick = { address ->
                        navController.navigate(AppRoute.BleDeviceDetail.createRoute(address))
                    },
                )
            }

            composable(
                route = AppRoute.WifiNetworkDetail.route,
                arguments = listOf(
                    navArgument(AppRoute.WifiNetworkDetail.ARG_BSSID) { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val bssid = Uri.decode(
                    backStackEntry.arguments?.getString(AppRoute.WifiNetworkDetail.ARG_BSSID) ?: ""
                )
                WifiNetworkDetailScreen(bssid = bssid, onBack = { navController.popBackStack() })
            }

            composable(
                route = AppRoute.BleDeviceDetail.route,
                arguments = listOf(
                    navArgument(AppRoute.BleDeviceDetail.ARG_ADDRESS) { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val address = Uri.decode(
                    backStackEntry.arguments?.getString(AppRoute.BleDeviceDetail.ARG_ADDRESS) ?: ""
                )
                BleDeviceDetailScreen(address = address, onBack = { navController.popBackStack() })
            }

            composable(
                route = AppRoute.Scan.route,
                arguments = listOf(
                    navArgument(AppRoute.Scan.ARG_URL) { type = NavType.StringType }
                ),
            ) { backStackEntry ->
                val encodedUrl = backStackEntry.arguments?.getString(AppRoute.Scan.ARG_URL) ?: ""
                val url = Uri.decode(encodedUrl)
                val context = LocalContext.current
                val saveScan: SaveScanUseCase = koinInject()
                val exportScan: ExportScanUseCase = koinInject()
                val exportDateFmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }
                LaunchedEffect(url) {
                    saveScan(
                        ScanResultEntity(
                            url = url,
                            scannedAtMs = System.currentTimeMillis(),
                            dnsRecordCount = 0,
                            sslValid = false,
                            sslDaysUntilExpiry = null,
                            pingReachable = false,
                            pingLatencyMs = null,
                        )
                    )
                }
                ScanScreen(
                    url = url,
                    onBack = { navController.popBackStack() },
                    onExport = {
                        val json = exportScan(
                            ScanExportModel(
                                url = url,
                                scannedAt = exportDateFmt.format(Date()),
                            )
                        )
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, json)
                        }
                        context.startActivity(Intent.createChooser(intent, "Export scan"))
                    },
                    tabScreens = scanTabs(url),
                )
            }
        }
    }
}

@Composable
private fun scanTabs(url: String): Map<ScanTab, @Composable () -> Unit> = mapOf(
    ScanTab.ALL to { AllScreen(url = url) },
    ScanTab.DNS to { DnsScreen(url = url) },
    ScanTab.SSL to { SslScreen(url = url) },
    ScanTab.HEADERS to { HeadersScreen(url = url) },
    ScanTab.PING to { PingScreen(url = url) },
    ScanTab.WHOIS to { WhoisScreen(url = url) },
    ScanTab.TRACEROUTE to { TracerouteScreen(url = url) },
    ScanTab.MAP to { MapScreen(url = url) },
)
