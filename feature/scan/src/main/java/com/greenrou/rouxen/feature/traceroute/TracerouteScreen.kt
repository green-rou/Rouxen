package com.greenrou.rouxen.feature.traceroute

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.traceroute.TracerouteHop
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun TracerouteScreen(
    url: String,
    viewModel: TracerouteViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.trace(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is TracerouteState.Idle -> Unit
            is TracerouteState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is TracerouteState.Unsupported -> TracerouteUnsupportedScreen()
            is TracerouteState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is TracerouteState.Running -> HopList(hops = s.hops, isRunning = true)
            is TracerouteState.Complete -> HopList(hops = s.hops, isRunning = false)
        }
    }
}

@Composable
private fun HopList(hops: List<TracerouteHop>, isRunning: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (isRunning) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(12.dp),
                        color = RouxenColors.Accent,
                        strokeWidth = 1.5.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tracing…", style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        items(hops, key = { it.hop }) { hop ->
            HopRow(hop)
        }
    }
}

@Composable
private fun HopRow(hop: TracerouteHop) {
    val latencyMs = hop.latencyMs
    RouxenCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = String.format("%2d", hop.hop),
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
                modifier = Modifier.width(28.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = hop.host ?: "*",
                    style = RouxenTypography.bodySmall,
                    color = if (hop.host != null) RouxenColors.TextPrimary else RouxenColors.TextSecondary,
                )
                if (latencyMs != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    LatencyBar(latencyMs)
                }
            }
            if (latencyMs != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$latencyMs ms",
                    style = RouxenTypography.labelSmall,
                    color = latencyColor(latencyMs),
                )
            }
        }
    }
}

@Composable
private fun LatencyBar(latencyMs: Long) {
    val fraction = (latencyMs / 500f).coerceIn(0f, 1f)
    LinearProgressIndicator(
        progress = { fraction },
        modifier = Modifier.fillMaxWidth().height(2.dp),
        color = latencyColor(latencyMs),
        trackColor = RouxenColors.Border,
    )
}

@Composable
fun TracerouteUnsupportedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            Text(
                text = "Traceroute",
                style = RouxenTypography.titleSmall,
                color = RouxenColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Not supported on this device — requires raw socket access (root)",
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun latencyColor(latencyMs: Long) = when {
    latencyMs < 50 -> RouxenColors.Accent
    latencyMs < 200 -> RouxenColors.Warning
    else -> RouxenColors.Error
}
