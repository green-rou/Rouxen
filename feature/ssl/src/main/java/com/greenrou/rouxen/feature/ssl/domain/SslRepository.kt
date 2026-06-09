package com.greenrou.rouxen.feature.ssl.domain

import com.greenrou.rouxen.core.network.http.HttpHeadersResult
import com.greenrou.rouxen.core.network.http.SslCertInfo

interface SslRepository {
    suspend fun getSslInfo(url: String): Result<SslCertInfo>
    suspend fun getHttpHeaders(url: String): Result<HttpHeadersResult>
}
