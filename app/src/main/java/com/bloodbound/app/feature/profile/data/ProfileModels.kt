// FILE: app/src/main/java/com/bloodbound/app/feature/profile/data/ProfileModels.kt
package com.bloodbound.app.feature.profile.data

data class ProfileDto(
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

data class UpdateContactRequest(val contactNumber: String)

data class UpdatePasswordRequest(
    val oldPassword: String,
    val newPassword: String
)

data class EligibilityDto(
    val isEligible: Boolean,
    val daysUntilEligible: Int,
    val nextEligibleDate: String?,
    val message: String?
)