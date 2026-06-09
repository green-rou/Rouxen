package com.greenrou.rouxen.feature.ssl.domain

import com.greenrou.rouxen.core.network.http.HttpHeadersResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetHttpHeadersUseCase(private val repository: SslRepository) {
    operator fun invoke(url: String): Flow<Result<HttpHeadersResult>> = flow {
        emit(repository.getHttpHeaders(url))
    }
}
