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
        return if (start.compareTo(other.start) != 0) {
            start.compareTo(other.start)
        } else {
            title.compareTo(other.title)
        }
    }
}

fun List<Summary>.filterVirtual(isVirtual: Boolean): List<Summary> {
    var list = mutableListOf<Summary>()
    for (summary in this) {
        if (summary.virtual == isVirtual) {
            list += summary
        }
    }
    return list
}

fun List<Summary>.filterTime(start: java.time.Instant?, end: java.time.Instant?): List<Summary> {
    var list = mutableListOf<Summary>()
    for (summary in this) {
        var inst = java.time.Instant.parse(summary.start)
        if (start != null && end != null) {
            if (inst >= start && inst <= end) {
                list += summary
            }
        } else if (start == null && end != null) {
            if (inst <= end) {
                list += summary
            }
        } else if (start != null) {
            if (inst >= start) {
                list += summary
            }
        } else {
            list += summary
        }
    }
    return list
}

fun List<Summary>.search(query: String): List<Summary> {
    TODO("Not yet implemented")
}
