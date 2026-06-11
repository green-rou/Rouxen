package com.greenrou.rouxen.feature.device.model

data class StorageStats(
    val usedGb: Float,
    val totalGb: Float,
    val freeGb: Float,
)

val StorageStats.usedPercent: Int
    get() = if (totalGb > 0f) ((usedGb / totalGb) * 100).toInt().coerceIn(0, 100) else 0
