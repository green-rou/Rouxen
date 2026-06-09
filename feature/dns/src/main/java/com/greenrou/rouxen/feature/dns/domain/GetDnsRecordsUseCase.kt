package com.greenrou.rouxen.feature.dns.domain

import com.greenrou.rouxen.core.network.dns.DnsRecord
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GetDnsRecordsUseCase(private val repository: DnsRepository) {
    operator fun invoke(host: String): Flow<Result<List<DnsRecord>>> = flow {
        emit(repository.getRecords(host))
    }
}
