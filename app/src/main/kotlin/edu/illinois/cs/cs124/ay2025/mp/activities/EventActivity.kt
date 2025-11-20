package edu.illinois.cs.cs124.ay2025.mp.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class EventActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)

        // Get the event ID from the intent
        val eventId = intent.getStringExtra("id")
        if (eventId == null) {
            Log.e(TAG, "No event ID provided")
            finish()
            return
        }

        // Fetch the event details from the server
        Client.getEvent(eventId) { result ->
            try {
                val event = result.value

                // Update UI on the main thread
                runOnUiThread {
                    // Set title
                    findViewById<TextView>(R.id.event_title).text = event.title

                    // Format and set time
                    val instant = Instant.parse(event.start)
                    val chicagoTime = instant.atZone(ZoneId.of("America/Chicago"))
                    val formatter = DateTimeFormatter.ofPattern("MMM d • h:mm a")
                    findViewById<TextView>(R.id.event_time).text = chicagoTime.format(formatter)

                    // Set location
                    findViewById<TextView>(R.id.event_location).text = event.location

                    // Set description
                    findViewById<TextView>(R.id.event_description).text = event.description

                    // Set URL
                    findViewById<TextView>(R.id.event_url).text = event.url

                    // Set source
                    findViewById<TextView>(R.id.event_source).text = event.source

                    // Set categories (join list into comma-separated string)
                    findViewById<TextView>(R.id.event_categories).text = event.categories.joinToString(", ")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event details", e)
            }
        }
    }
}

private val TAG = EventActivity::class.java.simpleName
