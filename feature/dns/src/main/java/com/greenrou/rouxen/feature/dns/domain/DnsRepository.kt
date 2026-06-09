package com.greenrou.rouxen.feature.dns.domain

import com.greenrou.rouxen.core.network.dns.DnsRecord

interface DnsRepository {
    suspend fun getRecords(host: String): Result<List<DnsRecord>>
}
