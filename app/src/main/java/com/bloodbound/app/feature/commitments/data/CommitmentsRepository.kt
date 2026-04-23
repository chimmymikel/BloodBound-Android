// FILE: app/src/main/java/com/bloodbound/app/feature/commitments/data/CommitmentsRepository.kt
package com.bloodbound.app.feature.commitments.data

import com.bloodbound.app.core.network.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommitmentsRepository @Inject constructor(private val api: CommitmentsApi) {

    suspend fun getCommitments(params: Map<String, String>): ApiResult<List<CommitmentDto>> = runCatching {
        val r = api.getCommitments(params)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data ?: emptyList<CommitmentDto>())
        else ApiResult.Error(r.body()?.message ?: "Failed to load commitments.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun createCommitment(requestId: Long): ApiResult<CommitmentDto> = runCatching {
        val r = api.createCommitment(CreateCommitmentBody(requestId))
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error(r.body()?.message ?: "Failed to commit.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun cancelCommitment(id: Long): ApiResult<Unit> = runCatching {
        val r = api.cancelCommitment(id)
        if (r.isSuccessful) ApiResult.Success(Unit)
        else ApiResult.Error(r.body()?.message ?: "Failed to cancel.")
    }.getOrElse { ApiResult.Error("Network error.") }
}