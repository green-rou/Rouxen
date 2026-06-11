package com.greenrou.rouxen.feature.traffic.ui

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
