package edu.illinois.cs.cs124.ay2025.mp.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2025.mp.helpers.getTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import edu.illinois.cs.cs124.ay2025.mp.models.filterTime
import edu.illinois.cs.cs124.ay2025.mp.models.filterVirtual
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * STEP 3: MainActivity is created after EventableApplication finishes initializing.
 * This is the main screen users see - displays a list of upcoming event summaries.
 */
class MainActivity :
    Activity(),
    SearchView.OnQueryTextListener {

    // List of event summaries to display (starts empty until loaded from server)
    private var summaries: List<Summary> = emptyList()

    // Adapter that manages how summaries are displayed in the RecyclerView
    private lateinit var listAdapter: SummaryListAdapter

    private var todayButtonClicked = false
    private var virtualButtonClicked = false
    private var currentSearchQuery: String = ""

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

        // Set up the calendar button (today filter)
        val todayButton: ToggleButton = findViewById(R.id.todayButton)
        todayButton.isChecked = true
        todayButton.alpha = 1.0f
        todayButtonClicked = true

        todayButton.setOnCheckedChangeListener { button, isChecked ->
            // Update button appearance
            if (isChecked) {
                button.alpha = 1.0f
            } else {
                button.alpha = 0.3f
            }
            // For now, just log the state change
            Log.d(TAG, "Today button toggled: $isChecked")
            todayButtonClicked = isChecked
            updateDisplayedSummaries()
        }

        // Set up the virtual button (virtual filter)
        val virtualButton: ToggleButton = findViewById(R.id.virtualButton)
        virtualButton.isChecked = false
        virtualButton.alpha = 0.3f
        virtualButtonClicked = false

        virtualButton.setOnCheckedChangeListener { button, isChecked ->
            // Update button appearance
            if (isChecked) {
                button.alpha = 1.0f
            } else {
                button.alpha = 0.3f
            }
            // Update the filter state and refresh the displayed summaries
            Log.d(TAG, "Virtual button toggled: $isChecked")
            virtualButtonClicked = isChecked
            updateDisplayedSummaries()
        }

        // Set up the search bar
        val searchView: SearchView = findViewById(R.id.search)
        searchView.setOnQueryTextListener(this)
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

        // Start with all summaries
        var filteredSummaries = summaries

        // Apply today filter if the today button is checked
        if (todayButtonClicked) {
            // Get current time
            val now = getTimeProvider().now()
            val chicagoZone = ZoneId.of("America/Chicago")

            // Get start of today in Chicago timezone
            val startOfDay = ZonedDateTime.ofInstant(now, chicagoZone)
                .toLocalDate()
                .atStartOfDay(chicagoZone)
                .toInstant()

            // Get end of today (start of tomorrow minus 1 millisecond)
            val endOfDay = startOfDay
                .atZone(chicagoZone)
                .plusDays(1)
                .minusNanos(1000000)
                .toInstant()

            // Filter events to only those happening today
            filteredSummaries = filteredSummaries.filterTime(startOfDay, endOfDay)
        }

        // Apply virtual filter if the virtual button is checked
        if (virtualButtonClicked) {
            filteredSummaries = filteredSummaries.filterVirtual(true)
        }

        // Apply search filter if there's a search query
        if (currentSearchQuery.isNotEmpty()) {
            filteredSummaries = filteredSummaries.search(currentSearchQuery)
        }

        // Sort the filtered results
        filteredSummaries = filteredSummaries.sorted()

        // Give the filtered summaries to the adapter, which triggers the RecyclerView to refresh
        listAdapter.setSummaries(filteredSummaries)
    }

    /**
     * Called when the user submits a search query.
     * @param query The search text
     * @return true if the query has been handled
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        Log.d(TAG, "Search submitted: $query")
        currentSearchQuery = query ?: ""
        updateDisplayedSummaries()
        return true
    }

    /**
     * Called when the search query text changes.
     * @param newText The new search text
     * @return true if the query has been handled
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        Log.d(TAG, "Search text changed: $newText")
        currentSearchQuery = newText ?: ""
        updateDisplayedSummaries()
        return true
    }
}

private val TAG = MainActivity::class.java.simpleName
