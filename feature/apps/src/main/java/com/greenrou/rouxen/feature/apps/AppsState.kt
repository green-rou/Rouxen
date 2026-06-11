package com.greenrou.rouxen.feature.apps

import com.greenrou.rouxen.feature.apps.model.InstalledApp

sealed class AppsState {
    object Loading : AppsState()
    object Empty : AppsState()
    data class Data(val apps: List<InstalledApp>) : AppsState()
}
