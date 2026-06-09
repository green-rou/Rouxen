package com.greenrou.rouxen.core.network.http

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit

class SslInspector(private val client: OkHttpClient) {

    suspend fun inspect(url: String): Result<SslCertInfo> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).head().build()
            val response = client.newCall(request).execute()
            val handshake = response.handshake
                ?: return@withContext Result.failure(Exception("No SSL handshake for $url"))

            val leaf = handshake.peerCertificates
                .filterIsInstance<X509Certificate>()
                .firstOrNull()
                ?: return@withContext Result.failure(Exception("No peer certificate"))

            val chain = handshake.peerCertificates
                .drop(1)
                .filterIsInstance<X509Certificate>()
                .map { it.subjectDN.name }

            val nowMs = System.currentTimeMillis()
            val daysLeft = TimeUnit.MILLISECONDS.toDays(leaf.notAfter.time - nowMs).toInt()

            Result.success(
                SslCertInfo(
                    subject = leaf.subjectDN.name,
                    issuer = leaf.issuerDN.name,
                    validFromMs = leaf.notBefore.time,
                    validToMs = leaf.notAfter.time,
                    daysUntilExpiry = daysLeft,
                    isValid = nowMs in leaf.notBefore.time..leaf.notAfter.time,
                    chain = chain,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
