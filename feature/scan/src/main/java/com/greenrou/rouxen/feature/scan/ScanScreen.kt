package com.greenrou.rouxen.feature.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.greenrou.rouxen.core.ui.theme.RouxenColors
import com.greenrou.rouxen.core.ui.theme.RouxenTypography

@Composable
fun ScanScreen(
    url: String,
    onBack: () -> Unit,
    onExport: () -> Unit,
    tabScreens: Map<ScanTab, @Composable () -> Unit>,
) {
    val tabNavController = rememberNavController()
    val backStack by tabNavController.currentBackStackEntryAsState()
    val currentTab = ScanTab.entries.find {
        backStack?.destination?.route == it.route
    } ?: ScanTab.ALL

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
    ) {
        ScanTopBar(url = url, onBack = onBack, onExport = onExport)

        ScrollableTabRow(
            selectedTabIndex = currentTab.ordinal,
            modifier = Modifier.fillMaxWidth(),
            containerColor = RouxenColors.Surface,
            contentColor = RouxenColors.Accent,
            edgePadding = 0.dp,
            indicator = {},
            divider = {},
        ) {
            ScanTab.entries.forEach { tab ->
                Tab(
                    selected = currentTab == tab,
                    onClick = {
                        tabNavController.navigate(tab.route) {
                            popUpTo(tabNavController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    text = {
                        Text(
                            text = tab.label,
                            style = RouxenTypography.labelMedium,
                            color = if (currentTab == tab) RouxenColors.Accent
                            else RouxenColors.TextSecondary,
                        )
                    },
                )
            }
        }

        NavHost(
            navController = tabNavController,
            startDestination = ScanTab.ALL.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            ScanTab.entries.forEach { tab ->
                composable(tab.route) {
                    tabScreens[tab]?.invoke() ?: TabPlaceholderScreen(tab.label)
                }
            }
        }
    }
}

@Composable
private fun ScanTopBar(url: String, onBack: () -> Unit, onExport: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(RouxenColors.Surface)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
            Text("←", style = RouxenTypography.titleSmall, color = RouxenColors.TextPrimary)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = url,
            style = RouxenTypography.labelSmall,
            color = RouxenColors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(4.dp))
        IconButton(onClick = onExport, modifier = Modifier.size(40.dp)) {
            Text("↑", style = RouxenTypography.titleSmall, color = RouxenColors.Accent)
        }
    }
}

@Composable
private fun TabPlaceholderScreen(label: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RouxenColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = RouxenTypography.bodyMedium,
            color = RouxenColors.TextSecondary,
            modifier = Modifier.padding(16.dp),
        )
    }
}
