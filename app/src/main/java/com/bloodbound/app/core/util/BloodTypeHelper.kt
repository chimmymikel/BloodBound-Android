package com.bloodbound.app.core.util

fun formatBloodType(raw: String?): String {
    if (raw.isNullOrBlank()) return "—"
    return raw.replace("_POSITIVE", "+").replace("_NEGATIVE", "−").replace("_", "")
}