// FILE: app/src/main/java/com/bloodbound/app/feature/auth/data/AuthModels.kt
package com.bloodbound.app.feature.auth.data

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
    val bloodType: String? = null
)

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
    val profilePicture: String?
)