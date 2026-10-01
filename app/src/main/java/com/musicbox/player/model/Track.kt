package com.musicbox.player.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory

data class Track(
    val id: String,
    val filePath: String,
    val fileName: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val artworkBytes: ByteArray? = null
) {
    val artworkBitmap: Bitmap? by lazy {
        artworkBytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Track
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()
}
