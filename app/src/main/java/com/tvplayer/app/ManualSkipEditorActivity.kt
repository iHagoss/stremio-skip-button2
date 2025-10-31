package com.tvplayer.app

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class ManualSkipEditorActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var addButton: Button
    private lateinit var prefs: SharedPreferences
    private lateinit var adapter: ArrayAdapter<String>
    private val skipRanges = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual_skip_editor)

        prefs = getSharedPreferences("tvplayer_prefs", MODE_PRIVATE)

        listView = findViewById(R.id.skipListView)
        addButton = findViewById(R.id.addSkipButton)

        loadRanges()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, skipRanges)
        listView.adapter = adapter

        addButton.setOnClickListener {
            // Example: add a dummy range, you can replace with a dialog for custom input
            val newRange = JSONObject()
            newRange.put("start", 0.0)
            newRange.put("end", 10.0)
            newRange.put("type", "cold_open")

            skipRanges.add("Start: 0s, End: 10s, Type: cold_open")
            saveRange(newRange)
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemLongClickListener { _, _, position, _ ->
            skipRanges.removeAt(position)
            saveAllRanges()
            adapter.notifyDataSetChanged()
            true
        }
    }

    private fun loadRanges() {
        val json = prefs.getString("MANUAL_SKIP_RANGES", "[]")
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            skipRanges.add("Start: ${obj.getDouble("start")}s, End: ${obj.getDouble("end")}s, Type: ${obj.getString("type")}")
        }
    }

    private fun saveRange(range: JSONObject) {
        val arr = JSONArray(prefs.getString("MANUAL_SKIP_RANGES", "[]"))
        arr.put(range)
        prefs.edit().putString("MANUAL_SKIP_RANGES", arr.toString()).apply()
    }

    private fun saveAllRanges() {
        val arr = JSONArray()
        for (item in skipRanges) {
            // crude parse back, ideally store structured objects
            val parts = item.split(", ")
            val start = parts[0].substringAfter("Start: ").substringBefore("s").toDouble()
            val end = parts[1].substringAfter("End: ").substringBefore("s").toDouble()
            val type = parts[2].substringAfter("Type: ")
            val obj = JSONObject()
            obj.put("start", start)
            obj.put("end", end)
            obj.put("type", type)
            arr.put(obj)
        }
        prefs.edit().putString("MANUAL_SKIP_RANGES", arr.toString()).apply()
    }
}
