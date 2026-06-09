package com.greenrou.rouxen.feature.ssl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.ssl.domain.GetSslInfoUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SslViewModel(private val getSslInfo: GetSslInfoUseCase) : ViewModel() {

    private val _state = MutableStateFlow<SslState>(SslState.Idle)
    val state: StateFlow<SslState> = _state.asStateFlow()

    fun analyze(url: String) {
        viewModelScope.launch {
            _state.value = SslState.Loading
            getSslInfo(url).collect { result ->
                _state.value = result.fold(
                    onSuccess = { SslState.Success(it) },
                    onFailure = { SslState.Error(it.message ?: "SSL error") },
                )
            }
        }
    }
}
