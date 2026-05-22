package com.bloodbound.app.feature.requests.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.commitments.data.CommitmentsRepository
import com.bloodbound.app.feature.requests.data.CreateRequestBody
import com.bloodbound.app.feature.requests.data.HospitalDto
import com.bloodbound.app.feature.requests.data.RequestDto
import com.bloodbound.app.feature.requests.data.RequestsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class RequestsUiState {
    object Loading : RequestsUiState()
    data class Success(val requests: List<RequestDto>) : RequestsUiState()
    data class Error(val message: String) : RequestsUiState()
}

@HiltViewModel
class RequestsViewModel @Inject constructor(
    private val requestsRepository: RequestsRepository,
    private val commitmentsRepository: CommitmentsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _user = MutableLiveData<StoredUser?>()
    val user: LiveData<StoredUser?> = _user

    private val _state = MutableLiveData<RequestsUiState>()
    val state: LiveData<RequestsUiState> = _state

    private val _committedIds = MutableLiveData<Set<Long>>(emptySet())
    val committedIds: LiveData<Set<Long>> = _committedIds

    // ✅ NEW — tracks whether user has an active PENDING commitment
    private val _hasPendingCommitment = MutableLiveData(false)
    val hasPendingCommitment: LiveData<Boolean> = _hasPendingCommitment

    private val _hospitals = MutableLiveData<List<HospitalDto>>(emptyList())
    val hospitals: LiveData<List<HospitalDto>> = _hospitals

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    private val _navigateToCommitments = MutableLiveData(false)
    val navigateToCommitments: LiveData<Boolean> = _navigateToCommitments

    init { loadData() }

    fun refresh() { loadData() }

    private fun loadData() {
        val user = tokenManager.getUser()
        _user.value = user ?: return

        viewModelScope.launch {
            _state.value = RequestsUiState.Loading

            if (user.role == "DONOR") {
                val commitsResult = commitmentsRepository.getCommitments(
                    mapOf("donorId" to user.id.toString())
                )
                if (commitsResult is ApiResult.Success) {
                    val allCommits = commitsResult.data

                    // ✅ FIXED — only PENDING blocks the user from new commits
                    _hasPendingCommitment.value = allCommits.any { it.status == "PENDING" }

                    // committedIds still includes PENDING + COMPLETED
                    // so we can show "✔ Committed" on requests the user already joined
                    _committedIds.value = allCommits
                        .filter { it.status == "PENDING" || it.status == "COMPLETED" }
                        .map { it.requestId }
                        .toSet()
                }

                val params = mutableMapOf("status" to "ACTIVE")
                if (user.bloodType != null && user.bloodType != "O_NEGATIVE") {
                    params["bloodType"] = user.bloodType
                }
                when (val r = requestsRepository.getRequests(params)) {
                    is ApiResult.Success -> _state.value = RequestsUiState.Success(r.data)
                    is ApiResult.Error   -> _state.value = RequestsUiState.Error(r.message)
                    else -> Unit
                }

            } else {
                val params = mapOf("requesterId" to user.id.toString())
                when (val r = requestsRepository.getRequests(params)) {
                    is ApiResult.Success -> _state.value = RequestsUiState.Success(r.data)
                    is ApiResult.Error   -> _state.value = RequestsUiState.Error(r.message)
                    else -> Unit
                }
            }
        }
    }

    fun loadHospitals() {
        viewModelScope.launch {
            val r = requestsRepository.getHospitals()
            if (r is ApiResult.Success) _hospitals.value = r.data
        }
    }

    fun commitToDonate(requestId: Long) {
        viewModelScope.launch {
            when (val r = commitmentsRepository.createCommitment(requestId)) {
                is ApiResult.Success -> {
                    _committedIds.value = (_committedIds.value ?: emptySet()) + requestId
                    // ✅ FIXED — immediately flag that user now has a pending commitment
                    // so other requests switch to "Unavailable" right away
                    _hasPendingCommitment.value = true
                    _toast.value = "Committed! Go to My Commitments for details. 🩸"
                    _navigateToCommitments.value = true
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    fun fulfillRequest(requestId: Long) {
        viewModelScope.launch {
            when (val r = requestsRepository.fulfillRequest(requestId)) {
                is ApiResult.Success -> {
                    _toast.value = "Request fulfilled! 🎉"
                    refresh()
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    fun postRequest(body: CreateRequestBody) {
        viewModelScope.launch {
            when (val r = requestsRepository.createRequest(body)) {
                is ApiResult.Success -> {
                    _toast.value = "Request posted successfully! 🩸"
                    kotlinx.coroutines.delay(300)
                    refresh()
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    fun clearToast()                 { _toast.value = null }
    fun clearNavigateToCommitments() { _navigateToCommitments.value = false }
}