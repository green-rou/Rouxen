package com.greenrou.rouxen.feature.dns.data

import com.greenrou.rouxen.core.network.dns.DnsClient
import com.greenrou.rouxen.core.network.dns.DnsRecord
import com.greenrou.rouxen.core.network.dns.DnsRecordType
import com.greenrou.rouxen.feature.dns.domain.DnsRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class DnsRepositoryImpl(private val client: DnsClient) : DnsRepository {

    override suspend fun getRecords(host: String): Result<List<DnsRecord>> =
        runCatching {
            coroutineScope {
                DnsRecordType.entries
                    .map { type -> async { client.lookup(host, type) } }
                    .map { it.await() }
                    .flatMap { it.getOrElse { emptyList() } }
            }
        }
}
