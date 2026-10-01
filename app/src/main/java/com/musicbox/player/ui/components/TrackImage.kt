package com.musicbox.player.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TrackImage(
    bitmap: Bitmap?,
    size: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val context = LocalContext.current
    val fallbackBitmap = remember {
        try {
            val resId = context.resources.getIdentifier("app_icon", "drawable", context.packageName)
            if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
        } catch (e: Exception) { null }
    }

    val displayBitmap = bitmap ?: fallbackBitmap

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color(0xFF1B0F2B)),
        contentAlignment = Alignment.Center
    ) {
        if (displayBitmap != null) {
            Image(
                bitmap = displayBitmap.asImageBitmap(),
                contentDescription = "Track Artwork",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(1.35f) // Масштабируем, убирая лишние поля вокруг Санса!
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
