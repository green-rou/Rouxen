package com.greenrou.rouxen.feature.wifi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.wifi.scanner.BleScanner
import com.greenrou.rouxen.feature.wifi.scanner.WifiScanner
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WifiScannerViewModel(
    private val wifiScanner: WifiScanner,
    private val bleScanner: BleScanner,
) : ViewModel() {

    private val _wifiState = MutableStateFlow<WifiScanState>(WifiScanState.Idle)
    val wifiState = _wifiState.asStateFlow()

    private val _bleState = MutableStateFlow<BleScanState>(BleScanState.Idle)
    val bleState = _bleState.asStateFlow()

    private var wifiJob: Job? = null
    private var bleJob: Job? = null

    fun startWifiScan() {
        wifiJob?.cancel()
        _wifiState.value = WifiScanState.Scanning
        wifiJob = viewModelScope.launch {
            wifiScanner.scan()
                .catch { e -> _wifiState.value = WifiScanState.Error(e.message ?: "Scan failed") }
                .collect { networks ->
                WifiScanCache.wifiNetworks = networks
                _wifiState.value = WifiScanState.Success(networks)
            }
        }
    }

    fun startBleScan() {
        bleJob?.cancel()
        _bleState.value = BleScanState.Scanning()
        bleJob = viewModelScope.launch {
            bleScanner.scan()
                .catch { e -> _bleState.value = BleScanState.Error(e.message ?: "Scan failed") }
                .collect { devices ->
                WifiScanCache.bleDevices = devices
                _bleState.value = BleScanState.Scanning(devices)
            }
        }
    }

    fun stopBleScan() {
        bleJob?.cancel()
        bleJob = null
        val current = _bleState.value
        _bleState.value = BleScanState.Success(
            if (current is BleScanState.Scanning) current.devices else emptyList()
        )
    }

    override fun onCleared() {
        super.onCleared()
        wifiJob?.cancel()
        bleJob?.cancel()
    }
}
