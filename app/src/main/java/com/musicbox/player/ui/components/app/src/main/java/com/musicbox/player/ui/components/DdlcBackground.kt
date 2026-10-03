package com.musicbox.player.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun DdlcBackground(modifier: Modifier = Modifier) {
    // Точный цвет горошка DDLC
    val dotColor = Color(0xFFF9C2D2)
    val dotRadius = 24f // Размер горошин
    val spacingX = 96f // Расстояние между горошинами
    val spacingY = 96f

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(Color.White) // Белый чистый фон

        val columns = (size.width / spacingX).toInt() + 2
        val rows = (size.height / spacingY).toInt() + 2

        for (row in 0..rows) {
            val offsetX = if (row % 2 == 1) spacingX / 2f else 0f
            for (col in 0..columns) {
                val cx = col * spacingX + offsetX
                val cy = row * spacingY
                drawCircle(
                    color = dotColor,
                    radius = dotRadius,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
