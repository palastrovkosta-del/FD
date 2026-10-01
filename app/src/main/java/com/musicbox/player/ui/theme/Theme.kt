package com.musicbox.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DusttaleColorScheme = darkColorScheme(
    primary = Color(0xFFFF1744),          // Кроваво-красный (Determination)
    onPrimary = Color.White,
    secondary = Color(0xFFA855F7),        // Фиолетовый глаз Санса
    onSecondary = Color.White,
    background = Color(0xFF08050E),       // Чернильно-черный фон
    onBackground = Color(0xFFFEE2E2),     // Светло-алый текст
    surface = Color(0xFF140A1E),          // Блоки треков
    onSurface = Color(0xFFFFFFFF),        // Кристально белый цвет названий
    surfaceVariant = Color(0xFF241033),   // Акцентные плашки
    onSurfaceVariant = Color(0xFFE2C4FF), // Лавандово-пепельный текст
    surfaceContainerHigh = Color(0xFF1C0D2A),
    error = Color(0xFFFF0033)
)

@Composable
fun MusicBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DusttaleColorScheme,
        content = content
    )
}
