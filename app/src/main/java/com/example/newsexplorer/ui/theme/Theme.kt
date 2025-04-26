package com.example.newsexplorer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// Define color schemes
private val LightColors = lightColorScheme(
    primary = Color(0xFF0066CC),
    onPrimary = Color.White,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color(0xFFF8F8F8),
    onBackground = Color(0xFF121212),
    surface = Color.White,
    onSurface = Color(0xFF121212)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4A9DFF),
    onPrimary = Color.Black,
    secondary = Color(0xFF03DAC5),
    onSecondary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF242424),
    onSurface = Color.White
)

// Font sizes
enum class FontSize { Small, Medium, Large }

val LocalFontSize = compositionLocalOf { FontSize.Medium }

@Composable
fun NewsExplorerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontSize: FontSize = FontSize.Medium,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    CompositionLocalProvider(LocalFontSize provides fontSize) {
        MaterialTheme(
            colorScheme = colors,
            typography = Typography,
            shapes = Shapes,
            content = content
        )
    }
}