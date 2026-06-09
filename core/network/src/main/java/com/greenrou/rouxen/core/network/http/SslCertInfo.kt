package com.greenrou.rouxen.core.network.http

data class SslCertInfo(
    val subject: String,
    val issuer: String,
    val validFromMs: Long,
    val validToMs: Long,
    val daysUntilExpiry: Int,
    val isValid: Boolean,
    val chain: List<String>,
)
