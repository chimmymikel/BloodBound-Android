// FILE: app/src/main/java/com/bloodbound/app/feature/auth/data/AuthModels.kt
package com.bloodbound.app.feature.auth.data

// ── What we send TO the backend ───────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val role: String,
    val contactNumber: String,
    val bloodType: String? = null  // only for DONOR
)

// ── What the backend sends BACK ────────────────────────────────────────
// Outer envelope: { success, data, message, error, timestamp }
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: ApiError?
)

data class ApiError(
    val code: String?,
    val message: String?
)

// The "data" object inside every auth response (login + register + /me)
data class AuthResponseData(
    val token: String,
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val contactNumber: String?,
    val bloodType: String?,
    val totalDonations: Int?,
    val lastDonationDate: String?,
    val createdAt: String?,
    val profilePicture: String?,
    val hospitalOrOrg: String?
)

// Convenience list used by blood type dropdowns
val BLOOD_TYPES = listOf(
    "O_POSITIVE"  to "O+",
    "O_NEGATIVE"  to "O\u2212",
    "A_POSITIVE"  to "A+",
    "A_NEGATIVE"  to "A\u2212",
    "B_POSITIVE"  to "B+",
    "B_NEGATIVE"  to "B\u2212",
    "AB_POSITIVE" to "AB+",
    "AB_NEGATIVE" to "AB\u2212"
)