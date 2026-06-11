package com.greenrou.rouxen.feature.device.model

data class DeviceStats(
    val cpu: CpuStats,
    val memory: MemoryStats,
    val battery: BatteryStats,
    val storage: StorageStats,
    val appMemory: AppMemoryStats,
    val device: DeviceInfo,
)
