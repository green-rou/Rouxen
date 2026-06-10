package com.greenrou.rouxen.core.network.traceroute

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.io.BufferedReader
import java.io.InputStreamReader

private val hopLinePattern =
    Regex("""^\s*(\d+)\s+(?:(\S+)\s+\([^)]+\)\s+(\d+(?:\.\d+)?)\s*ms|(\*))""")

class TracerouteClient {

    fun trace(host: String): Flow<TracerouteResult> = flow {
        try {
            val process = ProcessBuilder(
                "traceroute", "-m", "30", "-q", "1", "-w", "2", host,
            )
                .redirectErrorStream(true)
                .start()

            var hopsEmitted = 0
            try {
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val hop = parseLine(line!!)
                    if (hop != null) {
                        hopsEmitted++
                        emit(TracerouteResult.Hop(hop))
                    }
                }
            } finally {
                process.destroyForcibly()
            }
            // The traceroute binary exists on most ROMs but needs CAP_NET_RAW for raw
            // ICMP/UDP probes, which app processes don't have — it exits with an
            // unparseable permission error and zero hop lines on non-rooted devices.
            emit(if (hopsEmitted > 0) TracerouteResult.Complete else TracerouteResult.Unsupported)
        } catch (_: Exception) {
            emit(TracerouteResult.Unsupported)
        }
    }.flowOn(Dispatchers.IO)

    private fun parseLine(line: String): TracerouteHop? {
        val match = hopLinePattern.find(line) ?: return null
        val hop = match.groupValues[1].toIntOrNull() ?: return null
        val isTimeout = match.groupValues[4] == "*"
        return if (isTimeout) {
            TracerouteHop(hop = hop, host = null, latencyMs = null)
        } else {
            TracerouteHop(
                hop = hop,
                host = match.groupValues[2].takeIf { it.isNotEmpty() },
                latencyMs = match.groupValues[3].toDoubleOrNull()?.toLong(),
            )
        }
    }
}
