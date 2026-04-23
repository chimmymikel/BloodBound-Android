// FILE: app/src/main/java/com/bloodbound/app/feature/auth/data/AuthRepository.kt
package com.bloodbound.app.feature.auth.data

import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.google.gson.Gson
import com.google.gson.JsonObject
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val tokenManager: TokenManager
) {
    suspend fun login(email: String, password: String): ApiResult<StoredUser> {
        return try {
            val response = authApi.login(LoginRequest(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                val user = data.toStoredUser()
                tokenManager.saveToken(data.token)
                tokenManager.saveUser(user)
                ApiResult.Success(user)
            } else {
                val msg = parseErrorBody(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Login failed."
                ApiResult.Error(msg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error. Check your connection.")
        }
    }

    suspend fun register(request: RegisterRequest): ApiResult<StoredUser> {
        return try {
            val response = authApi.register(request)
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                val user = data.toStoredUser()
                tokenManager.saveToken(data.token)
                tokenManager.saveUser(user)
                ApiResult.Success(user)
            } else {
                val msg = parseErrorBody(response.errorBody()?.string())
                    ?: response.body()?.message
                    ?: "Registration failed."
                ApiResult.Error(msg)
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error. Check your connection.")
        }
    }

    suspend fun getMe(): ApiResult<StoredUser> {
        return try {
            val response = authApi.me()
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                val user = data.toStoredUser()
                tokenManager.saveUser(user)
                ApiResult.Success(user)
            } else {
                ApiResult.Error("Session expired. Please log in again.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error.")
        }
    }

    private fun AuthResponseData.toStoredUser() = StoredUser(
        id = id, fullName = fullName, email = email, role = role,
        contactNumber = contactNumber, bloodType = bloodType,
        totalDonations = totalDonations ?: 0,
        lastDonationDate = lastDonationDate,
        createdAt = createdAt, profilePicture = profilePicture
    )

    private fun parseErrorBody(body: String?): String? {
        if (body == null) return null
        return try {
            val obj = Gson().fromJson(body, JsonObject::class.java)
            obj.get("error")?.asJsonObject?.get("message")?.asString
                ?: obj.get("message")?.asString
        } catch (e: Exception) { null }
    }
}