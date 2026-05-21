package com.bloodbound.app.feature.requests.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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

    private var currentFilter: String = "ALL"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        setupFilters()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun setupFilters() {
        val filters = listOf(
            binding.chipFilterAll,
            binding.chipFilterCritical,
            binding.chipFilterHigh,
            binding.chipFilterStandard
        )

        binding.chipFilterAll.setOnClickListener {
            currentFilter = "ALL"
            setActiveFilter(binding.chipFilterAll, filters)
            reRenderList()
        }
        binding.chipFilterCritical.setOnClickListener {
            currentFilter = "CRITICAL"
            setActiveFilter(binding.chipFilterCritical, filters)
            reRenderList()
        }
        binding.chipFilterHigh.setOnClickListener {
            currentFilter = "HIGH"
            setActiveFilter(binding.chipFilterHigh, filters)
            reRenderList()
        }
        binding.chipFilterStandard.setOnClickListener {
            currentFilter = "STANDARD"
            setActiveFilter(binding.chipFilterStandard, filters)
            reRenderList()
        }
    }

    private fun setActiveFilter(selectedView: TextView, allFilters: List<TextView>) {
        allFilters.forEach { tv ->
            tv.setBackgroundResource(R.drawable.bg_filter_inactive)
            tv.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
        }

        selectedView.setBackgroundResource(R.drawable.bg_filter_active)
        selectedView.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_primary))
    }

    private fun reRenderList() {
        val currentState = viewModel.state.value
        if (currentState is RequestsUiState.Success) {
            renderSuccessState(currentState)
        }
    }

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
                    renderSuccessState(state)
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

        viewModel.navigateToCommitments.observe(viewLifecycleOwner) { should ->
            if (!should) return@observe
            viewModel.clearNavigateToCommitments()
            findNavController().navigate(R.id.myCommitmentsFragment)
        }

        binding.btnPostRequest.setOnClickListener {
            PostRequestDialog().show(childFragmentManager, "PostRequest")
        }
    }

    private fun renderSuccessState(state: RequestsUiState.Success) {
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility     = View.GONE

        val filteredList = if (currentFilter == "ALL") {
            state.requests
        } else {
            state.requests.filter { it.toString().contains(currentFilter, ignoreCase = true) }
        }

        if (filteredList.isEmpty()) {
            binding.rvRequests.visibility = View.GONE
            binding.tvEmpty.visibility    = View.VISIBLE
            binding.tvEmpty.text = if (state.requests.isEmpty()) "No active requests right now." else "No $currentFilter requests found."
        } else {
            binding.tvEmpty.visibility    = View.GONE
            binding.rvRequests.visibility = View.VISIBLE

            val user   = viewModel.user.value ?: return
            val isDonor = user.role == "DONOR"

            if (isDonor) {
                val elig       = calcEligibility(user.lastDonationDate)
                val committed  = viewModel.committedIds.value ?: emptySet()
                val hasActive  = committed.isNotEmpty()

                binding.rvRequests.adapter = DonorRequestAdapter(
                    items                = filteredList,
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
                    items     = filteredList,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}