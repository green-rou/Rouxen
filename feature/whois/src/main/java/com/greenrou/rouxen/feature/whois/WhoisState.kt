package com.greenrou.rouxen.feature.whois

import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse

sealed class WhoisState {
    object Idle : WhoisState()
    object Loading : WhoisState()
    data class Success(
        val ipInfo: IpInfoResponse,
        val whoisText: String,
    ) : WhoisState()
    data class Error(val message: String) : WhoisState()
}
