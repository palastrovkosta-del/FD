package com.musicbox.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.musicbox.player.ui.components.FullScreenPlayer
import com.musicbox.player.ui.components.MiniPlayer
import com.musicbox.player.ui.components.PlayerMenuBottomSheet
import com.musicbox.player.ui.components.TrackItem
import com.musicbox.player.ui.viewmodel.MusicViewModel

@Composable
fun MainScreen(viewModel: MusicViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.importFiles(uris)
    }

    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.clearSelection()
    }

    Scaffold(
        topBar = {
            if (uiState.isSelectionMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Cancel")
                    }
                    Text(
                        "Выбрано: ${uiState.selectedTrackIds.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                    IconButton(onClick = { viewModel.selectAll() }) {
                        Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                    }
                    IconButton(onClick = { viewModel.shareSelectedTracks(context) }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                    IconButton(onClick = { viewModel.requestBatchDeleteConfirmation() }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete Selected",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        filePickerLauncher.launch(
                            arrayOf(
                                "audio/mpeg", "audio/flac", "audio/mp4", "audio/x-m4a",
                                "audio/wav", "audio/ogg", "audio/aac", "audio/*"
                            )
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Track")
                }
            }
        },
        bottomBar = {
            if (uiState.currentTrack != null) {
                MiniPlayer(
                    track = uiState.currentTrack!!,
                    isPlaying = uiState.isPlaying,
                    currentPositionMs = uiState.currentPosition,
                    durationMs = uiState.duration,
                    repeatMode = uiState.repeatMode,
                    isShuffle = uiState.isShuffle,
                    onPlayPauseClick = { viewModel.togglePlayPause() },
                    onPrevClick = { viewModel.prevTrack() },
                    onNextClick = { viewModel.nextTrack() },
                    onMenuClick = { viewModel.setMenuBottomSheetVisible(true) },
                    onSeekTo = { viewModel.seekTo(it) },
                    onClick = { viewModel.setFullScreenPlayerVisible(true) }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!uiState.isSelectionMode) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    placeholder = { Text("Поиск трека или исполнителя...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp)
                )
            }

            if (uiState.filteredTracks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.padding(bottom = 16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (uiState.tracks.isEmpty())
                                "Нет треков.\nНажмите +, чтобы добавить аудиофайлы"
                            else "Ничего не найдено",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.filteredTracks, key = { it.id }) { track ->
                        val isSelected = uiState.selectedTrackIds.contains(track.id)
                        TrackItem(
                            track = track,
                            isCurrentPlaying = track.id == uiState.currentTrack?.id,
                            isPlaying = uiState.isPlaying,
                            isSelectionMode = uiState.isSelectionMode,
                            isSelected = isSelected,
                            onSelectToggle = { viewModel.toggleSelectTrack(track.id) },
                            onClick = { viewModel.playTrack(track) },
                            onDeleteClick = { viewModel.requestDeleteConfirmation(track) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(88.dp))
                    }
                }
            }
        }
    }

    // Диалог подтверждения удаления 1 трека
    uiState.trackPendingDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Удалить трек?") },
            text = { Text("Файл «${track.title}» будет безвозвратно удален.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteTrack() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Диалог массового удаления
    if (uiState.isBatchDeleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBatchDeleteDialog() },
            title = { Text("Удалить выбранные треки?") },
            text = { Text("Будет безвозвратно удалено треков: ${uiState.selectedTrackIds.size}.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmBatchDelete() }) {
                    Text("Удалить все", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBatchDeleteDialog() }) {
                    Text("Отмена")
                }
            }
        )
    }

    // Telegram-меню (Зациклить трек, зациклить список, случайный порядок)
    if (uiState.isMenuBottomSheetVisible) {
        PlayerMenuBottomSheet(
            repeatMode = uiState.repeatMode,
            isShuffle = uiState.isShuffle,
            onRepeatOneClick = { viewModel.toggleRepeatOne() },
            onRepeatAllClick = { viewModel.toggleRepeatAll() },
            onShuffleClick = { viewModel.toggleShuffle() },
            onReverseOrderClick = { viewModel.reverseQueue() },
            onDismissRequest = { viewModel.setMenuBottomSheetVisible(false) }
        )
    }

    // Полноэкранный плеер
    AnimatedVisibility(
        visible = uiState.isFullScreenPlayerVisible && uiState.currentTrack != null,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        uiState.currentTrack?.let { track ->
            FullScreenPlayer(
                track = track,
                isPlaying = uiState.isPlaying,
                currentPositionMs = uiState.currentPosition,
                durationMs = uiState.duration,
                onBackClick = { viewModel.setFullScreenPlayerVisible(false) },
                onMenuClick = { viewModel.setMenuBottomSheetVisible(true) },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.nextTrack() },
                onPrevClick = { viewModel.prevTrack() },
                onSeekTo = { viewModel.seekTo(it) }
            )
        }
    }
}
