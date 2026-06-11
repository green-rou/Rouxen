package com.greenrou.rouxen.feature.traffic.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap

class ReverseDnsResolver {

    private val cache = ConcurrentHashMap<String, String>()
    private val semaphore = Semaphore(MAX_CONCURRENT)

    suspend fun resolve(ip: String): String? = withContext(Dispatchers.IO) {
        cache[ip]?.let { return@withContext it.takeIf { it.isNotEmpty() } }
        semaphore.withPermit {
            cache[ip]?.let { return@withPermit it.takeIf { it.isNotEmpty() } }
            val result = try {
                withTimeoutOrNull(LOOKUP_TIMEOUT_MS) {
                    InetAddress.getByName(ip).canonicalHostName.takeIf { it != ip }
                }
            } catch (_: Exception) {
                null
            }
            cache[ip] = result ?: NO_HOSTNAME
            result
        }
    }

    companion object {
        private const val MAX_CONCURRENT = 4
        private const val LOOKUP_TIMEOUT_MS = 1_500L
        private const val NO_HOSTNAME = ""
    }
}
