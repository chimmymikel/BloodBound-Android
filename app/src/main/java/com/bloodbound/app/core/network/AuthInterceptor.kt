package com.bloodbound.app.core.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenManager: TokenManager
) : Interceptor {

    private val skipPaths = listOf("auth/login", "auth/register")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (skipPaths.any { request.url.toString().contains(it) }) {
            return chain.proceed(request)
        }
        val token = tokenManager.getToken()
        val newRequest = if (token != null) {
            request.newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else request
        return chain.proceed(newRequest)
    }
}