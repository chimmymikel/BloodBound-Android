// FILE: app/src/main/java/com/bloodbound/app/core/util/EligibilityHelper.kt
package com.bloodbound.app.core.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class EligibilityResult(val eligible: Boolean, val daysLeft: Int)

fun calcEligibility(lastDonationDate: String?): EligibilityResult {
    if (lastDonationDate.isNullOrBlank()) return EligibilityResult(true, 0)
    return try {
        val last = LocalDateTime.parse(lastDonationDate.take(19))
        val now  = LocalDateTime.now()

        // Match React exactly:
        // Math.ceil(Math.abs(now - last) / (1000 * 60 * 60 * 24))
        val diffMillis = ChronoUnit.MILLIS.between(last, now).toDouble()
        val diffDays   = ceil(diffMillis / (1000.0 * 60.0 * 60.0 * 24.0)).toLong()

        val remaining = 56 - diffDays
        EligibilityResult(
            eligible = remaining <= 0,
            daysLeft = maxOf(remaining.toInt(), 0)
        )
    } catch (e: Exception) {
        EligibilityResult(true, 0)
    }
}