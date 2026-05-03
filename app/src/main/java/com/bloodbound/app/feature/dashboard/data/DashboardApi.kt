package com.bloodbound.app.feature.dashboard.data

import com.bloodbound.app.feature.auth.data.ApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// ── Data shapes matching your Spring Boot backend exactly ──────────────

data class RequestDto(
    val id: Long,
    val bloodType: String?,
    val units: Int?,
    val urgency: String?,
    val status: String?,
    val notes: String?,
    val location: String?,
    val createdAt: String?,
    val hospitalName: String?,
    val commitmentCount: Int?,
    val requesterName: String?,
    val requesterContactNumber: String?,
    val committedDonors: List<DonorCard>?
)

data class DonorCard(
    val name: String?,
    val contactNumber: String?,
    val bloodType: String?
)

data class EligibilityDto(
    val isEligible: Boolean,
    val daysUntilEligible: Int,
    val nextEligibleDate: String?,
    val message: String?
)

// ── Retrofit interface — mirrors your RequestController + ProfileController ──

interface DashboardApi {

    // GET /api/v1/requests?status=ACTIVE&bloodType=O_POSITIVE
    // GET /api/v1/requests?requesterId=16
    @GET("requests")
    suspend fun getRequests(
        @Query("status")      status: String?      = null,
        @Query("bloodType")   bloodType: String?   = null,
        @Query("requesterId") requesterId: Long?   = null
    ): Response<ApiResponse<List<RequestDto>>>

    // GET /api/v1/profile/{id}/eligibility
    @GET("profile/{id}/eligibility")
    suspend fun getEligibility(
        @Path("id") userId: Long
    ): Response<ApiResponse<EligibilityDto>>
}