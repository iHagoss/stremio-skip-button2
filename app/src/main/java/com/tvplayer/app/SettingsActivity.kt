package com.tvplayer.app

import android.content.SharedPreferences
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

        val prefs = getSharedPreferences("tvplayerprefs", MODEPRIVATE)

        // Load saved values
        traktInput.setText(prefs.getString("TRAKTAPIKEY", ""))
        tmdbInput.setText(prefs.getString("TMDBAPIKEY", ""))
        tvdbInput.setText(prefs.getString("TVDBAPIKEY", ""))
        val logic = prefs.getString("SKIP_LOGIC", "Defaults")
        logicSpinner.setSelection(resources.getStringArray(R.array.skiplogicoptions).indexOf(logic))

        saveButton.setOnClickListener {
            prefs.edit()
                .putString("TRAKTAPIKEY", traktInput.text.toString())
                .putString("TMDBAPIKEY", tmdbInput.text.toString())
                .putString("TVDBAPIKEY", tvdbInput.text.toString())
                .putString("SKIP_LOGIC", logicSpinner.selectedItem.toString())
                .apply()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
