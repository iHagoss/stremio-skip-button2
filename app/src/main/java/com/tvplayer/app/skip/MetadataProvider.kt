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

data class EpisodeMetadata(
    val runtimeSec: Int,
    val traktId: String? = null,
    val tmdbId: String? = null,
    val imdbId: String? = null,
    val tvdbId: String? = null
)

interface MetadataProvider {
    fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataCallback)
    
    interface MetadataCallback {
        fun onSuccess(metadata: EpisodeMetadata)
        fun onError(error: Exception)
    }
}

class TraktProvider(private val apiKey: String?) : MetadataProvider {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "TraktProvider"
        private const val BASE_URL = "https://api.trakt.tv"
        private const val CLIENT_ID_HEADER = "trakt-api-key"
        private const val API_VERSION = "2"
    }
    
    data class TraktEpisodeResponse(
        @SerializedName("season")
        val season: Int,
        
        @SerializedName("number")
        val number: Int,
        
        @SerializedName("title")
        val title: String,
        
        @SerializedName("ids")
        val ids: TraktIds,
        
        @SerializedName("runtime")
        val runtime: Int
    )
    
    data class TraktIds(
        @SerializedName("trakt")
        val trakt: Long,
        
        @SerializedName("tvdb")
        val tvdb: Long?,
        
        @SerializedName("imdb")
        val imdb: String?,
        
        @SerializedName("tmdb")
        val tmdb: Long?
    )
    
    override fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        if (apiKey.isNullOrEmpty()) {
            Log.d(TAG, "No Trakt API key configured, skipping")
            mainHandler.post {
                callback.onError(Exception("No Trakt API key configured"))
            }
            return
        }
        
        val parts = parseEpisodeId(episodeId)
        if (parts == null) {
            Log.e(TAG, "Invalid episodeId format: $episodeId")
            mainHandler.post {
                callback.onError(Exception("Invalid episodeId format"))
            }
            return
        }
        
        val (showId, season, episode) = parts
        val url = "$BASE_URL/shows/$showId/seasons/$season/episodes/$episode?extended=full"
        
        Log.d(TAG, "Fetching Trakt metadata from: $url")
        
        val request = Request.Builder()
            .url(url)
            .header(CLIENT_ID_HEADER, apiKey)
            .header("trakt-api-version", API_VERSION)
            .header("Content-Type", "application/json")
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch Trakt metadata", e)
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
                    
                    Log.d(TAG, "Received Trakt response")
                    
                    val traktResponse = gson.fromJson(responseBody, TraktEpisodeResponse::class.java)
                    
                    val metadata = EpisodeMetadata(
                        runtimeSec = traktResponse.runtime * 60,
                        traktId = traktResponse.ids.trakt.toString(),
                        tmdbId = traktResponse.ids.tmdb?.toString(),
                        imdbId = traktResponse.ids.imdb,
                        tvdbId = traktResponse.ids.tvdb?.toString()
                    )
                    
                    Log.d(TAG, "Parsed Trakt metadata: runtime=${metadata.runtimeSec}s")
                    
                    mainHandler.post {
                        callback.onSuccess(metadata)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing Trakt metadata", e)
                    mainHandler.post {
                        callback.onError(e)
                    }
                }
            }
        })
    }
    
    private fun parseEpisodeId(episodeId: String): Triple<String, Int, Int>? {
        return try {
            val parts = episodeId.split("-")
            if (parts.size < 3) {
                return null
            }
            
            val seasonStr = parts[parts.size - 2]
            val episodeStr = parts[parts.size - 1]
            val season = seasonStr.toInt()
            val episode = episodeStr.toInt()
            
            val showSlug = parts.dropLast(2).joinToString("-")
            
            Log.d(TAG, "Parsed episodeId '$episodeId' -> show: '$showSlug', season: $season, episode: $episode")
            Triple(showSlug, season, episode)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse episodeId: $episodeId", e)
            null
        }
    }
}

class TMDbProvider(private val apiKey: String?) : MetadataProvider {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    
    companion object {
        private const val TAG = "TMDbProvider"
        private const val BASE_URL = "https://api.themoviedb.org/3"
    }
    
    data class TMDbEpisodeResponse(
        @SerializedName("air_date")
        val airDate: String?,
        
        @SerializedName("episode_number")
        val episodeNumber: Int,
        
        @SerializedName("id")
        val id: Long,
        
        @SerializedName("name")
        val name: String,
        
        @SerializedName("season_number")
        val seasonNumber: Int,
        
        @SerializedName("runtime")
        val runtime: Int?,
        
        @SerializedName("show_id")
        val showId: Long?
    )
    
    override fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        if (apiKey.isNullOrEmpty()) {
            Log.d(TAG, "No TMDb API key configured, skipping")
            mainHandler.post {
                callback.onError(Exception("No TMDb API key configured"))
            }
            return
        }
        
        val parts = parseEpisodeId(episodeId)
        if (parts == null) {
            Log.e(TAG, "Invalid episodeId format: $episodeId")
            mainHandler.post {
                callback.onError(Exception("Invalid episodeId format"))
            }
            return
        }
        
        val (tvId, seasonNumber, episodeNumber) = parts
        val url = "$BASE_URL/tv/$tvId/season/$seasonNumber/episode/$episodeNumber?api_key=$apiKey&language=en-US"
        
        Log.d(TAG, "Fetching TMDb metadata from: $url")
        
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to fetch TMDb metadata", e)
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
                    
                    Log.d(TAG, "Received TMDb response")
                    
                    val tmdbResponse = gson.fromJson(responseBody, TMDbEpisodeResponse::class.java)
                    
                    val runtimeSec = (tmdbResponse.runtime ?: 0) * 60
                    
                    val metadata = EpisodeMetadata(
                        runtimeSec = runtimeSec,
                        tmdbId = tmdbResponse.id.toString()
                    )
                    
                    Log.d(TAG, "Parsed TMDb metadata: runtime=${metadata.runtimeSec}s")
                    
                    mainHandler.post {
                        callback.onSuccess(metadata)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing TMDb metadata", e)
                    mainHandler.post {
                        callback.onError(e)
                    }
                }
            }
        })
    }
    
    private fun parseEpisodeId(episodeId: String): Triple<String, Int, Int>? {
        return try {
            val parts = episodeId.split("-")
            if (parts.size < 3) {
                return null
            }
            
            val seasonStr = parts[parts.size - 2]
            val episodeStr = parts[parts.size - 1]
            val season = seasonStr.toInt()
            val episode = episodeStr.toInt()
            
            val tvId = parts.dropLast(2).joinToString("-")
            
            Log.d(TAG, "Parsed episodeId '$episodeId' -> tvId: '$tvId', season: $season, episode: $episode")
            Triple(tvId, season, episode)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse episodeId: $episodeId", e)
            null
        }
    }
}

class TVDBProvider(private val apiKey: String?) : MetadataProvider {
    
    companion object {
        private const val TAG = "TVDBProvider"
    }
    
    override fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        Log.d(TAG, "TVDB provider stub - not yet implemented")
        callback.onError(Exception("TVDB provider not yet implemented"))
    }
}

class IMDbProvider : MetadataProvider {
    
    companion object {
        private const val TAG = "IMDbProvider"
    }
    
    override fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        Log.d(TAG, "IMDb provider stub - enrichment only, no direct API")
        callback.onError(Exception("IMDb provider is enrichment only"))
    }
}

class MetadataProviderChain(private val providers: List<MetadataProvider>) {
    
    companion object {
        private const val TAG = "MetadataProviderChain"
    }
    
    fun fetchMetadata(episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        fetchFromNextProvider(0, episodeId, fileId, callback)
    }
    
    private fun fetchFromNextProvider(index: Int, episodeId: String, fileId: String, callback: MetadataProvider.MetadataCallback) {
        if (index >= providers.size) {
            Log.d(TAG, "All metadata providers failed or not configured")
            callback.onError(Exception("No metadata providers available"))
            return
        }
        
        val provider = providers[index]
        Log.d(TAG, "Trying provider ${index + 1}/${providers.size}: ${provider::class.simpleName}")
        
        provider.fetchMetadata(episodeId, fileId, object : MetadataProvider.MetadataCallback {
            override fun onSuccess(metadata: EpisodeMetadata) {
                Log.d(TAG, "Provider ${provider::class.simpleName} succeeded")
                callback.onSuccess(metadata)
            }
            
            override fun onError(error: Exception) {
                Log.d(TAG, "Provider ${provider::class.simpleName} failed: ${error.message}")
                fetchFromNextProvider(index + 1, episodeId, fileId, callback)
            }
        })
    }
}
