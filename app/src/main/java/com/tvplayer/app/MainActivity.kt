package com.tvplayer.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.tvplayer.app.skip.MetadataProviderChain
import com.tvplayer.app.skip.SkipOverlay
import com.tvplayer.app.skip.SkipRangeManager
import com.tvplayer.app.skip.TraktProvider
import com.tvplayer.app.skip.TMDbProvider
import com.tvplayer.app.skip.TVDBProvider
import org.json.JSONArray

class MainActivity : AppCompatActivity() {

    private lateinit var playerView: StyledPlayerView
    private var player: ExoPlayer? = null
    private lateinit var skipOverlayContainer: FrameLayout

    private lateinit var skipRangeManager: SkipRangeManager
    private var skipOverlay: SkipOverlay? = null

    private val updateHandler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null

    companion object {
        private const val TAG = "MainActivity"
        private const val VIDEO_URL =
            "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val UPDATE_INTERVAL_MS = 500L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playerView = findViewById(R.id.player_view)
        skipOverlayContainer = findViewById(R.id.skipOverlayContainer)

        skipRangeManager = createSkipRangeManager()

        initializePlayer()
        setupSkipOverlay()
    }

    private fun createSkipRangeManager(): SkipRangeManager {
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)
        val traktApiKey = prefs.getString("TRAKT_API_KEY", null)
        val tmdbApiKey = prefs.getString("TMDB_API_KEY", null)
        val tvdbApiKey = prefs.getString("TVDB_API_KEY", null)
        val skipLogic = prefs.getString("SKIP_LOGIC", "Defaults")

        val providers = mutableListOf<com.tvplayer.app.skip.MetadataProvider>()

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
                player?.let { skipOverlay?.updatePosition(it.currentPosition) }
                updateHandler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
        updateHandler.post(updateRunnable!!)
    }

    private fun stopPositionUpdates() {
        updateRunnable?.let { updateHandler.removeCallbacks(it) }
        updateRunnable = null
    }

    private fun handleNextEpisode() {
        Toast.makeText(this, "Next Episode - placeholder", Toast.LENGTH_LONG).show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
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

    override fun onStart() {
        super.onStart()
        player?.playWhenReady = true
    }

    override fun onStop() {
        super.onStop()
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPositionUpdates()
        skipOverlay?.cleanup()
        player?.release()
        player = null
    }
}
