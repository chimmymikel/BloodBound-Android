// FILE: app/src/main/java/com/bloodbound/app/feature/dashboard/ui/DashboardViewModel.kt
package com.bloodbound.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.dashboard.data.DashboardRepository
import com.bloodbound.app.feature.dashboard.data.EligibilityDto
import com.bloodbound.app.feature.dashboard.data.RequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── UI state for the request list section ─────────────────────────────
sealed class DashboardUiState {
    object Loading                                     : DashboardUiState()
    data class Success(val requests: List<RequestDto>) : DashboardUiState()
    data class Error(val message: String)              : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    // The logged-in user read from EncryptedSharedPreferences
    private val _user = MutableLiveData<StoredUser?>()
    val user: LiveData<StoredUser?> = _user

    // Request list state
    private val _requestsState = MutableLiveData<DashboardUiState>()
    val requestsState: LiveData<DashboardUiState> = _requestsState

    // Donor-only eligibility from server
    private val _eligibility = MutableLiveData<EligibilityDto?>()
    val eligibility: LiveData<EligibilityDto?> = _eligibility

    init {
        // Load on first creation
        loadData()
    }

    // Called by onResume() — clears stale eligibility so UI shows "Checking…"
    fun refresh() {
        _eligibility.value = null
        loadData()
    }

    private fun loadData() {
        val storedUser = tokenManager.getUser()
        _user.value = storedUser

        if (storedUser == null) return

        viewModelScope.launch {
            _requestsState.value = DashboardUiState.Loading

            if (storedUser.role == "DONOR") {
                // Fetch eligibility from server first — this is the authoritative number
                val eligResult = repository.getEligibility(storedUser.id)
                if (eligResult is ApiResult.Success) {
                    _eligibility.value = eligResult.data
                }

                // Then fetch matching blood requests
                when (val result = repository.getRequestsForDonor(storedUser.bloodType)) {
                    is ApiResult.Success ->
                        _requestsState.value = DashboardUiState.Success(result.data)
                    is ApiResult.Error   ->
                        _requestsState.value = DashboardUiState.Error(result.message)
                    is ApiResult.Loading -> Unit
                }

            } else {
                // REQUESTER — fetch their own posted requests
                when (val result = repository.getRequestsForRequester(storedUser.id)) {
                    is ApiResult.Success ->
                        _requestsState.value = DashboardUiState.Success(result.data)
                    is ApiResult.Error   ->
                        _requestsState.value = DashboardUiState.Error(result.message)
                    is ApiResult.Loading -> Unit
                }
            }
        }
    }
}