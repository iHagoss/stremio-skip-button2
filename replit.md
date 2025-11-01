# Smart Player Android TV App - Replit Project

## Project Overview

This project contains the complete source code for **Smart Player**, an Android TV application built with Kotlin and ExoPlayer. The app features:

- Fullscreen video playback optimized for Android TV/Firestick
- Skip intro/credits functionality with remote API integration
- Real-time playback information display
- Settings management for API keys and preferences
- External player integration (works with Stremio, Syncler+, etc.)

## Important Note

⚠️ **This project cannot be built or run in Replit** because:
- Android app development requires Android SDK, Gradle, and specific build tools
- These tools are not available in the Replit environment
- The actual APK compilation happens via **GitHub Actions**

## Project Status

✅ **Project Structure: Complete**

All required files have been created:
- ✅ Gradle configuration files
- ✅ Android Manifest with TV launcher and permissions
- ✅ Kotlin source files (MainActivity, SettingsActivity, helpers)
- ✅ XML layouts optimized for TV
- ✅ Resource files (strings, colors, themes, drawables)
- ✅ GitHub Actions workflow for APK building
- ✅ Documentation (README, .gitignore)

## How to Build the APK

### Step 1: Push to GitHub

```bash
git init
git add .
git commit -m "Initial commit: Smart Player Android TV app"
git remote add origin https://github.com/YOUR_USERNAME/smart-player.git
git push -u origin main
```

### Step 2: GitHub Actions Build

Once pushed to GitHub:
1. GitHub Actions will automatically trigger
2. The workflow will compile the APK using Android SDK
3. Download the APK from:
   - Actions tab → Latest workflow run → Artifacts
   - Releases section (for main branch pushes)

### Step 3: Install on Android TV

1. Transfer the APK to your Android TV/Firestick
2. Enable "Install from Unknown Sources"
3. Install using a file manager
4. Launch from the TV home screen

## Project Structure

```
SmartPlayer/
├── app/
│   ├── src/main/
│   │   ├── java/com/smartplayer/tv/         # Kotlin source files
│   │   ├── res/                              # Resources (layouts, strings, etc.)
│   │   └── AndroidManifest.xml               # App manifest
│   ├── build.gradle.kts                      # App-level Gradle config
│   └── proguard-rules.pro                    # ProGuard rules
├── gradle/                                    # Gradle wrapper
├── .github/workflows/build-apk.yml            # CI/CD for APK building
├── build.gradle.kts                           # Root Gradle config
├── settings.gradle.kts                        # Gradle settings
├── gradle.properties                          # Gradle properties
├── README.md                                  # User documentation
├── .gitignore                                 # Git ignore rules
└── validate_android_project.py                # Structure validation script
```

## Validation Workflow

The `validate-project` workflow in Replit validates that all required files are present and properly structured. This ensures the project is ready for GitHub Actions to build the APK.

Run validation manually:
```bash
python validate_android_project.py
```

## Configuration

### Skip Markers API

Configure the API URL in settings that returns JSON:
```json
{
  "intro": { "start": 0, "end": 90 },
  "credits": { "start": 2500, "end": 2700 }
}
```

Times are in seconds.

### Settings Available

- Trakt API Key
- TMDB API Key
- Skip Marker API URL
- Enable/Disable Skip Intro/Credits
- Default Playback Speed (1.0x - 2.0x)

## Features

### Playback Screen
- Fullscreen ExoPlayer with DPAD navigation
- Real-time display:
  - Elapsed time
  - Remaining time
  - Total duration
  - Current wall-clock time
  - Estimated end time (updates with playback speed changes)

### Skip Functionality
- Auto-skip intro on playback start
- DPAD-focusable "Skip Intro" button during intro range
- DPAD-focusable "Skip Credits" button during credits range
- Buttons hide when outside their active ranges

### Settings
- TV-optimized UI with large text
- All controls are DPAD focusable
- Settings persist across app launches

### External Player Integration
- Accepts video URLs via Android Intent
- Works with Stremio, Syncler+, and similar apps
- Immediate playback with all features active

## Technical Details

- **Language**: Kotlin
- **Min SDK**: API 21 (Android 5.0)
- **Target SDK**: API 34 (Android 14)
- **Dependencies**:
  - AndroidX Media3 (ExoPlayer) 1.2.0
  - AndroidX Leanback 1.0.0
  - OkHttp 4.12.0
  - Gson 2.10.1

## Recent Changes

- **2025-11-01**: Initial project creation with complete Android TV app structure

## User Preferences

None configured yet.
