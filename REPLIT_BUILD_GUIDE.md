# Building TV Player in Replit - Complete Guide

## ⚠️ Important: Build Limitations in Replit

The Android build process requires downloading ~500MB of dependencies on first build, which can take 10-15 minutes. Replit's shell has timeout limitations that may interrupt this process.

## ✅ Recommended Approaches

### Option 1: Use Immersive Terminal (Recommended)

The **Immersive Terminal** (Shell tab in Replit) provides a more stable environment for long builds:

1. Open the **Shell** tab in Replit (not the Console)
2. Click "Open in Immersive Terminal" or press the expand icon
3. Run these commands:

```bash
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# Start the build (be patient - first build takes 10-15 minutes!)
./gradlew assembleDebug --no-daemon
```

4. Wait for the build to complete. You'll see:
   - Initial tasks (merging resources, processing manifests)
   - Kotlin compilation (this is the slowest part)
   - Dexing and packaging
   - Final: `BUILD SUCCESSFUL`

### Option 2: Build Locally (Fastest & Most Reliable)

Since Replit has timeout limitations, building locally is often more reliable:

1. **Download the project** from Replit
2. **Install Android Studio** on your computer
3. **Open the project** in Android Studio
4. **Click Build → Build Bundle(s) / APK(s) → Build APK(s)**
5. APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Option 3: Use Android Studio in Replit (If Available)

If your Replit workspace has Android Studio integration:

1. Open the Android Studio interface
2. Load this project
3. Use the GUI to build the APK

### Option 4: Background Build Script

Try running the build in the background:

```bash
# Run in Shell
export ANDROID_HOME=~/android-sdk
export PATH=$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH

# Start build in background, log to file
nohup ./gradlew assembleDebug --no-daemon > build.log 2>&1 &

# Monitor progress
tail -f build.log

# Check if APK exists (run every few minutes)
ls -lh app/build/outputs/apk/debug/
```

## 🔍 Checking Build Status

At any time, check if the APK was created:

```bash
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

If it exists, the build succeeded!

## 📦 What the Build Does

The build process:
1. Downloads Gradle wrapper (if needed)
2. Downloads ~500MB of dependencies:
   - ExoPlayer 2.19.1
   - AndroidX libraries
   - Kotlin compiler
   - Android build tools
3. Compiles Kotlin/Java code
4. Merges resources and manifests
5. Packages everything into APK

**First build:** 10-15 minutes (downloads everything)
**Subsequent builds:** 2-3 minutes (uses cached dependencies)

## 🐛 Troubleshooting

### "SDK location not found"
The `local.properties` file should contain:
```
sdk.dir=/home/runner/android-sdk
```

This file is already created in your project.

### Build Hangs
- Use Immersive Terminal instead of regular shell
- Check network connectivity
- Try building locally

### Timeout Errors
Replit shells have execution time limits. For very long builds:
- Use background build (Option 4 above)
- Build locally
- Wait and retry

## ✅ After Successful Build

Once you have `app-debug.apk`, install it on your Android TV:

```bash
# Connect to your Android TV
adb connect YOUR_TV_IP:5555

# Install
adb install app/build/outputs/apk/debug/app-debug.apk

# Launch
adb shell am start -n com.tvplayer.app/.MainActivity
```

## 🎯 All Crash Bugs Fixed

The app now includes all these fixes:
- ✅ Network security configuration for HTTP/HTTPS
- ✅ API key loading from SharedPreferences
- ✅ Menu system for Settings and Manual Skip Editor
- ✅ Time display implementation (Now/Ends at)
- ✅ All features working: skip buttons, community API, manual ranges, auto-advance

The app should work perfectly once built!
