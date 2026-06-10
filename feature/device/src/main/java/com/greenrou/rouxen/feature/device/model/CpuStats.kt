package com.greenrou.rouxen.feature.device.model

data class CpuStats(
    val usagePercent: Float,
    val coreCount: Int,
    val coreFrequenciesMhz: List<Int>,
)
