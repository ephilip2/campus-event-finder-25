package edu.illinois.cs.cs124.ay2025.mp.network

import android.os.Build
import com.fasterxml.jackson.core.type.TypeReference
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2025.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import okhttp3.OkHttpClient
import okhttp3.Request
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
