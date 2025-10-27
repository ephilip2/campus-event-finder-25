package edu.illinois.cs.cs124.ay2025.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2025.mp.network.Server

const val DEFAULT_SERVER_PORT = 8024

const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT"

private const val SERVER_STARTUP_TIMEOUT_MS = 8000L

class EventableApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Build.FINGERPRINT != "robolectric") {
            val serverThread = Thread(Server::startServer)
            serverThread.start()
            try {
                serverThread.join(SERVER_STARTUP_TIMEOUT_MS)
                check(!serverThread.isAlive) {
                    "Server failed to start within ${SERVER_STARTUP_TIMEOUT_MS / 1000} seconds"
                }
            } catch (e: InterruptedException) {
                throw IllegalStateException("Server startup interrupted", e)
            }
        }
    }
}
