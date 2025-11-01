#!/usr/bin/env python3
"""
Android Project Structure Validator
Validates that all required files for the Smart Player Android TV app are present.
"""

import os
import sys
from pathlib import Path

def check_file_exists(filepath, description):
    """Check if a file exists and print status."""
    if os.path.exists(filepath):
        print(f"✓ {description}: {filepath}")
        return True
    else:
        print(f"✗ MISSING {description}: {filepath}")
        return False

def validate_project():
    """Validate the Android project structure."""
    print("=" * 60)
    print("Smart Player Android TV App - Project Structure Validation")
    print("=" * 60)
    print()
    
    all_files_present = True
    
    print("Checking Gradle Configuration Files:")
    print("-" * 60)
    gradle_files = [
        ("settings.gradle.kts", "Gradle settings"),
        ("build.gradle.kts", "Root build config"),
        ("gradle.properties", "Gradle properties"),
        ("app/build.gradle.kts", "App build config"),
        ("gradle/wrapper/gradle-wrapper.properties", "Gradle wrapper"),
    ]
    for filepath, desc in gradle_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("Checking Android Manifest and Resources:")
    print("-" * 60)
    android_files = [
        ("app/src/main/AndroidManifest.xml", "Android Manifest"),
        ("app/src/main/res/values/strings.xml", "Strings resources"),
        ("app/src/main/res/values/colors.xml", "Colors resources"),
        ("app/src/main/res/values/themes.xml", "Themes"),
    ]
    for filepath, desc in android_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("Checking Layout Files:")
    print("-" * 60)
    layout_files = [
        ("app/src/main/res/layout/activity_main.xml", "Main activity layout"),
        ("app/src/main/res/layout/activity_settings.xml", "Settings activity layout"),
        ("app/src/main/res/layout/custom_player_controls.xml", "Custom player controls"),
    ]
    for filepath, desc in layout_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("Checking Kotlin Source Files:")
    print("-" * 60)
    kotlin_files = [
        ("app/src/main/java/com/smartplayer/tv/MainActivity.kt", "MainActivity"),
        ("app/src/main/java/com/smartplayer/tv/SettingsActivity.kt", "SettingsActivity"),
        ("app/src/main/java/com/smartplayer/tv/PlayerManager.kt", "PlayerManager"),
        ("app/src/main/java/com/smartplayer/tv/SkipMarkerManager.kt", "SkipMarkerManager"),
        ("app/src/main/java/com/smartplayer/tv/PreferencesHelper.kt", "PreferencesHelper"),
    ]
    for filepath, desc in kotlin_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("Checking Drawable Resources:")
    print("-" * 60)
    drawable_files = [
        ("app/src/main/res/drawable/button_selector.xml", "Button selector"),
        ("app/src/main/res/drawable/edit_text_background.xml", "EditText background"),
        ("app/src/main/res/drawable/spinner_background.xml", "Spinner background"),
        ("app/src/main/res/drawable/app_banner.xml", "App banner"),
    ]
    for filepath, desc in drawable_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("Checking CI/CD and Documentation:")
    print("-" * 60)
    meta_files = [
        (".github/workflows/build-apk.yml", "GitHub Actions workflow"),
        ("README.md", "README documentation"),
        (".gitignore", "Git ignore file"),
    ]
    for filepath, desc in meta_files:
        all_files_present &= check_file_exists(filepath, desc)
    
    print()
    print("=" * 60)
    if all_files_present:
        print("✓ SUCCESS: All required files are present!")
        print()
        print("Project is ready for GitHub Actions to build the APK.")
        print()
        print("Next steps:")
        print("1. Push this project to a GitHub repository")
        print("2. GitHub Actions will automatically build the APK")
        print("3. Download the APK from Actions artifacts or Releases")
        print("=" * 60)
        return 0
    else:
        print("✗ FAILURE: Some required files are missing!")
        print("=" * 60)
        return 1

if __name__ == "__main__":
    sys.exit(validate_project())
