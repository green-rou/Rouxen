package com.greenrou.rouxen.feature.device

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.device.model.DeviceStats
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceMonitorViewModel(
    private val repository: DeviceStatsRepository,
) : ViewModel() {

    private val _stats = MutableStateFlow<DeviceStats?>(null)
    val stats = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            repository.snapshot()
            while (true) {
                _stats.value = repository.snapshot()
                delay(1000)
            }
        }
    }
}
