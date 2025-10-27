package edu.illinois.cs.cs124.ay2025.mp.models

open class Summary(val id: String, val title: String, val start: String, val location: String) {

    constructor(eventData: EventData) : this(
        id = eventData.id,
        title = eventData.title,
        start = eventData.start,
        location = eventData.location,
    )

    override fun equals(other: Any?) = when {
        other !is Summary -> false
        else -> id == other.id
    }

    override fun hashCode() = id.hashCode()
}
