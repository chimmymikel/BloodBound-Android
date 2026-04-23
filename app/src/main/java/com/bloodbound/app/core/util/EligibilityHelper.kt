package com.bloodbound.app.core.util

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

data class EligibilityResult(val eligible: Boolean, val daysLeft: Int)

fun calcEligibility(lastDonationDate: String?): EligibilityResult {
    if (lastDonationDate.isNullOrBlank()) return EligibilityResult(true, 0)
    return try {
        val last      = LocalDateTime.parse(lastDonationDate.take(19))
        val daysSince = ChronoUnit.DAYS.between(last, LocalDateTime.now())
        val remaining = 56 - daysSince
        EligibilityResult(remaining <= 0, maxOf(remaining.toInt(), 0))
    } catch (e: Exception) { EligibilityResult(true, 0) }
}