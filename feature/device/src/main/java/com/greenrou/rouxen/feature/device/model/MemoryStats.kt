package com.greenrou.rouxen.feature.device.model

data class MemoryStats(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val thresholdMb: Long,
    val isLowMemory: Boolean,
)

val MemoryStats.usedPercent: Int
    get() = if (totalMb > 0) ((usedMb * 100) / totalMb).toInt().coerceIn(0, 100) else 0
