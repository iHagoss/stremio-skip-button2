package com.smartplayer.tv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class SkipMarker(
    @SerializedName("start") val start: Long,
    @SerializedName("end") val end: Long
)

data class SkipMarkers(
    @SerializedName("intro") val intro: SkipMarker?,
    @SerializedName("credits") val credits: SkipMarker?
)

class SkipMarkerManager(private val apiUrl: String) {
    private val client = OkHttpClient()
    private val gson = Gson()

    suspend fun fetchSkipMarkers(): SkipMarkers? {
        if (apiUrl.isEmpty()) {
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(apiUrl)
                    .build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val json = response.body?.string()
                    json?.let { gson.fromJson(it, SkipMarkers::class.java) }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun isInIntroRange(positionMs: Long, markers: SkipMarkers?): Boolean {
        markers?.intro?.let {
            val positionSec = positionMs / 1000
            return positionSec >= it.start && positionSec <= it.end
        }
        return false
    }

    fun isInCreditsRange(positionMs: Long, markers: SkipMarkers?): Boolean {
        markers?.credits?.let {
            val positionSec = positionMs / 1000
            return positionSec >= it.start && positionSec <= it.end
        }
        return false
    }

    fun getIntroEndPosition(markers: SkipMarkers?): Long? {
        return markers?.intro?.end?.times(1000)
    }

    fun getCreditsEndPosition(markers: SkipMarkers?, durationMs: Long): Long? {
        return markers?.credits?.end?.times(1000) ?: durationMs
    }
}
