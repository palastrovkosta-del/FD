package com.musicbox.player.ui.screens

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.musicbox.player.ui.components.ParticleBackground

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(
    isVisible: Boolean,
    onSplashFinished: () -> Unit
) {
    val context = LocalContext.current
    val rawId = remember {
        context.resources.getIdentifier("splash", "raw", context.packageName)
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(animationSpec = tween(600))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onSplashFinished() },
            contentAlignment = Alignment.Center
        ) {
            if (rawId != 0) {
                // Если есть видеоролик res/raw/splash.mp4
                val videoUri = Uri.parse("android.resource://${context.packageName}/$rawId")
                val exoPlayer = remember {
                    ExoPlayer.Builder(context).build().apply {
                        setMediaItem(MediaItem.fromUri(videoUri))
                        prepare()
                        playWhenReady = true
                        addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_ENDED) {
                                    onSplashFinished()
                                }
                            }
                        })
                    }
                }

                DisposableEffect(Unit) {
                    onDispose { exoPlayer.release() }
                }

                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Если видеоролика ещё нет в raw — неоновый вступительный сплэш
                ParticleBackground()
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onSplashFinished() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "FD MUSIC",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White
                    )
                }
            }

            Text(
                "Пропустить >",
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 48.dp, end = 20.dp)
            )
        }
    }
}
