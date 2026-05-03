// FILE: app/src/main/java/com/bloodbound/app/feature/auth/data/AuthRepository.kt
package com.bloodbound.app.feature.auth.data

import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    retrofit: Retrofit,
    private val tokenManager: TokenManager
) {
    private val api = retrofit.create(AuthApi::class.java)

    suspend fun login(email: String, password: String): ApiResult<AuthResponseData> {
        return try {
            val response = api.login(LoginRequest(email.trim(), password))
            val body = response.body()

            when {
                response.isSuccessful && body?.success == true && body.data != null -> {
                    // Save JWT + user data locally
                    tokenManager.saveToken(body.data.token)
                    tokenManager.saveUser(body.data.toStoredUser())
                    ApiResult.Success(body.data)
                }
                response.code() == 401 -> {
                    ApiResult.Error("Incorrect email or password.")
                }
                else -> {
                    val msg = body?.error?.message
                        ?: body?.message
                        ?: "Login failed. Please try again."
                    ApiResult.Error(msg)
                }
            }
        } catch (e: Exception) {
            ApiResult.Error(
                "Network error. Please check your connection.\n${e.localizedMessage ?: ""}"
            )
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        contactNumber: String,
        bloodType: String?
    ): ApiResult<AuthResponseData> {
        return try {
            val response = api.register(
                RegisterRequest(
                    fullName        = fullName,
                    email           = email.trim(),
                    password        = password,
                    confirmPassword = confirmPassword,
                    role            = role,
                    contactNumber   = contactNumber,
                    bloodType       = bloodType
                )
            )
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                tokenManager.saveToken(body.data.token)
                tokenManager.saveUser(body.data.toStoredUser())
                ApiResult.Success(body.data)
            } else {
                ApiResult.Error(
                    body?.error?.message ?: body?.message ?: "Registration failed."
                )
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage ?: "Please check your connection."}")
        }
    }

    suspend fun getMe(): ApiResult<AuthResponseData> {
        return try {
            val response = api.me()
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                tokenManager.saveUser(body.data.toStoredUser())
                ApiResult.Success(body.data)
            } else {
                if (response.code() == 401) tokenManager.clearAll()
                ApiResult.Error(body?.message ?: "Session expired. Please log in again.")
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.localizedMessage ?: ""}")
        }
    }

    fun signOut() = tokenManager.clearAll()
    fun getStoredUser(): StoredUser? = tokenManager.getUser()
    fun hasToken(): Boolean = tokenManager.hasToken()
}

// Convert auth response → the lightweight object stored in EncryptedSharedPreferences
fun AuthResponseData.toStoredUser() = StoredUser(
    id               = id,
    fullName         = fullName,
    email            = email,
    role             = role,
    contactNumber    = contactNumber,
    bloodType        = bloodType,
    totalDonations   = totalDonations,
    lastDonationDate = lastDonationDate,
    createdAt        = createdAt,
    profilePicture   = profilePicture
)