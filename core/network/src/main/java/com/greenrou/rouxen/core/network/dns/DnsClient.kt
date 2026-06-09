package com.greenrou.rouxen.core.network.dns

interface DnsClient {
    suspend fun lookup(host: String, type: DnsRecordType): Result<List<DnsRecord>>
}
