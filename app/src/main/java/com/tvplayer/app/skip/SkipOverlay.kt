package com.tvplayer.app.skip

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import com.tvplayer.app.R

class SkipOverlay(
    private val context: Context,
    private val container: FrameLayout,
    private val skipRangeManager: SkipRangeManager
) {
    
    private var skipButton: Button? = null
    private var nextEpisodeButton: Button? = null
    private var countdownText: TextView? = null
    
    private var currentRanges: List<SkipRangeManager.SkipRange> = emptyList()
    private var currentActiveRange: SkipRangeManager.SkipRange? = null
    
    private val autoAdvanceHandler = Handler(Looper.getMainLooper())
    private var autoAdvanceRunnable: Runnable? = null
    private var autoAdvanceCountdown = 0
    private var autoAdvanceCancelled = false
    
    private var onSeekListener: ((Long) -> Unit)? = null
    private var onNextEpisodeListener: (() -> Unit)? = null
    
    companion object {
        private const val TAG = "SkipOverlay"
        private const val AUTO_ADVANCE_DELAY_SECONDS = 10
    }
    
    init {
        setupOverlay()
    }
    
    private fun setupOverlay() {
        val overlayView = LayoutInflater.from(context).inflate(R.layout.skip_overlay, container, false)
        
        skipButton = overlayView.findViewById(R.id.skip_button)
        nextEpisodeButton = overlayView.findViewById(R.id.next_episode_button)
        countdownText = overlayView.findViewById(R.id.countdown_text)
        
        skipButton?.setOnClickListener {
            currentActiveRange?.let { range ->
                Log.d(TAG, "Skip button clicked for range: ${range.type}")
                val seekPositionMs = (range.end * 1000).toLong()
                onSeekListener?.invoke(seekPositionMs)
                hideSkipButton()
            }
        }
        
        nextEpisodeButton?.setOnClickListener {
            Log.d(TAG, "Next episode button clicked")
            cancelAutoAdvance()
            hideNextEpisodeButton()
            onNextEpisodeListener?.invoke()
        }
        
        container.addView(overlayView)
        hideAllButtons()
    }
    
    fun setRanges(ranges: List<SkipRangeManager.SkipRange>) {
        currentRanges = ranges
        Log.d(TAG, "Skip ranges loaded: ${ranges.size} ranges")
    }
    
    fun updatePosition(currentPositionMs: Long) {
        val currentPositionSeconds = currentPositionMs / 1000.0
        val activeRange = skipRangeManager.findActiveRange(currentPositionSeconds, currentRanges)
        
        if (activeRange != currentActiveRange) {
            currentActiveRange = activeRange
            
            if (activeRange != null) {
                handleRangeEntered(activeRange)
            } else {
                handleRangeExited()
            }
        }
        
        updateCountdown()
    }
    
    private fun handleRangeEntered(range: SkipRangeManager.SkipRange) {
        Log.d(TAG, "Entered range: ${range.type} (${range.start}s - ${range.end}s)")
        
        when (range.type.lowercase()) {
            "credits_end" -> {
                hideSkipButton()
                showNextEpisodeButton()
                startAutoAdvance()
            }
            else -> {
                hideNextEpisodeButton()
                cancelAutoAdvance()
                showSkipButton(range)
            }
        }
    }
    
    private fun handleRangeExited() {
        hideSkipButton()
        
        if (!autoAdvanceCancelled) {
            cancelAutoAdvance()
            hideNextEpisodeButton()
        }
    }
    
    private fun showSkipButton(range: SkipRangeManager.SkipRange) {
        val label = skipRangeManager.getRangeLabel(range.type)
        skipButton?.text = label
        skipButton?.visibility = View.VISIBLE
        Log.d(TAG, "Showing skip button: $label")
    }
    
    private fun hideSkipButton() {
        skipButton?.visibility = View.GONE
    }
    
    private fun showNextEpisodeButton() {
        nextEpisodeButton?.visibility = View.VISIBLE
        countdownText?.visibility = View.VISIBLE
        Log.d(TAG, "Showing next episode button")
    }
    
    private fun hideNextEpisodeButton() {
        nextEpisodeButton?.visibility = View.GONE
        countdownText?.visibility = View.GONE
    }
    
    private fun startAutoAdvance() {
        autoAdvanceCountdown = AUTO_ADVANCE_DELAY_SECONDS
        autoAdvanceCancelled = false
        
        autoAdvanceRunnable = object : Runnable {
            override fun run() {
                if (autoAdvanceCancelled) {
                    return
                }
                
                autoAdvanceCountdown--
                
                if (autoAdvanceCountdown <= 0) {
                    Log.d(TAG, "Auto-advancing to next episode")
                    hideNextEpisodeButton()
                    onNextEpisodeListener?.invoke()
                } else {
                    autoAdvanceHandler.postDelayed(this, 1000)
                }
            }
        }
        
        autoAdvanceHandler.postDelayed(autoAdvanceRunnable!!, 1000)
        Log.d(TAG, "Auto-advance started: $AUTO_ADVANCE_DELAY_SECONDS seconds")
    }
    
    private fun cancelAutoAdvance() {
        autoAdvanceCancelled = true
        autoAdvanceRunnable?.let {
            autoAdvanceHandler.removeCallbacks(it)
        }
        autoAdvanceRunnable = null
        Log.d(TAG, "Auto-advance cancelled")
    }
    
    private fun updateCountdown() {
        if (autoAdvanceCountdown > 0 && !autoAdvanceCancelled) {
            countdownText?.text = "Playing next in $autoAdvanceCountdown..."
        }
    }
    
    fun setOnSeekListener(listener: (Long) -> Unit) {
        onSeekListener = listener
    }
    
    fun setOnNextEpisodeListener(listener: () -> Unit) {
        onNextEpisodeListener = listener
    }
    
    private fun hideAllButtons() {
        hideSkipButton()
        hideNextEpisodeButton()
    }
    
    fun cleanup() {
        cancelAutoAdvance()
        autoAdvanceHandler.removeCallbacksAndMessages(null)
    }
}
