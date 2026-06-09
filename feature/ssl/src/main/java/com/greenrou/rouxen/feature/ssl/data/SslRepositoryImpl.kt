package com.greenrou.rouxen.feature.ssl.data

import com.greenrou.rouxen.core.network.http.HttpClient
import com.greenrou.rouxen.core.network.http.HttpHeadersResult
import com.greenrou.rouxen.core.network.http.SslCertInfo
import com.greenrou.rouxen.core.network.http.SslInspector
import com.greenrou.rouxen.feature.ssl.domain.SslRepository

class SslRepositoryImpl(
    private val httpClient: HttpClient,
    private val sslInspector: SslInspector,
) : SslRepository {

    override suspend fun getSslInfo(url: String): Result<SslCertInfo> =
        sslInspector.inspect(url)

    override suspend fun getHttpHeaders(url: String): Result<HttpHeadersResult> =
        httpClient.getHeaders(url)
}
