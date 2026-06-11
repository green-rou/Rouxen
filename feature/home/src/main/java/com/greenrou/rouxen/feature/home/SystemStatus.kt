package com.greenrou.rouxen.feature.home

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager

data class SystemStatusSnapshot(
    val batteryPercent: Int,
    val memoryUsedPercent: Int,
    val temperatureCelsius: Float,
    val wifiConnected: Boolean,
    val bluetoothEnabled: Boolean,
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

    val bluetoothEnabled = try {
        context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true
    } catch (_: SecurityException) {
        false
    }

    return SystemStatusSnapshot(
        batteryPercent = batteryPercent,
        memoryUsedPercent = memoryUsedPercent,
        temperatureCelsius = temperature,
        wifiConnected = wifiConnected,
        bluetoothEnabled = bluetoothEnabled,
    )
}
