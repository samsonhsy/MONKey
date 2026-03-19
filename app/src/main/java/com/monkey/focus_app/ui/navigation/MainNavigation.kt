package com.monkey.focus_app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.monkey.focus_app.ui.home.HomeScreen
import com.monkey.focus_app.ui.session.SessionListScreen


sealed class MainRoute(val route: String) {
    data object Home : MainRoute("home")
    data object SessionList : MainRoute("sessions")

//    id for editing specific session
    data object SessionEdit : MainRoute("sessions/{id}") {
        fun create(id: String) = "sessions/$id"
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
    )
}

@Preview
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
                                navController.navigate(destination.route){
                                    popUpTo(navController.graph.findStartDestination().id){
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainRoute.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(MainRoute.Home.route) {
                HomeScreen(navController = navController)
            }
            composable(MainRoute.SessionList.route) {
                SessionListScreen(navController = navController)
            }
        }
    }

}