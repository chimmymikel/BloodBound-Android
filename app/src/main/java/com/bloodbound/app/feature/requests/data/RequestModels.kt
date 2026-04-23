// FILE: app/src/main/java/com/bloodbound/app/feature/requests/data/RequestModels.kt
package com.bloodbound.app.feature.requests.data

data class RequestDto(
    val id: Long,
    val bloodType: String,
    val units: Int,
    val urgency: String,
    val status: String,
    val notes: String?,
    val location: String?,
    val createdAt: String,
    val hospitalName: String?,
    val commitmentCount: Int?,
    val requesterName: String?,
    val requesterContactNumber: String?,
    val committedDonors: List<DonorCard>?
)

data class DonorCard(
    val name: String,
    val contactNumber: String,
    val bloodType: String
)

data class CreateRequestBody(
    val bloodType: String,
    val units: Int,
    val urgency: String,
    val notes: String?,
    val location: String,
    val requesterId: Long,
    val hospitalId: Long
)

data class HospitalDto(
    val id: Long,
    val name: String,
    val address: String?,
    val phone: String?,
    val latitude: Double?,
    val longitude: Double?
)