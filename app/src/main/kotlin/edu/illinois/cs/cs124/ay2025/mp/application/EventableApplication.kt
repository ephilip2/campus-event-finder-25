package edu.illinois.cs.cs124.ay2025.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2025.mp.network.Server

const val DEFAULT_SERVER_PORT = 8024

const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT"

private const val SERVER_STARTUP_TIMEOUT_MS = 8000L

/**
 * Custom Application class - this is created FIRST when the app starts, before any activities.
 * It's specified in AndroidManifest.xml with android:name attribute.
 */
class EventableApplication : Application() {
    /**
     * STEP 2: This runs after the Android system creates the Application instance.
     * This is where we initialize app-wide resources like the mock server.
     */
    override fun onCreate() {
        super.onCreate()

        // Only start the server in production (not during Robolectric unit tests)
        if (Build.FINGERPRINT != "robolectric") {
            // Create a new thread to run the server (prevents blocking the main UI thread)
            val serverThread = Thread(Server::startServer)
            serverThread.start()

            try {
                // Wait for the server to finish starting (up to 8 seconds)
                // This blocks the app startup until the server is ready
                serverThread.join(SERVER_STARTUP_TIMEOUT_MS)

                // Verify the server actually finished (didn't timeout)
                check(!serverThread.isAlive) {
                    "Server failed to start within ${SERVER_STARTUP_TIMEOUT_MS / 1000} seconds"
                }
            } catch (e: InterruptedException) {
                throw IllegalStateException("Server startup interrupted", e)
            }
        }
    }
}
