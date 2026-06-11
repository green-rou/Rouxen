package com.greenrou.rouxen.feature.traffic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.core.network.ipinfo.IpInfoClient
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RemoteAddressViewModel(
    private val ipInfoClient: IpInfoClient,
) : ViewModel() {

    private val _expandedIps = MutableStateFlow<Set<String>>(emptySet())
    val expandedIps: StateFlow<Set<String>> = _expandedIps.asStateFlow()

    private val _ipInfo = MutableStateFlow<Map<String, IpInfoResponse>>(emptyMap())
    val ipInfo: StateFlow<Map<String, IpInfoResponse>> = _ipInfo.asStateFlow()

    fun toggle(ip: String) {
        val nowExpanded = ip !in _expandedIps.value
        _expandedIps.update { if (nowExpanded) it + ip else it - ip }
        if (nowExpanded && ip !in _ipInfo.value) {
            viewModelScope.launch {
                ipInfoClient.getInfo(ip).onSuccess { info ->
                    _ipInfo.update { it + (ip to info) }
                }
            }
        }
    }
}
