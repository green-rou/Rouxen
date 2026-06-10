package com.greenrou.rouxen.feature.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.feature.home.R
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

private data class Tool(
    @DrawableRes val iconRes: Int,
    val title: String,
    val subtitle: String,
    val available: Boolean = true,
)

private val tools = listOf(
    Tool(R.drawable.ic_tool_analyzer, "Site Analyzer", "DNS · SSL · Headers"),
    Tool(R.drawable.ic_tool_wifi, "WiFi & BLE", "Networks & devices"),
    Tool(R.drawable.ic_tool_monitor, "Device Monitor", "CPU · RAM · Battery", available = false),
)

@Composable
fun HomeScreen(
    onSiteAnalyzer: () -> Unit,
    onWifiScanner: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background)
            .padding(16.dp),
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Rouxen",
            style = RouxenTypography.titleMedium,
            color = RouxenColors.Accent,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Network analyzer",
            style = RouxenTypography.bodySmall,
            color = RouxenColors.TextSecondary,
        )

        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(0.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(tools) { tool ->
                ToolTile(
                    tool = tool,
                    onClick = when (tool.title) {
                        "Site Analyzer" -> onSiteAnalyzer
                        "WiFi & BLE" -> onWifiScanner
                        else -> null
                    },
                )
            }
        }
    }
}

@Composable
private fun ToolTile(tool: Tool, onClick: (() -> Unit)?) {
    val alpha = if (tool.available) 1f else 0.4f
    val borderColor = if (tool.available) RouxenColors.Border
    else RouxenColors.Border.copy(alpha = 0.3f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(RouxenColors.Surface)
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(4.dp))
            .then(
                if (onClick != null && tool.available) {
                    Modifier.clickable(onClick = onClick)
                } else Modifier
            )
            .padding(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Icon(
                painter = painterResource(id = tool.iconRes),
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = RouxenColors.Accent.copy(alpha = alpha),
            )
            Column {
                Text(
                    text = tool.title,
                    style = RouxenTypography.bodySmall,
                    color = RouxenColors.TextPrimary.copy(alpha = alpha),
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (tool.available) tool.subtitle else "Coming soon",
                    style = RouxenTypography.labelSmall,
                    color = RouxenColors.TextSecondary.copy(alpha = alpha),
                )
            }
        }
    }
}
