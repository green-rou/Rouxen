package com.greenrou.rouxen.feature.traffic.ui

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.ConnectionState
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection

@Composable
fun ConnectionsTab(connections: List<TrafficConnection>, appLabels: Map<Int, String>) {
    if (connections.isEmpty()) {
        EmptyHint("No active connections")
        return
    }
    val sorted = remember(connections) { connections.sortedByDescending { it.lastActivityMs } }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = sorted,
            key = { "${it.protocol}:${it.localPort}:${it.remoteAddress.hostAddress}:${it.remotePort}" },
        ) { connection ->
            ConnectionRow(connection, appLabels[connection.uid] ?: "UID ${connection.uid}")
        }
    }
}

@Composable
private fun ConnectionRow(connection: TrafficConnection, appLabel: String) {
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge(label = connection.protocol.name, status = BadgeStatus.Neutral)
                StatusBadge(label = connection.state.name, status = stateBadgeStatus(connection.state))
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "↓ ${formatBytes(connection.rxBytes)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.Accent,
                )
                Text(
                    text = "↑ ${formatBytes(connection.txBytes)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = appLabel, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
        Text(
            text = "${connection.localPort} → ${connection.remoteAddress.hostAddress}:${connection.remotePort}",
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
        )
    }
}

private fun stateBadgeStatus(state: ConnectionState): BadgeStatus = when (state) {
    ConnectionState.ESTABLISHED, ConnectionState.ACTIVE -> BadgeStatus.Success
    ConnectionState.CLOSING -> BadgeStatus.Warning
    ConnectionState.IDLE, ConnectionState.CLOSED -> BadgeStatus.Neutral
}
