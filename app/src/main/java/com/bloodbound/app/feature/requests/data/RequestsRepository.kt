// FILE: app/src/main/java/com/bloodbound/app/feature/requests/data/RequestsRepository.kt
package com.bloodbound.app.feature.requests.data

import com.bloodbound.app.core.network.ApiResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RequestsRepository @Inject constructor(private val api: RequestsApi) {

    suspend fun getRequests(params: Map<String, String>): ApiResult<List<RequestDto>> = runCatching {
        val r = api.getRequests(params)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data ?: emptyList<RequestDto>())
        else ApiResult.Error(r.body()?.message ?: "Failed to load requests.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun createRequest(body: CreateRequestBody): ApiResult<RequestDto> = runCatching {
        val r = api.createRequest(body)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error(r.body()?.message ?: "Failed to post request.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun fulfillRequest(id: Long): ApiResult<RequestDto> = runCatching {
        val r = api.fulfillRequest(id)
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data!!)
        else ApiResult.Error(r.body()?.message ?: "Failed to fulfill request.")
    }.getOrElse { ApiResult.Error("Network error.") }

    suspend fun getHospitals(): ApiResult<List<HospitalDto>> = runCatching {
        val r = api.getHospitals()
        if (r.isSuccessful && r.body()?.success == true) ApiResult.Success(r.body()!!.data ?: emptyList<HospitalDto>())
        else ApiResult.Error("Failed to load hospitals.")
    }.getOrElse { ApiResult.Error("Network error.") }
}