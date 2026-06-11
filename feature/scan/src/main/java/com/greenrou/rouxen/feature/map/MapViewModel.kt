package com.greenrou.rouxen.feature.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

class MapViewModel(
    private val ipInfoClient: IpInfoClient,
    private val okHttpClient: OkHttpClient,
) : ViewModel() {

    private val _state = MutableStateFlow<MapState>(MapState.Idle)
    val state: StateFlow<MapState> = _state.asStateFlow()

    private val _imageState = MutableStateFlow<MapImageState>(MapImageState.Idle)
    val imageState: StateFlow<MapImageState> = _imageState.asStateFlow()

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

    fun loadMapImage(lat: Double, lon: Double) {
        viewModelScope.launch {
            _imageState.value = MapImageState.Loading
            _imageState.value = try {
                val bitmap = withContext(Dispatchers.IO) { fetchStaticMapBitmap(okHttpClient, lat, lon) }
                MapImageState.Loaded(bitmap)
            } catch (_: Exception) {
                MapImageState.Error
            }
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
