package com.greenrou.rouxen.feature.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import com.greenrou.rouxen.feature.wifi.model.BleDevice
import com.greenrou.rouxen.feature.wifi.model.WifiNetwork
import com.greenrou.rouxen.feature.wifi.model.distanceLabel
import com.greenrou.rouxen.feature.wifi.model.frequencyBand
import com.greenrou.rouxen.feature.wifi.model.level
import com.greenrou.rouxen.feature.wifi.model.securityType
import org.koin.androidx.compose.koinViewModel

private val WIFI_PERMISSIONS = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    add(Manifest.permission.ACCESS_COARSE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(Manifest.permission.BLUETOOTH_SCAN)
        add(Manifest.permission.BLUETOOTH_CONNECT)
    }
}.toTypedArray()

@Composable
fun WifiScannerScreen(
    onBack: () -> Unit = {},
    onNetworkClick: (String) -> Unit = {},
    onDeviceClick: (String) -> Unit = {},
    viewModel: WifiScannerViewModel = koinViewModel(),
) {
    val context = LocalContext.current

    fun hasPermissions() = WIFI_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    var granted by remember { mutableStateOf(hasPermissions()) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> granted = result.values.all { it } }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopBleScan() }
    }

    val wifiState by viewModel.wifiState.collectAsState()
    val bleState by viewModel.bleState.collectAsState()
    val radio = rememberRadioState(context)
    var selectedTab by remember { mutableIntStateOf(0) }

    val isWifiScanning = wifiState is WifiScanState.Scanning
    val isBleScanning = bleState is BleScanState.Scanning
    val showScanAction = granted && if (selectedTab == 0) {
        radio.wifiOn && radio.locationOn
    } else {
        radio.btOn && radio.locationOn
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        ScannerHeader(
            onBack = onBack,
            actions = {
                if (showScanAction) {
                    if (selectedTab == 0) {
                        HeaderScanButton(
                            label = "Scan",
                            enabled = !isWifiScanning,
                            showSpinner = isWifiScanning,
                            active = false,
                            onClick = viewModel::startWifiScan,
                        )
                    } else {
                        HeaderScanButton(
                            label = if (isBleScanning) "Stop" else "Scan",
                            enabled = true,
                            showSpinner = false,
                            active = isBleScanning,
                            onClick = {
                                if (isBleScanning) viewModel.stopBleScan() else viewModel.startBleScan()
                            },
                        )
                    }
                }
            },
        )
        if (!granted) {
            PermissionContent(onRequest = { launcher.launch(WIFI_PERMISSIONS) })
        } else {
            ScannerContent(
                onNetworkClick = onNetworkClick,
                onDeviceClick = onDeviceClick,
                wifiState = wifiState,
                bleState = bleState,
                radio = radio,
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
            )
        }
    }
}

@Composable
private fun ScannerHeader(
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("← Home", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
        }
        Text(
            text = "WiFi & BLE Scanner",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier
                .padding(start = 4.dp)
                .weight(1f),
        )
        actions()
    }
}

@Composable
private fun PermissionContent(onRequest: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
        ) {
            Text(
                text = "Permissions required",
                style = RouxenTypography.titleSmall,
                color = RouxenColors.TextPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Location and Bluetooth access are needed to scan WiFi networks and BLE devices.",
                style = RouxenTypography.bodySmall,
                color = RouxenColors.TextSecondary,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRequest,
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouxenColors.Accent,
                    contentColor = RouxenColors.Background,
                ),
                shape = RoundedCornerShape(4.dp),
            ) {
                Text("Grant permissions", style = RouxenTypography.labelMedium)
            }
        }
    }
}

private data class RadioState(
    val wifiOn: Boolean,
    val btOn: Boolean,
    val locationOn: Boolean,
)

@Composable
private fun rememberRadioState(context: Context): RadioState {
    var wifiOn by remember { mutableStateOf(isWifiEnabled(context)) }
    var btOn by remember { mutableStateOf(isBluetoothEnabled(context)) }
    var locationOn by remember { mutableStateOf(isLocationEnabled(context)) }

    DisposableEffect(Unit) {
        val filter = IntentFilter().apply {
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                wifiOn = isWifiEnabled(context)
                btOn = isBluetoothEnabled(context)
                locationOn = isLocationEnabled(context)
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }

    return RadioState(wifiOn = wifiOn, btOn = btOn, locationOn = locationOn)
}

@Composable
private fun RadioDisabledHint(
    title: String,
    description: String? = null,
    buttonLabel: String,
    onEnable: () -> Unit,
) {
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
            if (description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextSecondary,
                    textAlign = TextAlign.Center,
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onEnable,
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

@SuppressLint("MissingPermission")
@Composable
private fun ScannerContent(
    onNetworkClick: (String) -> Unit,
    onDeviceClick: (String) -> Unit,
    wifiState: WifiScanState,
    bleState: BleScanState,
    radio: RadioState,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    val context = LocalContext.current
    val tabs = listOf("WiFi", "BLE")

    ScrollableTabRow(
        selectedTabIndex = selectedTab,
        containerColor = RouxenColors.Surface,
        contentColor = RouxenColors.Accent,
        edgePadding = 0.dp,
        indicator = {},
        divider = {},
    ) {
        tabs.forEachIndexed { index, label ->
            Tab(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                text = {
                    Text(
                        text = label,
                        style = RouxenTypography.labelMedium,
                        color = if (selectedTab == index) RouxenColors.Accent
                        else RouxenColors.TextSecondary,
                    )
                },
            )
        }
    }

    when (selectedTab) {
        0 -> WifiTab(
            state = wifiState,
            onNetworkClick = onNetworkClick,
            wifiOn = radio.wifiOn,
            locationOn = radio.locationOn,
            onEnableWifi = {
                val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    Intent(Settings.Panel.ACTION_WIFI)
                } else {
                    Intent(Settings.ACTION_WIFI_SETTINGS)
                }
                context.startActivity(intent)
            },
            onEnableLocation = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
        )
        1 -> BleTab(
            state = bleState,
            onDeviceClick = onDeviceClick,
            btOn = radio.btOn,
            locationOn = radio.locationOn,
            onEnableBluetooth = {
                context.startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            },
            onEnableLocation = {
                context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            },
        )
    }
}

@Composable
private fun WifiTab(
    state: WifiScanState,
    onNetworkClick: (String) -> Unit,
    wifiOn: Boolean,
    locationOn: Boolean,
    onEnableWifi: () -> Unit,
    onEnableLocation: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(RouxenColors.Background)) {
        when {
            !wifiOn -> RadioDisabledHint(
                title = "WiFi is disabled",
                buttonLabel = "Enable WiFi",
                onEnable = onEnableWifi,
            )
            !locationOn -> RadioDisabledHint(
                title = "Location is disabled",
                description = "Location access is required to scan for nearby WiFi networks.",
                buttonLabel = "Enable Location",
                onEnable = onEnableLocation,
            )
            else -> when (state) {
                is WifiScanState.Idle -> EmptyHint("Tap Scan to discover nearby networks")
                is WifiScanState.Scanning -> LoadingContent()
                is WifiScanState.Error -> ErrorContent(state.message)
                is WifiScanState.Success -> WifiList(state.networks, onNetworkClick)
            }
        }
    }
}

@Composable
private fun BleTab(
    state: BleScanState,
    onDeviceClick: (String) -> Unit,
    btOn: Boolean,
    locationOn: Boolean,
    onEnableBluetooth: () -> Unit,
    onEnableLocation: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().background(RouxenColors.Background)) {
        when {
            !btOn -> RadioDisabledHint(
                title = "Bluetooth is disabled",
                buttonLabel = "Enable Bluetooth",
                onEnable = onEnableBluetooth,
            )
            !locationOn -> RadioDisabledHint(
                title = "Location is disabled",
                description = "Location access is required to scan for nearby Bluetooth devices.",
                buttonLabel = "Enable Location",
                onEnable = onEnableLocation,
            )
            else -> when (state) {
                is BleScanState.Idle -> EmptyHint("Tap Scan to discover BLE devices")
                is BleScanState.Scanning -> BleList(state.devices, isScanning = true, onDeviceClick)
                is BleScanState.Success -> BleList(state.devices, isScanning = false, onDeviceClick)
                is BleScanState.Error -> ErrorContent(state.message)
            }
        }
    }
}

@Composable
private fun WifiList(networks: List<WifiNetwork>, onNetworkClick: (String) -> Unit) {
    if (networks.isEmpty()) {
        EmptyHint("No networks found")
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(networks, key = { it.bssid }) { WifiNetworkCard(it, onClick = { onNetworkClick(it.bssid) }) }
    }
}

@Composable
private fun BleList(devices: List<BleDevice>, isScanning: Boolean, onDeviceClick: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isScanning) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    color = RouxenColors.Accent,
                    strokeWidth = 1.5.dp,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${devices.size} device${if (devices.size != 1) "s" else ""} found…",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
            }
        }
        if (devices.isEmpty()) {
            EmptyHint("Scanning for BLE devices…")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(devices, key = { it.address }) { BleDeviceCard(it, onClick = { onDeviceClick(it.address) }) }
            }
        }
    }
}

@Composable
private fun WifiNetworkCard(network: WifiNetwork, onClick: () -> Unit) {
    RouxenCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = network.ssid,
                        style = RouxenTypography.bodySmall,
                        color = RouxenColors.TextPrimary,
                    )
                    if (WifiScanCache.connectionInfo?.bssid.equals(network.bssid, ignoreCase = true)) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "● Connected",
                            style = RouxenTypography.labelSmall,
                            color = RouxenColors.Accent,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = network.bssid,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalBars(level = network.level)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${network.rssi} dBm · ${network.frequencyBand}",
                        style = RouxenTypography.labelSmall,
                        color = RouxenColors.TextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(
                label = network.securityType,
                status = if (network.securityType == "Open") BadgeStatus.Warning else BadgeStatus.Success,
            )
        }
    }
}

@Composable
private fun BleDeviceCard(device: BleDevice, onClick: () -> Unit) {
    RouxenCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = device.address,
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary,
                )
                device.manufacturerId?.let {
                    Text(
                        text = "Manufacturer: 0x${it.toString(16).uppercase()}",
                        style = RouxenTypography.labelSmall,
                        color = RouxenColors.TextSecondary,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalBars(level = device.level)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${device.rssi} dBm · ${device.distanceLabel}",
                        style = RouxenTypography.labelSmall,
                        color = RouxenColors.TextSecondary,
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge(label = "BLE", status = BadgeStatus.Neutral)
        }
    }
}

@Composable
private fun SignalBars(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.Bottom) {
        for (i in 0..3) {
            val filled = i < level
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height((4 + i * 3).dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(if (filled) RouxenColors.Accent else RouxenColors.Border),
            )
        }
    }
}

@Composable
private fun HeaderScanButton(
    label: String,
    enabled: Boolean,
    showSpinner: Boolean,
    active: Boolean,
    onClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showSpinner) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        TextButton(onClick = onClick, enabled = enabled) {
            Text(
                text = label,
                style = RouxenTypography.labelMedium,
                color = when {
                    !enabled -> RouxenColors.TextSecondary
                    active -> RouxenColors.Error
                    else -> RouxenColors.Accent
                },
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.size(32.dp),
            color = RouxenColors.Accent,
            strokeWidth = 2.dp,
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text, style = RouxenTypography.bodySmall, color = RouxenColors.TextSecondary)
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        StatusBadge(label = message, status = BadgeStatus.Error)
    }
}
