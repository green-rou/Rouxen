package com.greenrou.rouxen.feature.traceroute

import com.greenrou.rouxen.core.network.traceroute.TracerouteHop

sealed class TracerouteState {
    object Idle : TracerouteState()
    object Loading : TracerouteState()
    object Unsupported : TracerouteState()
    data class Running(val hops: List<TracerouteHop>) : TracerouteState()
    data class Complete(val hops: List<TracerouteHop>) : TracerouteState()
    data class Error(val message: String) : TracerouteState()
}
