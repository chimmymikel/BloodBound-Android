package com.bloodbound.app.core.util

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class EligibilityResult(val eligible: Boolean, val daysLeft: Int)

fun calcEligibility(lastDonationDate: String?): EligibilityResult {
    if (lastDonationDate.isNullOrBlank()) return EligibilityResult(true, 0)
    return try {
        val last = LocalDate.parse(lastDonationDate.take(10))
        val now  = LocalDate.now()

        val diffDays = ChronoUnit.DAYS.between(last, now)

        val eligible = diffDays >= 56
        val daysLeft = if (eligible) 0 else maxOf((56 - diffDays - 1).toInt(), 0)

        EligibilityResult(eligible = eligible, daysLeft = daysLeft)
    } catch (e: Exception) {
        EligibilityResult(true, 0)
    }
}