package com.greenrou.rouxen.navigation.scan

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.dns.DnsRecord
import com.greenrou.rouxen.core.network.http.HttpHeadersResult
import com.greenrou.rouxen.core.network.http.SecurityHeaderCheck
import com.greenrou.rouxen.core.network.http.SslCertInfo
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.network.system.PortResult
import com.greenrou.rouxen.core.network.traceroute.TracerouteHop
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.dns.DnsState
import com.greenrou.rouxen.feature.dns.DnsViewModel
import com.greenrou.rouxen.feature.map.MapState
import com.greenrou.rouxen.feature.map.MapViewModel
import com.greenrou.rouxen.feature.map.StaticMapImage
import com.greenrou.rouxen.feature.ping.PingState
import com.greenrou.rouxen.feature.ping.PingViewModel
import com.greenrou.rouxen.feature.ssl.HeadersState
import com.greenrou.rouxen.feature.ssl.HeadersViewModel
import com.greenrou.rouxen.feature.ssl.SslState
import com.greenrou.rouxen.feature.ssl.SslViewModel
import com.greenrou.rouxen.feature.traceroute.TracerouteState
import com.greenrou.rouxen.feature.traceroute.TracerouteViewModel
import com.greenrou.rouxen.feature.whois.WhoisState
import com.greenrou.rouxen.feature.whois.WhoisViewModel
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AllScreen(
    url: String,
    dnsViewModel: DnsViewModel = koinViewModel(),
    sslViewModel: SslViewModel = koinViewModel(),
    headersViewModel: HeadersViewModel = koinViewModel(),
    pingViewModel: PingViewModel = koinViewModel(),
    whoisViewModel: WhoisViewModel = koinViewModel(),
    tracerouteViewModel: TracerouteViewModel = koinViewModel(),
    mapViewModel: MapViewModel = koinViewModel(),
) {
    LaunchedEffect(url) {
        dnsViewModel.analyze(url)
        sslViewModel.analyze(url)
        headersViewModel.analyze(url)
        pingViewModel.analyze(url)
        whoisViewModel.analyze(url)
        tracerouteViewModel.trace(url)
        mapViewModel.locate(url)
    }

    val dnsState by dnsViewModel.state.collectAsState()
    val sslState by sslViewModel.state.collectAsState()
    val headersState by headersViewModel.state.collectAsState()
    val pingState by pingViewModel.state.collectAsState()
    val whoisState by whoisViewModel.state.collectAsState()
    val tracerouteState by tracerouteViewModel.state.collectAsState()
    val mapState by mapViewModel.state.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // ---- DNS ----
        item { SectionHeader("DNS") }
        item { DnsSection(dnsState) }

        // ---- SSL ----
        item { SectionHeader("SSL") }
        item { SslSection(sslState, url) }

        // ---- Headers ----
        item { SectionHeader("Headers") }
        headersSection(headersState)

        // ---- Ping & Ports ----
        item { SectionHeader("Ping & Ports") }
        item { PingSection(pingState) }

        // ---- Whois / IP Info ----
        item { SectionHeader("Whois") }
        whoisSection(whoisState)

        // ---- Traceroute ----
        item { SectionHeader("Traceroute") }
        item { TracerouteSection(tracerouteState) }

        // ---- Map ----
        item { SectionHeader("Map") }
        item { MapSection(mapState, mapViewModel) }
    }
}

// ============================================================
// Section header
// ============================================================

@Composable
private fun SectionHeader(title: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = RouxenTypography.titleMedium,
            color = RouxenColors.Accent,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(RouxenColors.Border),
        )
    }
}

// ============================================================
// Generic loading / error helpers
// ============================================================

@Composable
private fun LoadingRow() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = RouxenColors.Accent,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun ErrorRow(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        StatusBadge(label = message, status = BadgeStatus.Error)
    }
}

// ============================================================
// DNS section
// ============================================================

@Composable
private fun DnsSection(state: DnsState) {
    when (state) {
        is DnsState.Idle, is DnsState.Loading -> LoadingRow()
        is DnsState.Error -> ErrorRow(state.message)
        is DnsState.Success -> {
            if (state.records.isEmpty()) {
                Text(
                    text = "No DNS records found",
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                )
            } else {
                val grouped = state.records.groupBy { it.type }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    grouped.forEach { (type, typeRecords) ->
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

// ============================================================
// SSL section
// ============================================================

@Composable
private fun SslSection(state: SslState, url: String) {
    when (state) {
        is SslState.Idle, is SslState.Loading -> LoadingRow()
        is SslState.Error -> ErrorRow(
            if (url.startsWith("http://")) "HTTPS required for SSL" else state.message
        )
        is SslState.Success -> SslCertDetails(state.certInfo)
    }
}

@Composable
private fun SslCertDetails(cert: SslCertInfo) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val expiryStatus = when {
        !cert.isValid -> BadgeStatus.Error
        cert.daysUntilExpiry < 30 -> BadgeStatus.Warning
        else -> BadgeStatus.Success
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        if (cert.chain.isNotEmpty()) {
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

@Composable
private fun CertRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}

// ============================================================
// Headers section
// ============================================================

private fun LazyListScope.headersSection(state: HeadersState) {
    when (state) {
        is HeadersState.Idle, is HeadersState.Loading -> item { LoadingRow() }
        is HeadersState.Error -> item { ErrorRow(state.message) }
        is HeadersState.Success -> {
            val result = state.result
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

// ============================================================
// Ping & Ports section
// ============================================================

@Composable
private fun PingSection(state: PingState) {
    when (state) {
        is PingState.Idle, is PingState.Loading -> LoadingRow()
        is PingState.Error -> ErrorRow(state.message)
        is PingState.Success -> {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                RouxenCard {
                    Text("Latency", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (state.ping.isReachable) "${state.ping.latencyMs} ms" else "Unreachable",
                            style = RouxenTypography.bodyLarge,
                            color = pingLatencyColor(state.ping.latencyMs, state.ping.isReachable),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        StatusBadge(
                            label = if (state.ping.isReachable) "REACHABLE" else "TIMEOUT",
                            status = if (state.ping.isReachable) BadgeStatus.Success else BadgeStatus.Error,
                        )
                    }
                }
                RouxenCard {
                    Text("Ports", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    state.ports.forEach { port ->
                        PortRow(port)
                        Spacer(modifier = Modifier.height(6.dp))
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

private fun pingLatencyColor(latencyMs: Long, isReachable: Boolean) = when {
    !isReachable -> RouxenColors.Error
    latencyMs < 50 -> RouxenColors.Accent
    latencyMs < 200 -> RouxenColors.Warning
    else -> RouxenColors.Error
}

// ============================================================
// Whois / IP Info section
// ============================================================

private fun LazyListScope.whoisSection(state: WhoisState) {
    when (state) {
        is WhoisState.Idle, is WhoisState.Loading -> item { LoadingRow() }
        is WhoisState.Error -> item { ErrorRow(state.message) }
        is WhoisState.Success -> {
            item { IpInfoCard(state.ipInfo) }
            item { WhoisTextCard(state.whoisText) }
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
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}

@Composable
private fun WhoisTextCard(text: String) {
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

// ============================================================
// Traceroute section
// ============================================================

@Composable
private fun TracerouteSection(state: TracerouteState) {
    when (state) {
        is TracerouteState.Idle, is TracerouteState.Loading -> LoadingRow()
        is TracerouteState.Unsupported -> Text(
            text = "Traceroute not supported on this device — requires raw socket access (root)",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextSecondary,
        )
        is TracerouteState.Error -> ErrorRow(state.message)
        is TracerouteState.Running -> TracerouteHopList(state.hops, isRunning = true)
        is TracerouteState.Complete -> TracerouteHopList(state.hops, isRunning = false)
    }
}

@Composable
private fun TracerouteHopList(hops: List<TracerouteHop>, isRunning: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (isRunning) {
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
        hops.forEach { hop -> TracerouteHopRow(hop) }
    }
}

@Composable
private fun TracerouteHopRow(hop: TracerouteHop) {
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
            Text(
                text = hop.host ?: "*",
                style = RouxenTypography.bodySmall,
                color = if (hop.host != null) RouxenColors.TextPrimary else RouxenColors.TextSecondary,
                modifier = Modifier.weight(1f),
            )
            if (latencyMs != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$latencyMs ms",
                    style = RouxenTypography.labelSmall,
                    color = tracerouteLatencyColor(latencyMs),
                )
            }
        }
    }
}

private fun tracerouteLatencyColor(latencyMs: Long) = when {
    latencyMs < 50 -> RouxenColors.Accent
    latencyMs < 200 -> RouxenColors.Warning
    else -> RouxenColors.Error
}

// ============================================================
// Map section
// ============================================================

@Composable
private fun MapSection(state: MapState, viewModel: MapViewModel) {
    when (state) {
        is MapState.Idle, is MapState.Loading -> LoadingRow()
        is MapState.Error -> ErrorRow(state.message)
        is MapState.Success -> StaticMapImage(lat = state.info.lat, lon = state.info.lon, viewModel = viewModel)
    }
}
