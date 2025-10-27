# TV Player - Android TV App

## Overview
An Android TV application built with ExoPlayer that provides intelligent intro and credits skipping functionality. The app fetches skip markers from a remote API and automatically handles video navigation.

## Features
- **Fullscreen Video Playback**: ExoPlayer-based media player with fullscreen PlayerView
- **Remote API Integration**: Fetches intro/credits timestamps from JSON API
- **Auto-Skip Intro**: Automatically seeks past intro when playback starts
- **Skip Intro Button**: Appears during intro range, seeks to intro end on click
- **Skip Credits Button**: Appears during credits range, seeks to video end on click
- **Android TV Optimized**: D-pad navigation support, LEANBACK launcher configuration

## Project Structure
```
app/
├── src/main/
│   ├── java/com/tvplayer/app/
│   │   ├── MainActivity.java          # Main activity with ExoPlayer
│   │   ├── ApiService.java            # HTTP client for API calls
│   │   └── SkipMarkers.java           # Data model for skip markers
│   ├── res/
│   │   ├── layout/
│   │   │   └── activity_main.xml      # Fullscreen player layout
│   │   ├── values/
│   │   │   ├── styles.xml             # Fullscreen theme
│   │   │   └── colors.xml             # App colors
│   │   └── drawable/
│   │       └── ic_launcher_foreground.xml
│   └── AndroidManifest.xml            # LEANBACK, INTERNET permissions
└── build.gradle                        # ExoPlayer, OkHttp, Gson dependencies
```

## API Format
The app expects a JSON response in this format:
```json
{
  "intro": {
    "start": 0,
    "end": 90
  },
  "credits": {
    "start": 2500,
    "end": 2700
  }
}
```

## Dependencies
- ExoPlayer 2.19.1 - Video playback
- AndroidX Leanback 1.0.0 - TV UI support
- OkHttp 4.11.0 - HTTP client
- Gson 2.10.1 - JSON parsing

## Configuration
Edit `MainActivity.java` to set:
- `VIDEO_URL`: Your video source URL
- `MARKERS_API_URL`: Your skip markers API endpoint

## Recent Changes
- 2025-10-27: Initial project creation with ExoPlayer integration, skip functionality, and Android TV support
- 2025-10-27: Fixed auto-skip intro to trigger when markers load (async API timing fix)
- 2025-10-27: Fixed Skip Intro button to hide after auto-skip occurs
- 2025-10-27: Removed duplicate auto-skip logic to prevent repeated seeks

## User Preferences
None yet
