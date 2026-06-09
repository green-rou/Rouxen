package com.greenrou.rouxen.feature.whois

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import com.greenrou.rouxen.core.network.whois.WhoisClient
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WhoisViewModel(
    private val whoisClient: WhoisClient,
    private val ipInfoClient: IpInfoClient,
) : ViewModel() {

    private val _state = MutableStateFlow<WhoisState>(WhoisState.Idle)
    val state: StateFlow<WhoisState> = _state.asStateFlow()

    fun analyze(url: String) {
        val host = extractHost(url)
        viewModelScope.launch {
            _state.value = WhoisState.Loading
            val whoisDeferred = async { whoisClient.lookup(host) }
            val ipInfoDeferred = async { ipInfoClient.getInfo(host) }
            val whoisResult = whoisDeferred.await()
            val ipInfoResult = ipInfoDeferred.await()
            _state.value = if (ipInfoResult.isSuccess) {
                WhoisState.Success(
                    ipInfo = ipInfoResult.getOrThrow(),
                    whoisText = whoisResult.getOrElse { "WHOIS lookup failed: ${it.message}" },
                )
            } else {
                WhoisState.Error(
                    ipInfoResult.exceptionOrNull()?.message ?: "Unknown error"
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
