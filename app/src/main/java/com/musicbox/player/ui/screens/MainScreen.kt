package com.musicbox.player.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.musicbox.player.ui.components.FullScreenPlayer
import com.musicbox.player.ui.components.MiniPlayer
import com.musicbox.player.ui.components.ParticleBackground
import com.musicbox.player.ui.components.TrackItem
import com.musicbox.player.ui.viewmodel.MusicViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    viewModel: MusicViewModel,
    shouldShowSplash: Boolean,
    onSplashDone: () -> Unit,
    onMinimize: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var isSplashVisible by remember { mutableStateOf(shouldShowSplash) }

    var newGroupName by remember { mutableStateOf("") }
    var groupToDelete by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.importFiles(uris)
    }

    BackHandler {
        if (uiState.isSelectionMode) {
            viewModel.clearSelection()
        } else if (uiState.isFullScreenPlayerVisible) {
            viewModel.setFullScreenPlayerVisible(false)
        } else {
            onMinimize()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ParticleBackground()

        Scaffold(
            containerColor = Color.Transparent,
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Отмена", tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            "Выбрано: ${uiState.selectedTrackIds.size}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        )
                        IconButton(onClick = { viewModel.selectAll() }) {
                            Icon(Icons.Default.DoneAll, contentDescription = "Выбрать все", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.setAddToGroupDialogVisible(true) }) {
                            Icon(Icons.Default.Folder, contentDescription = "В группу", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.shareSelectedTracks(context) }) {
                            Icon(Icons.Default.Share, contentDescription = "Поделиться", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.requestBatchDeleteConfirmation() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = MaterialTheme.colorScheme.error)
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
                                    "audio/*", "application/ogg", "audio/mpeg", "audio/flac", "audio/mp4", "audio/x-m4a"
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить музыку")
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
                        onCycleRepeat = { viewModel.cycleRepeatMode() },
                        onToggleShuffle = { viewModel.toggleShuffle() },
                        onPlayPauseClick = { viewModel.togglePlayPause() },
                        onPrevClick = { viewModel.prevTrack() },
                        onNextClick = { viewModel.nextTrack() },
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
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        placeholder = { Text("Поиск трека...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (uiState.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Очистить", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAllSelected = uiState.selectedGroup == null
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isAllSelected) MaterialTheme.colorScheme.primary else Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .combinedClickable(onClick = { viewModel.selectGroup(null) })
                        ) {
                            Text(
                                "ALL",
                                color = if (isAllSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                .combinedClickable(onClick = { viewModel.setCreateGroupDialogVisible(true) })
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CreateNewFolder, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.height(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ Группа", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        uiState.groups.keys.forEach { groupName ->
                            val isGroupSelected = uiState.selectedGroup == groupName
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isGroupSelected) MaterialTheme.colorScheme.primary else Color.White,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { viewModel.selectGroup(groupName) },
                                        onLongClick = { groupToDelete = groupName }
                                    )
                            ) {
                                Text(
                                    groupName,
                                    color = if (isGroupSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
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
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            )
                            Text(
                                text = if (uiState.tracks.isEmpty())
                                    "Нет треков\nНажмите +, чтобы добавить аудиофайлы"
                                else "В этой группе пока нет треков",
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
                                onClick = { viewModel.onTrackItemClick(track) },
                                onDeleteClick = { viewModel.requestDeleteConfirmation(track) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }
        }

        // Индикатор добавления больших папок (4 ГБ)
        if (uiState.isImporting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Добавляем песни... Пожалуйста, подождите", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }

        // Вступительный ролик (при холодном старте)
        if (isSplashVisible) {
            SplashScreen(
                isVisible = isSplashVisible,
                onSplashFinished = {
                    isSplashVisible = false
                    onSplashDone()
                }
            )
        }
    }

    // Диалог кнопки "Class" (выбор группы для текущего трека)
    if (uiState.isClassDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.setClassDialogVisible(false) },
            title = { Text("Перенести трек в класс/группу", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
            text = {
                if (uiState.groups.isEmpty()) {
                    Text("Сначала создайте хотя бы одну группу (например, DDLC) через «+ Группа»!")
                } else {
                    Column {
                        uiState.groups.keys.forEach { g ->
                            TextButton(
                                onClick = { viewModel.moveCurrentTrackToGroup(g) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("🌸 $g", textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth(), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setClassDialogVisible(false) }) { Text("Закрыть") }
            }
        )
    }

    if (uiState.isCreateGroupDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.setCreateGroupDialogVisible(false) },
            title = { Text("Новая группа", color = MaterialTheme.colorScheme.primary) },
            text = {
                OutlinedTextField(
                    value = newGroupName,
                    onValueChange = { newGroupName = it },
                    placeholder = { Text("Название (например, DDLC)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.createGroup(newGroupName)
                    newGroupName = ""
                }) { Text("Создать") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.setCreateGroupDialogVisible(false) }) { Text("Отмена") }
            }
        )
    }

    groupToDelete?.let { gName ->
        AlertDialog(
            onDismissRequest = { groupToDelete = null },
            title = { Text("Удалить группу «$gName»?") },
            text = { Text("Песни останутся в общем списке ALL.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGroup(gName)
                    groupToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { groupToDelete = null }) { Text("Отмена") }
            }
        )
    }

    if (uiState.isAddToGroupDialogVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.setAddToGroupDialogVisible(false) },
            title = { Text("Добавить в группу", color = MaterialTheme.colorScheme.primary) },
            text = {
                if (uiState.groups.isEmpty()) {
                    Text("Сначала создайте хотя бы одну группу через «+ Группа»")
                } else {
                    Column {
                        uiState.groups.keys.forEach { g ->
                            TextButton(
                                onClick = { viewModel.addSelectedTracksToGroup(g) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(g, textAlign = TextAlign.Start, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { viewModel.setAddToGroupDialogVisible(false) }) { Text("Закрыть") }
            }
        )
    }

    uiState.trackPendingDelete?.let { track ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Удалить трек?") },
            text = { Text("Файл «${track.title}» будет удален с устройства.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDeleteTrack() }) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) { Text("Отмена") }
            }
        )
    }

    if (uiState.isBatchDeleteConfirmVisible) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissBatchDeleteDialog() },
            title = { Text("Удалить выбранные треки?") },
            text = { Text("Будет удалено треков: ${uiState.selectedTrackIds.size}.") },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmBatchDelete() }) {
                    Text("Удалить все", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBatchDeleteDialog() }) { Text("Отмена") }
            }
        )
    }

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
                repeatMode = uiState.repeatMode,
                isShuffle = uiState.isShuffle,
                onCycleRepeat = { viewModel.cycleRepeatMode() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onBackClick = { viewModel.setFullScreenPlayerVisible(false) },
                onPlayPauseClick = { viewModel.togglePlayPause() },
                onNextClick = { viewModel.nextTrack() },
                onPrevClick = { viewModel.prevTrack() },
                onSeekTo = { viewModel.seekTo(it) },
                onClassClick = { viewModel.setClassDialogVisible(true) }
            )
        }
    }
}
