package com.musicbox.player.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMenuBottomSheet(
    repeatMode: Int,
    isShuffle: Boolean,
    onRepeatOneClick: () -> Unit,
    onRepeatAllClick: () -> Unit,
    onShuffleClick: () -> Unit,
    onReverseOrderClick: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(bottom = 32.dp)) {
            MenuItemRow(
                icon = Icons.Default.RepeatOne,
                title = "Зациклить трек",
                isActive = repeatMode == Player.REPEAT_MODE_ONE,
                onClick = onRepeatOneClick
            )
            MenuItemRow(
                icon = Icons.Default.Repeat,
                title = "Зациклить список",
                isActive = repeatMode == Player.REPEAT_MODE_ALL,
                onClick = onRepeatAllClick
            )
            MenuItemRow(
                icon = Icons.Default.Shuffle,
                title = "Случайный порядок",
                isActive = isShuffle,
                onClick = onShuffleClick
            )
            MenuItemRow(
                icon = Icons.Default.SwapVert,
                title = "Обратный порядок",
                isActive = false,
                onClick = {
                    onReverseOrderClick()
                    onDismissRequest()
                }
            )
        }
    }
}

@Composable
private fun MenuItemRow(icon: ImageVector, title: String, isActive: Boolean, onClick: () -> Unit) {
    val tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = tint)
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = tint)
    }
}
