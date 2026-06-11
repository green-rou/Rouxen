package com.greenrou.rouxen.feature.traffic.ui

import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.feature.traffic.model.ConnectionState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val KB = 1024.0
private const val MB = KB * 1024
private const val GB = MB * 1024

fun formatRate(bps: Long): String = when {
    bps >= MB -> "%.1f MB/s".format(bps / MB)
    bps >= KB -> "%.1f KB/s".format(bps / KB)
    else -> "$bps B/s"
}

fun formatBytes(bytes: Long): String = when {
    bytes >= GB -> "%.2f GB".format(bytes / GB)
    bytes >= MB -> "%.1f MB".format(bytes / MB)
    bytes >= KB -> "%.1f KB".format(bytes / KB)
    else -> "$bytes B"
}

fun formatTimestamp(epochMs: Long): String =
    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(epochMs))

internal fun stateBadgeStatus(state: ConnectionState): BadgeStatus = when (state) {
    ConnectionState.ESTABLISHED, ConnectionState.ACTIVE -> BadgeStatus.Success
    ConnectionState.CLOSING -> BadgeStatus.Warning
    ConnectionState.IDLE, ConnectionState.CLOSED -> BadgeStatus.Neutral
}
