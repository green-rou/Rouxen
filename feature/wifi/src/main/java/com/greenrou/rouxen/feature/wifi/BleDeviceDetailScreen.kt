package com.greenrou.rouxen.feature.wifi

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.wifi.model.distanceLabel
import org.koin.androidx.compose.koinViewModel

@SuppressLint("MissingPermission")
@Composable
fun BleDeviceDetailScreen(
    address: String,
    onBack: () -> Unit,
    viewModel: BleGattViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val device = WifiScanCache.bleDevices.firstOrNull { it.address == address }
    val gattState by viewModel.gattState.collectAsState()

    DisposableEffect(Unit) {
        onDispose { viewModel.disconnect() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        DetailHeader(title = "BLE Device", onBack = onBack)

        if (device == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Device not found in last scan",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            return@Column
        }

        val btManager = context.getSystemService(BluetoothManager::class.java)
        val btDevice = btManager?.adapter?.getRemoteDevice(address)
        val bondState = when (btDevice?.bondState) {
            BluetoothDevice.BOND_BONDED -> "Bonded"
            BluetoothDevice.BOND_BONDING -> "Bonding..."
            else -> "Not bonded"
        }
        val deviceType = when (btDevice?.type) {
            BluetoothDevice.DEVICE_TYPE_CLASSIC -> "Classic"
            BluetoothDevice.DEVICE_TYPE_LE -> "BLE only"
            BluetoothDevice.DEVICE_TYPE_DUAL -> "Dual-mode"
            else -> "Unknown"
        }
        val signalQuality = (device.rssi + 100).coerceIn(0, 100)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                RouxenCard {
                    DetailSectionLabel("Identity")
                    DetailRow("Name", device.name)
                    DetailRow("MAC", device.address)
                    DetailRow("Type", deviceType)
                    DetailRow("Bond", bondState)
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Manufacturer")
                    val mfId = device.manufacturerId
                    if (mfId != null) {
                        DetailRow("ID", "0x${mfId.toString(16).uppercase().padStart(4, '0')}")
                        DetailRow("Name", manufacturerName(mfId))
                    } else {
                        Text(
                            text = "No manufacturer advertisement data",
                            style = RouxenTypography.bodySmall,
                            color = RouxenColors.TextSecondary,
                        )
                    }
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Signal")
                    DetailRow("RSSI", "${device.rssi} dBm")
                    DetailRow("Quality", "$signalQuality%")
                    DetailRow("Distance", device.distanceLabel)
                    Spacer(modifier = Modifier.height(8.dp))
                    SignalBar(quality = signalQuality)
                }
            }

            item {
                GattSection(
                    state = gattState,
                    onConnect = { viewModel.connect(address) },
                    onDisconnect = { viewModel.disconnect() },
                    onRead = { svcUuid, charUuid -> viewModel.readCharacteristic(svcUuid, charUuid) },
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun GattSection(
    state: BleGattState,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onRead: (svcUuid: String, charUuid: String) -> Unit,
) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DetailSectionLabel("GATT Connection")
            when (state) {
                is BleGattState.Disconnected -> Button(
                    onClick = onConnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RouxenColors.Accent,
                        contentColor = RouxenColors.Background,
                    ),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Connect", style = RouxenTypography.labelMedium)
                }
                is BleGattState.Connecting -> Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        color = RouxenColors.Accent,
                        strokeWidth = 1.5.dp,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Connecting…", style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
                }
                is BleGattState.Connected -> Button(
                    onClick = onDisconnect,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RouxenColors.Error,
                        contentColor = RouxenColors.TextPrimary,
                    ),
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text("Disconnect", style = RouxenTypography.labelMedium)
                }
                is BleGattState.Error -> TextButton(onClick = onConnect) {
                    Text("Retry", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
                }
            }
        }

        when (state) {
            is BleGattState.Error -> {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.message,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.Error,
                )
            }
            is BleGattState.Connected -> {
                Spacer(modifier = Modifier.height(12.dp))
                state.services.forEach { svc ->
                    ServiceRow(service = svc, onRead = onRead)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            else -> Unit
        }
    }
}

@Composable
private fun ServiceRow(
    service: GattService,
    onRead: (svcUuid: String, charUuid: String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(RouxenColors.Background)
            .border(1.dp, RouxenColors.Border, RoundedCornerShape(4.dp))
            .padding(10.dp),
    ) {
        Text(
            text = service.name,
            style = RouxenTypography.labelMedium,
            color = RouxenColors.TextPrimary,
        )
        Text(
            text = service.uuid,
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        service.characteristics.forEach { char ->
            CharacteristicRow(
                char = char,
                onRead = { onRead(service.uuid, char.uuid) },
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CharacteristicRow(char: GattCharacteristic, onRead: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = char.name,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextPrimary,
                )
                Text(
                    text = char.uuid,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            if ("READ" in char.properties) {
                TextButton(
                    onClick = onRead,
                    modifier = Modifier.height(28.dp),
                ) {
                    Text("Read", style = RouxenTypography.labelSmall, color = RouxenColors.Accent)
                }
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            char.properties.forEach { prop ->
                PropChip(prop)
            }
        }
        char.value?.let { value ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = RouxenTypography.bodySmall,
                color = RouxenColors.Accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(RouxenColors.Accent.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            )
        }
    }
}

@Composable
private fun PropChip(label: String) {
    Text(
        text = label,
        style = RouxenTypography.labelSmall,
        color = RouxenColors.TextSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(RouxenColors.Border)
            .padding(horizontal = 5.dp, vertical = 2.dp),
    )
}

private fun manufacturerName(id: Int): String = when (id) {
    0x004C -> "Apple"
    0x0006 -> "Microsoft"
    0x0075 -> "Samsung"
    0x00E0 -> "Google"
    0x038F -> "Bose"
    0x0157 -> "Xiaomi"
    0x0087 -> "Garmin"
    0x0171 -> "Amazon"
    0x0499 -> "Ruuvi Innovations"
    0x0059 -> "Nordic Semiconductor"
    0x0025 -> "Apple"
    0x01FF -> "Sony"
    0x004F -> "Samsung Electronics"
    else -> "Unknown (0x${id.toString(16).uppercase().padStart(4, '0')})"
}
