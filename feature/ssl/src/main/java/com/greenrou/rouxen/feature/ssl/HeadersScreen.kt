package com.greenrou.rouxen.feature.ssl

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.http.HttpHeadersResult
import com.greenrou.rouxen.core.network.http.SecurityHeaderCheck
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun HeadersScreen(
    url: String,
    viewModel: HeadersViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.analyze(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is HeadersState.Idle -> Unit
            is HeadersState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is HeadersState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is HeadersState.Success -> HttpHeadersContent(s.result)
        }
    }
}

@Composable
private fun HttpHeadersContent(result: HttpHeadersResult) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            RouxenCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Status",
                        style = RouxenTypography.titleSmall,
                        color = RouxenColors.TextPrimary,
                    )
                    StatusBadge(
                        label = "${result.statusCode}",
                        status = if (result.statusCode < 400) BadgeStatus.Success else BadgeStatus.Error,
                    )
                }
                if (result.redirectChain.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Redirected from ${result.redirectChain.size} URL(s)",
                        style = RouxenTypography.bodySmall,
                        color = RouxenColors.TextSecondary,
                    )
                }
            }
        }
        item {
            RouxenCard {
                Text(
                    text = "Security Headers",
                    style = RouxenTypography.titleSmall,
                    color = RouxenColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.securityHeaders.forEach { check ->
                    SecurityHeaderRow(check)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
        item {
            RouxenCard {
                Text(
                    text = "All Headers",
                    style = RouxenTypography.titleSmall,
                    color = RouxenColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                result.headers.forEach { (name, value) ->
                    Column(modifier = Modifier.padding(vertical = 2.dp)) {
                        Text(
                            text = name,
                            style = RouxenTypography.labelSmall,
                            color = RouxenColors.TextSecondary,
                        )
                        Text(
                            text = value,
                            style = RouxenTypography.bodySmall,
                            color = RouxenColors.TextPrimary,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SecurityHeaderRow(check: SecurityHeaderCheck) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = check.name,
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        StatusBadge(
            label = if (check.present) "PRESENT" else "MISSING",
            status = if (check.present) BadgeStatus.Success else BadgeStatus.Error,
        )
    }
}
