package com.tvplayer.app.skip

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

class SkipRangeManager(private val metadataProvider: MetadataProviderChain? = null) {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "SkipRangeManager"
        private const val BASE_URL = "https://busy-jacinta-shugi-c2885b2e.koyeb.app"
    }
    
    data class SkipRange(
        @SerializedName("start")
        val start: Double,
        
        @SerializedName("end")
        val end: Double,
        
        @SerializedName("type")
        val type: String
    )
    
    data class SkipRangesResponse(
        @SerializedName("ranges")
        val ranges: List<SkipRange>?
    )
    
    interface SkipRangesCallback {
        fun onSuccess(ranges: List<SkipRange>)
        fun onError(error: Exception)
    }
    
    fun fetchSkipRanges(episodeId: String, fileHash: String, callback: SkipRangesCallback) {
        val url = "$BASE_URL/ranges/$episodeId?fileId=$fileHash"
        
        Log.d(TAG, "Fetching skip ranges from: $url")
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch skip ranges", e)
                mainHandler.post {
                    callback.onError(e)
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                try {
                    if (!response.isSuccessful) {
                        throw IOException("Unexpected response code: ${response.code}")
                    }
                    
                    val responseBody = response.body?.string()
                    if (responseBody.isNullOrEmpty()) {
                        throw IOException("Empty response body")
                    }
                    
                    Log.d(TAG, "Received response: $responseBody")
                    
                    val rangesResponse = gson.fromJson(responseBody, SkipRangesResponse::class.java)
                    val ranges = rangesResponse.ranges ?: emptyList()
                    
                    Log.d(TAG, "Parsed ${ranges.size} skip ranges")
                    
                    mainHandler.post {
                        callback.onSuccess(ranges)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing skip ranges", e)
                    mainHandler.post {
                        callback.onError(e)
                    }
                }
            }
        })
    }
    
    fun getRangeLabel(type: String): String {
        return when (type.lowercase()) {
            "cold_open" -> "Skip Cold Open"
            "credits" -> "Skip Credits"
            "credits_end" -> "Skip End Credits"
            "intro" -> "Skip Intro"
            "recap" -> "Skip Recap"
            "preview" -> "Skip Preview"
            else -> "Skip"
        }
    }
    
    fun isInRange(currentPositionSeconds: Double, range: SkipRange): Boolean {
        return currentPositionSeconds >= range.start && currentPositionSeconds < range.end
    }
    
    fun findActiveRange(currentPositionSeconds: Double, ranges: List<SkipRange>): SkipRange? {
        return ranges.firstOrNull { range ->
            isInRange(currentPositionSeconds, range)
        }
    }
    
    fun fetchSkipRangesWithMetadata(episodeId: String, fileHash: String, callback: SkipRangesCallback) {
        if (metadataProvider == null) {
            Log.d(TAG, "No metadata provider configured, using community ranges only")
            fetchSkipRanges(episodeId, fileHash, callback)
            return
        }
        
        Log.d(TAG, "Fetching metadata for validation")
        metadataProvider.fetchMetadata(episodeId, fileHash, object : MetadataProvider.MetadataCallback {
            override fun onSuccess(metadata: EpisodeMetadata) {
                Log.d(TAG, "Got metadata: runtime=${metadata.runtimeSec}s, traktId=${metadata.traktId}, tmdbId=${metadata.tmdbId}")
                
                fetchSkipRanges(episodeId, fileHash, object : SkipRangesCallback {
                    override fun onSuccess(ranges: List<SkipRange>) {
                        val validatedRanges = validateRanges(ranges, metadata.runtimeSec)
                        Log.d(TAG, "Validated ${validatedRanges.size}/${ranges.size} ranges against runtime")
                        callback.onSuccess(validatedRanges)
                    }
                    
                    override fun onError(error: Exception) {
                        callback.onError(error)
                    }
                })
            }
            
            override fun onError(error: Exception) {
                Log.d(TAG, "Metadata fetch failed, continuing with unvalidated ranges: ${error.message}")
                fetchSkipRanges(episodeId, fileHash, callback)
            }
        })
    }
    
    private fun validateRanges(ranges: List<SkipRange>, runtimeSec: Int): List<SkipRange> {
        if (runtimeSec <= 0) {
            Log.d(TAG, "Runtime unknown or zero, skipping validation")
            return ranges
        }
        
        return ranges.filter { range ->
            val isValid = range.start >= 0 && range.end <= runtimeSec && range.start < range.end
            if (!isValid) {
                Log.w(TAG, "Invalid range filtered out: ${range.type} (${range.start}s - ${range.end}s) exceeds runtime ${runtimeSec}s")
            }
            isValid
        }
    }
    
    fun detectRangesLocally(filePath: String): List<SkipRange> {
        Log.d(TAG, "detectRangesLocally() called for: $filePath")
        Log.d(TAG, "Local skip range detection not yet implemented")
        return emptyList()
    }
}
