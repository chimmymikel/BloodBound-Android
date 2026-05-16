// FILE: app/src/main/java/com/bloodbound/app/core/util/StringHelper.kt
package com.bloodbound.app.core.util

/**
 * Converts "MICHELLE HABON" to "Michelle Habon"
 */
fun String?.toTitleCase(): String {
    if (this.isNullOrBlank()) return ""

    return this.lowercase().split(" ").joinToString(" ") { word ->
        word.replaceFirstChar { it.uppercase() }
    }
}