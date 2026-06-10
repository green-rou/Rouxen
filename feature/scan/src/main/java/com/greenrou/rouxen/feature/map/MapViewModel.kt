package com.greenrou.rouxen.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MapViewModel(private val ipInfoClient: IpInfoClient) : ViewModel() {

    private val _state = MutableStateFlow<MapState>(MapState.Idle)
    val state: StateFlow<MapState> = _state.asStateFlow()

    fun locate(url: String) {
        val host = extractHost(url)
        viewModelScope.launch {
            _state.value = MapState.Loading
            _state.value = ipInfoClient.getInfo(host).fold(
                onSuccess = { MapState.Success(it) },
                onFailure = { MapState.Error(it.message ?: "Unknown error") },
            )
        }
    }

    private fun extractHost(url: String): String {
        return try {
            android.net.Uri.parse(url).host ?: url
        } catch (_: Exception) {
            url
        }
    }
}
