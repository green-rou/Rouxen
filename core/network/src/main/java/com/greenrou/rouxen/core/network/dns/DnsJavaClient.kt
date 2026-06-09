package com.greenrou.rouxen.core.network.dns

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xbill.DNS.ExtendedResolver
import org.xbill.DNS.Lookup
import org.xbill.DNS.Resolver

class DnsJavaClient(
    private val resolver: Resolver = buildDefaultResolver(),
) : DnsClient {

    override suspend fun lookup(host: String, type: DnsRecordType): Result<List<DnsRecord>> =
        withContext(Dispatchers.IO) {
            try {
                val lookup = Lookup(host, type.code)
                lookup.setResolver(resolver)
                val records = lookup.run()
                when (lookup.result) {
                    Lookup.SUCCESSFUL -> Result.success(
                        records?.map { it.toDnsRecord() } ?: emptyList()
                    )
                    Lookup.HOST_NOT_FOUND, Lookup.TYPE_NOT_FOUND -> Result.success(emptyList())
                    else -> Result.failure(DnsException(lookup.errorString ?: "Unknown DNS error"))
                }
            } catch (e: Exception) {
                Result.failure(DnsException("DNS lookup failed for $host", e))
            }
        }
}

private fun buildDefaultResolver(): Resolver =
    ExtendedResolver(arrayOf("8.8.8.8", "1.1.1.1"))
