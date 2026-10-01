package com.musicbox.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonPurpleColorScheme = darkColorScheme(
    primary = Color(0xFFA855F7),          // Яркий неоновый фиолетовый
    onPrimary = Color.White,
    secondary = Color(0xFFC084FC),        // Светло-фиолетовый акцент
    onSecondary = Color.Black,
    background = Color(0xFF0F0B1E),       // Глубокий ночной фиолетовый фон
    onBackground = Color(0xFFF3E8FF),     // Светлый текст
    surface = Color(0xFF18122B),          // Цвет карточек
    onSurface = Color(0xFFFFFFFF),        // Кристально белый цвет названий
    surfaceVariant = Color(0xFF261E43),   // Выделенные элементы
    onSurfaceVariant = Color(0xFFD8B4FE), // Лавандовый вторичный текст
    surfaceContainerHigh = Color(0xFF20173D),
    error = Color(0xFFFF5252)
)

@Composable
fun MusicBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NeonPurpleColorScheme,
        content = content
    )
}
