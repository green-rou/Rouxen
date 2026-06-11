package com.greenrou.rouxen.feature.traffic

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.connectionKey
import com.greenrou.rouxen.feature.traffic.ui.DetailHeader
import com.greenrou.rouxen.feature.traffic.ui.DetailRow
import com.greenrou.rouxen.feature.traffic.ui.DetailSectionLabel
import com.greenrou.rouxen.feature.traffic.ui.formatBytes
import com.greenrou.rouxen.feature.traffic.ui.formatTimestamp
import com.greenrou.rouxen.feature.traffic.ui.stateBadgeStatus
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ConnectionDetailScreen(
    connectionKey: String,
    onBack: () -> Unit,
    viewModel: TrafficMonitorViewModel = koinViewModel(),
    ipInfoClient: IpInfoClient = koinInject(),
) {
    val connections by viewModel.connections.collectAsState()
    val appSummaries by viewModel.appSummaries.collectAsState()
    val connection = connections.firstOrNull { it.connectionKey == connectionKey }

    var hostname by remember(connectionKey) { mutableStateOf<String?>(null) }
    var ipInfo by remember(connectionKey) { mutableStateOf<IpInfoResponse?>(null) }
    var ipInfoFailed by remember(connectionKey) { mutableStateOf(false) }

    val remoteIp = connection?.remoteAddress?.hostAddress
    LaunchedEffect(remoteIp) {
        if (remoteIp == null) return@LaunchedEffect
        hostname = viewModel.resolveHostname(remoteIp)
        ipInfoClient.getInfo(remoteIp)
            .onSuccess { ipInfo = it }
            .onFailure { ipInfoFailed = true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        DetailHeader(title = "Connection Details", onBack = onBack)

        if (connection == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Connection no longer active",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            return@Column
        }

        val appLabel = appSummaries.firstOrNull { it.uid == connection.uid }?.appLabel
            ?: "UID ${connection.uid}"

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
                        DetailSectionLabel("Status")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            StatusBadge(label = connection.protocol.name, status = BadgeStatus.Neutral)
                            StatusBadge(
                                label = connection.state.name,
                                status = stateBadgeStatus(connection.state),
                            )
                        }
                    }
                    DetailRow("App", appLabel)
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Endpoint")
                    DetailRow("Local Port", "${connection.localPort}")
                    DetailRow("Remote Address", connection.remoteAddress.hostAddress ?: "—")
                    hostname?.let { DetailRow("Hostname", it) }
                    DetailRow("Remote Port", "${connection.remotePort}")
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Traffic")
                    DetailRow("Received", formatBytes(connection.rxBytes))
                    DetailRow("Sent", formatBytes(connection.txBytes))
                    DetailRow("Last Activity", formatTimestamp(connection.lastActivityMs))
                }
            }

            item {
                RouxenCard {
                    DetailSectionLabel("Remote Info")
                    val info = ipInfo
                    when {
                        info != null -> {
                            DetailRow("Country", info.country)
                            DetailRow("Region", info.region)
                            DetailRow("City", info.city)
                            DetailRow("ISP", info.isp)
                            DetailRow("Org", info.org)
                            DetailRow("AS", info.asNumber)
                        }
                        ipInfoFailed -> Text(
                            text = "Unavailable",
                            style = RouxenTypography.labelSmall,
                            color = RouxenColors.TextSecondary,
                        )
                        else -> Text(
                            text = "Loading…",
                            style = RouxenTypography.labelSmall,
                            color = RouxenColors.TextSecondary,
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}
