package com.musicbox.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class SakuraPetal(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val isPetal: Boolean,
    val alpha: Float
)

@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        List(35) {
            SakuraPetal(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 6f + 3f,
                speed = Random.nextFloat() * 0.35f + 0.15f,
                isPetal = Random.nextBoolean(),
                alpha = Random.nextFloat() * 0.35f + 0.15f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "sakura")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sakuraProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (p in particles) {
            // Лепестки плавно кружатся и опускаются сверху вниз
            val currentY = ((p.initialY + progress * p.speed) % 1f) * height
            val currentX = (p.initialX * width + kotlin.math.sin(progress * 6.28f + p.initialY) * 25f).toFloat()

            val petalColor = if (p.isPetal) Color(0xFFFF8DA1) else Color(0xFFFFB6C1)

            drawCircle(
                color = petalColor.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(currentX, currentY)
            )
        }
    }
}
