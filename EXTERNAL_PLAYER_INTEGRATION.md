# External Player Integration Guide

## Overview

The TV Player app now includes optional integration hooks for external player applications like Stremio and Syncler+. When enabled, the app broadcasts playback telemetry and time information that external apps can consume.

## How to Enable

1. Open the app on your Android TV device
2. Navigate to **Settings** from the menu
3. Scroll down to the **External Player Integration** section
4. Toggle **"Integrate with Stremio/Syncler+ ExoPlayer"** to ON
5. Click **Save**

**Note**: Changes take effect immediately when you return to the player. The app automatically refreshes the integration mode when resuming from Settings, so there's no need to restart the app.

## What It Does

When external player integration is enabled, the app broadcasts the following information every 500ms via Android Intent broadcasts:

### Playback Telemetry
- **Current Position**: Current playback position in milliseconds
- **Duration**: Total video duration in milliseconds
- **Playback Speed**: Current playback speed (1.0 = normal, 2.0 = 2x, etc.)
- **Is Playing**: Boolean indicating if video is currently playing

### Time Information
- **Elapsed Time**: Formatted elapsed time (mm:ss)
- **Remaining Time**: Formatted remaining time (mm:ss)
- **Total Time**: Formatted total duration (mm:ss)
- **Current Clock Time**: Current time of day (HH:mm)
- **Projected End Time**: When the video will finish based on playback speed (HH:mm)

### Skip Ranges
- **Skip Range Count**: Number of available skip ranges
- **Skip Range Data**: For each range:
  - Start time (seconds)
  - End time (seconds)
  - Type (cold_open, intro, recap, preview, credits, credits_end)

## Broadcast Intent Details

### Action
```
com.tvplayer.app.PLAYBACK_STATE
```

### Extras
```kotlin
EXTRA_POSITION: Long              // Current position in ms
EXTRA_DURATION: Long              // Total duration in ms
EXTRA_SPEED: Float                // Playback speed
EXTRA_IS_PLAYING: Boolean         // Is playing state

EXTRA_TIME_ELAPSED: String        // "12:34"
EXTRA_TIME_REMAINING: String      // "5:43"
EXTRA_TIME_TOTAL: String          // "18:17"
EXTRA_TIME_NOW: String            // "14:23"
EXTRA_TIME_END: String            // "14:41"

EXTRA_SKIP_RANGES_COUNT: Int      // Number of ranges
skip_range_0_start: Double        // Range 0 start in seconds
skip_range_0_end: Double          // Range 0 end in seconds
skip_range_0_type: String         // Range 0 type
// ... continues for each range
```

## For External App Developers

To receive playback state broadcasts from TV Player:

```kotlin
// Register a BroadcastReceiver
val receiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "com.tvplayer.app.PLAYBACK_STATE") {
            val position = intent.getLongExtra("position", 0L)
            val duration = intent.getLongExtra("duration", 0L)
            val speed = intent.getFloatExtra("speed", 1.0f)
            val isPlaying = intent.getBooleanExtra("is_playing", false)
            
            val timeElapsed = intent.getStringExtra("time_elapsed")
            val timeRemaining = intent.getStringExtra("time_remaining")
            
            val skipRangesCount = intent.getIntExtra("skip_ranges_count", 0)
            for (i in 0 until skipRangesCount) {
                val start = intent.getDoubleExtra("skip_range_${i}_start", 0.0)
                val end = intent.getDoubleExtra("skip_range_${i}_end", 0.0)
                val type = intent.getStringExtra("skip_range_${i}_type")
                // Use skip range data
            }
            
            // Process playback state
        }
    }
}

// Register the receiver
val filter = IntentFilter("com.tvplayer.app.PLAYBACK_STATE")
context.registerReceiver(receiver, filter)
```

## Performance Considerations

- Broadcasts are sent every 500ms when playback is active
- Broadcasts are only sent when external integration is enabled
- No broadcasts are sent when the app is in the background or playback is stopped
- The integration is non-invasive and has minimal performance impact

## Privacy & Security

- All broadcasts are local to the device (not sent over network)
- No personal data is included in broadcasts
- Only playback state and time information is shared
- External apps cannot control playback through these broadcasts (read-only)

## Disabling Integration

To disable external player integration:

1. Open **Settings**
2. Toggle **"Integrate with Stremio/Syncler+ ExoPlayer"** to OFF
3. Click **Save**

Broadcasts will stop immediately when integration is disabled.

## Troubleshooting

**Broadcasts not received:**
- Ensure integration is enabled in Settings
- Check that your app has permission to receive broadcasts
- Verify the BroadcastReceiver is properly registered
- Make sure video playback is active

**Data seems incorrect:**
- Time info is adjusted for playback speed
- Skip ranges may be empty if none are available for the current video
- Duration may be 0 for live streams or if not yet determined

## Technical Architecture

The integration uses a modular architecture:

- **PlayerIntegrationMode**: Sealed class with Internal and ExternalHook modes
- **PlaybackTelemetryProvider**: Interface for providing playback state
- **ExternalPlayerIntegration**: Manages broadcast logic and state
- **TimeInfo/SkipRangeInfo**: Data classes for structured information

This architecture ensures clean separation of concerns and makes it easy to add more integration types in the future.
