package com.monkey.focus_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.monkey.focus_app.ui.theme.MONKeyTheme
import com.monkey.focus_app.ui.navigation.MainNavigation

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