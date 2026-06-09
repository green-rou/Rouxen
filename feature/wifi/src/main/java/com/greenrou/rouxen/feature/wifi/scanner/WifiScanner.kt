package com.greenrou.rouxen.feature.wifi.scanner

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import com.greenrou.rouxen.feature.wifi.model.WifiNetwork
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers

class WifiScanner(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun scan(): Flow<List<WifiNetwork>> = callbackFlow {
        val wifiManager = context.applicationContext
            .getSystemService(Context.WIFI_SERVICE) as WifiManager

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    val results = wifiManager.scanResults.map { sr ->
                        WifiNetwork(
                            ssid = sr.SSID.ifBlank { "<hidden>" },
                            bssid = sr.BSSID,
                            rssi = sr.level,
                            frequency = sr.frequency,
                            capabilities = sr.capabilities,
                        )
                    }.sortedByDescending { it.rssi }
                    trySend(results)
                }
            }
        }

        context.registerReceiver(
            receiver,
            IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION),
        )

        @Suppress("DEPRECATION")
        wifiManager.startScan()

        awaitClose { context.unregisterReceiver(receiver) }
    }.flowOn(Dispatchers.IO)
}
