package com.greenrou.rouxen.feature.dns

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.dns.DnsRecord
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel

@Composable
fun DnsScreen(
    url: String,
    viewModel: DnsViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.analyze(url) }

    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is DnsState.Idle -> Unit
            is DnsState.Loading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is DnsState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is DnsState.Success -> DnsRecordList(records = s.records)
        }
    }
}

@Composable
private fun DnsRecordList(records: List<DnsRecord>) {
    if (records.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "No DNS records found",
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
            )
        }
        return
    }

    val grouped = records.groupBy { it.type }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        grouped.forEach { (type, typeRecords) ->
            item {
                RouxenCard {
                    Text(
                        text = type,
                        style = RouxenTypography.titleSmall,
                        color = RouxenColors.Accent,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    typeRecords.forEach { record ->
                        DnsRecordRow(record)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DnsRecordRow(record: DnsRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = record.value,
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "${record.ttl}s",
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
        )
    }
}
