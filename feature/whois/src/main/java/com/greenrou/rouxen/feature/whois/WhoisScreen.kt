package com.greenrou.rouxen.feature.whois

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun WhoisScreen(
    url: String,
    viewModel: WhoisViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.analyze(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is WhoisState.Idle -> Unit
            is WhoisState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is WhoisState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is WhoisState.Success -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item { IpInfoCard(s.ipInfo) }
                item { WhoisCard(s.whoisText) }
            }
        }
    }
}

@Composable
private fun IpInfoCard(info: IpInfoResponse) {
    RouxenCard {
        Text("IP Info", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        IpInfoRow("IP", info.ip)
        IpInfoRow("Country", info.country)
        IpInfoRow("Region", info.region)
        IpInfoRow("City", info.city)
        IpInfoRow("ISP", info.isp)
        IpInfoRow("Org", info.org)
        IpInfoRow("AS", info.asNumber)
        IpInfoRow("Coordinates", "${info.lat}, ${info.lon}")
    }
}

@Composable
private fun IpInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(
            text = value,
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
        )
    }
}

@Composable
private fun WhoisCard(text: String) {
    RouxenCard {
        Text("WHOIS", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
        ) {
            Text(
                text = text,
                style = RouxenTypography.labelSmall,
                color = RouxenColors.TextSecondary,
            )
        }
    }
}
