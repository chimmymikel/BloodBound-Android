package com.bloodbound.app.feature.dashboard.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.commitments.data.CommitmentsRepository
import com.bloodbound.app.feature.dashboard.data.DashboardRepository
import com.bloodbound.app.feature.dashboard.data.EligibilityDto
import com.bloodbound.app.feature.dashboard.data.RequestDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading                                     : DashboardUiState()
    data class Success(val requests: List<RequestDto>) : DashboardUiState()
    data class Error(val message: String)              : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository,
    private val commitmentsRepository: CommitmentsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _user = MutableLiveData<StoredUser?>()
    val user: LiveData<StoredUser?> = _user

    private val _requestsState = MutableLiveData<DashboardUiState>()
    val requestsState: LiveData<DashboardUiState> = _requestsState

    // Keeps EligibilityDto so the Fragment observer still works unchanged
    private val _eligibility = MutableLiveData<EligibilityDto?>()
    val eligibility: LiveData<EligibilityDto?> = _eligibility

    // ✅ NEW — request IDs the user has PENDING or COMPLETED commitments for
    private val _committedIds = MutableLiveData<Set<Long>>(emptySet())
    val committedIds: LiveData<Set<Long>> = _committedIds

    // ✅ NEW — true only when user has a PENDING commitment (blocks new commits)
    private val _hasPendingCommitment = MutableLiveData(false)
    val hasPendingCommitment: LiveData<Boolean> = _hasPendingCommitment

    init { loadData() }

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

                // ✅ Fetch commitments FIRST so committedIds and
                // hasPendingCommitment are ready before the request list renders
                val commitsResult = commitmentsRepository.getCommitments(
                    mapOf("donorId" to storedUser.id.toString())
                )
                if (commitsResult is ApiResult.Success) {
                    val allCommits = commitsResult.data
                    _hasPendingCommitment.value = allCommits.any { it.status == "PENDING" }
                    _committedIds.value = allCommits
                        .filter { it.status == "PENDING" || it.status == "COMPLETED" }
                        .map { it.requestId }
                        .toSet()
                }

                // Fetch server eligibility (authoritative 56-day check)
                val eligResult = repository.getEligibility(storedUser.id)
                if (eligResult is ApiResult.Success) {
                    _eligibility.value = eligResult.data
                }

                // Fetch matching blood requests for this donor
                when (val result = repository.getRequestsForDonor(storedUser.bloodType)) {
                    is ApiResult.Success ->
                        _requestsState.value = DashboardUiState.Success(result.data)
                    is ApiResult.Error ->
                        _requestsState.value = DashboardUiState.Error(result.message)
                    is ApiResult.Loading -> Unit
                }

            } else {
                // REQUESTER — fetch their own posted requests
                when (val result = repository.getRequestsForRequester(storedUser.id)) {
                    is ApiResult.Success ->
                        _requestsState.value = DashboardUiState.Success(result.data)
                    is ApiResult.Error ->
                        _requestsState.value = DashboardUiState.Error(result.message)
                    is ApiResult.Loading -> Unit
                }
            }
        }
    }

    // ✅ Called when user taps Commit on the Dashboard
    fun commitToDonate(requestId: Long) {
        viewModelScope.launch {
            when (val r = commitmentsRepository.createCommitment(requestId)) {
                is ApiResult.Success -> {
                    // Immediately update local state so UI reacts without re-fetching
                    _committedIds.value = (_committedIds.value ?: emptySet()) + requestId
                    _hasPendingCommitment.value = true
                }
                is ApiResult.Error -> {
                    // Surface error if you have a snackbar/toast mechanism
                }
                is ApiResult.Loading -> Unit
            }
        }
    }
}