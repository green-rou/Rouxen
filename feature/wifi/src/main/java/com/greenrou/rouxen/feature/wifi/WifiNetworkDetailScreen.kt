package com.greenrou.rouxen.feature.wifi

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.wifi.model.WifiNetwork
import com.greenrou.rouxen.feature.wifi.model.frequencyBand
import com.greenrou.rouxen.feature.wifi.model.securityType

private val WifiNetwork.channel: Int
    get() = when {
        frequency < 3000 -> (frequency - 2407) / 5
        frequency < 5925 -> (frequency - 5000) / 5
        else -> (frequency - 5950) / 5 + 1
    }

private val WifiNetwork.signalQuality: Int
    get() = (rssi + 100).coerceIn(0, 100)

private val WifiNetwork.generation: String
    get() {
        val c = capabilities.uppercase()
        return when {
            "EHT" in c -> "Wi-Fi 7 (802.11be)"
            "HE" in c -> if (frequency >= 5925) "Wi-Fi 6E (802.11ax)" else "Wi-Fi 6 (802.11ax)"
            "VHT" in c -> "Wi-Fi 5 (802.11ac)"
            "HT" in c -> "Wi-Fi 4 (802.11n)"
            frequency < 3000 -> "Legacy (802.11g/b)"
            else -> "Legacy (802.11a)"
        }
    }

private val WifiNetwork.networkMode: String
    get() = when {
        "P2P" in capabilities -> "Wi-Fi Direct"
        "IBSS" in capabilities -> "Ad-Hoc"
        "ESS" in capabilities -> "Infrastructure"
        else -> "Unknown"
    }

private val WifiNetwork.wpsEnabled: Boolean
    get() = "WPS" in capabilities

private val WifiNetwork.encryption: String
    get() = buildList {
        if ("CCMP" in capabilities) add("CCMP (AES)")
        if ("TKIP" in capabilities) add("TKIP")
        if ("WEP" in capabilities) add("WEP")
    }.joinToString(" + ").ifEmpty { "None" }

private val WifiNetwork.securityDetail: String
    get() = capabilities
        .removePrefix("[")
        .removeSuffix("]")
        .split("][")
        .joinToString("\n")

private val WifiNetwork.isHidden: Boolean
    get() = ssid == "<hidden>"

private val OUI_VENDORS = mapOf(
    "00:17:F2" to "Apple", "00:23:12" to "Apple", "00:26:B9" to "Apple",
    "3C:5A:B4" to "Google", "54:60:09" to "Google", "F4:F5:DB" to "Google",
    "14:91:82" to "TP-Link", "F4:F2:6D" to "TP-Link", "50:FF:20" to "TP-Link",
    "E0:28:6D" to "Netgear", "A0:21:B7" to "Netgear", "C0:FF:D4" to "Netgear",
    "00:18:E7" to "Linksys", "CC:40:D0" to "Linksys", "20:AA:4B" to "Linksys",
    "1C:AF:F7" to "ASUS", "04:D4:C4" to "ASUS", "A8:9C:ED" to "Belkin",
    "28:CD:C1" to "D-Link", "00:26:18" to "Cisco", "00:23:33" to "Cisco",
    "50:57:A8" to "Huawei", "8C:34:FD" to "Huawei", "68:72:51" to "Xiaomi",
    "F4:8E:92" to "Xiaomi", "28:EE:52" to "Ubiquiti", "FC:EC:DA" to "Ubiquiti",
    "00:50:F2" to "Microsoft", "00:0C:E7" to "Motorola", "88:1F:A1" to "Eero",
)

private fun apVendor(bssid: String): String {
    val prefix = bssid.take(8).uppercase()
    return OUI_VENDORS.entries.firstOrNull { it.key.uppercase() == prefix }?.value ?: "Unknown"
}

@Composable
fun WifiNetworkDetailScreen(bssid: String, onBack: () -> Unit) {
    val network = WifiScanCache.wifiNetworks.firstOrNull { it.bssid == bssid }
    val connection = WifiScanCache.connectionInfo
        ?.takeIf { it.bssid.equals(bssid, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        DetailHeader(title = "WiFi Network", onBack = onBack)

        if (network == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Network not found in last scan",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                item {
                    RouxenCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DetailSectionLabel("Identity")
                            if (connection != null) {
                                StatusBadge(label = "Connected", status = BadgeStatus.Success)
                            }
                        }
                        DetailRow("SSID", if (network.isHidden) "<hidden>" else network.ssid)
                        DetailRow("BSSID", network.bssid)
                        if (network.isHidden) {
                            Spacer(modifier = Modifier.height(6.dp))
                            StatusBadge(label = "Hidden network", status = BadgeStatus.Warning)
                        }
                    }
                }

                if (connection != null) {
                    item {
                        RouxenCard {
                            DetailSectionLabel("Connection")
                            DetailRow("IP Address", connection.ipAddress)
                            DetailRow("Link Speed", "${connection.linkSpeedMbps} Mbps")
                            connection.rxLinkSpeedMbps?.let {
                                DetailRow("RX Link Speed", "$it Mbps")
                            }
                        }
                    }
                }

                item {
                    RouxenCard {
                        DetailSectionLabel("Signal")
                        DetailRow("RSSI", "${network.rssi} dBm")
                        DetailRow("Quality", "${network.signalQuality}%")
                        Spacer(modifier = Modifier.height(8.dp))
                        SignalBar(quality = network.signalQuality)
                    }
                }

                item {
                    RouxenCard {
                        DetailSectionLabel("Frequency")
                        DetailRow("Band", network.frequencyBand)
                        DetailRow("Frequency", "${network.frequency} MHz")
                        DetailRow("Channel", "${network.channel}")
                        DetailRow("Generation", network.generation)
                    }
                }

                item {
                    RouxenCard {
                        DetailSectionLabel("Network")
                        DetailRow("Mode", network.networkMode)
                        DetailRow("WPS", if (network.wpsEnabled) "Supported" else "Not supported")
                        DetailRow("Encryption", network.encryption)
                    }
                }

                item {
                    RouxenCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            DetailSectionLabel("Security")
                            StatusBadge(
                                label = network.securityType,
                                status = if (network.securityType == "Open") BadgeStatus.Warning
                                else BadgeStatus.Success,
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = network.securityDetail,
                            style = RouxenTypography.bodySmall,
                            color = RouxenColors.TextSecondary,
                        )
                    }
                }

                item {
                    RouxenCard {
                        DetailSectionLabel("AP Hardware")
                        DetailRow("Vendor", apVendor(network.bssid))
                        DetailRow("OUI", network.bssid.take(8).uppercase())
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
internal fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
        }
        Text(
            text = title,
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
internal fun DetailSectionLabel(label: String) {
    Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
internal fun DetailRow(label: String, value: String) {
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
internal fun SignalBar(quality: Int) {
    val color = when {
        quality >= 70 -> RouxenColors.Accent
        quality >= 40 -> RouxenColors.Warning
        else -> RouxenColors.Error
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
                .fillMaxWidth(quality / 100f)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
    }
}
