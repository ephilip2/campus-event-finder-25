package edu.illinois.cs.cs124.ay2025.mp.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import edu.illinois.cs.cs124.ay2025.mp.network.Client

/**
 * STEP 3: MainActivity is created after EventableApplication finishes initializing.
 * This is the main screen users see - displays a list of upcoming event summaries.
 */
class MainActivity : Activity() {

    // List of event summaries to display (starts empty until loaded from server)
    private var summaries: List<Summary> = emptyList()

    // Adapter that manages how summaries are displayed in the RecyclerView
    private lateinit var listAdapter: SummaryListAdapter

    /**
     * STEP 3: Called when the activity is first created.
     * This is where we set up the UI layout and initialize views.
     */
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // INFLATE THE LAYOUT: Convert activity_main.xml into actual View objects
        setContentView(R.layout.activity_main)
        title = "Discover Events"

        // Create the adapter (initially with empty list)
        listAdapter = SummaryListAdapter(summaries, this)

        // Find the RecyclerView that was inflated from XML
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        // Configure it to display items in a vertical scrolling list
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Attach our adapter to the RecyclerView
        recyclerView.adapter = listAdapter

        // Set up the toolbar at the top of the screen
        setActionBar(findViewById(R.id.toolbar))

        // Handle system UI insets (status bar, navigation bar) for edge-to-edge display
        findViewById<android.view.View>(R.id.container).setOnApplyWindowInsetsListener { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsets.CONSUMED
        }
    }

    /**
     * STEP 4: Called when the activity becomes visible to the user.
     * This happens after onCreate() and also when returning from another app.
     */
    override fun onResume() {
        super.onResume()
        // Load event summaries from the server every time the screen appears
        loadSummaries()
    }

    /**
     * STEP 5: Make an HTTP request to get event summaries from the server.
     * This runs on a background thread to avoid blocking the UI.
     */
    private fun loadSummaries() {
        // Client.getSummaries() makes an HTTP GET request to http://localhost:8024/summaries/
        Client.getSummaries { result ->
            try {
                // Extract the list of summaries from the result
                summaries = result.value
                // Switch back to the main UI thread to update the display
                runOnUiThread(this::updateDisplayedSummaries)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating summary list", e)
            }
        }
    }

    /**
     * STEP 6: Update the RecyclerView with the new list of summaries.
     * This must run on the main UI thread.
     */
    private fun updateDisplayedSummaries() {
        if (summaries.isEmpty()) {
            return
        }
        // Give the new summaries to the adapter, which triggers the RecyclerView to refresh
        listAdapter.setSummaries(summaries)
    }
}

private val TAG = MainActivity::class.java.simpleName
