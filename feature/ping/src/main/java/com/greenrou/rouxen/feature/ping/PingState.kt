package com.greenrou.rouxen.feature.ping

import com.greenrou.rouxen.core.network.system.PingResult
import com.greenrou.rouxen.core.network.system.PortResult

sealed class PingState {
    object Idle : PingState()
    object Loading : PingState()
    data class Success(
        val ping: PingResult,
        val ports: List<PortResult>,
    ) : PingState()
    data class Error(val message: String) : PingState()
}
