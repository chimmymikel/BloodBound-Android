// FILE: app/src/main/java/com/bloodbound/app/core/network/ApiResponse.kt
package com.bloodbound.app.core.network

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)