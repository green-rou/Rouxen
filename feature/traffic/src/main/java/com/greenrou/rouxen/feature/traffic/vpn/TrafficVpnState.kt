package com.greenrou.rouxen.feature.traffic.vpn

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface VpnStatus {
    data object Stopped : VpnStatus
    data object Starting : VpnStatus
    data object Running : VpnStatus
    data class Error(val reason: String) : VpnStatus
}

object TrafficVpnState {
    private val _status = MutableStateFlow<VpnStatus>(VpnStatus.Stopped)
    val status: StateFlow<VpnStatus> = _status.asStateFlow()

    fun setStarting() {
        _status.value = VpnStatus.Starting
    }

    fun setRunning() {
        _status.value = VpnStatus.Running
    }

    fun setStopped() {
        _status.value = VpnStatus.Stopped
    }

    fun setError(reason: String) {
        _status.value = VpnStatus.Error(reason)
    }
}
