package com.smartplayer.tv

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var playerManager: PlayerManager
    private lateinit var skipMarkerManager: SkipMarkerManager
    private lateinit var preferencesHelper: PreferencesHelper
    private var playerView: PlayerView? = null
    private var elapsedTimeText: TextView? = null
    private var remainingTimeText: TextView? = null
    private var totalDurationText: TextView? = null
    private var currentTimeText: TextView? = null
    private var estimatedEndText: TextView? = null
    private var skipIntroButton: Button? = null
    private var skipCreditsButton: Button? = null
    
    private var skipMarkers: SkipMarkers? = null
    private val updateHandler = Handler(Looper.getMainLooper())
    private val timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())
    private var isPlaybackMode = false
    
    private val updateTimeRunnable = object : Runnable {
        override fun run() {
            updateTimeInfo()
            updateHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        preferencesHelper = PreferencesHelper(this)
        playerManager = PlayerManager(this)
        skipMarkerManager = SkipMarkerManager(preferencesHelper.skipMarkerApiUrl)
        
        handleIntent(intent)
    }
    
    override fun onResume() {
        super.onResume()
        refreshSettings()
    }
    
    private fun refreshSettings() {
        skipMarkerManager = SkipMarkerManager(preferencesHelper.skipMarkerApiUrl)
        
        if (isPlaybackMode && playerManager.isPlaying()) {
            if (preferencesHelper.skipMarkersEnabled) {
                loadSkipMarkers()
            } else {
                skipMarkers = null
                skipIntroButton?.visibility = View.GONE
                skipCreditsButton?.visibility = View.GONE
            }
        }
    }

    private fun initializeViews() {
        playerView = findViewById(R.id.player_view)
        elapsedTimeText = findViewById(R.id.elapsed_time)
        remainingTimeText = findViewById(R.id.remaining_time)
        totalDurationText = findViewById(R.id.total_duration)
        currentTimeText = findViewById(R.id.current_time)
        estimatedEndText = findViewById(R.id.estimated_end)
        skipIntroButton = findViewById(R.id.skip_intro_button)
        skipCreditsButton = findViewById(R.id.skip_credits_button)
        
        skipIntroButton?.setOnClickListener {
            skipMarkers?.let { markers ->
                skipMarkerManager.getIntroEndPosition(markers)?.let { endPos ->
                    playerManager.seekTo(endPos)
                }
            }
        }
        
        skipCreditsButton?.setOnClickListener {
            skipMarkers?.let { markers ->
                val duration = playerManager.getDuration()
                skipMarkerManager.getCreditsEndPosition(markers, duration)?.let { endPos ->
                    playerManager.seekTo(endPos)
                }
            }
        }
    }

    private fun showLandingScreen() {
        setContentView(R.layout.landing_screen)
        isPlaybackMode = false
        
        findViewById<Button>(R.id.open_settings_button).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }
    
    private fun showPlaybackScreen() {
        setContentView(R.layout.activity_main)
        isPlaybackMode = true
        initializeViews()
    }
    
    private fun handleIntent(intent: Intent) {
        val videoUrl = when (intent.action) {
            Intent.ACTION_VIEW -> intent.data?.toString()
            else -> intent.getStringExtra("video_url")
        }
        
        if (videoUrl.isNullOrEmpty()) {
            showLandingScreen()
            return
        }
        
        showPlaybackScreen()
        startPlayback(videoUrl)
    }

    private fun startPlayback(videoUrl: String) {
        val speed = preferencesHelper.defaultPlaybackSpeed
        val player = playerManager.initializePlayer(videoUrl, speed)
        playerView?.let { playerManager.attachPlayerView(it) }
        
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_READY && preferencesHelper.skipMarkersEnabled) {
                    loadSkipMarkers()
                }
            }
        })
        
        updateHandler.post(updateTimeRunnable)
    }

    private fun loadSkipMarkers() {
        lifecycleScope.launch {
            skipMarkers = skipMarkerManager.fetchSkipMarkers()
            skipMarkers?.intro?.let {
                val currentPos = playerManager.getCurrentPosition()
                if (currentPos < it.end * 1000) {
                    playerManager.seekTo(it.end * 1000)
                }
            }
        }
    }

    private fun updateTimeInfo() {
        if (!isPlaybackMode || !playerManager.isPlaying() && playerManager.getCurrentPosition() == 0L) {
            return
        }
        
        val currentPos = playerManager.getCurrentPosition()
        val duration = playerManager.getDuration()
        val speed = playerManager.getPlaybackSpeed()
        
        val elapsed = formatTime(currentPos)
        val remaining = formatTime(duration - currentPos)
        val total = formatTime(duration)
        val now = timeFormat.format(Date())
        
        val remainingMs = duration - currentPos
        val adjustedRemainingMs = (remainingMs / speed).toLong()
        val estimatedEndTime = System.currentTimeMillis() + adjustedRemainingMs
        val endsAt = timeFormat.format(Date(estimatedEndTime))
        
        elapsedTimeText?.text = getString(R.string.elapsed_time, elapsed)
        remainingTimeText?.text = getString(R.string.remaining_time, remaining)
        totalDurationText?.text = getString(R.string.total_duration, total)
        currentTimeText?.text = getString(R.string.current_time, now)
        estimatedEndText?.text = getString(R.string.estimated_end, endsAt)
        
        skipMarkers?.let { markers ->
            if (skipMarkerManager.isInIntroRange(currentPos, markers)) {
                skipIntroButton?.visibility = View.VISIBLE
            } else {
                skipIntroButton?.visibility = View.GONE
            }
            
            if (skipMarkerManager.isInCreditsRange(currentPos, markers)) {
                skipCreditsButton?.visibility = View.VISIBLE
            } else {
                skipCreditsButton?.visibility = View.GONE
            }
        }
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        val hours = (milliseconds / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return when (keyCode) {
            KeyEvent.KEYCODE_MENU -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onKeyDown(keyCode, event)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        updateHandler.removeCallbacks(updateTimeRunnable)
        playerManager.releasePlayer()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }
}
