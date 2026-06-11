package com.greenrou.rouxen.feature.traffic.model

data class AppTrafficSummary(
    val uid: Int,
    val packageName: String?,
    val appLabel: String,
    val rxRateBps: Long,
    val txRateBps: Long,
    val rxTotalBytes: Long,
    val txTotalBytes: Long,
    val connectionCount: Int,
)
