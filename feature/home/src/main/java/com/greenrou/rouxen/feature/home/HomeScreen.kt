package com.greenrou.rouxen.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.feature.home.R
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import kotlinx.coroutines.delay

private data class Tool(
    @DrawableRes val iconRes: Int,
    val title: String,
    val subtitle: String,
    val available: Boolean = true,
)

private val tools = listOf(
    Tool(R.drawable.ic_tool_analyzer, "Site Analyzer", "DNS · SSL · Headers"),
    Tool(R.drawable.ic_tool_wifi, "WiFi & BLE", "Networks & devices"),
    Tool(R.drawable.ic_tool_monitor, "Device Monitor", "CPU · RAM · Battery"),
    Tool(R.drawable.ic_tool_traffic, "Traffic Monitor", "Live usage · Connections"),
)

@Composable
fun HomeScreen(
    onSiteAnalyzer: () -> Unit,
    onWifiScanner: () -> Unit,
    onDeviceMonitor: () -> Unit,
    onTrafficMonitor: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Rouxen",
            style = RouxenTypography.titleMedium,
            color = RouxenColors.Accent,
        )

        Spacer(modifier = Modifier.height(24.dp))

        val context = LocalContext.current
        var status by remember { mutableStateOf(readSystemStatus(context)) }
        LaunchedEffect(Unit) {
            while (true) {
                status = readSystemStatus(context)
                delay(3000)
            }
        }
        SystemStatusBar(status)

        Spacer(modifier = Modifier.height(24.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tools) { tool ->
                ToolTile(
                    tool = tool,
                    onClick = when (tool.title) {
                        "Site Analyzer" -> onSiteAnalyzer
                        "WiFi & BLE" -> onWifiScanner
                        "Device Monitor" -> onDeviceMonitor
                        "Traffic Monitor" -> onTrafficMonitor
                        else -> null
                    },
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LiveActivityCard(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ToolTile(tool: Tool, onClick: (() -> Unit)?) {
    val alpha = if (tool.available) 1f else 0.4f
    val borderColor = if (tool.available) RouxenColors.Border
    else RouxenColors.Border.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(RouxenColors.Surface)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .then(
                if (onClick != null && tool.available) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                painter = painterResource(id = tool.iconRes),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = RouxenColors.Accent.copy(alpha = alpha),
            )
            Column {
                Text(
                    text = tool.title,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary.copy(alpha = alpha),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (tool.available) tool.subtitle else "Coming soon",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary.copy(alpha = alpha),
                )
            }
        }
    }
}

@Composable
private fun SystemStatusBar(status: SystemStatusSnapshot) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            StatusItem(R.drawable.ic_status_battery, "${status.batteryPercent}%", Modifier.weight(1f))
            StatusItem(R.drawable.ic_status_memory, "${status.memoryUsedPercent}%", Modifier.weight(1f))
            StatusItem(R.drawable.ic_status_temp, "${status.temperatureCelsius.toInt()}°C", Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            val wifiLabel = when {
                status.wifiSsid != null -> status.wifiSsid
                !status.wifiEnabled -> "Off"
                else -> "No WiFi"
            }
            ConnectivityStatusItem(
                iconRes = R.drawable.ic_tool_wifi,
                label = wifiLabel,
                active = status.wifiConnected,
                modifier = Modifier.weight(1f),
            )

            val bluetoothLabel = when {
                status.bluetoothDeviceName != null -> status.bluetoothDeviceName
                status.bluetoothEnabled -> "On"
                else -> "Off"
            }
            ConnectivityStatusItem(
                iconRes = R.drawable.ic_status_bluetooth,
                label = bluetoothLabel,
                active = status.bluetoothDeviceName != null,
                modifier = Modifier.weight(1f),
            )

            ConnectivityStatusItem(
                iconRes = R.drawable.ic_status_location,
                label = if (status.locationEnabled) "On" else "Off",
                active = status.locationEnabled,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatusItem(@DrawableRes iconRes: Int, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = RouxenColors.Accent,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
    }
}

@Composable
private fun ConnectivityStatusItem(
    @DrawableRes iconRes: Int,
    label: String,
    active: Boolean,
    modifier: Modifier = Modifier,
) {
    val tint = if (active) RouxenColors.Accent else RouxenColors.TextSecondary
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = RouxenTypography.labelSmall,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}
