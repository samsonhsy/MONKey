package com.monkey.focus_app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.monkey.focus_app.ui.home.HomeScreen
import com.monkey.focus_app.ui.focus_tag.FocusTagScreen
import com.monkey.focus_app.ui.focus_tag.FocusTagEditScreen
import com.monkey.focus_app.ui.focus_tag.RestrictAppsScreen
import com.monkey.focus_app.ui.session.SessionEditScreen
import com.monkey.focus_app.ui.session.SessionListScreen
import com.monkey.focus_app.ui.settings.SettingsScreen
import com.monkey.focus_app.ui.theme.MONKeyTheme


sealed class MainRoute(val route: String) {
    data object Home : MainRoute("home")
    data object SessionList : MainRoute("sessions")
    data object FocusTags : MainRoute("focus_tags")
    data object FocusTagEdit : MainRoute("focus_tag_edit/{id}") {
        fun create(id: String) = "focus_tag_edit/$id"
    }
    data object FocusTagRestrictApps : MainRoute("focus_tag_restrict_apps/{id}") {
        fun create(id: String) = "focus_tag_restrict_apps/$id"
    }
    data object Settings : MainRoute("settings")

//    id for editing specific session
    data object SessionEdit : MainRoute("session_edit/{id}") {
        fun create(id: String) = "session_edit/$id"
    }
}

enum class TopLevelDestination(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(
        route = MainRoute.Home.route,
        title = "Home",
        icon = Icons.Default.Home
    ),
    SESSIONS(
        route = MainRoute.SessionList.route,
        title = "Sessions",
        icon = Icons.AutoMirrored.Filled.List
    ),
    FOCUS_TAGS(
        route = MainRoute.FocusTags.route,
        title = "Tags",
        icon = Icons.Default.Sell
    ),
    SETTINGS(
        route = MainRoute.Settings.route,
        title = "Settings",
        icon = Icons.Default.Settings
    )
}

@Composable
fun MainNavigation(){
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val showBottomBar = TopLevelDestination.entries.any{
                it.route == currentDestination?.route
            }
            if (showBottomBar) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        NavigationBarItem(
                            selected = currentDestination?.hierarchy?.any{
                                it.route == destination.route
                            } == true,
                            icon = { Icon(
                                destination.icon, contentDescription = destination.title
                            ) },
                            label = { Text(destination.title) },
                            onClick = {
                                navController.navigateToTopLevel(destination.route)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
            enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) },
            popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
            popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) },
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainRoute.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(MainRoute.SessionList.route) {
                SessionListScreen(navController = navController)
            }
            composable(MainRoute.FocusTags.route) {
                FocusTagScreen(navController = navController)
            }
            composable(
                route = MainRoute.FocusTagEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("id") ?: "new"
                FocusTagEditScreen(navController = navController, tagId = tagId)
            }
            composable(
                route = MainRoute.FocusTagRestrictApps.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val tagId = backStackEntry.arguments?.getString("id") ?: "new"
                RestrictAppsScreen(navController = navController, tagId = tagId)
            }
            composable(
                route = MainRoute.SessionEdit.route,
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("id") ?: "new"
                SessionEditScreen(navController = navController, sessionId = sessionId)
            }
            composable(MainRoute.Settings.route) {
                SettingsScreen()
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun MainNavigationPreviewDark() {
    MONKeyTheme(darkTheme = true) {
        MainNavigation()
    }
}
@Preview(showBackground = true)
@Composable
fun MainNavigationPreviewLight() {
    MONKeyTheme {
        MainNavigation()
    }
}

fun NavController.navigateToTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}
