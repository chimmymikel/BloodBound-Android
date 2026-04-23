// FILE: app/src/main/java/com/bloodbound/app/feature/requests/data/RequestsApi.kt
package com.bloodbound.app.feature.requests.data

import com.bloodbound.app.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface RequestsApi {
    @GET("requests")
    suspend fun getRequests(@QueryMap params: Map<String, String>): Response<ApiResponse<List<RequestDto>>>

    @POST("requests")
    suspend fun createRequest(@Body body: CreateRequestBody): Response<ApiResponse<RequestDto>>

    @PATCH("requests/{id}/fulfill")
    suspend fun fulfillRequest(@Path("id") id: Long): Response<ApiResponse<RequestDto>>

    @GET("hospitals")
    suspend fun getHospitals(): Response<ApiResponse<List<HospitalDto>>>
}