package com.greenrou.rouxen.feature.traffic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection

private data class RemoteGroup(
    val ip: String,
    val rxBytes: Long,
    val txBytes: Long,
    val connectionCount: Int,
    val apps: List<String>,
)

@Composable
fun RemoteAddressesTab(
    connections: List<TrafficConnection>,
    appLabels: Map<Int, String>,
    expandedIps: Set<String>,
    ipInfo: Map<String, IpInfoResponse>,
    onToggle: (String) -> Unit,
    resolveHostname: suspend (String) -> String?,
) {
    if (connections.isEmpty()) {
        EmptyHint("No active connections")
        return
    }
    val groups = remember(connections, appLabels) {
        connections.groupBy { it.remoteAddress.hostAddress }
            .map { (ip, conns) ->
                RemoteGroup(
                    ip = ip,
                    rxBytes = conns.sumOf { it.rxBytes },
                    txBytes = conns.sumOf { it.txBytes },
                    connectionCount = conns.size,
                    apps = conns.map { appLabels[it.uid] ?: "UID ${it.uid}" }.distinct(),
                )
            }
            .sortedByDescending { it.rxBytes + it.txBytes }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(items = groups, key = { it.ip }) { group ->
            RemoteAddressRow(
                group = group,
                expanded = group.ip in expandedIps,
                ipInfo = ipInfo[group.ip],
                onToggle = { onToggle(group.ip) },
                resolveHostname = resolveHostname,
            )
        }
    }
}

@Composable
private fun RemoteAddressRow(
    group: RemoteGroup,
    expanded: Boolean,
    ipInfo: IpInfoResponse?,
    onToggle: () -> Unit,
    resolveHostname: suspend (String) -> String?,
) {
    var hostname by remember(group.ip) { mutableStateOf<String?>(null) }
    var resolved by remember(group.ip) { mutableStateOf(false) }
    LaunchedEffect(group.ip) {
        hostname = resolveHostname(group.ip)
        resolved = true
    }

    RouxenCard(modifier = Modifier.clickable(onClick = onToggle)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hostname ?: if (resolved) group.ip else "Resolving…",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary,
                )
                if (hostname != null) {
                    Text(text = group.ip, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
                }
                Text(
                    text = "${group.connectionCount} connection${if (group.connectionCount != 1) "s" else ""}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "↓ ${formatBytes(group.rxBytes)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.Accent,
                )
                Text(
                    text = "↑ ${formatBytes(group.txBytes)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = group.apps.joinToString(", "),
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
        )

        if (expanded) {
            Spacer(modifier = Modifier.height(8.dp))
            if (ipInfo != null) {
                IpInfoRows(ipInfo)
            } else {
                Text(text = "Loading…", style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
            }
        }
    }
}

@Composable
private fun IpInfoRows(info: IpInfoResponse) {
    InfoRow("Country", info.country)
    InfoRow("Region", info.region)
    InfoRow("City", info.city)
    InfoRow("ISP", info.isp)
    InfoRow("Org", info.org)
    InfoRow("AS", info.asNumber)
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
