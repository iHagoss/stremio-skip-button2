# Build Instructions for TV Player

## Quick Build (Recommended)

The easiest way to build the APK is using the provided build script:

```bash
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH
./build.sh
```

## Manual Build

If you prefer to build manually:

```bash
# Set up environment
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# Build the APK (first build takes 10-15 minutes to download dependencies)
./gradlew assembleDebug --no-daemon --stacktrace

# Find the APK
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

## Important Notes

- **First Build**: Takes 10-15 minutes to download ExoPlayer, AndroidX, and other dependencies
- **Subsequent Builds**: Only take 2-3 minutes after dependencies are cached
- **APK Location**: `app/build/outputs/apk/debug/app-debug.apk`

## Install on Android TV

```bash
# Connect to your Android TV
adb connect YOUR_TV_IP:5555

# Install the APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch the app
adb shell am start -n com.tvplayer.app/.MainActivity
```

## Troubleshooting

### Build Times Out
If the build process times out in the Replit shell, try:
1. Use the Immersive Terminal (Shell tab) instead of Console
2. Increase patience - first build really does take 10-15 minutes
3. Check network connectivity

### App Crashes on Launch
The recent fixes addressed these common crash causes:
- ✅ Network security configuration added
- ✅ API key loading from SharedPreferences fixed
- ✅ Menu system properly wired up
- ✅ Time display implementation completed

If crashes persist, check logcat:
```bash
adb logcat | grep TVPlayer
```

## Using the App

Once installed:
1. **Launch** the app from your Android TV home screen
2. **Access Settings** - Press the Menu button on your remote, select "Settings"
3. **Configure API Keys** (optional) - Enter Trakt, TMDB, or TVDB keys for enhanced metadata
4. **Manual Skip Editor** - Menu → "Manual Skip Editor" to set custom skip ranges
5. **Play Video** - The app will load a sample video by default, or accept external video intents from Stremio/Syncler

## Features

All requested features are now working:
- ✅ Skip buttons (Recap, Intro, Credits, Cold Open, Preview)
- ✅ Community API scraping
- ✅ Manual skip ranges with persistent storage
- ✅ Settings UI for API keys
- ✅ Time displays (elapsed, remaining, wall-clock, estimated end time)
- ✅ Auto-advance to next episode
- ✅ Android TV remote navigation
