# Smart Player - Android TV App

A complete Android TV app for video playback with ExoPlayer, featuring skip intro/credits functionality, settings management, and external player integration.

## Features

- **Fullscreen ExoPlayer Video Playback**: Optimized for Android TV / Firestick remote navigation (DPAD focusable)
- **Real-time Info Display**: Shows elapsed time, remaining time, total duration, current time, and estimated end time
- **Dynamic Playback Speed**: Adjustable speed with instant "Ends at" recalculation
- **Skip Intro/Credits**: Fetches JSON markers from remote API and automatically skips intro/credits
- **TV-Optimized Settings**: Configure API keys (Trakt, TMDB), feature toggles, and playback preferences
- **External Player Integration**: Accept video URLs via Android Intent (works with Stremio, Syncler+, etc.)
- **Persistent Settings**: All preferences stored in SharedPreferences

## Project Structure

```
SmartPlayer/
├── app/
│   ├── src/main/
│   │   ├── java/com/smartplayer/tv/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SettingsActivity.kt
│   │   │   ├── PlayerManager.kt
│   │   │   ├── SkipMarkerManager.kt
│   │   │   └── PreferencesHelper.kt
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml
│   │   │   │   ├── activity_settings.xml
│   │   │   │   └── custom_player_controls.xml
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   ├── colors.xml
│   │   │   │   └── themes.xml
│   │   │   └── drawable/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/build-apk.yml
```

## Building the APK

### Option 1: GitHub Actions (Recommended)

1. Push this project to a GitHub repository
2. GitHub Actions will automatically build the APK on every push to main/master
3. Download the APK from the Actions artifacts or Releases section

### Option 2: Local Build with Android Studio

1. Clone this repository
2. Open the project in Android Studio
3. Wait for Gradle sync to complete
4. Build → Build Bundle(s) / APK(s) → Build APK(s)
5. The APK will be in `app/build/outputs/apk/release/`

### Option 3: Command Line Build

```bash
chmod +x gradlew
./gradlew assembleRelease
```

The APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

## Installation

1. Transfer the APK to your Android TV / Firestick device
2. Enable "Install from Unknown Sources" in device settings
3. Install the APK using a file manager
4. The app will appear in your TV launcher

## Configuration

### Settings Menu

Access settings from the launcher or press the Menu button during playback:

- **Trakt API Key**: Enter your Trakt API key for tracking
- **TMDB API Key**: Enter your TMDB API key for metadata
- **Skip Marker API URL**: URL endpoint that returns skip markers in JSON format
- **Enable Skip Intro/Credits**: Toggle automatic skip functionality
- **Default Playback Speed**: Choose from 1.0x, 1.25x, 1.5x, 1.75x, 2.0x

### Skip Markers API Format

The skip marker API should return JSON in this format:

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

Times are in seconds. The app will automatically skip the intro on playback start and show skip buttons during the respective ranges.

## Usage

### As External Player

1. In apps like Stremio or Syncler+, go to settings
2. Set Smart Player as the external video player
3. When you play a video, it will open in Smart Player with all features active

### Direct Launch

1. Launch the app from your TV launcher
2. The app will open and wait for a video URL via Intent

## Requirements

- **Minimum SDK**: Android 5.0 (API 21)
- **Target SDK**: Android 14 (API 34)
- **Permissions**: INTERNET, ACCESS_NETWORK_STATE
- **Libraries**: AndroidX Media3 (ExoPlayer), OkHttp, Gson

## Development

### Dependencies

The project uses:
- Kotlin 1.9.20
- AndroidX Media3 1.2.0 (ExoPlayer)
- AndroidX Leanback 1.0.0
- OkHttp 4.12.0
- Gson 2.10.1

### Gradle Wrapper

If you don't have the Gradle wrapper files, run:

```bash
gradle wrapper --gradle-version 8.2
```

## License

This project is provided as-is for educational and personal use.

## Support

For issues, feature requests, or questions, please open an issue on the GitHub repository.
