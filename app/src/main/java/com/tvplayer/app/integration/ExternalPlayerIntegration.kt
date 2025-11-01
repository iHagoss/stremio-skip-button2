package com.tvplayer.app.integration

import android.content.Context
import android.content.Intent
import android.util.Log

class ExternalPlayerIntegration(
    private val context: Context,
    private val mode: PlayerIntegrationMode
) {
    private var telemetryProvider: PlaybackTelemetryProvider? = null

    fun setTelemetryProvider(provider: PlaybackTelemetryProvider) {
        telemetryProvider = provider
    }

    fun broadcastPlaybackState() {
        if (mode != PlayerIntegrationMode.ExternalHook) return

        telemetryProvider?.let { provider ->
            val telemetry = provider.getPlaybackTelemetry()
            val timeInfo = provider.getTimeInfo()
            val skipRanges = provider.getSkipRanges()

            if (telemetry != null) {
                val intent = Intent(ACTION_PLAYBACK_STATE).apply {
                    putExtra(EXTRA_POSITION, telemetry.currentPosition)
                    putExtra(EXTRA_DURATION, telemetry.duration)
                    putExtra(EXTRA_SPEED, telemetry.playbackSpeed)
                    putExtra(EXTRA_IS_PLAYING, telemetry.isPlaying)
                    
                    timeInfo?.let {
                        putExtra(EXTRA_TIME_ELAPSED, it.elapsed)
                        putExtra(EXTRA_TIME_REMAINING, it.remaining)
                        putExtra(EXTRA_TIME_TOTAL, it.total)
                        putExtra(EXTRA_TIME_NOW, it.currentTime)
                        putExtra(EXTRA_TIME_END, it.endTime)
                    }
                    
                    putExtra(EXTRA_SKIP_RANGES_COUNT, skipRanges.size)
                    skipRanges.forEachIndexed { index, range ->
                        putExtra("${EXTRA_SKIP_RANGE_PREFIX}${index}_start", range.start)
                        putExtra("${EXTRA_SKIP_RANGE_PREFIX}${index}_end", range.end)
                        putExtra("${EXTRA_SKIP_RANGE_PREFIX}${index}_type", range.type)
                    }
                }

                context.sendBroadcast(intent)
                Log.d(TAG, "Broadcast playback state: pos=${telemetry.currentPosition}, dur=${telemetry.duration}")
            }
        }
    }

    fun isEnabled(): Boolean = mode == PlayerIntegrationMode.ExternalHook

    companion object {
        private const val TAG = "ExternalPlayerIntegration"
        
        const val ACTION_PLAYBACK_STATE = "com.tvplayer.app.PLAYBACK_STATE"
        const val EXTRA_POSITION = "position"
        const val EXTRA_DURATION = "duration"
        const val EXTRA_SPEED = "speed"
        const val EXTRA_IS_PLAYING = "is_playing"
        const val EXTRA_TIME_ELAPSED = "time_elapsed"
        const val EXTRA_TIME_REMAINING = "time_remaining"
        const val EXTRA_TIME_TOTAL = "time_total"
        const val EXTRA_TIME_NOW = "time_now"
        const val EXTRA_TIME_END = "time_end"
        const val EXTRA_SKIP_RANGES_COUNT = "skip_ranges_count"
        const val EXTRA_SKIP_RANGE_PREFIX = "skip_range_"
    }
}
