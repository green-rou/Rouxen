package com.greenrou.rouxen.core.network.traceroute

sealed class TracerouteResult {
    data class Hop(val hop: TracerouteHop) : TracerouteResult()
    object Unsupported : TracerouteResult()
    object Complete : TracerouteResult()
    data class Error(val message: String) : TracerouteResult()
}
