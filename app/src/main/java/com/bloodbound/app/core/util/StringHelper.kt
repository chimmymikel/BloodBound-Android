package com.bloodbound.app.core.util

import java.text.SimpleDateFormat
import java.util.Locale

fun String?.toTitleCase(): String {
    if (this.isNullOrBlank()) return ""
    return this.lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}

fun formatDate(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    val cleanRaw = raw.substringBefore("T")
    return try {
        val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val output = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        output.format(input.parse(cleanRaw)!!)
    } catch (e: Exception) {
        raw
    }
}