package com.bloodbound.app.core.util

import java.time.LocalDate
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.ceil

data class EligibilityResult(val eligible: Boolean, val daysLeft: Int)

/**
 * Mirrors the web app's calcEligibility exactly:
 *
 *   const diffDays = Math.ceil(
 *     Math.abs(new Date() - new Date(lastDonationDate)) / (1000 * 60 * 60 * 24)
 *   );
 *   const remaining = 56 - diffDays;
 *
 * Key: JS parses "yyyy-MM-dd" as UTC midnight, so we do the same with atStartOfDay(UTC).
 *      Then we take the ceiling of the millisecond difference divided by ms-per-day.
 */
fun calcEligibility(lastDonationDate: String?): EligibilityResult {
    if (lastDonationDate.isNullOrBlank()) return EligibilityResult(eligible = true, daysLeft = 0)

    return try {
        // JS: new Date("2026-03-30") → UTC midnight
        val last: ZonedDateTime = LocalDate.parse(lastDonationDate.take(10))
            .atStartOfDay(ZoneOffset.UTC)

        // JS: new Date() → current instant in UTC
        val now: ZonedDateTime = ZonedDateTime.now(ZoneOffset.UTC)

        // JS: Math.ceil(Math.abs(now - last) / ms_per_day)
        val diffMillis = ChronoUnit.MILLIS.between(last, now).toDouble()
        val msPerDay   = (1000.0 * 60.0 * 60.0 * 24.0)
        val diffDays   = ceil(diffMillis / msPerDay).toLong()

        // JS: remaining = 56 - diffDays
        val remaining = 56L - diffDays
        val eligible  = remaining <= 0
        val daysLeft  = if (eligible) 0 else remaining.toInt()

        EligibilityResult(eligible = eligible, daysLeft = daysLeft)
    } catch (e: Exception) {
        EligibilityResult(eligible = true, daysLeft = 0)
    }
}