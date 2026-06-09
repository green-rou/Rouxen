package com.greenrou.rouxen.core.network.system

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

private val portServices = mapOf(
    21 to "FTP",
    22 to "SSH",
    25 to "SMTP",
    53 to "DNS",
    80 to "HTTP",
    443 to "HTTPS",
    3306 to "MySQL",
    5432 to "PostgreSQL",
    8080 to "HTTP-Alt",
    8443 to "HTTPS-Alt",
)

private const val PING_ATTEMPTS = 3
private const val PING_TIMEOUT_MS = 2000
private const val PORT_TIMEOUT_MS = 500

class SystemNetworkClient {

    suspend fun ping(host: String): Result<PingResult> = withContext(Dispatchers.IO) {
        try {
            val address = InetAddress.getByName(host)
            val latencies = (1..PING_ATTEMPTS).map {
                val start = System.currentTimeMillis()
                val reachable = address.isReachable(PING_TIMEOUT_MS)
                if (reachable) System.currentTimeMillis() - start else -1L
            }
            val successLatencies = latencies.filter { it >= 0 }
            if (successLatencies.isNotEmpty()) {
                Result.success(
                    PingResult(
                        host = host,
                        isReachable = true,
                        latencyMs = successLatencies.average().toLong(),
                    )
                )
            } else {
                val fallbackLatency = tcpPing(host)
                Result.success(
                    PingResult(
                        host = host,
                        isReachable = fallbackLatency >= 0,
                        latencyMs = if (fallbackLatency >= 0) fallbackLatency else 0L,
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scanPorts(host: String): Result<List<PortResult>> =
        withContext(Dispatchers.IO) {
            try {
                coroutineScope {
                    val results = portServices.map { (port, service) ->
                        async {
                            val open = isPortOpen(host, port)
                            PortResult(port = port, isOpen = open, serviceName = service)
                        }
                    }.map { it.await() }
                    Result.success(results.sortedBy { it.port })
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun tcpPing(host: String): Long {
        return try {
            val start = System.currentTimeMillis()
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, 80), PING_TIMEOUT_MS)
            }
            System.currentTimeMillis() - start
        } catch (_: Exception) {
            try {
                val start = System.currentTimeMillis()
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(host, 443), PING_TIMEOUT_MS)
                }
                System.currentTimeMillis() - start
            } catch (_: Exception) {
                -1L
            }
        }
    }

    private fun isPortOpen(host: String, port: Int): Boolean {
        return try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(host, port), PORT_TIMEOUT_MS)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
