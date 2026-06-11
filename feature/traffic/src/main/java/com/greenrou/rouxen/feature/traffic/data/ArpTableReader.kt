package com.greenrou.rouxen.feature.traffic.data

import com.greenrou.rouxen.feature.traffic.model.ArpEntry
import java.io.File

class ArpTableReader {

    fun read(): List<ArpEntry> = try {
        File("/proc/net/arp").readLines().drop(1).mapNotNull { line ->
            val cols = line.trim().split(Regex("\\s+"))
            if (cols.size < 6) return@mapNotNull null
            val mac = cols[3]
            if (mac == "00:00:00:00:00:00") return@mapNotNull null
            ArpEntry(ipAddress = cols[0], macAddress = mac, device = cols[5])
        }
    } catch (_: Exception) {
        emptyList()
    }
}
