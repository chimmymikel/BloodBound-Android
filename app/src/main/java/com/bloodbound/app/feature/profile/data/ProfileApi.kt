package com.bloodbound.app.feature.profile.data

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*
import com.bloodbound.app.feature.auth.data.ApiResponse

interface ProfileApi {

    @GET("profile/{id}")
    suspend fun getProfile(@Path("id") id: Long): Response<ApiResponse<ProfileDto>>

    @PUT("profile/{id}")
    suspend fun updateProfile(
        @Path("id") id: Long,
        @Body body: UpdateContactRequest
    ): Response<ApiResponse<ProfileDto>>

    @PUT("profile/{id}/password")
    suspend fun updatePassword(
        @Path("id") id: Long,
        @Body body: UpdatePasswordRequest
    ): Response<ApiResponse<Unit>>

    // Photo Upload Endpoint
    @Multipart
    @POST("profile/{id}/photo")
    suspend fun uploadPhoto(
        @Path("id") id: Long,
        @Part file: MultipartBody.Part  // "file" matches Spring Boot's @RequestParam("file")
    ): Response<ApiResponse<Unit>>

    @GET("profile/{id}/eligibility")
    suspend fun checkEligibility(@Path("id") id: Long): Response<ApiResponse<EligibilityDto>>
}