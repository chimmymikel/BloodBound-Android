package com.bloodbound.app.core.network

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

data class StoredUser(
    val id: Long,
    val fullName: String,
    val email: String,
    val role: String,
    val contactNumber: String? = null,
    val bloodType: String? = null,
    val totalDonations: Int? = 0,
    val lastDonationDate: String? = null,
    val createdAt: String? = null,
    val profilePicture: String? = null
)

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()

    private val prefs by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context, "bb_secure_prefs", masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveToken(token: String) = prefs.edit().putString(KEY_TOKEN, token).apply()
    fun getToken(): String?       = prefs.getString(KEY_TOKEN, null)
    fun clearToken()              = prefs.edit().remove(KEY_TOKEN).apply()
    fun saveUser(user: StoredUser)= prefs.edit().putString(KEY_USER, gson.toJson(user)).apply()
    fun getUser(): StoredUser?    = prefs.getString(KEY_USER, null)?.let {
        try { gson.fromJson(it, StoredUser::class.java) } catch (e: Exception) { null }
    }
    fun clearAll()  = prefs.edit().clear().apply()
    fun hasToken()  = getToken() != null

    companion object {
        private const val KEY_TOKEN = "jwt_token"
        private const val KEY_USER  = "stored_user"
    }
}