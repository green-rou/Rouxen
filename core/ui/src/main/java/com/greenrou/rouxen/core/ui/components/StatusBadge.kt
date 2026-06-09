package com.greenrou.rouxen.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTheme
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

enum class BadgeStatus { Success, Warning, Error, Neutral }

@Composable
fun StatusBadge(
    label: String,
    status: BadgeStatus,
    modifier: Modifier = Modifier,
) {
    val (dotColor, borderColor) = when (status) {
        BadgeStatus.Success -> RouxenColors.Accent to RouxenColors.Accent.copy(alpha = 0.3f)
        BadgeStatus.Warning -> RouxenColors.Warning to RouxenColors.Warning.copy(alpha = 0.3f)
        BadgeStatus.Error -> RouxenColors.Error to RouxenColors.Error.copy(alpha = 0.3f)
        BadgeStatus.Neutral -> RouxenColors.TextSecondary to RouxenColors.Border
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(RouxenColors.Surface)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(6.dp)
                .clip(RoundedCornerShape(50))
                .background(dotColor),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = RouxenTypography.labelMedium,
            color = RouxenColors.TextPrimary,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0D1117)
@Composable
private fun StatusBadgePreview() {
    RouxenTheme {
        Row(modifier = Modifier.padding(8.dp)) {
            StatusBadge("ONLINE", BadgeStatus.Success)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge("EXPIRING", BadgeStatus.Warning)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge("MISSING", BadgeStatus.Error)
            Spacer(modifier = Modifier.width(8.dp))
            StatusBadge("UNKNOWN", BadgeStatus.Neutral)
        }
    }
}
