// FILE: app/src/main/java/com/bloodbound/app/feature/dashboard/ui/DashboardFragment.kt
package com.bloodbound.app.feature.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bloodbound.app.core.util.calcEligibility
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.databinding.FragmentDashboardBinding
import com.bloodbound.app.feature.dashboard.data.RequestDto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun observeViewModel() {
        // Populate user info cards
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            val isDonor = user.role == "DONOR"

            binding.tvWelcome.text = "Welcome, ${user.fullName} 👋"
            binding.tvSubtitle.text = if (isDonor)
                "Track your eligibility and find blood donation requests near you."
            else
                "Manage your blood requests and track incoming donor commitments."

            if (isDonor) {
                // Stat card 2: blood type
                binding.labelStat2.text = "BLOOD TYPE"
                binding.valueStat2.text = formatBloodType(user.bloodType)
                binding.subStat2.text   = "Registered at sign-up"

                // Stat card 1: eligibility (will update when eligibility loads)
                val elig = calcEligibility(user.lastDonationDate)
                binding.labelStat1.text = "YOUR STATUS"
                binding.valueStat1.text = if (elig.eligible) "Ready ✔️"
                else "In ${elig.daysLeft} days ⏳"
                binding.subStat1.text   = if (elig.eligible) "You can commit to requests."
                else "56-day waiting period in progress."

                binding.tvRequestsHeader.text = "Nearby Blood Requests"
            } else {
                binding.labelStat1.text = "ACTIVE REQUESTS"
                binding.labelStat2.text = "CONTACT"
                binding.valueStat2.text = user.contactNumber ?: "—"
                binding.subStat2.text   = "Primary contact for donors"
                binding.tvRequestsHeader.text = "Your Active Requests"
            }
        }

        // Server-side eligibility (overrides client-side calc for donors)
        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe
            binding.valueStat1.text = if (elig.isEligible) "Ready ✔️"
            else "In ${elig.daysUntilEligible}d ⏳"
            binding.subStat1.text   = elig.message ?: ""
        }

        // Request list
        viewModel.requestsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is DashboardUiState.Loading -> {
                    binding.progressRequests.visibility = View.VISIBLE
                    binding.rvRequests.visibility       = View.GONE
                    binding.layoutEmpty.visibility      = View.GONE
                    binding.tvError.visibility          = View.GONE
                }
                is DashboardUiState.Success -> {
                    binding.progressRequests.visibility = View.GONE
                    binding.tvError.visibility          = View.GONE

                    if (state.requests.isEmpty()) {
                        binding.rvRequests.visibility  = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvRequests.visibility  = View.VISIBLE

                        // Update active request count for requester
                        val user = viewModel.user.value
                        if (user?.role == "REQUESTER") {
                            val activeCount = state.requests.count { it.status == "ACTIVE" }
                            binding.valueStat1.text = "$activeCount Active"
                            binding.subStat1.text   = if (activeCount > 0)
                                "Monitoring donor commitments." else "No active emergencies."
                        }

                        setupAdapter(state.requests)
                    }
                }
                is DashboardUiState.Error -> {
                    binding.progressRequests.visibility = View.GONE
                    binding.rvRequests.visibility       = View.GONE
                    binding.layoutEmpty.visibility      = View.GONE
                    binding.tvError.visibility          = View.VISIBLE
                    binding.tvError.text                = "⚠ ${state.message}"
                }
            }
        }
    }

    private fun setupAdapter(requests: List<RequestDto>) {
        val isDonor = viewModel.user.value?.role == "DONOR"
        binding.rvRequests.adapter = RequestSummaryAdapter(requests, isDonor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}