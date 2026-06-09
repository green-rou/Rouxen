package com.greenrou.rouxen.feature.ping

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.system.PortResult
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun PingScreen(
    url: String,
    viewModel: PingViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.analyze(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is PingState.Idle -> Unit
            is PingState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is PingState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is PingState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    RouxenCard {
                        Text("Latency", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (s.ping.isReachable) "${s.ping.latencyMs} ms" else "Unreachable",
                                style = RouxenTypography.bodyLarge,
                                color = latencyColor(s.ping.latencyMs, s.ping.isReachable),
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            StatusBadge(
                                label = if (s.ping.isReachable) "REACHABLE" else "TIMEOUT",
                                status = if (s.ping.isReachable) BadgeStatus.Success else BadgeStatus.Error,
                            )
                        }
                    }
                }
                item {
                    RouxenCard {
                        Text("Ports", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
                        Spacer(modifier = Modifier.height(8.dp))
                        s.ports.forEach { port ->
                            PortRow(port)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PortRow(port: PortResult) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(text = "${port.port}", style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
            Text(text = port.serviceName, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        }
        StatusBadge(
            label = if (port.isOpen) "OPEN" else "CLOSED",
            status = if (port.isOpen) BadgeStatus.Success else BadgeStatus.Neutral,
        )
    }
}

private fun latencyColor(latencyMs: Long, isReachable: Boolean) = when {
    !isReachable -> RouxenColors.Error
    latencyMs < 50 -> RouxenColors.Accent
    latencyMs < 200 -> RouxenColors.Warning
    else -> RouxenColors.Error
}
