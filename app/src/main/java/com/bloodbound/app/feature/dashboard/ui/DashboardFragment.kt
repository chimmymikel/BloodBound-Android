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

    // ── Lifecycle ──────────────────────────────────────────────────────

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

    override fun onResume() {
        super.onResume()
        // Fetch fresh data from backend every time this screen becomes visible
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Setup ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    // ── Observers ──────────────────────────────────────────────────────

    private fun observeViewModel() {

        // 1. Static user info — name, blood type, role label
        //    Show placeholder for eligibility — wait for server response
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            val isDonor = user.role == "DONOR"

            binding.tvWelcome.text  = "Welcome, ${user.fullName} 👋"
            binding.tvSubtitle.text = if (isDonor)
                "Track your eligibility and find blood donation requests near you."
            else
                "Manage your blood requests and track incoming donor commitments."

            if (isDonor) {
                // Blood type card — static, never changes
                binding.labelStat2.text = "BLOOD TYPE"
                binding.valueStat2.text = formatBloodType(user.bloodType)
                binding.subStat2.text   = "Registered at sign-up"

                // Eligibility card — show placeholder until server responds
                binding.labelStat1.text = "YOUR STATUS"
                binding.valueStat1.text = "Checking…"
                binding.subStat1.text   = "Verifying eligibility with server"

                binding.tvRequestsHeader.text = "Nearby Blood Requests"

            } else {
                // Requester stat cards
                binding.labelStat1.text = "ACTIVE REQUESTS"
                binding.valueStat1.text = "—"
                binding.subStat1.text   = "Loading…"

                binding.labelStat2.text = "CONTACT"
                binding.valueStat2.text = user.contactNumber ?: "—"
                binding.subStat2.text   = "Primary contact for donors"

                binding.tvRequestsHeader.text = "Your Active Requests"
            }
        }

        // 2. Eligibility from server — use isEligible from server (authoritative)
        //    but recalculate daysLeft using Math.ceil to match the React web app
        //    (server uses floor division, React uses Math.ceil — 1 day difference)
        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe

            // Recalculate using ceil to match React web app exactly
            val user      = viewModel.user.value
            val localCalc = calcEligibility(user?.lastDonationDate)

            if (elig.isEligible || localCalc.eligible) {
                binding.valueStat1.text = "Ready to Donate ✔️"
                binding.subStat1.text   = "You can commit to active requests."
            } else {
                // localCalc.daysLeft uses Math.ceil — matches React web app
                binding.valueStat1.text = "In ${localCalc.daysLeft}d ⏳"
                binding.subStat1.text   = "56-day waiting period in progress."
            }
        }

        // 3. Request list — loading / success / error
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

                    // Update active count label for requesters
                    val user = viewModel.user.value
                    if (user?.role == "REQUESTER") {
                        val activeCount = state.requests.count { it.status == "ACTIVE" }
                        binding.valueStat1.text = if (activeCount > 0)
                            "$activeCount Active 📡"
                        else
                            "No Active Requests 💤"
                        binding.subStat1.text = if (activeCount > 0)
                            "Monitoring incoming commitments."
                        else
                            "You currently have no emergency requests."
                    }

                    if (state.requests.isEmpty()) {
                        binding.rvRequests.visibility  = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else {
                        binding.layoutEmpty.visibility = View.GONE
                        binding.rvRequests.visibility  = View.VISIBLE
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

    // ── Adapter ────────────────────────────────────────────────────────

    private fun setupAdapter(requests: List<RequestDto>) {
        val isDonor = viewModel.user.value?.role == "DONOR"
        binding.rvRequests.adapter = RequestSummaryAdapter(requests, isDonor)
    }
}