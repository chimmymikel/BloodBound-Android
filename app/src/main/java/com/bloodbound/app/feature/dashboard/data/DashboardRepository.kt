package com.bloodbound.app.feature.dashboard.data

import com.bloodbound.app.core.network.ApiResult
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    retrofit: Retrofit
) {
    private val api = retrofit.create(DashboardApi::class.java)

    // Called for DONOR — O_NEGATIVE sees all requests (universal donor)
    suspend fun getRequestsForDonor(
        bloodType: String?
    ): ApiResult<List<RequestDto>> {
        return try {
            val filter = if (bloodType == "O_NEGATIVE") null else bloodType
            val response = api.getRequests(
                status    = "ACTIVE",
                bloodType = filter
            )
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                ApiResult.Success(body.data ?: emptyList())
            } else {
                ApiResult.Error(body?.message ?: "Failed to load requests.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage ?: ""}")
        }
    }

    // Called for REQUESTER — shows only their own requests
    suspend fun getRequestsForRequester(
        requesterId: Long
    ): ApiResult<List<RequestDto>> {
        return try {
            val response = api.getRequests(requesterId = requesterId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true) {
                ApiResult.Success(body.data ?: emptyList())
            } else {
                ApiResult.Error(body?.message ?: "Failed to load requests.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage ?: ""}")
        }
    }

    // Called for DONOR only — checks 56-day cooldown from server
    suspend fun getEligibility(
        userId: Long
    ): ApiResult<EligibilityDto> {
        return try {
            val response = api.getEligibility(userId)
            val body = response.body()
            if (response.isSuccessful && body?.success == true && body.data != null) {
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(body?.message ?: "Failed to check eligibility.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage ?: ""}")
        }
    }
}