// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/AuthViewModel.kt
package com.bloodbound.app.feature.auth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.feature.auth.data.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// All possible UI states for the auth screens
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val role: String) : AuthState()  // role = "DONOR" or "REQUESTER"
    data class Error(val message: String) : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _authState

    // ── Login ─────────────────────────────────────────────────────────
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Email and password are required.")
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading

            when (val result = repository.login(email, password)) {
                is ApiResult.Success -> {
                    _authState.value = AuthState.Success(result.data.role)
                }
                is ApiResult.Error -> {
                    _authState.value = AuthState.Error(result.message)
                }
                is ApiResult.Loading -> Unit
            }
        }
    }

    // ── Register ──────────────────────────────────────────────────────
    fun register(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String,
        role: String,
        contactNumber: String,
        bloodType: String?
    ) {
        // Client-side validation — mirrors React exactly
        val cleanPhone = contactNumber.replace(Regex("[\\s-]"), "")
        val phoneRegex = Regex("^(09|\\+639)\\d{9}$")

        val error = when {
            firstName.isBlank() || lastName.isBlank() ||
                    email.isBlank() || password.isBlank() ||
                    contactNumber.isBlank() ->
                "All required fields must be filled."
            !phoneRegex.matches(cleanPhone) ->
                "Invalid contact number. Use 09XXXXXXXXX or +639XXXXXXXXX."
            password != confirmPassword ->
                "Passwords do not match."
            password.length < 8 ->
                "Password must be at least 8 characters."
            else -> null
        }

        if (error != null) {
            _authState.value = AuthState.Error(error)
            return
        }

        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = repository.register(
                fullName        = "${firstName.trim()} ${lastName.trim()}",
                email           = email.trim(),
                password        = password,
                confirmPassword = confirmPassword,
                role            = role,
                contactNumber   = cleanPhone,
                bloodType       = bloodType
            )
            when (result) {
                is ApiResult.Success -> _authState.value = AuthState.Success(result.data.role)
                is ApiResult.Error   -> _authState.value = AuthState.Error(result.message)
                is ApiResult.Loading -> Unit
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }
}