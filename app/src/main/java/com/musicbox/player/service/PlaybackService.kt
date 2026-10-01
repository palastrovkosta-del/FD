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
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                val connectionResult = super.onConnect(session, controller)
                val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                availableSessionCommands.add(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
                availableSessionCommands.add(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))

                return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                    .setAvailableSessionCommands(availableSessionCommands.build())
                    .setAvailablePlayerCommands(connectionResult.availablePlayerCommands)
                    .setCustomLayout(buildButtons(exoPlayer))
                    .build()
            }

            override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
                session.setCustomLayout(controller, buildButtons(exoPlayer))
            }

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
                    }
                    ACTION_TOGGLE_SHUFFLE -> {
                        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
                    }
                }
                updateButtons(session, exoPlayer)
                return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
            }
        }

        exoPlayer.addListener(object : Player.Listener {
            override fun onRepeatModeChanged(repeatMode: Int) {
                mediaSession?.let { updateButtons(it, exoPlayer) }
            }
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                mediaSession?.let { updateButtons(it, exoPlayer) }
            }
        })

        mediaSession = MediaSession.Builder(this, exoPlayer)
            .setSessionActivity(sessionActivityIntent)
            .setCallback(callback)
            .build()
    }

    private fun buildButtons(player: ExoPlayer): List<CommandButton> {
        val shuffleIcon = if (player.shuffleModeEnabled) {
            android.R.drawable.ic_menu_directions
        } else {
            android.R.drawable.ic_menu_send
        }
        val shuffleName = if (player.shuffleModeEnabled) "Перемешивание (ВКЛ)" else "Перемешивание (ВЫКЛ)"

        val (repeatIcon, repeatName) = when (player.repeatMode) {
            Player.REPEAT_MODE_ONE -> Pair(android.R.drawable.ic_menu_revert, "Повтор трека (1)")
            Player.REPEAT_MODE_ALL -> Pair(android.R.drawable.ic_menu_rotate, "Повтор списка (ВКЛ)")
            else -> Pair(android.R.drawable.ic_menu_close_clear_cancel, "Повтор (ВЫКЛ)")
        }

        val shuffleCmd = CommandButton.Builder()
            .setSessionCommand(SessionCommand(ACTION_TOGGLE_SHUFFLE, Bundle.EMPTY))
            .setDisplayName(shuffleName)
            .setIconResId(shuffleIcon)
            .build()

        val repeatCmd = CommandButton.Builder()
            .setSessionCommand(SessionCommand(ACTION_CYCLE_REPEAT, Bundle.EMPTY))
            .setDisplayName(repeatName)
            .setIconResId(repeatIcon)
            .build()

        return listOf(shuffleCmd, repeatCmd)
    }

    private fun updateButtons(session: MediaSession, player: ExoPlayer) {
        session.setCustomLayout(buildButtons(player))
    }

    // При смахивании вкладки — музыка полностью выключается и виджет с экрана блокировки убирается!
    override fun onTaskRemoved(rootIntent: Intent?) {
        player?.let { p ->
            p.stop()
            p.clearMediaItems()
        }
        stopSelf()
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
