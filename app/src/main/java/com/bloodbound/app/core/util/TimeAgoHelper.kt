package com.bloodbound.app.core.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun timeAgo(isoString: String?): String {
    if (isoString.isNullOrBlank()) return ""
    return try {
        val past    = LocalDateTime.parse(isoString.take(19))
        val seconds = ChronoUnit.SECONDS.between(past, LocalDateTime.now())
        when {
            seconds < 60    -> "just now"
            seconds < 3600  -> "${seconds / 60}m ago"
            seconds < 86400 -> "${seconds / 3600}h ago"
            else            -> "${seconds / 86400}d ago"
        }
    } catch (e: Exception) { "" }
}

fun formatDisplayDate(isoString: String?): String {
    if (isoString.isNullOrBlank()) return "—"
    return try {
        val d = LocalDateTime.parse(isoString.take(19))
        val month = d.month.name.lowercase().replaceFirstChar { it.uppercase() }
        "$month ${d.dayOfMonth}, ${d.year}"
    } catch (e: Exception) { "—" }
}