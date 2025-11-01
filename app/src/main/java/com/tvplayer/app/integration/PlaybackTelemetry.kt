package com.tvplayer.app.integration

data class PlaybackTelemetry(
    val currentPosition: Long,
    val duration: Long,
    val playbackSpeed: Float,
    val isPlaying: Boolean
)

data class TimeInfo(
    val elapsed: String,
    val remaining: String,
    val total: String,
    val currentTime: String,
    val endTime: String
)

data class SkipRangeInfo(
    val start: Double,
    val end: Double,
    val type: String
)

interface PlaybackTelemetryProvider {
    fun getPlaybackTelemetry(): PlaybackTelemetry?
    fun getTimeInfo(): TimeInfo?
    fun getSkipRanges(): List<SkipRangeInfo>
}
