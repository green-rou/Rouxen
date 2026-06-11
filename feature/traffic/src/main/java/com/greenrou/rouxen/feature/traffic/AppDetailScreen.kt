package com.greenrou.rouxen.feature.traffic

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection
import com.greenrou.rouxen.feature.traffic.model.connectionKey
import com.greenrou.rouxen.feature.traffic.ui.DetailHeader
import com.greenrou.rouxen.feature.traffic.ui.DetailRow
import com.greenrou.rouxen.feature.traffic.ui.DetailSectionLabel
import com.greenrou.rouxen.feature.traffic.ui.formatBytes
import com.greenrou.rouxen.feature.traffic.ui.formatRate
import com.greenrou.rouxen.feature.traffic.ui.stateBadgeStatus
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppDetailScreen(
    uid: Int,
    onBack: () -> Unit,
    onConnectionClick: (String) -> Unit,
    viewModel: TrafficMonitorViewModel = koinViewModel(),
) {
    val appSummaries by viewModel.appSummaries.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val summary = appSummaries.firstOrNull { it.uid == uid }
    val appConnections = remember(connections, uid) {
        connections.filter { it.uid == uid }.sortedByDescending { it.lastActivityMs }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        DetailHeader(title = "App Details", onBack = onBack)

        if (summary == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "App no longer active",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            return@Column
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                RouxenCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppIcon(packageName = summary.packageName)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = summary.appLabel,
                                style = RouxenTypography.bodyMedium,
                                color = RouxenColors.TextPrimary,
                            )
                            if (summary.packageName != null) {
                                Text(
                                    text = summary.packageName,
                                    style = RouxenTypography.labelSmall,
                                    color = RouxenColors.TextSecondary,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    DetailRow("UID", "${summary.uid}")
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Traffic")
                    DetailRow("Download rate", "↓ ${formatRate(summary.rxRateBps)}")
                    DetailRow("Upload rate", "↑ ${formatRate(summary.txRateBps)}")
                    DetailRow("Total received", formatBytes(summary.rxTotalBytes))
                    DetailRow("Total sent", formatBytes(summary.txTotalBytes))
                    DetailRow("Connections", "${summary.connectionCount}")
                }
            }

            item { DetailSectionLabel("Connections") }

            items(appConnections, key = { it.connectionKey }) { connection ->
                AppConnectionRow(
                    connection = connection,
                    onClick = { onConnectionClick(connection.connectionKey) },
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AppIcon(packageName: String?) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        packageName?.let {
            try {
                context.packageManager.getApplicationIcon(it).toBitmap().asImageBitmap()
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }
    if (icon != null) {
        Image(bitmap = icon, contentDescription = null, modifier = Modifier.size(40.dp))
    } else {
        Box(modifier = Modifier.size(40.dp).background(RouxenColors.Border))
    }
}

@Composable
private fun AppConnectionRow(connection: TrafficConnection, onClick: () -> Unit) {
    RouxenCard(modifier = Modifier.clickable(onClick = onClick)) {
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
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "${connection.localPort} → ${connection.remoteAddress.hostAddress}:${connection.remotePort}",
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
        )
    }
}
