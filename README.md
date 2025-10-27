# TV Player - Android TV App

An Android TV application with ExoPlayer that provides intelligent intro and credits skipping functionality.

## Features

- **Fullscreen Video Playback**: ExoPlayer-based media player optimized for TV
- **Remote API Integration**: Fetches intro/credits timestamps from JSON API
- **Auto-Skip Intro**: Automatically seeks past intro when playback starts
- **Skip Intro Button**: Appears during intro range with D-pad navigation support
- **Skip Credits Button**: Appears during credits range, seeks to video end
- **Android TV Optimized**: LEANBACK launcher, remote control navigation

## API Format

The app expects a JSON response from your API endpoint:

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

All timestamps are in seconds.

## Configuration

Edit `app/src/main/java/com/tvplayer/app/MainActivity.java`:

```java
// Line 28-29
private static final String VIDEO_URL = "YOUR_VIDEO_URL_HERE";
private static final String MARKERS_API_URL = "YOUR_API_ENDPOINT_HERE";
```

## Project Structure

```
TVPlayer/
├── app/
│   ├── src/main/
│   │   ├── java/com/tvplayer/app/
│   │   │   ├── MainActivity.java       # Main player activity
│   │   │   ├── ApiService.java         # HTTP client for API
│   │   │   └── SkipMarkers.java        # Data model
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml   # Fullscreen player UI
│   │   │   └── values/
│   │   │       ├── styles.xml          # Fullscreen theme
│   │   │       └── colors.xml
│   │   └── AndroidManifest.xml         # Permissions & launcher config
│   └── build.gradle                     # Dependencies
├── build.gradle                         # Project config
└── settings.gradle
```

## Building the App

### Prerequisites

1. **Install Android SDK Command Line Tools:**

```bash
# Download command line tools
wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip

# Setup SDK directory
mkdir -p ~/android-sdk/cmdline-tools
unzip commandlinetools-linux-9477386_latest.zip -d ~/android-sdk/cmdline-tools
cd ~/android-sdk/cmdline-tools
mkdir latest
mv cmdline-tools/* latest/ 2>/dev/null || mv bin lib NOTICE.txt source.properties latest/

# Set environment variables
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$PATH
export PATH=$ANDROID_HOME/platform-tools:$PATH

# Accept licenses and install packages
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
```

2. **Verify Java is installed:**

```bash
java -version
```

### Build APK

```bash
# Make gradlew executable
chmod +x gradlew

# Build debug APK
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Install on Device

```bash
# Connect Android TV device via ADB
adb connect YOUR_TV_IP_ADDRESS:5555

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.tvplayer.app/.MainActivity
```

## Dependencies

- **ExoPlayer 2.19.1**: Media playback engine
- **AndroidX Leanback 1.0.0**: TV UI components
- **OkHttp 4.11.0**: HTTP client for API calls
- **Gson 2.10.1**: JSON parsing

## Android Manifest Permissions

- `INTERNET`: Required for API calls and video streaming
- `SYSTEM_ALERT_WINDOW`: For overlay permissions
- `LEANBACK`: Marks app as Android TV compatible

## How It Works

1. **On Startup**: App fetches skip markers from remote API
2. **Auto-Skip**: When playback starts, if current position is in intro range, automatically seeks to intro end
3. **Position Monitoring**: Every 500ms, checks current playback position
4. **Button Visibility**: Shows/hides skip buttons based on current position within marker ranges
5. **Skip Actions**: 
   - Skip Intro: Seeks to `intro.end`
   - Skip Credits: Seeks to video end

## Remote Control Navigation

All buttons are focusable and support D-pad navigation on Firestick/Android TV remotes:
- Navigate with D-pad arrows
- Press OK/Select to activate skip buttons

## Customization

### Change Video Source

Modify `VIDEO_URL` in `MainActivity.java` to point to your video source (HLS, MP4, DASH, etc.)

### Adjust Skip Behavior

In `MainActivity.java`:
- `autoSkipIntro()`: Modify auto-skip logic
- `updateSkipButtonsVisibility()`: Adjust when buttons appear
- Adjust polling interval (currently 500ms)

### Styling

Edit `app/src/main/res/layout/activity_main.xml` to customize button appearance and position.

## Troubleshooting

**Buttons don't appear:**
- Check that API is returning valid JSON with intro/credits timestamps
- Verify timestamps are in seconds (not milliseconds)
- Check Logcat for API errors: `adb logcat | grep TVPlayer`

**Auto-skip not working:**
- Ensure intro start time is 0 or close to beginning
- Check that playback starts before intro end time

**Video won't play:**
- Verify VIDEO_URL is accessible
- Check INTERNET permission is granted
- Ensure video format is supported by ExoPlayer

## License

This is a sample project for educational purposes.
