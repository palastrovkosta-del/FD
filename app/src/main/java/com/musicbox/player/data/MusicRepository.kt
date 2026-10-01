package com.musicbox.player.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.musicbox.player.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class MusicRepository(private val context: Context) {
    private val supportedExtensions = setOf("mp3", "flac", "m4a", "wav", "ogg", "aac")
    val musicDir: File
        get() = File(context.filesDir, "music").apply { if (!exists()) mkdirs() }

    private val groupsFile: File
        get() = File(context.filesDir, "groups.json")

    suspend fun getTracks(): List<Track> = withContext(Dispatchers.IO) {
        val files = musicDir.listFiles { file ->
            file.isFile && supportedExtensions.contains(file.extension.lowercase())
        } ?: emptyArray()
        files.mapNotNull { extractTrackMetadata(it) }.sortedBy { it.title.lowercase() }
    }

    suspend fun importFiles(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val newTrackIds = mutableListOf<String>()
        for (uri in uris) {
            val fileName = queryFileName(uri) ?: "track_${UUID.randomUUID()}.mp3"
            val extension = fileName.substringAfterLast(".", "").lowercase()
            if (extension in supportedExtensions) {
                val destFile = getUniqueFile(musicDir, fileName)
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(destFile).use { output -> input.copyTo(output) }
                }
                newTrackIds.add(destFile.absolutePath)
            }
        }
        newTrackIds
    }

    suspend fun deleteTrack(track: Track): Boolean = withContext(Dispatchers.IO) {
        val file = File(track.filePath)
        if (file.exists()) file.delete() else false
    }

    suspend fun loadGroups(): Map<String, Set<String>> = withContext(Dispatchers.IO) {
        if (!groupsFile.exists()) return@withContext emptyMap()
        try {
            val json = JSONObject(groupsFile.readText())
            val result = mutableMapOf<String, Set<String>>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val array = json.getJSONArray(key)
                val set = mutableSetOf<String>()
                for (i in 0 until array.length()) {
                    set.add(array.getString(i))
                }
                result[key] = set
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    suspend fun saveGroups(groups: Map<String, Set<String>>) = withContext(Dispatchers.IO) {
        try {
            val json = JSONObject()
            groups.forEach { (name, tracks) ->
                json.put(name, org.json.JSONArray(tracks))
            }
            groupsFile.writeText(json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun extractTrackMetadata(file: File): Track? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val titleTag = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artistTag = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val albumTag = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 0L
            val rawArtwork = retriever.embeddedPicture
            val safeArtwork = rawArtwork?.let { downscaleArtwork(it) }

            Track(
                id = file.absolutePath,
                filePath = file.absolutePath,
                fileName = file.name,
                title = titleTag?.takeIf { it.isNotBlank() } ?: file.nameWithoutExtension,
                artist = artistTag?.takeIf { it.isNotBlank() } ?: "Неизвестный исполнитель",
                album = albumTag?.takeIf { it.isNotBlank() } ?: "Неизвестный альбом",
                durationMs = durationMs,
                artworkBytes = safeArtwork
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    private fun downscaleArtwork(bytes: ByteArray): ByteArray {
        return try {
            val original = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return bytes
            val maxSize = 400
            val width = original.width
            val height = original.height
            if (width <= maxSize && height <= maxSize) return bytes
            val ratio = width.toFloat() / height.toFloat()
            val finalWidth = if (ratio > 1f) maxSize else (maxSize * ratio).toInt()
            val finalHeight = if (ratio > 1f) (maxSize / ratio).toInt() else maxSize
            val scaled = Bitmap.createScaledBitmap(original, finalWidth, finalHeight, true)
            val stream = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 85, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            bytes
        }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) name = cursor.getString(nameIndex)
        }
        return name
    }

    private fun getUniqueFile(directory: File, fileName: String): File {
        var file = File(directory, fileName)
        if (!file.exists()) return file
        val nameWithoutExt = fileName.substringBeforeLast(".")
        val ext = fileName.substringAfterLast(".", "")
        var counter = 1
        while (file.exists()) {
            file = File(directory, "${nameWithoutExt}_$counter.$ext")
            counter++
        }
        return file
    }
}
