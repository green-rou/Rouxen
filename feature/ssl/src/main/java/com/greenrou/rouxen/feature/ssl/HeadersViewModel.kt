package com.greenrou.rouxen.feature.ssl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.ssl.domain.GetHttpHeadersUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HeadersViewModel(private val getHttpHeaders: GetHttpHeadersUseCase) : ViewModel() {

    private val _state = MutableStateFlow<HeadersState>(HeadersState.Idle)
    val state: StateFlow<HeadersState> = _state.asStateFlow()

    fun analyze(url: String) {
        viewModelScope.launch {
            _state.value = HeadersState.Loading
            getHttpHeaders(url).collect { result ->
                _state.value = result.fold(
                    onSuccess = { HeadersState.Success(it) },
                    onFailure = { HeadersState.Error(it.message ?: "Headers error") },
                )
            }
        }
    }
}
