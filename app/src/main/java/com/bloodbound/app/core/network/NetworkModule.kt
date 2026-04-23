// FILE: app/src/main/java/com/bloodbound/app/core/network/NetworkModule.kt
package com.bloodbound.app.core.network

import com.bloodbound.app.feature.auth.data.AuthApi
import com.bloodbound.app.feature.commitments.data.CommitmentsApi
import com.bloodbound.app.feature.profile.data.ProfileApi
import com.bloodbound.app.feature.requests.data.RequestsApi
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://bloodbound-backend.onrender.com/api/v1/"

    @Provides @Singleton
    fun provideOkHttpClient(auth: AuthInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(auth)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .callTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create()
                )
            )
            .build()

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideRequestsApi(r: Retrofit): RequestsApi = r.create(RequestsApi::class.java)
    @Provides @Singleton fun provideCommitmentsApi(r: Retrofit): CommitmentsApi = r.create(CommitmentsApi::class.java)
    @Provides @Singleton fun provideProfileApi(r: Retrofit): ProfileApi = r.create(ProfileApi::class.java)
}