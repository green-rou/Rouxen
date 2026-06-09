package com.greenrou.rouxen.feature.ssl

import com.greenrou.rouxen.core.network.http.SslCertInfo

sealed class SslState {
    object Idle : SslState()
    object Loading : SslState()
    data class Success(val certInfo: SslCertInfo) : SslState()
    data class Error(val message: String) : SslState()
}
