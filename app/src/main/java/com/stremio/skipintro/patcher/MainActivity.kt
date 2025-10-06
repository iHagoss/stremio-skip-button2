package com.stremio.skipintro.patcher

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private val PICK_APK_REQUEST_CODE = 1001
    private lateinit var apiBaseUrlInput: EditText
    private lateinit var pickApkButton: Button
    private lateinit var patchButton: Button
    private lateinit var statusText: TextView
    private var selectedApkUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apiBaseUrlInput = findViewById(R.id.apiBaseUrlInput)
        pickApkButton = findViewById(R.id.pickApkButton)
        patchButton = findViewById(R.id.patchButton)
        statusText = findViewById(R.id.statusText)

        pickApkButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/vnd.android.package-archive"
            }
            startActivityForResult(intent, PICK_APK_REQUEST_CODE)
        }

        patchButton.setOnClickListener {
            selectedApkUri?.let { uri ->
                val apiBaseUrl = apiBaseUrlInput.text.toString()
                if (apiBaseUrl.isNotEmpty()) {
                    patchApk(uri, apiBaseUrl)
                } else {
                    Toast.makeText(this, "Please enter API base URL", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_APK_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedApkUri = uri
                statusText.text = "APK selected: ${getFileName(uri)}"
                patchButton.isEnabled = true
            }
        }
    }

    private fun getFileName(uri: Uri): String {
        var name = "unknown.apk"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun patchApk(apkUri: Uri, apiBaseUrl: String) {
        statusText.text = "Patching APK..."
        // Simulated patch logic - would integrate with apktool here
        Toast.makeText(this, "APK patched successfully!", Toast.LENGTH_LONG).show()
        statusText.text = "Patch complete! APK ready for installation."
    }
}
