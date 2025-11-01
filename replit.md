# TV Player - Android TV App

## Overview
An Android TV application built with Kotlin and ExoPlayer that provides intelligent intro and credits skipping functionality. The app can be used as a standalone player or launched as an external video player from apps like Stremio and Syncler. It fetches skip markers from a community-powered API and automatically handles video navigation with auto-advance to next episode.

## Features
- **Fullscreen Video Playback**: ExoPlayer-based media player with fullscreen PlayerView
- **Community Skip-Range API Integration**: Fetches skip ranges from https://busy-jacinta-shugi-c2885b2e.koyeb.app
- **Dynamic Skip Buttons**: Context-aware buttons that appear based on playback position
  - Skip Cold Open
  - Skip Intro
  - Skip Credits
  - Skip End Credits
  - Skip Recap
  - Skip Preview
- **Next Episode Auto-Advance**: Automatic progression with 10-second countdown when credits end
- **Modular Architecture**: Clean separation with SkipRangeManager and SkipOverlay classes
- **Android TV Optimized**: D-pad navigation support, LEANBACK launcher configuration
- **External Player Support**: Works with Stremio and Syncler for seamless integration

## Project Structure
```
app/
├── src/main/
│   ├── java/com/tvplayer/app/
│   │   ├── MainActivity.kt            # Main activity with ExoPlayer (Kotlin)
│   │   ├── ApiService.java            # Legacy HTTP client
│   │   ├── SkipMarkers.java           # Legacy data model
│   │   └── skip/
│   │       ├── SkipRangeManager.kt    # Community API integration
│   │       └── SkipOverlay.kt         # Skip button UI manager
│   ├── res/
│   │   ├── layout/
│   │   │   ├── activity_main.xml      # Fullscreen player with overlay container
│   │   │   └── skip_overlay.xml       # Skip buttons overlay layout
│   │   ├── values/
│   │   │   ├── styles.xml             # Fullscreen theme
│   │   │   └── colors.xml             # App colors
│   │   └── drawable/
│   │       └── ic_launcher_foreground.xml
│   └── AndroidManifest.xml            # LEANBACK, INTERNET permissions
└── build.gradle                        # Kotlin + ExoPlayer dependencies
```

## Community API Integration

### Endpoint
```
GET https://busy-jacinta-shugi-c2885b2e.koyeb.app/ranges/{episodeId}?fileId={fileHash}
```

### Response Format
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

### Supported Range Types
- `cold_open` - Cold open sequence before intro
- `intro` - Opening credits/theme song
- `recap` - Previously on... recap segment
- `preview` - Next episode preview
- `credits` - End credits
- `credits_end` - Post-credits scene (triggers auto-advance)

## Dependencies
- **Kotlin 1.9.10** - Primary language
- **ExoPlayer 2.19.1** - Video playback
- **AndroidX Core KTX 1.12.0** - Kotlin extensions
- **AndroidX AppCompat 1.6.1** - Compatibility library
- **AndroidX Leanback 1.0.0** - TV UI support
- **OkHttp 4.11.0** - HTTP client
- **Gson 2.10.1** - JSON parsing

## Architecture

### MetadataProvider System
Optional metadata enrichment layer that validates skip ranges against episode runtime:
- **TraktProvider**: Fetches metadata from Trakt.tv API (requires API key)
- **TMDbProvider**: Fetches metadata from TheMovieDB API (requires API key)
- **TVDBProvider**: Stub for TVDB API integration
- **IMDbProvider**: Stub for enrichment only
- **MetadataProviderChain**: Tries providers in sequence until one succeeds

### SkipRangeManager
Handles all communication with the community skip-range API:
- Fetches skip ranges for episodes
- Parses JSON responses into typed data models
- Validates ranges against episode runtime (if metadata available)
- Provides helper methods for range detection
- Generates contextual button labels
- Stub for local range detection: `detectRangesLocally(filePath)`

### SkipOverlay
Manages the skip button UI and auto-advance logic:
- Shows/hides skip buttons based on playback position
- Handles seek operations on button press
- Implements 10-second countdown for auto-advance
- Manages next episode transition

### MainActivity
Integrates ExoPlayer with skip functionality:
- Initializes ExoPlayer and handles video playback
- Sets up position update listeners (500ms intervals)
- Manages external intent handling for Stremio/Syncler
- Coordinates between SkipRangeManager and SkipOverlay
- Optionally initializes metadata providers if API keys are set

## Configuration

### Environment Variables (Optional)
Set these environment variables to enable metadata providers:
- `TRAKT_API_KEY`: Trakt.tv API client ID
- `TMDB_API_KEY`: TheMovieDB API key
- `TVDB_API_KEY`: TVDB API key (stub, not yet implemented)

If no API keys are set, the app uses community skip ranges only without validation.

### Code Customization
Edit `MainActivity.kt` to customize:
- `episodeId` and `fileHash` parameters for API calls
- Default skip ranges if API is unavailable
- Update interval for position tracking (default: 500ms)

## Building the APK

### Important: Use Immersive Terminal for First Build
This project requires Android SDK to build. The **first build takes 10-15 minutes** to download and cache dependencies. You **must** use the Immersive Terminal (Shell tab) for the initial build.

#### Build Steps:
1. Open the Immersive Terminal in Replit
2. Run the following commands:
```bash
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
./gradlew assembleDebug --no-daemon
```

3. Wait 10-15 minutes for the first build (downloads ExoPlayer, AndroidX libraries)
4. APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

#### Quick Build Script (After First Build)
Once dependencies are cached, you can use the build script:
```bash
./build.sh
```

The script will:
1. Verify Java and Android SDK installation
2. Build debug APK using Gradle
3. Output APK to: `app/build/outputs/apk/debug/app-debug.apk`

**Note:** Subsequent builds are much faster (2-3 minutes) after the first successful build.

## Installation on Android TV
```bash
adb connect YOUR_TV_IP:5555
adb install app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.tvplayer.app/.MainActivity
```

## Build Environment
- **Android SDK**: Installed in `~/android-sdk/` with command line tools, platform-tools, and build-tools
- **Java**: OpenJDK 19.0.2 with GraalVM CE 22.3.1
- **Kotlin**: 1.9.10 with Android plugin configured
- **Gradle**: Configured with optimized JVM settings for cloud build environment

To build the APK:
```bash
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
./gradlew assembleDebug
```

The debug APK will be output to: `app/build/outputs/apk/debug/app-debug.apk`

## External Player Integration

The app now includes optional integration hooks for external player applications like Stremio and Syncler+. This feature can be enabled in Settings and provides:

- **Playback Telemetry Broadcasting**: Current position, duration, playback speed, and playing state
- **Time Information Sharing**: Elapsed, remaining, total time, plus current clock time and projected end time
- **Skip Range Exposure**: All available skip ranges with timestamps and types
- **Non-Invasive Design**: Only broadcasts when enabled; no impact when disabled
- **Read-Only Interface**: External apps can observe state but cannot control playback

### Implementation Details
- **PlayerIntegrationMode**: Sealed class managing Internal vs ExternalHook modes
- **PlaybackTelemetryProvider**: Interface for providing playback state data
- **ExternalPlayerIntegration**: Manages broadcast logic via Android Intents
- **Broadcast Frequency**: Every 500ms during active playback
- **Intent Action**: `com.tvplayer.app.PLAYBACK_STATE`

See `EXTERNAL_PLAYER_INTEGRATION.md` for detailed integration guide and API documentation.

## Enhanced Custom Player Controls

The player controls now feature a comprehensive time information display with:

- **Elapsed Time** (14sp, green): Current playback position
- **Remaining Time** (14sp, red): Time until video ends (negative format)
- **Total Duration** (13sp, grey): Complete video length
- **Current Time** (13sp, blue): Real-world clock time
- **End Time** (13sp, orange): Projected finish time based on playback speed

All time calculations account for playback speed adjustments and are displayed in a dark rounded background strip that appears only when controls are visible.

## Recent Bug Fixes (2025-11-01)
- **Critical:** Fixed API key loading - Settings now properly connects to MainActivity via SharedPreferences
- **Critical:** Added network security config to allow HTTP/HTTPS traffic
- **Feature:** Added menu system to access Settings and Manual Skip Editor from main player
- **Feature:** Implemented time display updates (Now/Ends at) in custom player controls
- **Cleanup:** Removed unused JSONArray import that could cause compilation issues

## Recent Changes
- 2025-10-31: Added external player integration with broadcast hooks for Stremio/Syncler+
- 2025-10-31: Enhanced time info display with balanced font sizes (14sp/13sp) and color coding
- 2025-10-31: Created PlayerIntegrationMode, PlaybackTelemetry, and ExternalPlayerIntegration classes
- 2025-10-31: Added Settings toggle for external player integration (pref_integrate_external_players)
- 2025-10-31: Fixed Gradle settings.gradle deprecation warning (url assignment syntax)
- 2025-10-31: Completed import to Replit environment with Android SDK and build configuration
- 2025-10-31: Configured Kotlin 1.9.10 support in Gradle build system
- 2025-10-31: Optimized gradle.properties for stable builds in cloud environment
- 2025-10-27: Added MetadataProvider system with Trakt and TMDb API integration
- 2025-10-27: Implemented skip range validation against episode runtime from metadata
- 2025-10-27: Added MetadataProviderChain for sequential provider fallback
- 2025-10-27: Created TraktProvider for real-time Trakt.tv API integration
- 2025-10-27: Created TMDbProvider for real-time TheMovieDB API integration
- 2025-10-27: Added TVDB and IMDb provider stubs for future implementation
- 2025-10-27: Added detectRangesLocally() stub for future local detection
- 2025-10-27: Updated SkipRangeManager with optional metadata provider chain
- 2025-10-27: Migrated MainActivity from Java to Kotlin
- 2025-10-27: Added community skip-range API integration with https://busy-jacinta-shugi-c2885b2e.koyeb.app
- 2025-10-27: Created modular skip package (SkipRangeManager.kt, SkipOverlay.kt)
- 2025-10-27: Implemented dynamic skip buttons for multiple range types (cold_open, intro, credits, credits_end)
- 2025-10-27: Added next episode auto-advance with 10-second countdown
- 2025-10-27: Updated layout to use skipOverlayContainer for clean UI separation
- 2025-10-27: Added Kotlin support with Gradle plugin configuration
- 2025-10-27: Initial project creation with ExoPlayer integration and Android TV support
- 2025-10-27: Added external video player support for Stremio/Syncler integration

## User Preferences
None yet
