// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/AuthViewModel.kt
package com.bloodbound.app.feature.auth.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.auth.data.AuthRepository
import com.bloodbound.app.feature.auth.data.RegisterRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class Success(val user: StoredUser) : AuthState()
    data class Error(val message: String)    : AuthState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repo: AuthRepository,
    val tokenManager: TokenManager
) : ViewModel() {

    private val _state = MutableLiveData<AuthState>(AuthState.Idle)
    val authState: LiveData<AuthState> = _state

    // ── Login ────────────────────────────────────────────────────────────────

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = AuthState.Error("Fields cannot be empty.")
            return
        }
        _state.value = AuthState.Loading
        viewModelScope.launch {
            _state.value = when (val r = repo.login(email.trim(), password)) {
                is ApiResult.Success -> AuthState.Success(r.data)
                is ApiResult.Error   -> AuthState.Error(r.message)
                else -> AuthState.Error("Unknown error.")
            }
        }
    }

    // ── Register ─────────────────────────────────────────────────────────────

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
        val validationError = validateRegister(
            firstName, lastName, email, password, confirmPassword, contactNumber
        )
        if (validationError != null) {
            _state.value = AuthState.Error(validationError)
            return
        }
        _state.value = AuthState.Loading
        viewModelScope.launch {
            val request = RegisterRequest(
                fullName        = "${firstName.trim()} ${lastName.trim()}",
                email           = email.trim(),
                password        = password,
                confirmPassword = confirmPassword,
                role            = role,
                contactNumber   = contactNumber.trim().replace(Regex("[\\s-]"), ""),
                bloodType       = if (role == "DONOR") bloodType else null
            )
            _state.value = when (val r = repo.register(request)) {
                is ApiResult.Success -> AuthState.Success(r.data)
                is ApiResult.Error   -> AuthState.Error(r.message)
                else -> AuthState.Error("Unknown error.")
            }
        }
    }

    private fun validateRegister(
        firstName: String, lastName: String, email: String,
        password: String, confirmPassword: String, contactNumber: String
    ): String? {
        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() ||
            password.isBlank() || contactNumber.isBlank())
            return "All required fields must be filled."
        val clean = contactNumber.replace(Regex("[\\s-]"), "")
        if (!Regex("^(09|\\+639)\\d{9}$").matches(clean))
            return "Invalid number. Use 09XXXXXXXXX or +639XXXXXXXXX."
        if (password != confirmPassword) return "Passwords do not match."
        if (password.length < 8) return "Password must be at least 8 characters."
        return null
    }

    fun resetState() { _state.value = AuthState.Idle }
}