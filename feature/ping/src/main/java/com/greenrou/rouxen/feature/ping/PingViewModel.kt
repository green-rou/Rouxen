package com.greenrou.rouxen.feature.ping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.system.SystemNetworkClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PingViewModel(private val client: SystemNetworkClient) : ViewModel() {

    private val _state = MutableStateFlow<PingState>(PingState.Idle)
    val state: StateFlow<PingState> = _state.asStateFlow()

    fun analyze(url: String) {
        val host = extractHost(url)
        viewModelScope.launch {
            _state.value = PingState.Loading
            val pingDeferred = async { client.ping(host) }
            val portsDeferred = async { client.scanPorts(host) }
            val pingResult = pingDeferred.await()
            val portsResult = portsDeferred.await()
            _state.value = if (pingResult.isSuccess && portsResult.isSuccess) {
                PingState.Success(
                    ping = pingResult.getOrThrow(),
                    ports = portsResult.getOrThrow(),
                )
            } else {
                PingState.Error(
                    pingResult.exceptionOrNull()?.message
                        ?: portsResult.exceptionOrNull()?.message
                        ?: "Unknown error"
                )
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
