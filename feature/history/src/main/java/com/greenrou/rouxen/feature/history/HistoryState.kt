package com.greenrou.rouxen.feature.history

import com.greenrou.rouxen.feature.history.db.ScanResultEntity

sealed class HistoryState {
    object Loading : HistoryState()
    object Empty : HistoryState()
    data class Data(val scans: List<ScanResultEntity>) : HistoryState()
}
