package com.musicbox.player.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.musicbox.player.model.Track

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackItem(
    track: Track,
    isCurrentPlaying: Boolean,
    isPlaying: Boolean,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onSelectToggle: () -> Unit,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val borderColor = if (isCurrentPlaying) Color(0xFFFF5B84) else Color.Black
    val borderWidth = if (isCurrentPlaying) 2.5.dp else 1.5.dp
    val bgColor = if (isSelected) Color(0xFFFFD9E4) else Color(0xFFFFF7F9)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(16.dp))
            .background(bgColor)
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelectToggle() else onClick() },
                onLongClick = onSelectToggle
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isSelectionMode) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onSelectToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFFFF5B84)),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        FilledIconButton(
            onClick = { if (isSelectionMode) onSelectToggle() else onClick() },
            modifier = Modifier
                .size(44.dp)
                .border(2.dp, Color.Black, RoundedCornerShape(14.dp)),
            shape = RoundedCornerShape(14.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isCurrentPlaying) Color(0xFFFF5B84) else Color(0xFFFFB6C1),
                contentColor = Color.White
            )
        ) {
            Icon(
                if (isCurrentPlaying && isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play/Pause",
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isCurrentPlaying) FontWeight.ExtraBold else FontWeight.Bold,
                color = if (isCurrentPlaying) Color(0xFFFF3366) else Color(0xFF2C1620),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${track.artist} • ${formatDuration(track.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8C5368),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TrackImage(
            bitmap = track.artworkBitmap,
            trackId = track.id,
            size = 44.dp,
            cornerRadius = 10.dp
        )

        if (!isSelectionMode) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFF9E6578)
                )
            }
        }
    }
}
