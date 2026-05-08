package com.bloodbound.app.feature.requests.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bloodbound.app.R
import com.bloodbound.app.core.util.calcEligibility
import com.bloodbound.app.databinding.FragmentActiveRequestsBinding
import com.bloodbound.app.feature.requests.ui.adapter.DonorRequestAdapter
import com.bloodbound.app.feature.requests.ui.adapter.RequesterRequestAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActiveRequestsFragment : Fragment() {

    private var _binding: FragmentActiveRequestsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RequestsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        observeViewModel()
    }

    override fun onResume() { super.onResume(); viewModel.refresh() }

    private fun observeViewModel() {

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            val isDonor = user.role == "DONOR"
            binding.tvTitle.text    = "Active Requests 🚨"
            binding.tvSubtitle.text = if (isDonor)
                "Browse emergencies and active requests."
            else
                "Manage your active blood postings and check donor commitments."
            binding.btnPostRequest.visibility = if (isDonor) View.GONE else View.VISIBLE
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is RequestsUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvRequests.visibility  = View.GONE
                    binding.tvEmpty.visibility     = View.GONE
                    binding.tvError.visibility     = View.GONE
                }
                is RequestsUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvError.visibility     = View.GONE

                    if (state.requests.isEmpty()) {
                        binding.rvRequests.visibility = View.GONE
                        binding.tvEmpty.visibility    = View.VISIBLE
                        binding.tvEmpty.text = "No active requests right now."
                    } else {
                        binding.tvEmpty.visibility    = View.GONE
                        binding.rvRequests.visibility = View.VISIBLE

                        val user   = viewModel.user.value ?: return@observe
                        val isDonor = user.role == "DONOR"

                        if (isDonor) {
                            val elig       = calcEligibility(user.lastDonationDate)
                            val committed  = viewModel.committedIds.value ?: emptySet()
                            val hasActive  = committed.isNotEmpty()

                            binding.rvRequests.adapter = DonorRequestAdapter(
                                items                = state.requests,
                                committedIds         = committed,
                                isEligible           = elig.eligible,
                                daysLeft             = elig.daysLeft,
                                hasActiveCommitment  = hasActive,
                                onCommit             = { requestId ->
                                    viewModel.commitToDonate(requestId)
                                }
                            )
                        } else {
                            binding.rvRequests.adapter = RequesterRequestAdapter(
                                items     = state.requests,
                                onFulfill = { requestId ->
                                    AlertDialog.Builder(requireContext())
                                        .setTitle("Mark as Fulfilled?")
                                        .setMessage("This will close the request and notify committed donors.")
                                        .setPositiveButton("Yes, Fulfill") { _, _ ->
                                            viewModel.fulfillRequest(requestId)
                                        }
                                        .setNegativeButton("Cancel", null)
                                        .show()
                                }
                            )
                        }
                    }
                }
                is RequestsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvRequests.visibility  = View.GONE
                    binding.tvEmpty.visibility     = View.GONE
                    binding.tvError.visibility     = View.VISIBLE
                    binding.tvError.text           = "⚠ ${state.message}"
                }
            }
        }

        viewModel.toast.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }

        // Navigate to My Commitments after a donor commits
        viewModel.navigateToCommitments.observe(viewLifecycleOwner) { should ->
            if (!should) return@observe
            viewModel.clearNavigateToCommitments()
            findNavController().navigate(R.id.myCommitmentsFragment)
        }

        binding.btnPostRequest.setOnClickListener {
            PostRequestDialog().show(childFragmentManager, "PostRequest")
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}