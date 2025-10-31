package com.tvplayer.app

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class ManualSkipEditorActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var addButton: Button
    private val skipRanges = mutableListOf<String>() // simple string list for demo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activitymanualskip_editor)

        listView = findViewById(R.id.skipListView)
        addButton = findViewById(R.id.addSkipButton)

        val adapter = ArrayAdapter(this, android.R.layout.simplelistitem_1, skipRanges)
        listView.adapter = adapter

        addButton.setOnClickListener {
            // For now, just add a dummy skip range
            skipRanges.add("Start: 0s, End: 10s, Type: cold_open")
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemLongClickListener { , , position, _ ->
            skipRanges.removeAt(position)
            adapter.notifyDataSetChanged()
            true
        }
    }
}
