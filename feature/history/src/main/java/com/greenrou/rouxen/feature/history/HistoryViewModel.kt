package com.greenrou.rouxen.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.history.domain.DeleteScanUseCase
import com.greenrou.rouxen.feature.history.domain.GetScanHistoryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    getHistory: GetScanHistoryUseCase,
    private val deleteScan: DeleteScanUseCase,
) : ViewModel() {

    val state: StateFlow<HistoryState> = getHistory()
        .map { scans ->
            if (scans.isEmpty()) HistoryState.Empty else HistoryState.Data(scans)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HistoryState.Loading,
        )

    fun delete(id: Long) {
        viewModelScope.launch { deleteScan(id) }
    }
}
