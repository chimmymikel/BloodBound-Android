// FILE: app/src/main/java/com/bloodbound/app/feature/auth/data/AuthApi.kt
package com.bloodbound.app.feature.auth.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(
        @Body body: LoginRequest
    ): Response<ApiResponse<AuthResponseData>>

    @POST("auth/register")
    suspend fun register(
        @Body body: RegisterRequest
    ): Response<ApiResponse<AuthResponseData>>

    @GET("auth/me")
    suspend fun me(): Response<ApiResponse<AuthResponseData>>
}