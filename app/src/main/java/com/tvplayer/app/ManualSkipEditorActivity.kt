package com.tvplayer.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class ManualSkipEditorActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var addButton: Button
    private lateinit var prefs: android.content.SharedPreferences
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
            showAddRangeDialog()
        }

        // Tap to edit
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = skipRanges[position]
            showEditRangeDialog(item, position)
        }

        // Long press to delete
        listView.setOnItemLongClickListener { _, _, position, _ ->
            skipRanges.removeAt(position)
            saveAllRanges()
            adapter.notifyDataSetChanged()
            true
        }
    }

    private fun showAddRangeDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_skip_range, null)
        val startInput = dialogView.findViewById<EditText>(R.id.startInput)
        val endInput = dialogView.findViewById<EditText>(R.id.endInput)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.typeSpinner)

        // Populate spinner with predefined skip types
        val types = resources.getStringArray(R.array.skip_type_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        typeSpinner.adapter = spinnerAdapter

        AlertDialog.Builder(this)
            .setTitle("Add Skip Range")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val start = startInput.text.toString().toDoubleOrNull() ?: 0.0
                val end = endInput.text.toString().toDoubleOrNull() ?: 0.0
                val type = typeSpinner.selectedItem.toString()

                if (end <= start) {
                    Toast.makeText(this, "End time must be greater than start time", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val newRange = JSONObject().apply {
                    put("start", start)
                    put("end", end)
                    put("type", type)
                }

                skipRanges.add("Start: ${start}s, End: ${end}s, Type: $type")
                sortRanges()
                saveAllRanges()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditRangeDialog(item: String, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_skip_range, null)
        val startInput = dialogView.findViewById<EditText>(R.id.startInput)
        val endInput = dialogView.findViewById<EditText>(R.id.endInput)
        val typeSpinner = dialogView.findViewById<Spinner>(R.id.typeSpinner)

        val types = resources.getStringArray(R.array.skip_type_options)
        val spinnerAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, types).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        typeSpinner.adapter = spinnerAdapter

        // Pre-fill with existing values
        try {
            val parts = item.split(", ")
            val start = parts[0].substringAfter("Start: ").substringBefore("s").toDouble()
            val end = parts[1].substringAfter("End: ").substringBefore("s").toDouble()
            val type = parts[2].substringAfter("Type: ")

            startInput.setText(start.toString())
            endInput.setText(end.toString())
            val index = types.indexOf(type)
            if (index >= 0) typeSpinner.setSelection(index)
        } catch (_: Exception) { }

        AlertDialog.Builder(this)
            .setTitle("Edit Skip Range")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newStart = startInput.text.toString().toDoubleOrNull() ?: 0.0
                val newEnd = endInput.text.toString().toDoubleOrNull() ?: 0.0
                val newType = typeSpinner.selectedItem.toString()

                if (newEnd <= newStart) {
                    Toast.makeText(this, "End time must be greater than start time", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                skipRanges[position] = "Start: ${newStart}s, End: ${newEnd}s, Type: $newType"
                sortRanges()
                saveAllRanges()
                adapter.notifyDataSetChanged()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun loadRanges() {
        val json = prefs.getString("MANUAL_SKIP_RANGES", "[]")
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val start = obj.optDouble("start", 0.0)
            val end = obj.optDouble("end", 0.0)
            val type = obj.optString("type", "unknown")
            skipRanges.add("Start: ${start}s, End: ${end}s, Type: $type")
        }
        sortRanges()
    }

    private fun saveAllRanges() {
        val arr = JSONArray()
        skipRanges.forEach { item ->
            try {
                val parts = item.split(", ")
                val start = parts[0].substringAfter("Start: ").substringBefore("s").toDouble()
                val end = parts[1].substringAfter("End: ").substringBefore("s").toDouble()
                val type = parts[2].substringAfter("Type: ")
                val obj = JSONObject().apply {
                    put("start", start)
                    put("end", end)
                    put("type", type)
                }
                arr.put(obj)
            } catch (_: Exception) { }
        }
        prefs.edit().putString("MANUAL_SKIP_RANGES", arr.toString()).apply()
    }

    private fun sortRanges() {
        skipRanges.sortBy {
            it.substringAfter("Start: ").substringBefore("s").toDoubleOrNull() ?: 0.0
        }
    }
}
