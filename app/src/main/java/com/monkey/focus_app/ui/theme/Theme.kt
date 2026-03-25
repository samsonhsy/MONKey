package com.monkey.focus_app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BrandOrange,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCardBackground, // Used for cards
    onPrimary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextWhite // High contrast text on dark cards
)

private val LightColorScheme = lightColorScheme(
    primary = BrandOrange,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCardBackground, // Used for cards
    onPrimary = TextWhite, // White text on Orange button
    onBackground = TextBlack,
    onSurface = TextBlack,
    onSurfaceVariant = TextBlack // High contrast text on light cards
)

@Composable
fun MONKeyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}