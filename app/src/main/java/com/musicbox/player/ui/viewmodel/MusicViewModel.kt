package com.musicbox.player.ui.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.musicbox.player.data.MusicRepository
import com.musicbox.player.model.Track
import com.musicbox.player.service.PlaybackService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

data class PlayerUiState(
    val tracks: List<Track> = emptyList(),
    val filteredTracks: List<Track> = emptyList(),
    val queue: List<Track> = emptyList(),
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffle: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val searchQuery: String = "",
    val isFullScreenPlayerVisible: Boolean = false,
    val trackPendingDelete: Track? = null,
    val isSelectionMode: Boolean = false,
    val selectedTrackIds: Set<String> = emptySet(),
    val isBatchDeleteConfirmVisible: Boolean = false,
    val groups: Map<String, Set<String>> = emptyMap(),
    val selectedGroup: String? = null,
    val isCreateGroupDialogVisible: Boolean = false,
    val isAddToGroupDialogVisible: Boolean = false
)

class MusicViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MusicRepository(application)
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var progressJob: Job? = null

    init {
        initMediaController()
        loadTracks()
        loadGroups()
    }

    private fun initMediaController() {
        val sessionToken = SessionToken(
            getApplication(),
            ComponentName(getApplication(), PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(getApplication(), sessionToken).buildAsync()
        controllerFuture?.addListener({
            mediaController = controllerFuture?.get()
            setupPlayerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _uiState.update { it.copy(isPlaying = isPlaying) }
                if (isPlaying) startProgressUpdate() else stopProgressUpdate()
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO &&
                    _uiState.value.repeatMode == Player.REPEAT_MODE_OFF) {
                    mediaController?.pause()
                    return
                }
                updateCurrentTrackFromPlayer()
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                _uiState.update { it.copy(repeatMode = repeatMode) }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                _uiState.update { it.copy(isShuffle = shuffleModeEnabled) }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY) {
                    _uiState.update {
                        it.copy(duration = mediaController?.duration?.coerceAtLeast(0L) ?: 0L)
                    }
                } else if (playbackState == Player.STATE_ENDED) {
                    _uiState.update { it.copy(isPlaying = false) }
                }
            }
        })
        updateCurrentTrackFromPlayer()
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (true) {
                mediaController?.let { controller ->
                    _uiState.update {
                        it.copy(
                            currentPosition = controller.currentPosition.coerceAtLeast(0L),
                            duration = controller.duration.coerceAtLeast(0L)
                        )
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdate() { progressJob?.cancel() }

    fun loadTracks() {
        viewModelScope.launch {
            val list = repository.getTracks()
            _uiState.update {
                it.copy(
                    tracks = list,
                    filteredTracks = filterList(list, it.searchQuery, it.selectedGroup, it.groups)
                )
            }
        }
    }

    private fun loadGroups() {
        viewModelScope.launch {
            val groups = repository.loadGroups()
            _uiState.update {
                it.copy(
                    groups = groups,
                    filteredTracks = filterList(it.tracks, it.searchQuery, it.selectedGroup, groups)
                )
            }
        }
    }

    fun selectGroup(groupName: String?) {
        _uiState.update {
            it.copy(
                selectedGroup = groupName,
                filteredTracks = filterList(it.tracks, it.searchQuery, groupName, it.groups)
            )
        }
    }

    fun createGroup(name: String) {
        if (name.isBlank()) return
        val updated = _uiState.value.groups.toMutableMap()
        if (!updated.containsKey(name)) {
            updated[name] = emptySet()
            _uiState.update { it.copy(groups = updated, isCreateGroupDialogVisible = false) }
            viewModelScope.launch { repository.saveGroups(updated) }
        }
    }

    fun deleteGroup(name: String) {
        val updated = _uiState.value.groups.toMutableMap()
        updated.remove(name)
        val newSelected = if (_uiState.value.selectedGroup == name) null else _uiState.value.selectedGroup
        _uiState.update {
            it.copy(
                groups = updated,
                selectedGroup = newSelected,
                filteredTracks = filterList(it.tracks, it.searchQuery, newSelected, updated)
            )
        }
        viewModelScope.launch { repository.saveGroups(updated) }
    }

    fun addSelectedTracksToGroup(groupName: String) {
        val updated = _uiState.value.groups.toMutableMap()
        val currentTracks = updated[groupName]?.toMutableSet() ?: mutableSetOf()
        currentTracks.addAll(_uiState.value.selectedTrackIds)
        updated[groupName] = currentTracks
        _uiState.update {
            it.copy(
                groups = updated,
                selectedTrackIds = emptySet(),
                isSelectionMode = false,
                isAddToGroupDialogVisible = false,
                filteredTracks = filterList(it.tracks, it.searchQuery, it.selectedGroup, updated)
            )
        }
        viewModelScope.launch { repository.saveGroups(updated) }
    }

    fun setCreateGroupDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isCreateGroupDialogVisible = visible) }
    }

    fun setAddToGroupDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(isAddToGroupDialogVisible = visible) }
    }

    fun importFiles(uris: List<Uri>) {
        val targetGroup = _uiState.value.selectedGroup
        viewModelScope.launch {
            val newTrackIds = repository.importFiles(uris)
            if (targetGroup != null && newTrackIds.isNotEmpty()) {
                val updated = _uiState.value.groups.toMutableMap()
                val currentTracks = updated[targetGroup]?.toMutableSet() ?: mutableSetOf()
                currentTracks.addAll(newTrackIds)
                updated[targetGroup] = currentTracks
                repository.saveGroups(updated)
                _uiState.update { it.copy(groups = updated) }
            }
            loadTracks()
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                searchQuery = query,
                filteredTracks = filterList(it.tracks, query, it.selectedGroup, it.groups)
            )
        }
    }

    private fun filterList(
        list: List<Track>,
        query: String,
        group: String?,
        groups: Map<String, Set<String>>
    ): List<Track> {
        val groupFiltered = if (group == null) {
            list
        } else {
            val idsInGroup = groups[group] ?: emptySet()
            list.filter { idsInGroup.contains(it.id) }
        }

        if (query.isBlank()) return groupFiltered
        return groupFiltered.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
    }

    fun onTrackItemClick(track: Track) {
        if (_uiState.value.currentTrack?.id == track.id) {
            togglePlayPause()
        } else {
            // Играет ТОЛЬКО список текущей выбранной группы!
            playTrack(track, _uiState.value.filteredTracks)
        }
    }

    // Подготовка крупного Санса (с обрезкой полей) для экрана блокировки
    private fun getArtworkUri(track: Track): Uri? {
        val cacheFile = File(getApplication<Application>().cacheDir, "lock_art_${track.id.hashCode()}.jpg")
        if (!cacheFile.exists()) {
            try {
                val bytes = track.artworkBytes ?: run {
                    val resId = getApplication<Application>().resources.getIdentifier(
                        "app_icon", "drawable", getApplication<Application>().packageName
                    )
                    if (resId != 0) {
                        val original = BitmapFactory.decodeResource(getApplication<Application>().resources, resId)
                        // Кропаем и центрируем картинку, чтобы Санс был крупным!
                        val cropSize = (original.width * 0.78f).toInt()
                        val startX = (original.width - cropSize) / 2
                        val startY = (original.height - cropSize) / 2
                        val cropped = Bitmap.createBitmap(original, startX, startY, cropSize, cropSize)

                        val stream = ByteArrayOutputStream()
                        cropped.compress(Bitmap.CompressFormat.JPEG, 92, stream)
                        stream.toByteArray()
                    } else null
                }
                if (bytes != null) {
                    FileOutputStream(cacheFile).use { it.write(bytes) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return if (cacheFile.exists()) Uri.fromFile(cacheFile) else null
    }

    private fun getArtworkBytes(track: Track): ByteArray? {
        return track.artworkBytes ?: run {
            try {
                val resId = getApplication<Application>().resources.getIdentifier(
                    "app_icon", "drawable", getApplication<Application>().packageName
                )
                if (resId != 0) {
                    val original = BitmapFactory.decodeResource(getApplication<Application>().resources, resId)
                    val cropSize = (original.width * 0.78f).toInt()
                    val startX = (original.width - cropSize) / 2
                    val startY = (original.height - cropSize) / 2
                    val cropped = Bitmap.createBitmap(original, startX, startY, cropSize, cropSize)

                    val stream = ByteArrayOutputStream()
                    cropped.compress(Bitmap.CompressFormat.JPEG, 92, stream)
                    stream.toByteArray()
                } else null
            } catch (e: Exception) { null }
        }
    }

    fun playTrack(track: Track, tracksQueue: List<Track> = _uiState.value.filteredTracks) {
        val controller = mediaController ?: return
        val startIndex = tracksQueue.indexOf(track).coerceAtLeast(0)
        val mediaItems = tracksQueue.map { t ->
            val artUri = getArtworkUri(t)
            val artBytes = getArtworkBytes(t)
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(t.title)
                .setArtist(t.artist)
                .setAlbumTitle(t.album)

            if (artUri != null) {
                metadataBuilder.setArtworkUri(artUri)
            }
            if (artBytes != null) {
                metadataBuilder.setArtworkData(artBytes, MediaMetadata.PICTURE_TYPE_FRONT_COVER)
            }

            MediaItem.Builder()
                .setMediaId(t.id)
                .setUri(Uri.fromFile(File(t.filePath)))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
        // Загружаем в ExoPlayer ТОЛЬКО треки текущей группы!
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        _uiState.update {
            it.copy(queue = tracksQueue, currentTrack = track)
        }
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) controller.pause() else controller.play()
    }

    fun nextTrack() { mediaController?.seekToNextMediaItem() }
    fun prevTrack() { mediaController?.seekToPreviousMediaItem() }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
        _uiState.update { it.copy(currentPosition = positionMs) }
    }

    fun setFullScreenPlayerVisible(visible: Boolean) {
        _uiState.update { it.copy(isFullScreenPlayerVisible = visible) }
    }

    fun cycleRepeatMode() {
        val controller = mediaController ?: return
        val nextMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        controller.repeatMode = nextMode
    }

    fun toggleShuffle() {
        val controller = mediaController ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun requestDeleteConfirmation(track: Track) {
        _uiState.update { it.copy(trackPendingDelete = track) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(trackPendingDelete = null) }
    }

    fun confirmDeleteTrack() {
        val trackToDelete = _uiState.value.trackPendingDelete ?: return
        val isCurrentlyPlaying = _uiState.value.currentTrack?.id == trackToDelete.id

        if (isCurrentlyPlaying) {
            mediaController?.stop()
            mediaController?.clearMediaItems()
            _uiState.update { it.copy(currentTrack = null, isPlaying = false, isFullScreenPlayerVisible = false) }
        }

        viewModelScope.launch {
            repository.deleteTrack(trackToDelete)
            val updatedTracks = _uiState.value.tracks.filter { it.id != trackToDelete.id }
            _uiState.update {
                it.copy(
                    tracks = updatedTracks,
                    filteredTracks = filterList(updatedTracks, it.searchQuery, it.selectedGroup, it.groups),
                    queue = it.queue.filter { q -> q.id != trackToDelete.id },
                    trackPendingDelete = null
                )
            }
        }
    }

    fun toggleSelectTrack(trackId: String) {
        val currentSelected = _uiState.value.selectedTrackIds.toMutableSet()
        if (currentSelected.contains(trackId)) currentSelected.remove(trackId) else currentSelected.add(trackId)
        _uiState.update {
            it.copy(selectedTrackIds = currentSelected, isSelectionMode = currentSelected.isNotEmpty())
        }
    }

    fun selectAll() {
        val allIds = _uiState.value.filteredTracks.map { it.id }.toSet()
        _uiState.update { it.copy(selectedTrackIds = allIds, isSelectionMode = true) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedTrackIds = emptySet(), isSelectionMode = false) }
    }

    fun requestBatchDeleteConfirmation() {
        _uiState.update { it.copy(isBatchDeleteConfirmVisible = true) }
    }

    fun dismissBatchDeleteDialog() {
        _uiState.update { it.copy(isBatchDeleteConfirmVisible = false) }
    }

    fun confirmBatchDelete() {
        val idsToDelete = _uiState.value.selectedTrackIds
        val currentTrackId = _uiState.value.currentTrack?.id

        if (currentTrackId != null && idsToDelete.contains(currentTrackId)) {
            mediaController?.stop()
            mediaController?.clearMediaItems()
            _uiState.update { it.copy(currentTrack = null, isPlaying = false, isFullScreenPlayerVisible = false) }
        }

        viewModelScope.launch {
            val tracksToDelete = _uiState.value.tracks.filter { idsToDelete.contains(it.id) }
            for (track in tracksToDelete) repository.deleteTrack(track)
            val remainingTracks = _uiState.value.tracks.filter { !idsToDelete.contains(it.id) }
            _uiState.update {
                it.copy(
                    tracks = remainingTracks,
                    filteredTracks = filterList(remainingTracks, it.searchQuery, it.selectedGroup, it.groups),
                    queue = it.queue.filter { q -> !idsToDelete.contains(q.id) },
                    selectedTrackIds = emptySet(),
                    isSelectionMode = false,
                    isBatchDeleteConfirmVisible = false
                )
            }
        }
    }

    fun shareSelectedTracks(context: Context) {
        val ids = _uiState.value.selectedTrackIds
        val selectedFiles = _uiState.value.tracks.filter { ids.contains(it.id) }
        if (selectedFiles.isEmpty()) return

        val uris = ArrayList<Uri>()
        for (track in selectedFiles) {
            val file = File(track.filePath)
            if (file.exists()) {
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                uris.add(uri)
            }
        }

        if (uris.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "audio/*"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Поделиться треками"))
        }
    }

    private fun updateCurrentTrackFromPlayer() {
        val controller = mediaController ?: return
        val currentMediaId = controller.currentMediaItem?.mediaId ?: return
        val track = _uiState.value.tracks.find { it.id == currentMediaId }
            ?: _uiState.value.queue.find { it.id == currentMediaId }
        track?.let { current ->
            _uiState.update {
                it.copy(
                    currentTrack = current,
                    duration = controller.duration.coerceAtLeast(0L),
                    currentPosition = controller.currentPosition.coerceAtLeast(0L)
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdate()
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
