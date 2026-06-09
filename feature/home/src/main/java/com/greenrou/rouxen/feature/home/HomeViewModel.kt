package com.greenrou.rouxen.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class HomeViewModel : ViewModel() {

    private val _input = MutableStateFlow("")
    val input: StateFlow<String> = _input.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun onInputChange(value: String) {
        _input.update { value }
        _error.update { null }
    }

    fun buildAnalyzeUrl(): String? {
        val raw = _input.value.trim()
        if (raw.isBlank()) {
            _error.update { "Enter a URL" }
            return null
        }
        val url = if (!raw.startsWith("http://") && !raw.startsWith("https://")) {
            "https://$raw"
        } else {
            raw
        }
        if (!isValidUrl(url)) {
            _error.update { "Invalid URL" }
            return null
        }
        return url
    }

    private fun isValidUrl(url: String): Boolean {
        return try {
            val uri = android.net.Uri.parse(url)
            val host = uri.host
            !host.isNullOrBlank() && host.contains(".")
        } catch (_: Exception) {
            false
        }
    }
}
