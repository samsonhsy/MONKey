package com.monkey.focus_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monkey.focus_app.ui.theme.MONKeyTheme
import com.monkey.focus_app.ui.home.HomeScreen
import com.monkey.focus_app.ui.navigation.MainNavigation
import com.monkey.focus_app.ui.navigation.MainRoute
import com.monkey.focus_app.ui.session.SessionListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MONKeyTheme {
                MainNavigation()
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainPreview() {
    MONKeyTheme {

    }
}