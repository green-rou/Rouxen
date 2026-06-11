package com.greenrou.rouxen.feature.traffic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.model.AppTrafficSummary

@Composable
fun ProcessesTab(summaries: List<AppTrafficSummary>, onClick: (Int) -> Unit) {
    if (summaries.isEmpty()) {
        EmptyHint("No active network usage")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(summaries, key = { it.uid }) { summary ->
            AppTrafficRow(summary, onClick = { onClick(summary.uid) })
        }
    }
}

@Composable
private fun AppTrafficRow(summary: AppTrafficSummary, onClick: () -> Unit) {
    RouxenCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.appLabel,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary,
                )
                Text(
                    text = "${summary.connectionCount} connections",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "↓ ${formatRate(summary.rxRateBps)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.Accent,
                )
                Text(
                    text = "↑ ${formatRate(summary.txRateBps)}",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        }
    }
}
