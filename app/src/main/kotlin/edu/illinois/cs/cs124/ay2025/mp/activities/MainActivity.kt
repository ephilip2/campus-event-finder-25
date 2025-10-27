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

class MainActivity : Activity() {

    private var summaries: List<Summary> = emptyList()

    private lateinit var listAdapter: SummaryListAdapter

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        setContentView(R.layout.activity_main)
        title = "Discover Event"

        listAdapter = SummaryListAdapter(summaries, this)

        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = listAdapter

        setActionBar(findViewById(R.id.toolbar))

        findViewById<android.view.View>(R.id.container).setOnApplyWindowInsetsListener { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsets.CONSUMED
        }
    }

    override fun onResume() {
        super.onResume()
        load_summaries()
    }

    private fun load_summaries() {
        Client.getSummaries { result ->
            try {
                summaries = result.value
                runOnUiThread(this::updateDisplayedSummaries)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating summary list", e)
            }
        }
    }

    private fun updateDisplayedSummaries() {
        if (summaries.isEmpty()) {
            return
        }
        listAdapter.setSummaries(summaries)
    }
}

private val TAG = MainActivity::class.java.simpleName
