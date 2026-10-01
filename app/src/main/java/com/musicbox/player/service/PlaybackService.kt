package com.musicbox.player.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.musicbox.player.MainActivity

class PlaybackService : MediaSessionService() {
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    companion object {
        const val ACTION_CYCLE_REPEAT = "ACTION_CYCLE_REPEAT"
        const val ACTION_TOGGLE_SHUFFLE = "ACTION_TOGGLE_SHUFFLE"
    }

    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(), true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        player = exoPlayer

        val sessionActivityIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val callback = object : MediaSession.Callback {
            override fun onCustomCommand(
                session: MediaSession,
                controller: MediaSession.ControllerInfo,
                customCommand: SessionCommand,
                args: Bundle
            ): ListenableFuture<SessionResult> {
                when (customCommand.customAction) {
                    ACTION_CYCLE_REPEAT -> {
                        exoPlayer.repeatMode = when (exoPlayer.repeatMode) {
                            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                            else -> Player.REPEAT_MODE_OFF
                        }
                        updateNotificationLayout()
                    }
                    ACTION_TOGGLE_SHUFFLE -> {
                        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
                        updateNotificationLayout()
                    }
                }
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(callback)
            .build()

        updateNotificationLayout()
    }

    private fun updateNotificationLayout() {
        val session = mediaSession ?: return
        val repeatCmd = CommandButton.Builder()
            .setSessionCommand(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
            .setDisplayName("Зациклить")
            .setIconResId(android.R.drawable.ic_menu_rotate)
            .build()

        val shuffleCmd = CommandButton.Builder()
            .setSessionCommand(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
            .setDisplayName("Перемешать")
            .setIconResId(android.R.drawable.ic_menu_directions)
            .build()

        session.setCustomLayout(listOf(repeatCmd, shuffleCmd))
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
