package com.greenrou.rouxen.feature.traffic.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.ConnectedDevice

@Composable
fun ConnectedDevicesTab(devices: List<ConnectedDevice>) {
    if (devices.isEmpty()) {
        EmptyHint(
            "No devices found. This may require an active hotspot, or /proc/net/arp may be " +
                "restricted on this device.",
        )
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = devices, key = { it.macAddress }) { device ->
            DeviceRow(device)
        }
    }
}

@Composable
private fun DeviceRow(device: ConnectedDevice) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.hostname ?: device.ipAddress,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary,
                )
                Text(
                    text = device.ipAddress,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = device.macAddress,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
                Text(
                    text = device.vendor ?: "Unknown vendor",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        }
    }
}
