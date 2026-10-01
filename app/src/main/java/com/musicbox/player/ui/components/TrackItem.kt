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
    val borderColor = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else Color(0xFFFFD1DC)
    val bgColor = if (isSelected) Color(0xFFFFE0EB) else Color.White

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
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
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        FilledIconButton(
            onClick = { if (isSelectionMode) onSelectToggle() else onClick() },
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isCurrentPlaying) Color.White else MaterialTheme.colorScheme.primary
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
                fontWeight = if (isCurrentPlaying) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${track.artist} • ${formatDuration(track.durationMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        TrackImage(bitmap = track.artworkBitmap, size = 44.dp, cornerRadius = 10.dp)

        if (!isSelectionMode) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}
