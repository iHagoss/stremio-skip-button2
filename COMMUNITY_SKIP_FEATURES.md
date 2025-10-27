# Community Skip-Range API Integration Features

## Overview
This document describes the community skip-range API integration features added to the Android TV ExoPlayer app.

## Features Added

### 1. Community Skip-Range API Integration
- **API Endpoint**: `https://busy-jacinta-shugi-c2885b2e.koyeb.app/ranges/{episodeId}?fileId={fileHash}`
- **Implementation**: `SkipRangeManager.kt` handles all API communication
- **Response Format**: JSON with array of skip ranges containing start, end, and type fields
- **Error Handling**: Falls back to default ranges if API is unavailable

### 2. Dynamic Skip Buttons
- Context-aware buttons that appear based on current playback position
- Supported range types:
  - **Cold Open**: "Skip Cold Open" button
  - **Intro**: "Skip Intro" button  
  - **Credits**: "Skip Credits" button
  - **End Credits**: "Skip End Credits" button
  - **Recap**: "Skip Recap" button
  - **Preview**: "Skip Preview" button
- Buttons automatically hide when exiting a range
- Click handler seeks to end of current range

### 3. Next Episode Auto-Advance
- Triggered when playback enters "credits_end" range
- Shows "Next Episode" button
- 10-second countdown timer displayed
- User can cancel by clicking button early
- Auto-advances after countdown expires
- Currently shows toast notification (ready for Syncler/Stremio integration)

### 4. Modular Architecture

#### SkipRangeManager.kt
```kotlin
- fetchSkipRanges(episodeId, fileHash, callback) // Fetches from API
- getRangeLabel(type) // Returns button label for range type
- isInRange(position, range) // Checks if position is within range
- findActiveRange(position, ranges) // Finds current active range
```

#### SkipOverlay.kt
```kotlin
- setRanges(ranges) // Updates skip ranges
- updatePosition(positionMs) // Called every 500ms by player
- setOnSeekListener(listener) // Handles skip button clicks
- setOnNextEpisodeListener(listener) // Handles next episode action
- cleanup() // Releases resources on destroy
```

#### MainActivity.kt
```kotlin
- initializePlayer() // Sets up ExoPlayer
- setupSkipOverlay() // Configures skip overlay
- fetchSkipRanges() // Loads ranges from API
- startPositionUpdates() // 500ms update loop
- handleNextEpisode() // Next episode callback (toast for now)
```

## UI Components

### Layout Structure
```xml
activity_main.xml:
  ├── StyledPlayerView (ExoPlayer)
  └── skipOverlayContainer (FrameLayout)
      └── skip_overlay.xml
          ├── skip_button (dynamic label)
          ├── countdown_text (auto-advance timer)
          └── next_episode_button
```

### Button Visibility Logic
- Only one type of button is visible at a time
- Skip button: Shown for all range types except credits_end
- Next episode button: Only shown during credits_end range
- Countdown text: Only shown with next episode button
- All buttons hidden when outside any range

## Position Update Loop
- Runs every 500ms while player is active
- Gets current playback position from ExoPlayer
- Checks if position is in any skip range
- Shows/hides buttons based on range changes
- Updates countdown text during auto-advance

## Auto-Advance Countdown
- Starts when entering credits_end range
- Counts down from 10 seconds
- Updates countdown text every second
- Can be cancelled by:
  - User clicking next episode button
  - User exiting credits_end range
  - Player being destroyed
- Prevents duplicate auto-advance triggers

## API Response Example
```json
{
  "ranges": [
    {
      "start": 0.0,
      "end": 10.0,
      "type": "cold_open"
    },
    {
      "start": 90.0,
      "end": 150.0,
      "type": "intro"
    },
    {
      "start": 2400.0,
      "end": 2500.0,
      "type": "credits"
    },
    {
      "start": 2500.0,
      "end": 2596.0,
      "type": "credits_end"
    }
  ]
}
```

## Default Fallback Ranges
If API fails, these default ranges are used:
```kotlin
cold_open: 0.0s - 10.0s
credits_end: 540.0s - 596.0s
```

## Customization Points

### In MainActivity.kt
```kotlin
// Episode ID and file hash for API
val episodeId = "demo-episode-1"
val fileHash = "sample-hash-12345"

// Position update interval
private const val UPDATE_INTERVAL_MS = 500L

// Default ranges if API unavailable
private fun useDefaultRanges() {
    // Modify default ranges here
}
```

### In SkipRangeManager.kt
```kotlin
// API base URL
private const val BASE_URL = "https://busy-jacinta-shugi-c2885b2e.koyeb.app"

// Button labels for range types
fun getRangeLabel(type: String): String {
    // Customize button labels here
}
```

### In SkipOverlay.kt
```kotlin
// Auto-advance countdown duration
private const val AUTO_ADVANCE_DELAY_SECONDS = 10
```

## Next Steps for Production

### 1. Integrate Metadata from Syncler/Stremio
Replace hardcoded episodeId and fileHash in MainActivity:
```kotlin
// Get from intent extras or metadata service
val episodeId = intent.getStringExtra("episode_id") ?: "demo-episode-1"
val fileHash = calculateFileHash(videoUrl)
```

### 2. Implement Real Next Episode Navigation
Replace toast notification with actual episode transition:
```kotlin
private fun handleNextEpisode() {
    val nextEpisodeUrl = getNextEpisodeUrl() // From metadata
    if (nextEpisodeUrl != null) {
        playVideo(nextEpisodeUrl)
    }
}
```

### 3. Add User Preferences
- Enable/disable auto-skip
- Enable/disable auto-advance
- Customize countdown duration
- Remember user choices

### 4. Cache Skip Ranges
- Store ranges locally for rewatched content
- Reduce API calls
- Improve offline support

### 5. Submit Skip Ranges
- Allow users to submit new ranges
- Crowdsource timestamp contributions
- POST endpoint for community contributions

## Testing Checklist

- [ ] Skip buttons appear at correct positions
- [ ] Buttons have correct labels for each range type
- [ ] Seeking works correctly on button press
- [ ] Next episode button appears during credits_end
- [ ] Countdown updates every second
- [ ] Auto-advance triggers after 10 seconds
- [ ] User can cancel auto-advance
- [ ] API errors fall back to defaults gracefully
- [ ] No memory leaks on activity destroy
- [ ] Position updates stop when player paused
- [ ] External intent handling still works
- [ ] D-pad navigation works on Android TV

## Build Instructions

Requires Android SDK. To build:
```bash
./build.sh
```

Or directly with Gradle:
```bash
./gradlew assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`
