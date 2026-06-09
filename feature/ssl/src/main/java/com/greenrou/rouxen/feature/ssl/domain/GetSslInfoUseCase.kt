package com.greenrou.rouxen.feature.ssl.domain

import com.greenrou.rouxen.core.network.http.SslCertInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetSslInfoUseCase(private val repository: SslRepository) {
    operator fun invoke(url: String): Flow<Result<SslCertInfo>> = flow {
        emit(repository.getSslInfo(url))
    }
}
