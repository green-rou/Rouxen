package com.greenrou.rouxen.feature.wifi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@SuppressLint("MissingPermission")
class BleGattViewModel(private val context: Context) : ViewModel() {

    private val _gattState = MutableStateFlow<BleGattState>(BleGattState.Disconnected)
    val gattState = _gattState.asStateFlow()

    private var gatt: BluetoothGatt? = null
    private var liveServices: List<GattService> = emptyList()

    fun connect(address: String) {
        if (_gattState.value !is BleGattState.Disconnected) return
        _gattState.value = BleGattState.Connecting
        val manager = context.getSystemService(BluetoothManager::class.java)
        val device = manager?.adapter?.getRemoteDevice(address) ?: run {
            _gattState.value = BleGattState.Error("Bluetooth not available")
            return
        }
        gatt = device.connectGatt(context, false, callback, BluetoothDevice.TRANSPORT_LE)
    }

    fun disconnect() {
        gatt?.disconnect()
        gatt?.close()
        gatt = null
        liveServices = emptyList()
        _gattState.value = BleGattState.Disconnected
    }

    fun readCharacteristic(serviceUuid: String, charUuid: String) {
        val g = gatt ?: return
        val svc = g.services?.firstOrNull { it.uuid.toString().equals(serviceUuid, ignoreCase = true) } ?: return
        val ch = svc.getCharacteristic(UUID.fromString(charUuid)) ?: return
        if (ch.properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            g.readCharacteristic(ch)
        }
    }

    private val callback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(g: BluetoothGatt, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> g.discoverServices()
                BluetoothProfile.STATE_DISCONNECTED -> {
                    gatt?.close()
                    gatt = null
                    liveServices = emptyList()
                    _gattState.value = BleGattState.Disconnected
                }
            }
        }

        override fun onServicesDiscovered(g: BluetoothGatt, status: Int) {
            if (status != BluetoothGatt.GATT_SUCCESS) {
                _gattState.value = BleGattState.Error("Service discovery failed (status $status)")
                return
            }
            liveServices = g.services.map { svc ->
                GattService(
                    uuid = svc.uuid.toString(),
                    name = gattServiceName(svc.uuid.toString()),
                    characteristics = svc.characteristics.map { ch ->
                        GattCharacteristic(
                            uuid = ch.uuid.toString(),
                            name = gattCharName(ch.uuid.toString()),
                            properties = charProperties(ch.properties),
                            value = null,
                        )
                    },
                )
            }
            _gattState.value = BleGattState.Connected(liveServices)
        }

        @Deprecated("Deprecated in API 33")
        @Suppress("DEPRECATION")
        override fun onCharacteristicRead(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            status: Int,
        ) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                applyCharValue(ch.uuid.toString(), ch.value ?: ByteArray(0), status)
            }
        }

        override fun onCharacteristicRead(
            g: BluetoothGatt,
            ch: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int,
        ) {
            applyCharValue(ch.uuid.toString(), value, status)
        }
    }

    private fun applyCharValue(uuid: String, bytes: ByteArray, status: Int) {
        if (status != BluetoothGatt.GATT_SUCCESS) return
        val display = formatBytes(bytes)
        liveServices = liveServices.map { svc ->
            svc.copy(
                characteristics = svc.characteristics.map { ch ->
                    if (ch.uuid.equals(uuid, ignoreCase = true)) ch.copy(value = display) else ch
                },
            )
        }
        _gattState.value = BleGattState.Connected(liveServices)
    }

    private fun formatBytes(bytes: ByteArray): String {
        val ascii = bytes.filter { it in 32..126 }.map { it.toInt().toChar() }.joinToString("")
        val hex = bytes.joinToString(" ") { "%02X".format(it) }
        return if (ascii.length >= (bytes.size + 1) / 2) "\"$ascii\"" else hex
    }

    override fun onCleared() {
        super.onCleared()
        gatt?.disconnect()
        gatt?.close()
    }
}

internal fun gattServiceName(uuid: String): String = when (uuid.take(8).lowercase()) {
    "00001800" -> "Generic Access"
    "00001801" -> "Generic Attribute"
    "0000180a" -> "Device Information"
    "0000180f" -> "Battery"
    "0000180d" -> "Heart Rate"
    "00001810" -> "Blood Pressure"
    "00001816" -> "Cycling Speed & Cadence"
    "00001818" -> "Cycling Power"
    "00001819" -> "Location & Navigation"
    "00001803" -> "Link Loss"
    "00001802" -> "Immediate Alert"
    "00001804" -> "TX Power"
    "00001805" -> "Current Time"
    else -> "Service (${uuid.take(8).uppercase()})"
}

private fun gattCharName(uuid: String): String = when (uuid.take(8).lowercase()) {
    "00002a00" -> "Device Name"
    "00002a01" -> "Appearance"
    "00002a04" -> "Preferred Conn. Params"
    "00002a05" -> "Service Changed"
    "00002a19" -> "Battery Level"
    "00002a23" -> "System ID"
    "00002a24" -> "Model Number"
    "00002a25" -> "Serial Number"
    "00002a26" -> "Firmware Revision"
    "00002a27" -> "Hardware Revision"
    "00002a28" -> "Software Revision"
    "00002a29" -> "Manufacturer Name"
    "00002a37" -> "Heart Rate Measurement"
    "00002a38" -> "Body Sensor Location"
    "00002a6e" -> "Temperature"
    "00002a6f" -> "Humidity"
    "00002a76" -> "UV Index"
    "00002a9b" -> "Body Composition Feature"
    "00002a9c" -> "Body Composition Measurement"
    else -> uuid.take(8).uppercase()
}

private fun charProperties(props: Int): Set<String> = buildSet {
    if (props and BluetoothGattCharacteristic.PROPERTY_READ != 0) add("READ")
    if (props and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) add("WRITE")
    if (props and BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE != 0) add("WRITE NR")
    if (props and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) add("NOTIFY")
    if (props and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) add("INDICATE")
    if (props and BluetoothGattCharacteristic.PROPERTY_BROADCAST != 0) add("BROADCAST")
    if (props and BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE != 0) add("SIGNED WRITE")
}
