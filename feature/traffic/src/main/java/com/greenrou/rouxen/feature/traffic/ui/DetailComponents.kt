package com.greenrou.rouxen.feature.traffic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

@Composable
internal fun DetailHeader(title: String, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TextButton(onClick = onBack) {
            Text("← Back", style = RouxenTypography.labelMedium, color = RouxenColors.Accent)
        }
        Text(
            text = title,
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextPrimary,
            modifier = Modifier.padding(start = 4.dp),
        )
    }
}

@Composable
internal fun DetailSectionLabel(label: String) {
    Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
internal fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
