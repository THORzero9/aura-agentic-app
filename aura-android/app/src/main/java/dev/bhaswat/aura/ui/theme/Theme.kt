package dev.bhaswat.aura.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define our custom Light Color Scheme using the colors from Color.kt
private val LightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = PrimaryText,
    background = AppBackground,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    secondary = AccentBlue, // You can define more specific colors if needed
    onSecondary = PrimaryText
)

// For the hackathon, we can use the same scheme for Dark mode to keep it simple
private val DarkColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = PrimaryText,
    background = AppBackground,
    surface = CardBackground,
    onBackground = PrimaryText,
    onSurface = PrimaryText,
    secondary = AccentBlue,
    onSecondary = PrimaryText
)

@Composable
fun AuraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            // We are not using dynamic color for this project, but leave the logic
            LightColorScheme
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}