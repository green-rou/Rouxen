package com.greenrou.rouxen.feature.home

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.BatteryManager
import androidx.core.location.LocationManagerCompat

data class SystemStatusSnapshot(
    val batteryPercent: Int,
    val memoryUsedPercent: Int,
    val temperatureCelsius: Float,
    val wifiConnected: Boolean,
    val wifiEnabled: Boolean,
    val wifiSsid: String?,
    val bluetoothEnabled: Boolean,
    val bluetoothDeviceName: String?,
    val locationEnabled: Boolean,
)

fun readSystemStatus(context: Context): SystemStatusSnapshot {
    val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
    val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
    val batteryPercent = if (scale > 0) level * 100 / scale else 0
    val temperature = (batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f

    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = ActivityManager.MemoryInfo()
    am.getMemoryInfo(memInfo)
    val memoryUsedPercent = if (memInfo.totalMem > 0) {
        (((memInfo.totalMem - memInfo.availMem) * 100) / memInfo.totalMem).toInt()
    } else 0

    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val caps = cm.getNetworkCapabilities(cm.activeNetwork)
    val wifiConnected = caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

    val wifiManager = context.applicationContext
        .getSystemService(Context.WIFI_SERVICE) as? WifiManager
    val wifiEnabled = wifiManager?.isWifiEnabled == true

    val wifiSsid = if (wifiConnected) {
        @Suppress("DEPRECATION")
        wifiManager?.connectionInfo?.ssid
            ?.trim('"')
            ?.takeIf { it.isNotBlank() && it != "<unknown ssid>" }
    } else null

    val bluetoothEnabled = try {
        context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
    } catch (_: SecurityException) {
        false
    }

    val bluetoothDeviceName = if (bluetoothEnabled) {
        try {
            context.getSystemService(BluetoothManager::class.java)?.adapter?.bondedDevices
                ?.firstOrNull { device ->
                    try {
                        device.javaClass.getMethod("isConnected").invoke(device) as? Boolean == true
                    } catch (_: Exception) {
                        false
                    }
                }
                ?.name
        } catch (_: SecurityException) {
            null
        }
    } else null

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    val locationEnabled = locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager)

    return SystemStatusSnapshot(
        batteryPercent = batteryPercent,
        memoryUsedPercent = memoryUsedPercent,
        temperatureCelsius = temperature,
        wifiConnected = wifiConnected,
        wifiEnabled = wifiEnabled,
        wifiSsid = wifiSsid,
        bluetoothEnabled = bluetoothEnabled,
        bluetoothDeviceName = bluetoothDeviceName,
        locationEnabled = locationEnabled,
    )
}
