package com.musicbox.player.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkTelegramColors = darkColorScheme(
    primary = Color(0xFF5288C1),
    onPrimary = Color.White,
    secondary = Color(0xFF64B5F6),
    background = Color(0xFF0E1621),
    surface = Color(0xFF17212B),
    surfaceVariant = Color(0xFF242F3D),
    onSurface = Color(0xFFF5F5F5),
    onSurfaceVariant = Color(0xFF7F91A4),
    surfaceContainerHigh = Color(0xFF1C2733)
)

private val LightTelegramColors = lightColorScheme(
    primary = Color(0xFF2481CC),
    onPrimary = Color.White,
    secondary = Color(0xFF3390EC),
    background = Color(0xFFF0F2F5),
    surface = Color.White,
    surfaceVariant = Color(0xFFE4E7EB),
    onSurface = Color(0xFF111111),
    onSurfaceVariant = Color(0xFF707579),
    surfaceContainerHigh = Color(0xFFF4F4F5)
)

@Composable
fun MusicBoxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkTelegramColors else LightTelegramColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
