package com.greenrou.rouxen.feature.traffic

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.traffic.ui.ConnectedDevicesTab
import com.greenrou.rouxen.feature.traffic.ui.ConnectionsTab
import com.greenrou.rouxen.feature.traffic.ui.ProcessesTab
import com.greenrou.rouxen.feature.traffic.ui.RemoteAddressesTab
import com.greenrou.rouxen.feature.traffic.vpn.TrafficVpnService
import com.greenrou.rouxen.feature.traffic.vpn.VpnStatus
import com.greenrou.rouxen.feature.traffic.vpn.vpnPrepareIntent
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrafficMonitorScreen(
    onBack: () -> Unit,
    onAppClick: (Int) -> Unit,
    onConnectionClick: (String) -> Unit,
    viewModel: TrafficMonitorViewModel = koinViewModel(),
    remoteAddressViewModel: RemoteAddressViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val vpnStatus by viewModel.vpnStatus.collectAsState()
    val connections by viewModel.connections.collectAsState()
    val appSummaries by viewModel.appSummaries.collectAsState()
    val appLabels = remember(appSummaries) { appSummaries.associate { it.uid to it.appLabel } }
    val expandedIps by remoteAddressViewModel.expandedIps.collectAsState()
    val ipInfo by remoteAddressViewModel.ipInfo.collectAsState()
    val connectedDevices by viewModel.connectedDevices.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            startTrafficVpnService(context)
        }
    }

    val onStart = {
        val prepareIntent = vpnPrepareIntent(context)
        if (prepareIntent != null) {
            launcher.launch(prepareIntent)
        } else {
            startTrafficVpnService(context)
        }
    }
    val onStop = { stopTrafficVpnService(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        TrafficMonitorHeader(
            onBack = onBack,
            status = vpnStatus,
            onStart = onStart,
            onStop = onStop,
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
            containerColor = RouxenColors.Surface,
            contentColor = RouxenColors.Accent,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},
        ) {
            TAB_LABELS.forEachIndexed { index, label ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = label,
                            style = RouxenTypography.labelMedium,
                            color = if (selectedTab == index) RouxenColors.Accent else RouxenColors.TextSecondary,
                        )
                    },
                )
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedTab) {
                0 -> GatedContent(status = vpnStatus, onStart = onStart) {
                    ProcessesTab(appSummaries, onClick = onAppClick)
                }
                1 -> GatedContent(status = vpnStatus, onStart = onStart) {
                    ConnectionsTab(connections, appLabels, onClick = onConnectionClick)
                }
                2 -> GatedContent(status = vpnStatus, onStart = onStart) {
                    RemoteAddressesTab(
                        connections = connections,
                        appLabels = appLabels,
                        expandedIps = expandedIps,
                        ipInfo = ipInfo,
                        onToggle = remoteAddressViewModel::toggle,
                        resolveHostname = viewModel::resolveHostname,
                    )
                }
                else -> ConnectedDevicesTab(connectedDevices)
            }
        }
    }
}

private val TAB_LABELS = listOf("Processes", "Connections", "Remote", "Devices")

private fun startTrafficVpnService(context: Context) {
    ContextCompat.startForegroundService(
        context,
        Intent(context, TrafficVpnService::class.java).setAction(TrafficVpnService.ACTION_START),
    )
}

private fun stopTrafficVpnService(context: Context) {
    context.startService(
        Intent(context, TrafficVpnService::class.java).setAction(TrafficVpnService.ACTION_STOP),
    )
}

@Composable
private fun TrafficMonitorHeader(
    onBack: () -> Unit,
    status: VpnStatus,
    onStart: () -> Unit,
    onStop: () -> Unit,
) {
    val (label, badgeStatus) = when (status) {
        VpnStatus.Stopped -> "STOPPED" to BadgeStatus.Neutral
        VpnStatus.Starting -> "STARTING" to BadgeStatus.Warning
        VpnStatus.Running -> "RUNNING" to BadgeStatus.Success
        is VpnStatus.Error -> "ERROR" to BadgeStatus.Error
    }
    val isActive = status is VpnStatus.Running || status is VpnStatus.Starting

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) {
                Text("← Home", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
            }
            Text(
                text = "Traffic Monitor",
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextPrimary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusBadge(label = label, status = badgeStatus)
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = if (isActive) onStop else onStart,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouxenColors.Accent,
                    contentColor = RouxenColors.Background,
                ),
                shape = RoundedCornerShape(4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            ) {
                Text(if (isActive) "Stop" else "Start", style = RouxenTypography.labelSmall)
            }
        }
    }
}

@Composable
private fun GatedContent(status: VpnStatus, onStart: () -> Unit, content: @Composable () -> Unit) {
    when (status) {
        VpnStatus.Stopped -> HintCard(
            title = "Traffic monitoring is stopped",
            description = "Tap Start to begin capturing live traffic. This routes all device traffic " +
                "through a local VPN — no data leaves your device. Starting this will disconnect any " +
                "other active VPN.",
            buttonLabel = "Start",
            onClick = onStart,
        )
        is VpnStatus.Error -> HintCard(
            title = "Monitoring stopped",
            description = status.reason,
            buttonLabel = "Retry",
            onClick = onStart,
        )
        VpnStatus.Starting, VpnStatus.Running -> content()
    }
}

@Composable
private fun HintCard(title: String, description: String, buttonLabel: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = title,
                style = RouxenTypography.titleSmall,
                color = RouxenColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouxenColors.Accent,
                    contentColor = RouxenColors.Background,
                ),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text(buttonLabel, style = RouxenTypography.labelMedium)
            }
        }
    }
}
