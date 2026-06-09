package com.greenrou.rouxen.feature.history.domain

import kotlinx.serialization.Serializable

@Serializable
data class ScanExportModel(
    val url: String,
    val scannedAt: String,
    val dnsRecords: List<DnsRecordExport> = emptyList(),
    val ssl: SslExport? = null,
    val ping: PingExport? = null,
)

@Serializable
data class DnsRecordExport(
    val type: String,
    val value: String,
    val ttl: Long,
)

@Serializable
data class SslExport(
    val subject: String,
    val issuer: String,
    val validFrom: String,
    val validTo: String,
    val daysUntilExpiry: Long,
    val isValid: Boolean,
)

@Serializable
data class PingExport(
    val isReachable: Boolean,
    val latencyMs: Long,
)
