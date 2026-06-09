package com.greenrou.rouxen.core.network.http

data class HttpHeadersResult(
    val statusCode: Int,
    val finalUrl: String,
    val redirectChain: List<String>,
    val headers: Map<String, String>,
    val securityHeaders: List<SecurityHeaderCheck>,
)

data class SecurityHeaderCheck(
    val name: String,
    val present: Boolean,
    val value: String?,
)
