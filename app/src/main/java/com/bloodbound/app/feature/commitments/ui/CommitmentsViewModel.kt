package com.bloodbound.app.feature.commitments.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bloodbound.app.core.network.ApiResult
import com.bloodbound.app.core.network.StoredUser
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.feature.commitments.data.CommitmentDto
import com.bloodbound.app.feature.commitments.data.CommitmentsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommitmentsViewModel @Inject constructor(
    private val repository: CommitmentsRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _user = MutableLiveData<StoredUser?>()
    val user: LiveData<StoredUser?> = _user

    private val _commitments = MutableLiveData<List<CommitmentDto>>(emptyList())
    val commitments: LiveData<List<CommitmentDto>> = _commitments

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _toast = MutableLiveData<String?>()
    val toast: LiveData<String?> = _toast

    init { loadData() }

    fun refresh() { loadData() }

    private fun loadData() {
        val user = tokenManager.getUser()
        _user.value = user ?: return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            when (val r = repository.getCommitments(mapOf("donorId" to user.id.toString()))) {
                is ApiResult.Success -> _commitments.value = r.data
                is ApiResult.Error   -> _error.value = r.message
                else -> Unit
            }
            _isLoading.value = false
        }
    }

    fun cancelCommitment(id: Long) {
        viewModelScope.launch {
            when (val r = repository.cancelCommitment(id)) {
                is ApiResult.Success -> {
                    _toast.value = "Commitment cancelled."
                    refresh()
                }
                is ApiResult.Error -> _toast.value = r.message
                else -> Unit
            }
        }
    }

    // Computed stats
    fun getActiveCount()    = _commitments.value?.count { it.status == "PENDING" } ?: 0
    fun getCompletedCount() = _commitments.value?.count { it.status == "COMPLETED" } ?: 0
    fun getCancelledCount() = _commitments.value?.count { it.status == "CANCELLED" } ?: 0
    fun getLivesImpacted()  = getCompletedCount() * 3

    fun clearToast() { _toast.value = null }
    fun clearError() { _error.value = null }
}