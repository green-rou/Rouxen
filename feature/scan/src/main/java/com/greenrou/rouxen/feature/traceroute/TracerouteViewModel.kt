package com.greenrou.rouxen.feature.traceroute

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.traceroute.TracerouteClient
import com.greenrou.rouxen.core.network.traceroute.TracerouteHop
import com.greenrou.rouxen.core.network.traceroute.TracerouteResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TracerouteViewModel(private val client: TracerouteClient) : ViewModel() {

    private val _state = MutableStateFlow<TracerouteState>(TracerouteState.Idle)
    val state: StateFlow<TracerouteState> = _state.asStateFlow()

    fun trace(url: String) {
        val host = extractHost(url)
        viewModelScope.launch {
            _state.value = TracerouteState.Loading
            val hops = mutableListOf<TracerouteHop>()
            client.trace(host).collect { result ->
                when (result) {
                    is TracerouteResult.Hop -> {
                        hops.add(result.hop)
                        _state.value = TracerouteState.Running(hops.toList())
                    }
                    is TracerouteResult.Complete -> _state.value = TracerouteState.Complete(hops.toList())
                    is TracerouteResult.Unsupported -> _state.value = TracerouteState.Unsupported
                    is TracerouteResult.Error -> _state.value = TracerouteState.Error(result.message)
                }
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
