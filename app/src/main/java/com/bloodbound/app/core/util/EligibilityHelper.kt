// FILE: app/src/main/java/com/bloodbound/app/core/util/EligibilityHelper.kt
package com.bloodbound.app.core.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class EligibilityResult(val eligible: Boolean, val daysLeft: Int)

fun calcEligibility(lastDonationDate: String?): EligibilityResult {
    if (lastDonationDate.isNullOrBlank()) return EligibilityResult(true, 0)
    return try {
        // Take only the date portion (first 10 chars: "yyyy-MM-dd")
        val last = LocalDate.parse(lastDonationDate.take(10))
        val now  = LocalDate.now()

        val diffDays  = ChronoUnit.DAYS.between(last, now)
        val remaining = 56 - diffDays

        EligibilityResult(
            eligible = remaining <= 0,
            daysLeft = maxOf(remaining.toInt(), 0)
        )
    } catch (e: Exception) {
        EligibilityResult(true, 0)
    }
}