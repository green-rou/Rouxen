package com.greenrou.rouxen.feature.device.model

data class AppMemoryStats(
    val javaHeapUsedMb: Long,
    val javaHeapMaxMb: Long,
    val nativeHeapUsedMb: Long,
    val totalPssMb: Long,
)
