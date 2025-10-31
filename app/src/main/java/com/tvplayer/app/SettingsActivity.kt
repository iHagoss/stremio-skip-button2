package com.tvplayer.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val traktInput = findViewById<EditText>(R.id.traktApiKeyInput)
        val tmdbInput = findViewById<EditText>(R.id.tmdbApiKeyInput)
        val tvdbInput = findViewById<EditText>(R.id.tvdbApiKeyInput)
        val logicSpinner = findViewById<Spinner>(R.id.skipLogicSpinner)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Use MODE_PRIVATE constant and consistent preference name
        val prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)

        // Load saved values with consistent keys
        traktInput.setText(prefs.getString("TRAKT_API_KEY", ""))
        tmdbInput.setText(prefs.getString("TMDB_API_KEY", ""))
        tvdbInput.setText(prefs.getString("TVDB_API_KEY", ""))

        val logic = prefs.getString("SKIP_LOGIC", "Defaults")
        val options = resources.getStringArray(R.array.skip_logic_options)
        val index = options.indexOf(logic)
        if (index >= 0) {
            logicSpinner.setSelection(index)
        }

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("TRAKT_API_KEY", traktInput.text.toString())
                .putString("TMDB_API_KEY", tmdbInput.text.toString())
                .putString("TVDB_API_KEY", tvdbInput.text.toString())
                .putString("SKIP_LOGIC", logicSpinner.selectedItem.toString())
                .apply()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
