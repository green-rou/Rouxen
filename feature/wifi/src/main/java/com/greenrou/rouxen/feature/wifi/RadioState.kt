package com.greenrou.rouxen.feature.wifi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import androidx.core.location.LocationManagerCompat

@SuppressLint("MissingPermission")
internal fun isBluetoothEnabled(context: Context): Boolean =
    context.getSystemService(BluetoothManager::class.java)?.adapter?.isEnabled == true

internal fun isWifiEnabled(context: Context): Boolean =
    (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)
        ?.isWifiEnabled == true

internal fun isLocationEnabled(context: Context): Boolean {
    val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    return lm != null && LocationManagerCompat.isLocationEnabled(lm)
}
