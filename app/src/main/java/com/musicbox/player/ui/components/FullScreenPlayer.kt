package com.musicbox.player.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import com.musicbox.player.model.Track

@Composable
fun FullScreenPlayer(
    track: Track,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    repeatMode: Int,
    isShuffle: Boolean,
    onCycleRepeat: () -> Unit,
    onToggleShuffle: () -> Unit,
    onBackClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onPrevClick: () -> Unit,
    onSeekTo: (Long) -> Unit
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var sliderDragValue by remember { mutableFloatStateOf(0f) }
    val safeDuration = durationMs.coerceAtLeast(1L)
    val sliderValue = if (isDraggingSlider) sliderDragValue
    else (currentPositionMs.toFloat() / safeDuration).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .systemBarsPadding()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад", tint = Color.White)
            }
            Text(
                "Сейчас играет",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.size(48.dp)) // Пустота вместо 3 точек
        }

        Spacer(modifier = Modifier.weight(0.5f))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            TrackImage(bitmap = track.artworkBitmap, size = maxWidth * 0.75f, cornerRadius = 24.dp)
        }

        Spacer(modifier = Modifier.weight(0.5f))

        Text(
            track.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            track.artist,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Slider(
            value = sliderValue,
            onValueChange = {
                isDraggingSlider = true
                sliderDragValue = it
            },
            onValueChangeFinished = {
                isDraggingSlider = false
                onSeekTo((sliderDragValue * safeDuration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val displayedCurrentTime = if (isDraggingSlider) (sliderDragValue * safeDuration).toLong() else currentPositionMs
            Text(formatDuration(displayedCurrentTime), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(formatDuration(durationMs), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопки управления (Перемешать, Назад, Пауза, Вперед, Зациклить)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                val tint = if (isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                Icon(Icons.Default.Shuffle, contentDescription = "Перемешать", tint = tint, modifier = Modifier.size(28.dp))
            }

            IconButton(onClick = onPrevClick, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Назад", tint = Color.White, modifier = Modifier.size(34.dp))
            }

            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(72.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Старт/Пауза",
                    modifier = Modifier.size(42.dp)
                )
            }

            IconButton(onClick = onNextClick, modifier = Modifier.size(52.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Вперед", tint = Color.White, modifier = Modifier.size(34.dp))
            }

            IconButton(onClick = onCycleRepeat) {
                val icon = if (repeatMode == Player.REPEAT_MODE_ONE) Icons.Default.RepeatOne else Icons.Default.Repeat
                val tint = if (repeatMode != Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                Icon(icon, contentDescription = "Зациклить", tint = tint, modifier = Modifier.size(28.dp))
            }
        }

        Spacer(modifier = Modifier.weight(0.5f))
    }
}
