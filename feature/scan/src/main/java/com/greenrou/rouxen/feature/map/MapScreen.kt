package com.greenrou.rouxen.feature.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun MapScreen(
    url: String,
    viewModel: MapViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.locate(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is MapState.Idle -> Unit
            is MapState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is MapState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is MapState.Success -> MapContent(info = s.info, viewModel = viewModel)
        }
    }
}

@Composable
private fun MapContent(info: IpInfoResponse, viewModel: MapViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StaticMapImage(lat = info.lat, lon = info.lon, viewModel = viewModel) }
        item { ServerLocationCard(info) }
    }
}

// ============================================================
// Static map image (OSM tiles)
// ============================================================

@Composable
fun StaticMapImage(lat: Double, lon: Double, viewModel: MapViewModel) {
    LaunchedEffect(lat, lon) { viewModel.loadMapImage(lat, lon) }
    val imageState by viewModel.imageState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(RouxenColors.Surface),
        contentAlignment = Alignment.Center,
    ) {
        when (val s = imageState) {
            is MapImageState.Loaded -> {
                Image(
                    bitmap = s.bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(RouxenColors.Accent, CircleShape)
                        .align(Alignment.Center),
                )
                Text(
                    text = "© OpenStreetMap contributors",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .background(RouxenColors.Background.copy(alpha = 0.6f))
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                )
            }
            is MapImageState.Error -> Text(
                text = "Map unavailable",
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
            )
            else -> CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
        }
    }
}

// ============================================================
// Server location info card
// ============================================================

@Composable
private fun ServerLocationCard(info: IpInfoResponse) {
    RouxenCard {
        Text("Server Location", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        MapInfoRow("IP", info.ip)
        MapInfoRow("City", info.city)
        MapInfoRow("Region", info.region)
        MapInfoRow("Country", info.country)
        MapInfoRow("ISP", info.isp)
        MapInfoRow("Org", info.org)
        MapInfoRow("AS", info.asNumber)
        MapInfoRow("Coordinates", "${info.lat}, ${info.lon}")
    }
}

@Composable
private fun MapInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
