package edu.illinois.cs.cs124.ay2025.mp.network

import android.os.Build
import com.fasterxml.jackson.core.type.TypeReference
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2025.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.models.Event
import edu.illinois.cs.cs124.ay2025.mp.models.Favorite
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

object Client {
    fun getSummaries(callback: (ResultMightThrow<List<Summary>>) -> Any?) {
        executor.execute {
            try {
                val request = Request.Builder()
                    .url("$SERVER_URL/summary/")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    val responseBody = response.body.string()
                    val summaries: List<Summary> =
                        objectMapper.readValue(responseBody, object : TypeReference<List<Summary>>() {})
                    callback(ResultMightThrow(summaries))
                }
            } catch (e: IOException) {
                callback(ResultMightThrow(e))
            }
        }
    }

    fun getEvent(id: String, callback: (ResultMightThrow<Event>) -> Any?) {
        executor.execute {
            try {
                val request = Request.Builder()
                    .url("$SERVER_URL/event/$id")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    val responseBody = response.body.string()
                    val event: Event = objectMapper.readValue(responseBody, Event::class.java)
                    callback(ResultMightThrow(event))
                }
            } catch (e: IOException) {
                callback(ResultMightThrow(e))
            }
        }
    }

    fun getFavorite(id: String, callback: (ResultMightThrow<Favorite>) -> Any?) {
        executor.execute {
            try {
                val request = Request.Builder()
                    .url("$SERVER_URL/favorite/$id")
                    .get()
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    val responseBody = response.body.string()
                    val favorite: Favorite = objectMapper.readValue(responseBody, Favorite::class.java)
                    callback(ResultMightThrow(favorite))
                }
            } catch (e: IOException) {
                callback(ResultMightThrow(e))
            }
        }
    }

    fun setFavorite(id: String, isFavorite: Boolean, callback: (ResultMightThrow<Favorite>) -> Any?) {
        executor.execute {
            try {
                val favoriteRequest = objectMapper.createObjectNode()
                favoriteRequest.put("id", id)
                favoriteRequest.put("favorite", isFavorite)

                val jsonBody = objectMapper.writeValueAsString(favoriteRequest)
                val requestBody = jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType())

                val request = Request.Builder()
                    .url("$SERVER_URL/favorite/")
                    .post(requestBody)
                    .build()

                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    val responseBody = response.body.string()
                    val favorite: Favorite = objectMapper.readValue(responseBody, Favorite::class.java)
                    callback(ResultMightThrow(favorite))
                }
            } catch (e: IOException) {
                callback(ResultMightThrow(e))
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    private val httpClient: OkHttpClient
    private val executor: ExecutorService

    init {
        val testing = Build.FINGERPRINT == "robolectric"

        httpClient =
            OkHttpClient.Builder()
                .callTimeout(4.seconds.toJavaDuration())
                .retryOnConnectionFailure(true)
                .build()

        executor = if (testing) {
            Executors.newSingleThreadExecutor()
        } else {
            Executors.newCachedThreadPool()
        }
    }
}
