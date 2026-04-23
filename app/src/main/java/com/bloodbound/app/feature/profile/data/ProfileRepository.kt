// FILE: app/src/main/java/com/bloodbound/app/feature/profile/data/ProfileRepository.kt
package com.bloodbound.app.feature.profile.data

import com.bloodbound.app.core.network.ApiResult
import okhttp3.MultipartBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(private val api: ProfileApi) {

    suspend fun getProfile(id: Long): ApiResult<ProfileDto> = runCatching {
        val r = api.getProfile(id)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error("Failed to load profile.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun updateContact(id: Long, number: String): ApiResult<ProfileDto> = runCatching {
        val r = api.updateProfile(id, UpdateContactRequest(number))
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error(r.body()?.message ?: "Update failed.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun updatePassword(id: Long, old: String, new: String): ApiResult<Unit> = runCatching {
        val r = api.updatePassword(id, UpdatePasswordRequest(old, new))
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(Unit)
        else ApiResult.Error(r.body()?.message ?: "Password update failed.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun uploadPhoto(id: Long, part: MultipartBody.Part): ApiResult<Unit> = runCatching {
        val r = api.uploadPhoto(id, part)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error("Photo upload failed.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun checkEligibility(id: Long): ApiResult<EligibilityDto> = runCatching {
        val r = api.checkEligibility(id)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error("Failed to check eligibility.")
    }.getOrElse { ApiResult.Error("Network error.") }
}