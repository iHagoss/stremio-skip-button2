package com.tvplayer.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tvplayer.app.skip.*
import com.tvplayer.app.integration.*
import org.json.JSONArray
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private var player: ExoPlayer? = null
    private lateinit var skipOverlayContainer: FrameLayout

    private lateinit var skipRangeManager: SkipRangeManager
    private var skipOverlay: SkipOverlay? = null

    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    // Time info views
    private lateinit var timeElapsed: TextView
    private lateinit var timeRemaining: TextView
    private lateinit var timeTotal: TextView
    private lateinit var timeNow: TextView
    private lateinit var timeEnd: TextView
    private lateinit var timeInfoLayout: LinearLayout

    // External player integration
    private lateinit var externalPlayerIntegration: ExternalPlayerIntegration

    companion object {
        private const val VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val UPDATE_INTERVAL_MS = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        skipOverlayContainer = findViewById(R.id.skipOverlayContainer)

        timeElapsed = playerView.findViewById(R.id.timeElapsed)
        timeRemaining = playerView.findViewById(R.id.timeRemaining)
        timeTotal = playerView.findViewById(R.id.timeTotal)
        timeNow = playerView.findViewById(R.id.timeNow)
        timeEnd = playerView.findViewById(R.id.timeEnd)
        timeInfoLayout = playerView.findViewById(R.id.timeInfoLayout)

        playerView.setControllerVisibilityListener { visibility ->
            timeInfoLayout.visibility = if (visibility == View.VISIBLE) View.VISIBLE else View.GONE
        }

        skipRangeManager = createSkipRangeManager()

        setupExternalPlayerIntegration()
        initializePlayer()
        setupSkipOverlay()
    }

    private fun setupExternalPlayerIntegration() {
        refreshExternalPlayerIntegration()
    }

    private fun createSkipRangeManager(): SkipRangeManager {
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)
        val traktApiKey = prefs.getString("TRAKT_API_KEY", null)
        val tmdbApiKey = prefs.getString("TMDB_API_KEY", null)
        val tvdbApiKey = prefs.getString("TVDB_API_KEY", null)
        val skipLogic = prefs.getString("SKIP_LOGIC", "Defaults")

        val providers = mutableListOf<MetadataProvider>()

        if (!traktApiKey.isNullOrEmpty() && skipLogic == "Trakt") {
            providers.add(TraktProvider(traktApiKey))
        }
        if (!tmdbApiKey.isNullOrEmpty() && skipLogic == "TMDb") {
            providers.add(TMDbProvider(tmdbApiKey))
        }
        if (!tvdbApiKey.isNullOrEmpty() && skipLogic == "TVDB") {
            providers.add(TVDBProvider(tvdbApiKey))
        }

        val metadataChain = if (providers.isNotEmpty()) {
            MetadataProviderChain(providers)
        } else null

        return SkipRangeManager(metadataChain)
    }

    private fun initializePlayer() {
        val videoUrl = getVideoUrlFromIntent() ?: return

        player = ExoPlayer.Builder(this).build().apply {
            playerView.player = this

            val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_READY -> {
                            startPositionUpdates()
                            applySkipLogic()
                        }
                        Player.STATE_ENDED -> {
                            stopPositionUpdates()
                        }
                    }
                }
            })
        }
    }

    private fun applySkipLogic() {
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)
        val skipLogic = prefs.getString("SKIP_LOGIC", "Defaults")

        when (skipLogic) {
            "Manual" -> useManualRanges()
            else -> fetchSkipRanges()
        }
    }

    private fun getVideoUrlFromIntent(): String? {
        val isExternalLaunch = Intent.ACTION_VIEW == intent.action

        val intentData = intent.data
        if (isValidVideoUri(intentData)) return intentData.toString()

        val extraStream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (isValidVideoUri(extraStream)) return extraStream.toString()

        if (isExternalLaunch) {
            Toast.makeText(this, "No valid video URL provided", Toast.LENGTH_LONG).show()
            finish()
            return null
        }

        return VIDEO_URL
    }

    private fun isValidVideoUri(uri: Uri?): Boolean {
        if (uri == null) return false
        val scheme = uri.scheme ?: return false
        return scheme == "http" || scheme == "https"
    }

    private fun setupSkipOverlay() {
        skipOverlay = SkipOverlay(this, skipOverlayContainer, skipRangeManager).apply {
            setOnSeekListener { seekPositionMs ->
                player?.seekTo(seekPositionMs)
            }
            setOnNextEpisodeListener { handleNextEpisode() }
        }
    }

    private fun fetchSkipRanges() {
        val episodeId = "game-of-thrones-1-1"
        val fileHash = "sample-hash-12345"

        skipRangeManager.fetchSkipRangesWithMetadata(
            episodeId,
            fileHash,
            object : SkipRangeManager.SkipRangesCallback {
                override fun onSuccess(ranges: List<SkipRangeManager.SkipRange>) {
                    skipOverlay?.setRanges(ranges)
                }

                override fun onError(error: Exception) {
                    useDefaultRanges()
                }
            })
    }

    private fun useDefaultRanges() {
        val defaultRanges = listOf(
            SkipRangeManager.SkipRange(start = 0.0, end = 10.0, type = "cold_open"),
            SkipRangeManager.SkipRange(start = 540.0, end = 596.0, type = "credits_end")
        )
        skipOverlay?.setRanges(defaultRanges)
    }

    private fun useManualRanges() {
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)
        val json = prefs.getString("MANUAL_SKIP_RANGES", "[]")
        val arr = JSONArray(json)
        val ranges = mutableListOf<SkipRangeManager.SkipRange>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            ranges.add(
                SkipRangeManager.SkipRange(
                    start = obj.getDouble("start"),
                    end = obj.getDouble("end"),
                    type = obj.getString("type")
                )
            )
        }
        skipOverlay?.setRanges(ranges)
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        updateRunnable = object : Runnable {
            override fun run() {
                player?.let {
                    skipOverlay?.updatePosition(it.currentPosition)
                    updateTimeInfo()
                    externalPlayerIntegration.broadcastPlaybackState()
                }
                updateHandler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun stopPositionUpdates() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun updateTimeInfo() {
        val p = player ?: return
        val elapsed = p.currentPosition
        val duration = p.duration.takeIf { it > 0 } ?: return
        val remaining = duration - elapsed
        val speed = p.playbackParameters.speed

        val adjustedRemaining = (remaining / speed).toLong()
        val now = System.currentTimeMillis()
        val endTime = now + adjustedRemaining

        timeElapsed.text = formatTime(elapsed)
        timeRemaining.text = "-${formatTime(remaining)}"
        timeTotal.text = "Total: ${formatTime(duration)}"
        timeNow.text = "Now: ${formatClock(now)}"
        timeEnd.text = "Ends: ${formatClock(endTime)}"
    }

    private fun formatTime(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    private fun formatClock(ms: Long): String {
        val cal = Calendar.getInstance().apply { timeInMillis = ms }
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        return String.format("%d:%02d", hour, minute)
    }

    private fun handleNextEpisode() {
        Toast.makeText(this, "Next Episode feature not implemented", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            R.id.action_manual_skip -> {
                startActivity(Intent(this, ManualSkipEditorActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshExternalPlayerIntegration()
    }

    private fun refreshExternalPlayerIntegration() {
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)
        val enabled = prefs.getBoolean("pref_integrate_external_players", false)
        val mode = PlayerIntegrationMode.fromPreference(enabled)

        externalPlayerIntegration = ExternalPlayerIntegration(this, mode)
        externalPlayerIntegration.setTelemetryProvider(object : PlaybackTelemetryProvider {
            override fun getPlaybackTelemetry(): PlaybackTelemetry? {
                val p = player ?: return null
                return PlaybackTelemetry(
                    currentPosition = p.currentPosition,
                    duration = p.duration.takeIf { it > 0 } ?: 0,
                    playbackSpeed = p.playbackParameters.speed,
                    isPlaying = p.isPlaying
                )
            }

            override fun getTimeInfo(): TimeInfo? {
                val p = player ?: return null
                val elapsed = p.currentPosition
                val duration = p.duration.takeIf { it > 0 } ?: return null
                val remaining = duration - elapsed
                val speed = p.playbackParameters.speed
                val adjustedRemaining = (remaining / speed).toLong()
                val now = System.currentTimeMillis()
                val endTime = now + adjustedRemaining

                return TimeInfo(
                    elapsed = formatTime(elapsed),
                    remaining = formatTime(remaining),
                    total = formatTime(duration),
                    currentTime = formatClock(now),
                    endTime = formatClock(endTime)
                )
            }

            override fun getSkipRanges(): List<SkipRangeInfo> {
                return skipOverlay?.getRanges()?.map {
                    SkipRangeInfo(start = it.start, end = it.end, type = it.type)
                } ?: emptyList()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPositionUpdates()
        player?.release()
        player = null
    }
}
