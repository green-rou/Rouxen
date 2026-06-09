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
import com.greenrou.rouxen.core.network.http.SslCertInfo
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SslScreen(
    url: String,
    viewModel: SslViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.analyze(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is SslState.Idle -> Unit
            is SslState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is SslState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(
                    label = if (url.startsWith("http://")) "HTTPS required for SSL"
                    else s.message,
                    status = BadgeStatus.Error,
                )
            }
            is SslState.Success -> SslCertDetails(s.certInfo)
        }
    }
}

@Composable
private fun SslCertDetails(cert: SslCertInfo) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val expiryStatus = when {
        !cert.isValid -> BadgeStatus.Error
        cert.daysUntilExpiry < 30 -> BadgeStatus.Warning
        else -> BadgeStatus.Success
    }

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
                        text = "Certificate",
                        style = RouxenTypography.titleSmall,
                        color = RouxenColors.TextPrimary,
                    )
                    StatusBadge(
                        label = if (cert.isValid) "VALID" else "INVALID",
                        status = expiryStatus,
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                CertRow("Subject", cert.subject)
                CertRow("Issuer", cert.issuer)
                CertRow("Valid from", dateFormat.format(Date(cert.validFromMs)))
                CertRow("Valid to", dateFormat.format(Date(cert.validToMs)))
                CertRow("Expires in", "${cert.daysUntilExpiry} days")
            }
        }
        if (cert.chain.isNotEmpty()) {
            item {
                RouxenCard {
                    Text(
                        text = "Chain",
                        style = RouxenTypography.titleSmall,
                        color = RouxenColors.TextPrimary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    cert.chain.forEachIndexed { i, name ->
                        Text(
                            text = "${i + 2}. $name",
                            style = RouxenTypography.bodySmall,
                            color = RouxenColors.TextSecondary,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CertRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
