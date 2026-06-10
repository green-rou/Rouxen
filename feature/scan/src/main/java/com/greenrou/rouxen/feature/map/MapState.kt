package com.greenrou.rouxen.feature.map

import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse

sealed class MapState {
    object Idle : MapState()
    object Loading : MapState()
    data class Success(val info: IpInfoResponse) : MapState()
    data class Error(val message: String) : MapState()
}
