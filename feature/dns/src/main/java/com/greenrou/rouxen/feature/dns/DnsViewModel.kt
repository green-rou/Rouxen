package com.greenrou.rouxen.feature.dns

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.dns.domain.GetDnsRecordsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DnsViewModel(private val getDnsRecords: GetDnsRecordsUseCase) : ViewModel() {

    private val _state = MutableStateFlow<DnsState>(DnsState.Idle)
    val state: StateFlow<DnsState> = _state.asStateFlow()

    fun analyze(url: String) {
        val host = extractHost(url)
        viewModelScope.launch {
            _state.value = DnsState.Loading
            getDnsRecords(host).collect { result ->
                _state.value = result.fold(
                    onSuccess = { DnsState.Success(it) },
                    onFailure = { DnsState.Error(it.message ?: "Unknown error") },
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
