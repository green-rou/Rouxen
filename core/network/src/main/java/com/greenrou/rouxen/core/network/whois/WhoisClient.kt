package com.greenrou.rouxen.core.network.whois

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket

private const val IANA_WHOIS = "whois.iana.org"
private const val WHOIS_PORT = 43
private const val TIMEOUT_MS = 10_000

class WhoisClient {

    suspend fun lookup(domain: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val ianaResponse = query(IANA_WHOIS, domain)
            val referralServer = parseReferral(ianaResponse)
            val finalResponse = if (referralServer != null) {
                query(referralServer, domain)
            } else {
                ianaResponse
            }
            Result.success(finalResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun query(server: String, domain: String): String {
        Socket().use { socket ->
            socket.soTimeout = TIMEOUT_MS
            socket.connect(java.net.InetSocketAddress(server, WHOIS_PORT), TIMEOUT_MS)
            val writer = PrintWriter(socket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
            writer.println(domain)
            return reader.readText()
        }
    }

    private fun parseReferral(response: String): String? {
        return response.lineSequence()
            .firstOrNull { it.startsWith("whois:", ignoreCase = true) }
            ?.substringAfter(":")
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }
}
