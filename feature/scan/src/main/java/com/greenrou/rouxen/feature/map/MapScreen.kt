package com.greenrou.rouxen.feature.map

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.network.ipinfo.IpInfoResponse
import com.greenrou.rouxen.core.ui.components.BadgeStatus
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.components.StatusBadge
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import org.koin.androidx.compose.koinViewModel
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MapScreen(
    url: String,
    viewModel: MapViewModel = koinViewModel(),
) {
    LaunchedEffect(url) { viewModel.locate(url) }
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        when (val s = state) {
            is MapState.Idle -> Unit
            is MapState.Loading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center).size(32.dp),
                color = RouxenColors.Accent,
                strokeWidth = 2.dp,
            )
            is MapState.Error -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                StatusBadge(label = s.message, status = BadgeStatus.Error)
            }
            is MapState.Success -> MapContent(info = s.info)
        }
    }
}

@Composable
private fun MapContent(info: IpInfoResponse) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { RadarMap(lat = info.lat, lon = info.lon) }
        item { ServerLocationCard(info) }
    }
}

// ============================================================
// Radar / HUD canvas
// ============================================================

@Composable
private fun RadarMap(lat: Double, lon: Double) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")

    // Rotating sweep angle: 0 -> 360 degrees, looping
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
        ),
        label = "sweepAngle",
    )

    // Pulsing "ping" ring: 0 -> 1 progress, restarting
    val pingProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pingProgress",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .background(RouxenColors.Surface),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Project lat/lon to canvas coordinates (equirectangular projection)
            val px = ((lon + 180.0) / 360.0 * w).toFloat()
            val py = ((90.0 - lat) / 180.0 * h).toFloat()
            val target = Offset(px, py)

            drawWorldGrid()
            drawCrosshair(target)
            drawRadarRings(target)
            drawSweep(center = Offset(w / 2f, h / 2f), angleDegrees = sweepAngle)
            drawPing(target, progress = pingProgress)
            drawCornerBrackets()
        }

        // Coordinate readout, top-left
        Text(
            text = "LAT %.4f  LON %.4f".format(lat, lon),
            style = RouxenTypography.labelSmall,
            color = RouxenColors.Accent,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp),
        )
    }
}

/** Faint world grid: meridians/parallels every 30 degrees, equirectangular projection. */
private fun DrawScope.drawWorldGrid() {
    val w = size.width
    val h = size.height
    val gridColor = RouxenColors.Border.copy(alpha = 0.4f)
    val centerLineColor = RouxenColors.Border.copy(alpha = 0.8f)

    // Vertical lines (meridians): every 30 deg of longitude -> 12 lines across width
    for (lonStep in -180..180 step 30) {
        val x = ((lonStep + 180) / 360f) * w
        drawLine(
            color = if (lonStep == 0) centerLineColor else gridColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 1.dp.toPx(),
        )
    }

    // Horizontal lines (parallels): every 30 deg of latitude -> lines across height
    for (latStep in -90..90 step 30) {
        val y = ((90 - latStep) / 180f) * h
        drawLine(
            color = if (latStep == 0) centerLineColor else gridColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

/** Faint dashed crosshair through the target point. */
private fun DrawScope.drawCrosshair(target: Offset) {
    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
    val color = RouxenColors.Accent.copy(alpha = 0.2f)

    drawLine(
        color = color,
        start = Offset(0f, target.y),
        end = Offset(size.width, target.y),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect,
    )
    drawLine(
        color = color,
        start = Offset(target.x, 0f),
        end = Offset(target.x, size.height),
        strokeWidth = 1.dp.toPx(),
        pathEffect = dashEffect,
    )
}

/** 3 concentric radar rings centered on the target, decreasing alpha outward. */
private fun DrawScope.drawRadarRings(target: Offset) {
    val maxRadius = size.minDimension * 0.35f
    val ringCount = 3
    for (i in 1..ringCount) {
        val radius = maxRadius * i / ringCount
        val alpha = 0.5f - (i - 1) * 0.15f
        drawCircle(
            color = RouxenColors.Accent.copy(alpha = alpha.coerceAtLeast(0.05f)),
            radius = radius,
            center = target,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

/** Animated rotating sweep line from canvas center. */
private fun DrawScope.drawSweep(center: Offset, angleDegrees: Float) {
    val radius = size.minDimension * 0.45f
    val angleRad = Math.toRadians(angleDegrees.toDouble())
    val end = Offset(
        x = center.x + (radius * cos(angleRad)).toFloat(),
        y = center.y + (radius * sin(angleRad)).toFloat(),
    )
    drawLine(
        color = RouxenColors.Accent.copy(alpha = 0.6f),
        start = center,
        end = end,
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
    )
}

/** Pulsing "ping" marker: static dot + expanding fading ring. */
private fun DrawScope.drawPing(target: Offset, progress: Float) {
    val maxRadius = size.minDimension * 0.35f

    // Expanding ring, fading out
    drawCircle(
        color = RouxenColors.Accent.copy(alpha = (1f - progress).coerceIn(0f, 1f)),
        radius = maxRadius * progress,
        center = target,
        style = Stroke(width = 2.dp.toPx()),
    )

    // Static center dot
    drawCircle(
        color = RouxenColors.Accent,
        radius = 5.dp.toPx(),
        center = target,
    )
}

/** Sci-fi HUD corner brackets in all 4 corners. */
private fun DrawScope.drawCornerBrackets() {
    val len = size.minDimension * 0.06f
    val stroke = 2.dp.toPx()
    val color = RouxenColors.Accent.copy(alpha = 0.7f)
    val w = size.width
    val h = size.height
    val inset = 8.dp.toPx()

    // Top-left
    drawLine(color, Offset(inset, inset), Offset(inset + len, inset), stroke)
    drawLine(color, Offset(inset, inset), Offset(inset, inset + len), stroke)

    // Top-right
    drawLine(color, Offset(w - inset, inset), Offset(w - inset - len, inset), stroke)
    drawLine(color, Offset(w - inset, inset), Offset(w - inset, inset + len), stroke)

    // Bottom-left
    drawLine(color, Offset(inset, h - inset), Offset(inset + len, h - inset), stroke)
    drawLine(color, Offset(inset, h - inset), Offset(inset, h - inset - len), stroke)

    // Bottom-right
    drawLine(color, Offset(w - inset, h - inset), Offset(w - inset - len, h - inset), stroke)
    drawLine(color, Offset(w - inset, h - inset), Offset(w - inset, h - inset - len), stroke)
}

// ============================================================
// Server location info card
// ============================================================

@Composable
private fun ServerLocationCard(info: IpInfoResponse) {
    RouxenCard {
        Text("Server Location", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
        Spacer(modifier = Modifier.height(8.dp))
        MapInfoRow("IP", info.ip)
        MapInfoRow("City", info.city)
        MapInfoRow("Region", info.region)
        MapInfoRow("Country", info.country)
        MapInfoRow("ISP", info.isp)
        MapInfoRow("Org", info.org)
        MapInfoRow("AS", info.asNumber)
        MapInfoRow("Coordinates", "${info.lat}, ${info.lon}")
    }
}

@Composable
private fun MapInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = RouxenTypography.labelSmall, color = RouxenColors.TextSecondary)
        Text(text = value, style = RouxenTypography.bodySmall, color = RouxenColors.TextPrimary)
    }
}
