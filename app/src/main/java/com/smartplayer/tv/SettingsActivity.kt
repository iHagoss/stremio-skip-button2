package com.smartplayer.tv

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class SettingsActivity : AppCompatActivity() {
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var traktApiKeyEdit: EditText
    private lateinit var tmdbApiKeyEdit: EditText
    private lateinit var skipMarkerApiUrlEdit: EditText
    private lateinit var enableSkipMarkersSwitch: SwitchCompat
    private lateinit var playbackSpeedSpinner: Spinner
    private lateinit var saveButton: Button
    
    private val speedOptions = listOf(
        "Normal (1.0x)" to 1.0f,
        "1.25x" to 1.25f,
        "1.5x" to 1.5f,
        "1.75x" to 1.75f,
        "2.0x" to 2.0f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        preferencesHelper = PreferencesHelper(this)
        
        initializeViews()
        loadSettings()
    }

    private fun initializeViews() {
        traktApiKeyEdit = findViewById(R.id.trakt_api_key_edit)
        tmdbApiKeyEdit = findViewById(R.id.tmdb_api_key_edit)
        skipMarkerApiUrlEdit = findViewById(R.id.skip_marker_api_url_edit)
        enableSkipMarkersSwitch = findViewById(R.id.enable_skip_markers_switch)
        playbackSpeedSpinner = findViewById(R.id.playback_speed_spinner)
        saveButton = findViewById(R.id.save_settings_button)
        
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            speedOptions.map { it.first }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        playbackSpeedSpinner.adapter = adapter
        
        saveButton.setOnClickListener {
            saveSettings()
        }
    }

    private fun loadSettings() {
        traktApiKeyEdit.setText(preferencesHelper.traktApiKey)
        tmdbApiKeyEdit.setText(preferencesHelper.tmdbApiKey)
        skipMarkerApiUrlEdit.setText(preferencesHelper.skipMarkerApiUrl)
        enableSkipMarkersSwitch.isChecked = preferencesHelper.skipMarkersEnabled
        
        val currentSpeed = preferencesHelper.defaultPlaybackSpeed
        val speedIndex = speedOptions.indexOfFirst { it.second == currentSpeed }
        if (speedIndex >= 0) {
            playbackSpeedSpinner.setSelection(speedIndex)
        }
    }

    private fun saveSettings() {
        preferencesHelper.traktApiKey = traktApiKeyEdit.text.toString()
        preferencesHelper.tmdbApiKey = tmdbApiKeyEdit.text.toString()
        preferencesHelper.skipMarkerApiUrl = skipMarkerApiUrlEdit.text.toString()
        preferencesHelper.skipMarkersEnabled = enableSkipMarkersSwitch.isChecked
        
        val selectedIndex = playbackSpeedSpinner.selectedItemPosition
        preferencesHelper.defaultPlaybackSpeed = speedOptions[selectedIndex].second
        
        Toast.makeText(this, "Settings saved successfully", Toast.LENGTH_SHORT).show()
        finish()
    }
}
