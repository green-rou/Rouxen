package com.greenrou.rouxen.feature.wifi.scanner

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import com.greenrou.rouxen.feature.wifi.model.BleDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

class BleScanner(private val context: Context) {

    @SuppressLint("MissingPermission")
    fun scan(): Flow<List<BleDevice>> = callbackFlow {
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
        val leScanner = bluetoothManager?.adapter?.bluetoothLeScanner
        if (leScanner == null) {
            close()
            return@callbackFlow
        }

        val devices = mutableMapOf<String, BleDevice>()

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        val callback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                val mfId = result.scanRecord?.manufacturerSpecificData
                    ?.takeIf { it.size() > 0 }
                    ?.keyAt(0)
                devices[result.device.address] = BleDevice(
                    name = result.device.name ?: "Unknown Device",
                    address = result.device.address,
                    rssi = result.rssi,
                    manufacturerId = mfId,
                )
                trySend(devices.values.sortedByDescending { it.rssi })
            }

            override fun onScanFailed(errorCode: Int) {
                close(Exception("BLE scan failed (code $errorCode)"))
            }
        }

        leScanner.startScan(null, settings, callback)

        awaitClose {
            try { leScanner.stopScan(callback) } catch (_: Exception) {}
        }
    }.flowOn(Dispatchers.IO)
}
