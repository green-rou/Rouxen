package com.greenrou.rouxen.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.greenrou.rouxen.R
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

private data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int,
)

private val bottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, "Home", R.drawable.ic_nav_home),
    BottomNavItem(AppRoute.Apps.route, "Apps", R.drawable.ic_nav_apps),
    BottomNavItem(AppRoute.Settings.route, "Settings", R.drawable.ic_nav_settings),
)

private val IndicatorHeight = 2.dp
private val IndicatorWidth = 32.dp

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val borderColor = RouxenColors.Border
    val borderStrokeWidthPx = with(LocalDensity.current) { 1.dp.toPx() }

    Surface(
        color = RouxenColors.Surface,
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = borderStrokeWidthPx,
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 8.dp)
                .navigationBarsPadding(),
        ) {
            bottomNavItems.forEach { item ->
                val selected = currentRoute == item.route
                val tint = if (selected) RouxenColors.Accent else RouxenColors.TextSecondary

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            navController.navigate(item.route) {
                                popUpTo(AppRoute.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(
                        modifier = Modifier
                            .height(IndicatorHeight)
                            .width(IndicatorWidth)
                            .background(if (selected) RouxenColors.Accent else Color.Transparent),
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(
                        painter = painterResource(id = item.iconRes),
                        contentDescription = item.label,
                        tint = tint,
                        modifier = Modifier.size(24.dp),
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = item.label, style = RouxenTypography.labelSmall, color = tint)
                }
            }
        }
    }
}
