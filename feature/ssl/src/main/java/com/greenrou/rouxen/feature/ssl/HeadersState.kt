package com.greenrou.rouxen.feature.ssl

import com.greenrou.rouxen.core.network.http.HttpHeadersResult

sealed class HeadersState {
    object Idle : HeadersState()
    object Loading : HeadersState()
    data class Success(val result: HttpHeadersResult) : HeadersState()
    data class Error(val message: String) : HeadersState()
}
