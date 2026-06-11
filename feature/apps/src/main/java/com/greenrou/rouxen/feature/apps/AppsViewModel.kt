package com.greenrou.rouxen.feature.apps

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenrou.rouxen.feature.apps.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel(private val context: Context) : ViewModel() {

    private val _allApps = MutableStateFlow<List<InstalledApp>?>(null)

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val state: StateFlow<AppsState> = combine(_allApps, _query) { apps, query ->
        when {
            apps == null -> AppsState.Loading
            apps.isEmpty() -> AppsState.Empty
            else -> {
                val filtered = if (query.isBlank()) {
                    apps
                } else {
                    apps.filter {
                        it.label.contains(query, ignoreCase = true) ||
                            it.packageName.contains(query, ignoreCase = true)
                    }
                }
                AppsState.Data(filtered)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppsState.Loading)

    init {
        viewModelScope.launch {
            _allApps.value = withContext(Dispatchers.IO) { loadApps() }
        }
    }

    fun onSearchQueryChange(query: String) {
        _query.value = query
    }

    private fun loadApps(): List<InstalledApp> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }

        return resolveInfos
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                InstalledApp(
                    packageName = resolveInfo.activityInfo.packageName,
                    label = resolveInfo.loadLabel(pm).toString(),
                    icon = resolveInfo.loadIcon(pm).toBitmap().asImageBitmap(),
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    fun launchApp(packageName: String) {
        context.packageManager.getLaunchIntentForPackage(packageName)?.let { launchIntent ->
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
        }
    }
}
