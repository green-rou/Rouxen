package com.greenrou.rouxen.feature.history.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val url: String,
    val scannedAtMs: Long,
    val dnsRecordCount: Int,
    val sslValid: Boolean,
    val sslDaysUntilExpiry: Long?,
    val pingReachable: Boolean,
    val pingLatencyMs: Long?,
)
