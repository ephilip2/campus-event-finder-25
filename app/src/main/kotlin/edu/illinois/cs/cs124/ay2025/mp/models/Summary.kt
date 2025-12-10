package edu.illinois.cs.cs124.ay2025.mp.models

open class Summary(
    val id: String,
    val title: String,
    val start: String,
    val location: String,
    val virtual: Boolean,
    val favorite: Boolean = false,
) : Comparable<Summary> {

    constructor(eventData: EventData) : this(
        id = eventData.id,
        title = eventData.title,
        start = eventData.start,
        location = eventData.location,
        virtual = eventData.virtual,
        favorite = false,
    )

    override fun equals(other: Any?) = when {
        other !is Summary -> false
        else -> id == other.id
    }

    override fun hashCode() = id.hashCode()

    override fun compareTo(other: Summary): Int = if (start.compareTo(other.start) != 0) {
        start.compareTo(other.start)
    } else {
        title.compareTo(other.title)
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

fun List<Summary>.filterFavorites(isFavorite: Boolean): List<Summary> {
    var list = mutableListOf<Summary>()
    for (summary in this) {
        if (summary.favorite == isFavorite) {
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
    if (query.isBlank()) {
        return this.toList()
    }

    val list = mutableListOf<Summary>()
    var sTerm = query
    var isVirtual: Boolean? = null
    var theLoc: String? = null

    if (query.contains("location:")) {
        val lPart = query.split("location:")[1].split(" ")[0]
        theLoc = lPart.toString()
        sTerm = sTerm.replace("location:$theLoc", "").trim()
    }
    if (query.contains("virtual:")) {
        val vPart = query.split("virtual:")[1].split(" ")[0]
        isVirtual = vPart.toBoolean()
        sTerm = sTerm.replace("virtual:$isVirtual", "").trim()
    }

    for (summary in this) {
        val matchesSearchTerm = sTerm.isBlank() ||
            summary.title.contains(sTerm, ignoreCase = true) ||
            summary.location.contains(sTerm, ignoreCase = true)

        val matchesLocation = theLoc == null || summary.location.contains(theLoc, ignoreCase = true)
        val matchesVirtual = isVirtual == null || summary.virtual == isVirtual

        if (matchesSearchTerm && matchesLocation && matchesVirtual) {
            list += summary
        }
    }

    list.sortBy { it.title }
    return list
}
