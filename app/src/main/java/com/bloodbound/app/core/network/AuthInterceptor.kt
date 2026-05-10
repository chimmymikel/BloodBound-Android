// FILE: app/src/main/java/com/bloodbound/app/core/network/AuthInterceptor.kt
package com.bloodbound.app.core.network

import android.content.Context
import android.content.Intent
import com.bloodbound.app.MainActivity // Make sure this matches your package
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context // 1. Added Context to launch Intents
) : Interceptor {

    private val skipPaths = listOf("auth/login", "auth/register")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip paths that don't need authentication
        if (skipPaths.any { request.url.toString().contains(it) }) {
            return chain.proceed(request)
        }

        // Attach the token if it exists
        val token = tokenManager.getToken()
        val newRequest = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else request

        // Execute the API call
        val response = chain.proceed(newRequest)

        // 2. Catch the 7-day token expiration (401 Unauthorized)
        if (response.code == 401) {
            // Wipe the invalid session data
            tokenManager.clearAll()

            // Restart MainActivity and clear the backstack
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        return response
    }
}