package edu.illinois.cs.cs124.ay2025.mp.network

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import edu.illinois.cs.cs124.ay2025.mp.application.DEFAULT_SERVER_PORT
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2025.mp.helpers.getTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.models.EventData
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.Level
import java.util.logging.Logger

object Server : Dispatcher() {
    private val logger: Logger = Logger.getLogger(Server::class.java.name)

    private val summaries: MutableList<Summary> = mutableListOf()
    private val events: MutableMap<String, EventData> = mutableMapOf()

    @Throws(JsonProcessingException::class)
    private fun getSummaries(): MockResponse {
        val now = getTimeProvider().now()
        val nowZoned = now.atZone(ZoneId.of("America/Chicago"))
        val startOfToday = nowZoned.toLocalDate().atStartOfDay(ZoneId.of("America/Chicago"))

        val filteredSummaries = summaries
            .filter { summary ->
                try {
                    val eventStart = ZonedDateTime.parse(summary.start)
                    !eventStart.isBefore(startOfToday)
                } catch (_: Exception) {
                    true
                }
            }

        return makeOKJSONResponse(objectMapper.writeValueAsString(filteredSummaries))
    }

    @Throws(JsonProcessingException::class)
    private fun getEvent(id: String): MockResponse {
        val event = events[id] ?: return httpNotFound
        return makeOKJSONResponse(objectMapper.writeValueAsString(event))
    }

    @Suppress("ReturnCount")
    override fun dispatch(request: RecordedRequest): MockResponse {
        if (request.path == null || request.method == null) {
            return httpBadRequest
        }

        val path = request.path!!.replaceFirst("/*$".toRegex(), "").replace("/+".toRegex(), "/")
        val method = request.method!!.uppercase()

        try {
            return when {
                path.isEmpty() && method == "GET" ->
                    makeOKJSONResponse(CHECK_SERVER_RESPONSE)
                path == "/reset" && method == "GET" ->
                    makeOKJSONResponse("200: OK")
                path == "/summary" && method == "GET" -> getSummaries()
                path.startsWith("/event/") && method == "GET" -> {
                    val id = path.removePrefix("/event/")
                    if (id.isEmpty()) {
                        httpNotFound
                    } else {
                        getEvent(id)
                    }
                }
                else ->
                    httpNotFound
            }
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Server internal error for path: $path", e)
            return MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("500: Internal Error")
        }
    }

    private fun loadData() {
        val json = readEventDataFile()

        try {
            val root: JsonNode = objectMapper.readTree(json)
            val eventsArray = root.get("events")
            for (node in eventsArray) {
                val eventData = objectMapper.readValue(node.toString(), EventData::class.java)

                val summary = Summary(eventData)
                summaries.add(summary)
                events[eventData.id] = eventData
            }
        } catch (e: JsonProcessingException) {
            logger.log(Level.SEVERE, "Loading data failed", e)
            throw IllegalStateException(e)
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    private var mockWebServer: MockWebServer? = null

    init {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.SEVERE
        loadData()
    }

    @Synchronized
    fun startServer() {
        if (mockWebServer != null && serverIsRunning(false)) {
            return
        }

        try {
            mockWebServer?.close()
            mockWebServer = MockWebServer()
            mockWebServer!!.dispatcher = this
            mockWebServer!!.start(DEFAULT_SERVER_PORT)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Startup failed", e)
            throw e
        }

        check(serverIsRunning(true)) { "Server should be running" }
    }

    @Synchronized
    fun stopServer() {
        mockWebServer?.close()
        mockWebServer = null
    }
}

private const val RETRY_COUNT = 8

private const val RETRY_DELAY = 512

fun serverIsRunning(wait: Boolean): Boolean = serverIsRunning(wait, RETRY_COUNT, RETRY_DELAY.toLong())

fun serverIsRunning(wait: Boolean, retryCount: Int, retryDelay: Long): Boolean {
    repeat(retryCount) {
        val client = OkHttpClient()
        val request = Request.Builder().url(SERVER_URL).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    check(response.body.string() == CHECK_SERVER_RESPONSE) {
                        "Another server is running on port $DEFAULT_SERVER_PORT"
                    }
                    return true
                }
            }
        } catch (_: IOException) {
            if (!wait) {
                return@repeat
            }
            try {
                Thread.sleep(retryDelay)
            } catch (_: InterruptedException) {
            }
        }
    }
    return false
}

@Suppress("unused")
fun resetServer(): Boolean {
    val client = OkHttpClient()
    val request =
        Request.Builder().url("$SERVER_URL/reset/").get().build()
    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

private fun makeOKJSONResponse(body: String): MockResponse = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_OK)
    .setBody(body)
    .setHeader("Content-Type", "application/json; charset=utf-8")

private val httpNotFound = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
    .setBody("404: Not Found")

private val httpBadRequest = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
    .setBody("400: Bad Request")

private fun readEventDataFile(): String = Server::class.java.getResource("/events.json")!!.readText()
