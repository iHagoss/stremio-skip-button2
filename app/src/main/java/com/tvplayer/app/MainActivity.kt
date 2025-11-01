package com.tvplayer.app

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
    
    private var timeNowTextView: TextView? = null
    private var timeEndTextView: TextView? = null
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    
    companion object {
        private const val TAG = "MainActivity"
        private const val VIDEO_URL = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        private const val UPDATE_INTERVAL_MS = 500L
        
        private const val ENV_TRAKT_API_KEY = "TRAKT_API_KEY"
        private const val ENV_TMDB_API_KEY = "TMDB_API_KEY"
