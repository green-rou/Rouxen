package com.greenrou.rouxen.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val RouxenColorScheme = darkColorScheme(
    primary = RouxenColors.Accent,
    onPrimary = RouxenColors.Background,
    background = RouxenColors.Background,
    onBackground = RouxenColors.TextPrimary,
    surface = RouxenColors.Surface,
    onSurface = RouxenColors.TextPrimary,
    surfaceVariant = RouxenColors.Surface,
    onSurfaceVariant = RouxenColors.TextSecondary,
    outline = RouxenColors.Border,
    outlineVariant = RouxenColors.Border,
    error = RouxenColors.Error,
    onError = RouxenColors.Background,
    secondary = RouxenColors.TextSecondary,
    onSecondary = RouxenColors.Background,
)

@Composable
fun RouxenTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RouxenColorScheme,
        typography = RouxenTypography,
        content = content,
    )
}
