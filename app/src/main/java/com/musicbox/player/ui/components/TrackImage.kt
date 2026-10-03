package com.musicbox.player.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Быстрый кэш в оперативной памяти для устранения фризов
private val bitmapCache = LruCache<String, Bitmap>(50)

@Composable
fun TrackImage(
    bitmap: Bitmap?,
    trackId: String? = null,
    size: Dp,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val context = LocalContext.current
    val cachedBitmap = remember(bitmap, trackId) {
        if (bitmap != null) return@remember bitmap
        if (trackId != null) {
            val fromCache = bitmapCache.get(trackId)
            if (fromCache != null) return@remember fromCache
        }
        try {
            val resId = context.resources.getIdentifier("app_icon", "drawable", context.packageName)
            if (resId != 0) {
                val decoded = BitmapFactory.decodeResource(context.resources, resId)
                if (trackId != null && decoded != null) bitmapCache.put(trackId, decoded)
                decoded
            } else null
        } catch (e: Exception) { null }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .border(1.5.dp, Color.Black, RoundedCornerShape(cornerRadius))
            .background(Color(0xFFFFD1DC)),
        contentAlignment = Alignment.Center
    ) {
        if (cachedBitmap != null) {
            Image(
                bitmap = cachedBitmap.asImageBitmap(),
                contentDescription = "Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Default.MusicNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size / 2)
            )
        }
    }
}

fun formatDuration(durationMs: Long): String {
    val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
    return String.format("%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
