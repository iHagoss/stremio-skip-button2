package com.tvplayer.app

import android.content.Intent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.ui.TrackSelectionDialogBuilder
import com.tvplayer.app.skip.*
import com.tvplayer.app.skip.MetadataProviderChain
import com.tvplayer.app.skip.SkipOverlay
import com.tvplayer.app.skip.SkipRangeManager
import com.tvplayer.app.integration.*
import com.tvplayer.app.skip.TraktProvider
import com.tvplayer.app.skip.TMDbProvider
import com.tvplayer.app.skip.TVDBProvider
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

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
        private const val VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val UPDATE_INTERVAL_MS = 500L
        
        private const val ENV_TRAKT_API_KEY = "TRAKT_API_KEY"
        private const val ENV_TMDB_API_KEY = "TMDB_API_KEY"
        private const val ENV_TVDB_API_KEY = "TVDB_API_KEY"
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
        val traktApiKey = System.getenv(ENV_TRAKT_API_KEY)
        val tmdbApiKey = System.getenv(ENV_TMDB_API_KEY)
        val tvdbApiKey = System.getenv(ENV_TVDB_API_KEY)
        
        val providers = mutableListOf<com.tvplayer.app.skip.MetadataProvider>()
        
        if (!traktApiKey.isNullOrEmpty()) {
            Log.d(TAG, "Trakt API key configured")
            providers.add(TraktProvider(traktApiKey))
        } else {
            Log.d(TAG, "Trakt API key not configured, skipping")
        }
        
        if (!tmdbApiKey.isNullOrEmpty()) {
            Log.d(TAG, "TMDb API key configured")
            providers.add(TMDbProvider(tmdbApiKey))
        } else {
            Log.d(TAG, "TMDb API key not configured, skipping")
        }
        
        if (!tvdbApiKey.isNullOrEmpty()) {
            Log.d(TAG, "TVDB API key configured")
            providers.add(TVDBProvider(tvdbApiKey))
        } else {
            Log.d(TAG, "TVDB API key not configured, skipping")
        }
        
        val metadataChain = if (providers.isNotEmpty()) {
            Log.d(TAG, "Metadata provider chain created with ${providers.size} providers")
            MetadataProviderChain(providers)
        } else {
            Log.d(TAG, "No metadata providers configured, using community ranges only")
            null
        }
        
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
                            Log.d(TAG, "Player ready, starting position updates")
                            startPositionUpdates()
                            fetchSkipRanges()
                        }
                        Player.STATE_ENDED -> {
                            Log.d(TAG, "Playback ended")
                            stopPositionUpdates()
                        }
                    }
                }
            })
        }
    }

    private fun getVideoUrlFromIntent(): String? {
        val isExternalLaunch = Intent.ACTION_VIEW == intent.action
        
        val intentData = intent.data
        if (isValidVideoUri(intentData)) {
            Log.d(TAG, "Using video URL from intent data: $intentData")
            return intentData.toString()
        }
        
        val extraStream = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
        if (isValidVideoUri(extraStream)) {
            Log.d(TAG, "Using video URL from extra stream: $extraStream")
            return extraStream.toString()
        }
        
        if (isExternalLaunch) {
            Toast.makeText(this, "No valid video URL provided", Toast.LENGTH_LONG).show()
            finish()
            return null
        }
        
        Log.d(TAG, "Using default video URL: $VIDEO_URL")
        return VIDEO_URL
    }

    private fun isValidVideoUri(uri: Uri?): Boolean {
        if (uri == null) return false
        
        val uriString = uri.toString()
        if (uriString.isNullOrEmpty()) return false
        
        val scheme = uri.scheme ?: return false
        
        return scheme == "http" || scheme == "https"
    }

    private fun setupSkipOverlay() {
        skipOverlay = SkipOverlay(this, skipOverlayContainer, skipRangeManager).apply {
            setOnSeekListener { seekPositionMs ->
                player?.seekTo(seekPositionMs)
                Log.d(TAG, "Seeking to position: ${seekPositionMs}ms")
            }
            
            setOnNextEpisodeListener {
                handleNextEpisode()
            }
        }
    }

    private fun fetchSkipRanges() {
        val episodeId = "game-of-thrones-1-1"
        val fileHash = "sample-hash-12345"
        
        Log.d(TAG, "Fetching skip ranges with metadata for episodeId: $episodeId, fileHash: $fileHash")
        
        skipRangeManager.fetchSkipRangesWithMetadata(episodeId, fileHash, object : SkipRangeManager.SkipRangesCallback {
            override fun onSuccess(ranges: List<SkipRangeManager.SkipRange>) {
                Log.d(TAG, "Successfully fetched ${ranges.size} skip ranges (validated against metadata)")
                skipOverlay?.setRanges(ranges)
            }
            
            override fun onError(error: Exception) {
                Log.e(TAG, "Failed to fetch skip ranges, using defaults", error)
                useDefaultRanges()
            }
        })
    }

    private fun useDefaultRanges() {
        val defaultRanges = listOf(
            SkipRangeManager.SkipRange(start = 0.0, end = 10.0, type = "cold_open"),
            SkipRangeManager.SkipRange(start = 540.0, end = 596.0, type = "credits_end")
        )
        Log.d(TAG, "Using ${defaultRanges.size} default skip ranges")
        skipOverlay?.setRanges(defaultRanges)
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        
        updateRunnable = object : Runnable {
            override fun run() {
                player?.let { player ->
                    val currentPosition = player.currentPosition
                    skipOverlay?.updatePosition(currentPosition)
                }
                updateHandler.postDelayed(this, UPDATE_INTERVAL_MS)
            }
        }
        
        updateHandler.post(updateRunnable!!)
        Log.d(TAG, "Position updates started")
    }

    private fun stopPositionUpdates() {
        updateRunnable?.let {
            updateHandler.removeCallbacks(it)
        }
        updateRunnable = null
        Log.d(TAG, "Position updates stopped")
    }

    private fun handleNextEpisode() {
        Toast.makeText(
            this,
            "Next Episode - This will be wired to Syncler/Stremio metadata",
            Toast.LENGTH_LONG
        ).show()
        
        Log.d(TAG, "Next episode triggered - awaiting metadata integration")
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
        Log.d(TAG, "Activity destroyed, resources released")
    }
}
