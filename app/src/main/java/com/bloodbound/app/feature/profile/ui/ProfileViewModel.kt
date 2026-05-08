// FILE: app/src/main/java/com/bloodbound/app/feature/profile/ui/ProfileViewModel.kt
package com.bloodbound.app.feature.profile.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.profile.data.EligibilityDto
import com.bloodbound.app.feature.profile.data.ProfileDto
import com.bloodbound.app.feature.profile.data.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _profile          = MutableLiveData<ProfileDto?>()
    val profile: LiveData<ProfileDto?> = _profile

    private val _eligibility      = MutableLiveData<EligibilityDto?>()
    val eligibility: LiveData<EligibilityDto?> = _eligibility

    private val _isLoading        = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    // Separate loading state for photo upload — shows overlay on the image
    private val _isUploadingPhoto = MutableLiveData(false)
    val isUploadingPhoto: LiveData<Boolean> = _isUploadingPhoto

    private val _toast            = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    private val _passwordSuccess  = MutableLiveData(false)
    val passwordSuccess: LiveData<Boolean> = _passwordSuccess

    private val _contactSaved     = MutableLiveData(false)
    val contactSaved: LiveData<Boolean> = _contactSaved

    init { loadData() }

    fun refresh() { loadData() }

    private fun loadData() {
        val user = tokenManager.getUser() ?: return
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getProfile(user.id)) {
                is ApiResult.Success -> {
                    _profile.value = r.data
                    // Keep local cache in sync
                    tokenManager.saveUser(r.data.toStoredUser())
                    // Load eligibility for donors
                    if (r.data.role == "DONOR") {
                        val elig = repository.checkEligibility(user.id)
                        if (elig is ApiResult.Success) _eligibility.value = elig.data
                    }
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    // ── Photo upload ──────────────────────────────────────────────────
    fun uploadPhoto(part: MultipartBody.Part) {
        val user = tokenManager.getUser() ?: return
        viewModelScope.launch {
            _isUploadingPhoto.value = true
            when (val r = repository.uploadPhoto(user.id, part)) {
                is ApiResult.Success -> {
                    _toast.value = "Profile photo updated! ✔"
                    // Reload to get the new base64 photo from server
                    loadData()
                }
                is ApiResult.Error -> {
                    _toast.value = "Upload failed: ${r.message}"
                }
                else -> Unit
            }
            _isUploadingPhoto.value = false
        }
    }

    // ── Contact update ────────────────────────────────────────────────
    fun updateContact(raw: String) {
        val clean = raw.trim().replace(Regex("[\\s-]"), "")
        val regex  = Regex("^(09|\\+639)\\d{9}$")
        if (!regex.matches(clean)) {
            _toast.value = "Invalid format. Use 09XXXXXXXXX or +639XXXXXXXXX"
            return
        }
        val user = tokenManager.getUser() ?: return
        viewModelScope.launch {
            when (val r = repository.updateContact(user.id, clean)) {
                is ApiResult.Success -> {
                    _profile.value = r.data
                    tokenManager.getUser()?.let {
                        tokenManager.saveUser(it.copy(contactNumber = clean))
                    }
                    _toast.value = "Contact number updated! ✔"
                    _contactSaved.value = true
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    // ── Password change ───────────────────────────────────────────────
    fun changePassword(old: String, new: String, confirm: String) {
        when {
            old.isBlank() || new.isBlank() || confirm.isBlank() ->
            { _toast.value = "All password fields are required."; return }
            new != confirm ->
            { _toast.value = "New passwords do not match."; return }
            new.length < 8 ->
            { _toast.value = "Password must be at least 8 characters."; return }
            new == old ->
            { _toast.value = "New password must differ from current."; return }
        }
        val user = tokenManager.getUser() ?: return
        viewModelScope.launch {
            when (val r = repository.updatePassword(user.id, old, new)) {
                is ApiResult.Success -> {
                    _toast.value = "Password changed successfully! ✔"
                    _passwordSuccess.value = true
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    // ── Logout ────────────────────────────────────────────────────────
    fun performLogout() {
        tokenManager.clearAll()
    }

    fun clearToast()           { _toast.value = null }
    fun clearPasswordSuccess() { _passwordSuccess.value = false }
    fun clearContactSaved()    { _contactSaved.value = false }
}

// Extension to convert ProfileDto → StoredUser for caching
private fun ProfileDto.toStoredUser() = StoredUser(
    id               = id,
    fullName         = fullName,
    email            = email,
    role             = role,
    contactNumber    = contactNumber,
    bloodType        = bloodType,
    totalDonations   = totalDonations,
    lastDonationDate = lastDonationDate,
    createdAt        = createdAt,
    profilePicture   = profilePicture
)