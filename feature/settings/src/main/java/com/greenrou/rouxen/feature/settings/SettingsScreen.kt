package com.greenrou.rouxen.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

@Composable
fun SettingsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Settings",
            style = RouxenTypography.titleMedium,
            color = RouxenColors.TextPrimary,
        )

        Spacer(modifier = Modifier.height(24.dp))

        RouxenCard {
            SettingsRow(label = "App", value = "Rouxen")
            SettingsRow(label = "Version", value = "1.0.0")
            SettingsRow(label = "Package", value = "com.greenrou.rouxen")
        }

        Spacer(modifier = Modifier.height(16.dp))

        RouxenCard {
            SettingsRow(label = "DNS resolver", value = "8.8.8.8 · 1.1.1.1")
            SettingsRow(label = "IP info provider", value = "ip-api.com")
            SettingsRow(label = "WHOIS server", value = "whois.iana.org")
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
