package com.greenrou.rouxen.feature.apps.model

import androidx.compose.ui.graphics.ImageBitmap

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: ImageBitmap,
)
