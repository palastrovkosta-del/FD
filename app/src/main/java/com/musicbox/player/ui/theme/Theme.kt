package com.musicbox.player.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Палитра DDLC (Тук-Тук Литературный клуб)
private val DdlcColorScheme = lightColorScheme(
    primary = Color(0xFFFF5B84),          // Клубнично-розовый цвет логотипа
    onPrimary = Color.White,
    secondary = Color(0xFFBA68C8),        // Нежно-лавандовый цвет Юри
    onSecondary = Color.White,
    background = Color(0xFFFFF0F5),       // Нежный молочно-розовый фон
    onBackground = Color(0xFF381E28),     // Тёмно-ягодный читаемый текст
    surface = Color(0xFFFFFFFF),          // Белые чистые карточки
    onSurface = Color(0xFF2A1520),        // Тёмный контрастный цвет названий
    surfaceVariant = Color(0xFFFFE0EB),   // Розовые поля ввода и кнопки
    onSurfaceVariant = Color(0xFF8A5368), // Мягкий подзаголовок
    surfaceContainerHigh = Color(0xFFFFEBF2),
    outline = Color(0xFFFF8DA1),          // Розовая кайма диалоговых окон
    error = Color(0xFFE53935)
)

@Composable
fun MusicBoxTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DdlcColorScheme,
        content = content
    )
}
