package com.musicbox.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.musicbox.player.model.Track

@Composable
fun MiniPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatMode: Int,
    isShuffle: Boolean,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onClick: () -> Unit
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }

    val safeDuration = durationMs.coerceAtLeast(1L)
    val sliderValue = if (isDraggingSlider) {
        dragProgress
    } else {
        (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, Color(0xFFFF1744), RoundedCornerShape(16.dp)),
        color = Color(0xF012071F),
        tonalElevation = 12.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${if (isPlaying) "❤️ " else "💜 "}${track.title}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "${track.artist}  [ HP 92/92 ]",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFFFA000), // HP стиль желтого цвета
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                TrackImage(bitmap = track.artworkBitmap, size = 46.dp, cornerRadius = 8.dp)
            }

            Slider(
                value = sliderValue,
                onValueChange = {
                    isDraggingSlider = true
                    dragProgress = it
                },
                onValueChangeFinished = {
                    isDraggingSlider = false
                    onSeekTo((dragProgress * safeDuration).toLong())
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFF1744),
                    activeTrackColor = Color(0xFFFFEB3B), // Undertale HP Bar Yellow
                    inactiveTrackColor = Color(0xFFC62828) // Undertale HP Bar Empty Red
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    formatDuration(if (isDraggingSlider) (dragProgress * safeDuration).toLong() else currentPositionMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCycleRepeat) {
                        val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                        val tint = if (repeatMode != Player.REPEAT_MODE_OFF) Color(0xFFFF1744) else Color.White.copy(alpha = 0.35f)
                        Icon(icon, contentDescription = "Repeat", tint = tint, modifier = Modifier.size(24.dp))
                    }

                    IconButton(onClick = onToggleShuffle) {
                        val tint = if (isShuffle) Color(0xFFA855F7) else Color.White.copy(alpha = 0.35f)
                        Icon(Icons.Default.Shuffle, contentDescription = "Shuffle", tint = tint, modifier = Modifier.size(24.dp))
                    }

                    IconButton(onClick = onPrevClick) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    IconButton(onClick = onPlayPauseClick) {
                        Icon(
                            if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color(0xFFFF1744),
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    IconButton(onClick = onNextClick) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }

                Text(
                    formatDuration(durationMs),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
