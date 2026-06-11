package com.greenrou.rouxen.feature.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.greenrou.rouxen.core.ui.components.RouxenCard
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography
import kotlinx.coroutines.delay

private val activityLog = listOf(
    "Initializing network scan...",
    "Interface wlan0: link up",
    "Resolving DNS A record...",
    "TLS handshake OK · TLS 1.3",
    "Ping 1.1.1.1: 11ms",
    "Scanning ports 22, 80, 443...",
    "BLE: 3 devices nearby",
    "Traffic ↓ 1.4 MB/s ↑ 220 KB/s",
    "Cert valid until 2027-01-15",
    "ARP table refreshed",
    "Checking firewall rules...",
    "Memory usage: 42%",
    "No threats detected",
    "Background scan complete",
    "Resolving AAAA record...",
    "MX record: aspmx.l.google.com",
    "WHOIS lookup: registrar found",
    "Traceroute: 9 hops · 38ms",
    "Ping 8.8.8.8: 14ms",
    "Port 443 open · port 23 closed",
    "HSTS header: enforced",
    "CSP policy: present",
    "WiFi: 'HomeNet_5G' · WPA3 · -54 dBm",
    "BLE: AirTag nearby, RSSI -61",
    "CPU load: 23% @ 1.8 GHz",
    "Battery: 87% · charging",
    "Storage: 64.2 GB free",
    "Thermal status: nominal",
    "New connection → 142.250.74.46:443",
    "Process chrome: ↓ 640 KB/s",
    "Geo lookup: Frankfurt, DE",
    "VPN tunnel established",
    "Loaded 142 installed apps",
    "Route table: 12 entries",
)

@Composable
fun LiveActivityCard(modifier: Modifier = Modifier) {
    RouxenCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PulsingDot()
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "SYSTEM_LOG",
                style = RouxenTypography.labelMedium,
                color = RouxenColors.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        TypewriterLine(messages = activityLog, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        ScanningBar()
    }
}

@Composable
private fun PulsingDot(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "dot")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "dotAlpha",
    )
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(RouxenColors.Accent.copy(alpha = alpha)),
    )
}

@Composable
private fun BlinkingCursor(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "cursor")
    val alpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
    Text(
        text = "█",
        style = RouxenTypography.bodyMedium,
        color = RouxenColors.Accent.copy(alpha = alpha),
        modifier = modifier,
    )
}

@Composable
private fun TypewriterLine(messages: List<String>, modifier: Modifier = Modifier) {
    var messageIndex by remember { mutableIntStateOf(0) }
    var visibleChars by remember { mutableIntStateOf(0) }

    LaunchedEffect(messageIndex) {
        val message = messages[messageIndex]
        for (i in 1..message.length) {
            visibleChars = i
            delay(35)
        }
        delay(1800)
        visibleChars = 0
        messageIndex = (messageIndex + 1) % messages.size
    }

    val message = messages[messageIndex]
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$ ",
            style = RouxenTypography.bodyMedium,
            color = RouxenColors.Accent,
        )
        Text(
            text = message.take(visibleChars),
            style = RouxenTypography.bodyMedium,
            color = RouxenColors.TextPrimary,
            maxLines = 1,
        )
        BlinkingCursor()
    }
}

@Composable
private fun ScanningBar(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "scan")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanProgress",
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(2.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(RouxenColors.Border),
    ) {
        val barWidth = maxWidth * 0.35f
        val x = -barWidth + (maxWidth + barWidth) * progress
        Box(
            modifier = Modifier
                .width(barWidth)
                .fillMaxHeight()
                .offset(x = x)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, RouxenColors.Accent, Color.Transparent),
                    ),
                ),
        )
    }
}
