package com.greenrou.rouxen.feature.device

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Debug
import android.os.PowerManager
import android.os.Process
import android.os.StatFs
import android.os.SystemClock
import com.greenrou.rouxen.feature.device.model.AppMemoryStats
import com.greenrou.rouxen.feature.device.model.BatteryStats
import com.greenrou.rouxen.feature.device.model.CpuStats
import com.greenrou.rouxen.feature.device.model.DeviceInfo
import com.greenrou.rouxen.feature.device.model.DeviceStats
import com.greenrou.rouxen.feature.device.model.MemoryStats
import com.greenrou.rouxen.feature.device.model.StorageStats
import java.io.RandomAccessFile

class DeviceStatsRepository(private val context: Context) {

    private var prevCpuTimeMs = 0L
    private var prevWallTimeMs = 0L

    fun snapshot(): DeviceStats = DeviceStats(
        cpu = cpuStats(),
        memory = memoryStats(),
        battery = batteryStats(),
        storage = storageStats(),
        appMemory = appMemoryStats(),
        device = deviceInfo(),
    )

    // System-wide /proc/stat is blocked by SELinux for app processes on Android 8+;
    // only this process's own CPU time (/proc/self, exposed via Process) is readable.
    @Suppress("DEPRECATION")
    private fun cpuStats(): CpuStats {
        val cpuTimeMs = Process.getElapsedCpuTime()
        val wallTimeMs = SystemClock.elapsedRealtime()
        val cpuDelta = cpuTimeMs - prevCpuTimeMs
        val wallDelta = wallTimeMs - prevWallTimeMs
        val coreCount = Runtime.getRuntime().availableProcessors()
        val usage = if (wallDelta > 0) {
            ((cpuDelta.toFloat() / wallDelta.toFloat()) / coreCount * 100f).coerceIn(0f, 100f)
        } else 0f
        prevCpuTimeMs = cpuTimeMs
        prevWallTimeMs = wallTimeMs

        val frequencies = (0 until coreCount).mapNotNull { core -> readCoreFrequencyMhz(core) }

        return CpuStats(
            usagePercent = usage,
            coreCount = coreCount,
            coreFrequenciesMhz = frequencies,
        )
    }

    private fun readCoreFrequencyMhz(core: Int): Int? = try {
        RandomAccessFile("/sys/devices/system/cpu/cpu$core/cpufreq/scaling_cur_freq", "r").use {
            it.readLine().trim().toInt() / 1000
        }
    } catch (_: Exception) {
        null
    }

    private fun memoryStats(): MemoryStats {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        am.getMemoryInfo(info)
        val totalMb = info.totalMem / (1024 * 1024)
        val availMb = info.availMem / (1024 * 1024)
        return MemoryStats(
            totalMb = totalMb,
            availableMb = availMb,
            usedMb = totalMb - availMb,
            thresholdMb = info.threshold / (1024 * 1024),
            isLowMemory = info.lowMemory,
        )
    }

    private fun batteryStats(): BatteryStats {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val pct = if (scale > 0) (level * 100 / scale) else 0
        val statusInt = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1) ?: -1
        val pluggedInt = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val capacity = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            .takeIf { it > 0 }
            ?.let { it / 1000 }

        return BatteryStats(
            level = pct,
            status = batteryStatusLabel(statusInt),
            health = batteryHealthLabel(healthInt),
            temperatureCelsius = temp,
            voltageMv = voltage,
            technology = technology,
            isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                statusInt == BatteryManager.BATTERY_STATUS_FULL,
            chargePlug = chargePlugLabel(pluggedInt),
            capacityMah = capacity,
        )
    }

    private fun batteryStatusLabel(status: Int): String = when (status) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        else -> "Unknown"
    }

    private fun batteryHealthLabel(health: Int): String = when (health) {
        BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
        BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
        BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
        BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
        BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
        BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
        else -> "Unknown"
    }

    private fun chargePlugLabel(plugged: Int): String = when (plugged) {
        BatteryManager.BATTERY_PLUGGED_AC -> "AC"
        BatteryManager.BATTERY_PLUGGED_USB -> "USB"
        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
        else -> "Unplugged"
    }

    private fun storageStats(): StorageStats {
        val stat = StatFs(context.filesDir.path)
        val totalBytes = stat.totalBytes
        val freeBytes = stat.availableBytes
        val usedBytes = totalBytes - freeBytes
        val gb = 1024f * 1024f * 1024f
        return StorageStats(
            usedGb = usedBytes / gb,
            totalGb = totalBytes / gb,
            freeGb = freeBytes / gb,
        )
    }

    private fun appMemoryStats(): AppMemoryStats {
        val runtime = Runtime.getRuntime()
        val javaUsed = (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024)
        val javaMax = runtime.maxMemory() / (1024 * 1024)
        val nativeHeap = Debug.getNativeHeapAllocatedSize() / (1024 * 1024)

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = am.getProcessMemoryInfo(intArrayOf(Process.myPid())).firstOrNull()
        val totalPss = (memInfo?.totalPss ?: 0) / 1024L

        return AppMemoryStats(
            javaHeapUsedMb = javaUsed,
            javaHeapMaxMb = javaMax,
            nativeHeapUsedMb = nativeHeap,
            totalPssMb = totalPss,
        )
    }

    private fun deviceInfo(): DeviceInfo {
        val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val thermal = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            thermalStatusLabel(pm.currentThermalStatus)
        } else "N/A"

        return DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            androidVersion = Build.VERSION.RELEASE,
            sdkLevel = Build.VERSION.SDK_INT,
            thermalStatus = thermal,
        )
    }

    private fun thermalStatusLabel(status: Int): String = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "Normal"
        PowerManager.THERMAL_STATUS_LIGHT -> "Light"
        PowerManager.THERMAL_STATUS_MODERATE -> "Moderate"
        PowerManager.THERMAL_STATUS_SEVERE -> "Severe"
        PowerManager.THERMAL_STATUS_CRITICAL -> "Critical"
        PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergency"
        PowerManager.THERMAL_STATUS_SHUTDOWN -> "Shutdown"
        else -> "Unknown"
    }
}
