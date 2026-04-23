// FILE: app/src/main/java/com/bloodbound/app/feature/commitments/data/CommitmentsApi.kt
package com.bloodbound.app.feature.commitments.data

import com.bloodbound.app.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface CommitmentsApi {
    @GET("commitments")
    suspend fun getCommitments(@QueryMap params: Map<String, String>): Response<ApiResponse<List<CommitmentDto>>>

    @POST("commitments")
    suspend fun createCommitment(@Body body: CreateCommitmentBody): Response<ApiResponse<CommitmentDto>>

    @DELETE("commitments/{id}")
    suspend fun cancelCommitment(@Path("id") id: Long): Response<ApiResponse<Unit>>
}