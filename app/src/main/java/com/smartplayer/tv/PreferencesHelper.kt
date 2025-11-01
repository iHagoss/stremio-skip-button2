package com.smartplayer.tv

import android.content.Context
import android.content.SharedPreferences

class PreferencesHelper(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "smart_player_prefs"
        private const val KEY_TRAKT_API = "trakt_api_key"
        private const val KEY_TMDB_API = "tmdb_api_key"
        private const val KEY_SKIP_MARKERS_ENABLED = "skip_markers_enabled"
        private const val KEY_DEFAULT_PLAYBACK_SPEED = "default_playback_speed"
        private const val KEY_SKIP_MARKER_API_URL = "skip_marker_api_url"
    }

    var traktApiKey: String
        get() = prefs.getString(KEY_TRAKT_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TRAKT_API, value).apply()

    var tmdbApiKey: String
        get() = prefs.getString(KEY_TMDB_API, "") ?: ""
        set(value) = prefs.edit().putString(KEY_TMDB_API, value).apply()

    var skipMarkersEnabled: Boolean
        get() = prefs.getBoolean(KEY_SKIP_MARKERS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_SKIP_MARKERS_ENABLED, value).apply()

    var defaultPlaybackSpeed: Float
        get() = prefs.getFloat(KEY_DEFAULT_PLAYBACK_SPEED, 1.0f)
        set(value) = prefs.edit().putFloat(KEY_DEFAULT_PLAYBACK_SPEED, value).apply()

    var skipMarkerApiUrl: String
        get() = prefs.getString(KEY_SKIP_MARKER_API_URL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SKIP_MARKER_API_URL, value).apply()
}
