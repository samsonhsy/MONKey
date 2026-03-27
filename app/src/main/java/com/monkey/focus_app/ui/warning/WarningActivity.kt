package com.monkey.focus_app.ui.warning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monkey.focus_app.ui.navigation.NavigationItem
import com.monkey.focus_app.ui.theme.MONKeyTheme

private val warningScreen = WarningScreen()

class WarningActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MONKeyTheme {
                val navController = rememberNavController()
                val modeName = NavigationItem.Novice.route
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = NavigationItem.Warning.route
                    ) {
                        composable(NavigationItem.Warning.route) {
                            warningScreen.Warning(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                modeName = modeName
                            )
                        }
                        composable(NavigationItem.Novice.route) {
                            warningScreen.Novice(
                                modifier = Modifier.padding(innerPadding),
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun WarningPreview() {
    MONKeyTheme {
//        warningScreen.Warning(navController = rememberNavController())
    }
}