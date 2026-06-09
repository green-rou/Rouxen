package com.greenrou.rouxen.core.network.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

private val securityHeaderNames = listOf(
    "Strict-Transport-Security",
    "Content-Security-Policy",
    "X-Frame-Options",
    "X-Content-Type-Options",
    "X-XSS-Protection",
    "Referrer-Policy",
    "Permissions-Policy",
)

class HttpClient(private val client: OkHttpClient) {

    suspend fun getHeaders(url: String): Result<HttpHeadersResult> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val response = client.newCall(request).execute()

            val redirectChain = buildList {
                var prior = response.priorResponse
                while (prior != null) {
                    add(prior.request.url.toString())
                    prior = prior.priorResponse
                }
            }.reversed()

            val headers = response.headers.associate { (name, value) -> name to value }

            val securityChecks = securityHeaderNames.map { name ->
                SecurityHeaderCheck(
                    name = name,
                    present = response.header(name) != null,
                    value = response.header(name),
                )
            }

            Result.success(
                HttpHeadersResult(
                    statusCode = response.code,
                    finalUrl = response.request.url.toString(),
                    redirectChain = redirectChain,
                    headers = headers,
                    securityHeaders = securityChecks,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
