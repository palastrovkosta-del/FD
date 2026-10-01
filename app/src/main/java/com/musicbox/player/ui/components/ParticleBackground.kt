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

private data class DustParticle(
    val initialX: Float,
    val initialY: Float,
    val radius: Float,
    val speed: Float,
    val isRed: Boolean,
    val alpha: Float
)

@Composable
fun ParticleBackground(modifier: Modifier = Modifier) {
    val particles = remember {
        List(30) {
            DustParticle(
                initialX = Random.nextFloat(),
                initialY = Random.nextFloat(),
                radius = Random.nextFloat() * 3.5f + 1.5f,
                speed = Random.nextFloat() * 0.5f + 0.2f,
                isRed = Random.nextBoolean(),
                alpha = Random.nextFloat() * 0.45f + 0.15f
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "dust")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustProgress"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        for (p in particles) {
            val currentY = (p.initialY - progress * p.speed).let { if (it < 0f) it + 1f else it } * height
            val currentX = p.initialX * width
            val particleColor = if (p.isRed) Color(0xFFFF1744) else Color(0xFFA855F7)

            drawCircle(
                color = particleColor.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(currentX, currentY)
            )
        }
    }
}
