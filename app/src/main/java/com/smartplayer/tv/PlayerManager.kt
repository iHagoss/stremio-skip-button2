package com.smartplayer.tv

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class PlayerManager(private val context: Context) {
    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var playbackSpeed: Float = 1.0f

    fun initializePlayer(videoUrl: String, speed: Float = 1.0f): ExoPlayer {
        releasePlayer()
        
        playbackSpeed = speed
        player = ExoPlayer.Builder(context).build().also {
            val mediaItem = MediaItem.fromUri(videoUrl)
            it.setMediaItem(mediaItem)
            it.playWhenReady = true
            it.prepare()
            it.playbackParameters = PlaybackParameters(playbackSpeed)
        }
        return player!!
    }

    fun attachPlayerView(view: PlayerView) {
        playerView = view
        view.player = player
    }

    fun setPlaybackSpeed(speed: Float) {
        playbackSpeed = speed
        player?.playbackParameters = PlaybackParameters(speed)
    }

    fun getPlaybackSpeed(): Float = playbackSpeed

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0

    fun getDuration(): Long = player?.duration ?: 0

    fun isPlaying(): Boolean = player?.isPlaying ?: false

    fun seekTo(positionMs: Long) {
        player?.seekTo(positionMs)
    }

    fun addListener(listener: Player.Listener) {
        player?.addListener(listener)
    }

    fun removeListener(listener: Player.Listener) {
        player?.removeListener(listener)
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}
