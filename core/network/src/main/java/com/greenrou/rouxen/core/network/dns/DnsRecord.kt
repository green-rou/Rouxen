package com.greenrou.rouxen.core.network.dns

data class DnsRecord(
    val type: String,
    val value: String,
    val ttl: Long,
)
