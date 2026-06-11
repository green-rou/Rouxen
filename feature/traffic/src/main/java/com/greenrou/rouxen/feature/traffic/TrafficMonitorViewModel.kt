package com.greenrou.rouxen.feature.traffic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.traffic.data.ArpTableReader
import com.greenrou.rouxen.feature.traffic.data.OuiVendorLookup
import com.greenrou.rouxen.feature.traffic.data.ReverseDnsResolver
import com.greenrou.rouxen.feature.traffic.data.TrafficRepository
import com.greenrou.rouxen.feature.traffic.model.AppTrafficSummary
import com.greenrou.rouxen.feature.traffic.model.ConnectedDevice
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection
import com.greenrou.rouxen.feature.traffic.vpn.TrafficVpnState
import com.greenrou.rouxen.feature.traffic.vpn.VpnStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

class TrafficMonitorViewModel(
    private val repository: TrafficRepository,
    private val dnsResolver: ReverseDnsResolver,
    private val arpReader: ArpTableReader,
) : ViewModel() {

    val vpnStatus: StateFlow<VpnStatus> = TrafficVpnState.status

    val connections: StateFlow<List<TrafficConnection>> = repository.connections

    val appSummaries: StateFlow<List<AppTrafficSummary>> = repository.appSummaries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun resolveHostname(ip: String): String? = dnsResolver.resolve(ip)

    val connectedDevices: StateFlow<List<ConnectedDevice>> = flow {
        while (true) {
            emit(
                arpReader.read().map { entry ->
                    ConnectedDevice(
                        ipAddress = entry.ipAddress,
                        macAddress = entry.macAddress,
                        vendor = OuiVendorLookup.lookup(entry.macAddress),
                        hostname = dnsResolver.resolve(entry.ipAddress),
                    )
                },
            )
            delay(ARP_POLL_INTERVAL_MS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private companion object {
        const val ARP_POLL_INTERVAL_MS = 5_000L
    }
}
