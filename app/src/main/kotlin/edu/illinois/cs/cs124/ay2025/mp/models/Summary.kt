package edu.illinois.cs.cs124.ay2025.mp.models

open class Summary(
    val id: String,
    val title: String,
    val start: String,
    val location: String,
    val virtual: Boolean,
) : Comparable<Summary> {

    constructor(eventData: EventData) : this(
        id = eventData.id,
        title = eventData.title,
        start = eventData.start,
        location = eventData.location,
        virtual = eventData.virtual,
    )

    override fun equals(other: Any?) = when {
        other !is Summary -> false
        else -> id == other.id
    }

    override fun hashCode() = id.hashCode()

    override fun compareTo(other: Summary): Int {
        TODO("Not yet implemented")
    }
}

fun List<Summary>.filterVirtual(virtual: Boolean): List<Summary> {
    TODO("Not yet implemented")
}

fun List<Summary>.filterTime(start: java.time.Instant?, end: java.time.Instant?): List<Summary> {
    TODO("Not yet implemented")
}

fun List<Summary>.search(query: String): List<Summary> {
    TODO("Not yet implemented")
}
