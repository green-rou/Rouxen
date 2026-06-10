package com.greenrou.rouxen.feature.device

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.device.model.AppMemoryStats
import com.greenrou.rouxen.feature.device.model.BatteryStats
import com.greenrou.rouxen.feature.device.model.CpuStats
import com.greenrou.rouxen.feature.device.model.DeviceInfo
import com.greenrou.rouxen.feature.device.model.DeviceStats
import com.greenrou.rouxen.feature.device.model.MemoryStats
import com.greenrou.rouxen.feature.device.model.StorageStats
import com.greenrou.rouxen.feature.device.model.usedPercent
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeviceMonitorScreen(
    onBack: () -> Unit,
    viewModel: DeviceMonitorViewModel = koinViewModel(),
) {
    val stats by viewModel.stats.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        MonitorHeader(onBack = onBack)

        val current = stats
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = RouxenColors.Accent,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            MonitorContent(current)
        }
    }
}

@Composable
private fun MonitorHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("← Home", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
        }
        Text(
            text = "Device Monitor",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
private fun MonitorContent(stats: DeviceStats) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }
        item { CpuCard(stats.cpu) }
        item { MemoryCard(stats.memory) }
        item { BatteryCard(stats.battery) }
        item { StorageCard(stats.storage) }
        item { AppMemoryCard(stats.appMemory) }
        item { DeviceInfoCard(stats.device) }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun CpuCard(cpu: CpuStats) {
    RouxenCard {
        MonitorSectionLabel("CPU")
        MonitorRow("App usage", "${cpu.usagePercent.toInt()}%")
        Spacer(modifier = Modifier.height(8.dp))
        MetricBar(percent = cpu.usagePercent.toInt())
        Spacer(modifier = Modifier.height(8.dp))
        MonitorRow("Cores", "${cpu.coreCount}")
        if (cpu.coreFrequenciesMhz.isNotEmpty()) {
            MonitorRow("Max frequency", "${cpu.coreFrequenciesMhz.max()} MHz")
            MonitorRow("Min frequency", "${cpu.coreFrequenciesMhz.min()} MHz")
        }
    }
}

@Composable
private fun MemoryCard(memory: MemoryStats) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonitorSectionLabel("RAM")
            if (memory.isLowMemory) {
                StatusBadge(label = "LOW MEMORY", status = BadgeStatus.Warning)
            }
        }
        MonitorRow("Used", "${memory.usedMb} MB / ${memory.totalMb} MB")
        Spacer(modifier = Modifier.height(8.dp))
        MetricBar(percent = memory.usedPercent)
        Spacer(modifier = Modifier.height(8.dp))
        MonitorRow("Available", "${memory.availableMb} MB")
        MonitorRow("Low memory threshold", "${memory.thresholdMb} MB")
    }
}

@Composable
private fun BatteryCard(battery: BatteryStats) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MonitorSectionLabel("Battery")
            StatusBadge(
                label = battery.status.uppercase(),
                status = if (battery.isCharging) BadgeStatus.Success else BadgeStatus.Neutral,
            )
        }
        MonitorRow("Level", "${battery.level}%")
        Spacer(modifier = Modifier.height(8.dp))
        MetricBar(percent = battery.level)
        Spacer(modifier = Modifier.height(8.dp))
        MonitorRow("Health", battery.health)
        MonitorRow("Temperature", "${battery.temperatureCelsius} °C")
        MonitorRow("Voltage", "${battery.voltageMv} mV")
        MonitorRow("Technology", battery.technology)
        MonitorRow("Power source", battery.chargePlug)
        battery.capacityMah?.let { MonitorRow("Capacity", "$it mAh") }
    }
}

@Composable
private fun StorageCard(storage: StorageStats) {
    RouxenCard {
        MonitorSectionLabel("Storage")
        MonitorRow("Used", "%.1f GB / %.1f GB".format(storage.usedGb, storage.totalGb))
        Spacer(modifier = Modifier.height(8.dp))
        MetricBar(percent = storage.usedPercent)
        Spacer(modifier = Modifier.height(8.dp))
        MonitorRow("Free", "%.1f GB".format(storage.freeGb))
    }
}

@Composable
private fun AppMemoryCard(appMemory: AppMemoryStats) {
    RouxenCard {
        MonitorSectionLabel("This App")
        MonitorRow("Java heap", "${appMemory.javaHeapUsedMb} MB / ${appMemory.javaHeapMaxMb} MB")
        MonitorRow("Native heap", "${appMemory.nativeHeapUsedMb} MB")
        MonitorRow("Total PSS", "${appMemory.totalPssMb} MB")
    }
}

@Composable
private fun DeviceInfoCard(device: DeviceInfo) {
    RouxenCard {
        MonitorSectionLabel("Device")
        MonitorRow("Model", device.model)
        MonitorRow("Manufacturer", device.manufacturer)
        MonitorRow("Android", "${device.androidVersion} (API ${device.sdkLevel})")
        MonitorRow("Thermal status", device.thermalStatus)
    }
}

@Composable
private fun MonitorSectionLabel(label: String) {
    Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun MonitorRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}

@Composable
private fun MetricBar(percent: Int) {
    val clamped = percent.coerceIn(0, 100)
    val color = when {
        clamped >= 85 -> RouxenColors.Error
        clamped >= 60 -> RouxenColors.Warning
        else -> RouxenColors.Accent
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(RouxenColors.Border),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped / 100f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}
