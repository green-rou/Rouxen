package com.greenrou.rouxen.feature.traffic.data

import android.content.Context
import com.greenrou.rouxen.feature.traffic.model.AppTrafficSummary
import com.greenrou.rouxen.feature.traffic.model.TrafficConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

class TrafficRepository(
    private val registry: ConnectionRegistry,
    private val rateAggregator: RateAggregator,
    private val context: Context,
) {
    val connections: StateFlow<List<TrafficConnection>> = registry.connectionsFlow

    fun appSummaries(): Flow<List<AppTrafficSummary>> =
        connections.map { conns ->
            conns.groupBy { it.uid }.map { (uid, group) ->
                val (rx, tx) = rateAggregator.rateBps(uid)
                val info = uidInfo(uid)
                AppTrafficSummary(
                    uid = uid,
                    packageName = info.packageName,
                    appLabel = info.label,
                    rxRateBps = rx,
                    txRateBps = tx,
                    rxTotalBytes = group.sumOf { it.rxBytes },
                    txTotalBytes = group.sumOf { it.txBytes },
                    connectionCount = group.size,
                )
            }.sortedByDescending { it.rxRateBps + it.txRateBps }
        }

    private data class UidInfo(val packageName: String?, val label: String)

    private val uidInfoCache = ConcurrentHashMap<Int, UidInfo>()

    private fun uidInfo(uid: Int): UidInfo = uidInfoCache.getOrPut(uid) {
        if (uid == TrafficConnection.UID_UNKNOWN) {
            return@getOrPut UidInfo(packageName = null, label = "Unknown / System")
        }
        val pm = context.packageManager
        val packages = try {
            pm.getPackagesForUid(uid)
        } catch (_: Exception) {
            null
        }
        val packageName = packages?.firstOrNull()
            ?: return@getOrPut UidInfo(packageName = null, label = "UID $uid")

        val label = runCatching { pm.getApplicationInfo(packageName, 0).loadLabel(pm).toString() }
            .getOrDefault(packageName)
        UidInfo(
            packageName = packageName,
            label = if (packages.size > 1) "$label (+${packages.size - 1})" else label,
        )
    }
}
