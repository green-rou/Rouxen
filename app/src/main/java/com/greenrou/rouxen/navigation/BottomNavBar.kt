package com.greenrou.rouxen.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.greenrou.rouxen.R
import com.greenrou.rouxen.core.ui.theme.RouxenColors

private data class BottomNavItem(
    val route: String,
    val label: String,
    val iconRes: Int,
)

private val bottomNavItems = listOf(
    BottomNavItem(AppRoute.Home.route, "Home", R.drawable.ic_nav_home),
    BottomNavItem(AppRoute.History.route, "History", R.drawable.ic_nav_history),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar(
        containerColor = RouxenColors.Surface,
        tonalElevation = 0.dp,
    ) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(AppRoute.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        painter = painterResource(item.iconRes),
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = RouxenColors.Accent,
                    selectedTextColor = RouxenColors.Accent,
                    unselectedIconColor = RouxenColors.TextSecondary,
                    unselectedTextColor = RouxenColors.TextSecondary,
                    indicatorColor = RouxenColors.Border,
                ),
            )
        }
    }
}
