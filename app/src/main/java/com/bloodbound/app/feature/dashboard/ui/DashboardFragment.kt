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
import java.text.SimpleDateFormat
import java.util.Locale

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

    // ── Helpers ────────────────────────────────────────────────────────

    /**
     * Converts "2024-11-01T00:00:00" or "2024-11-01" → "Nov 1, 2024"
     * Falls back to the raw string if parsing fails.
     */
    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        val cleanRaw = raw.substringBefore("T")
        return try {
            val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            output.format(input.parse(cleanRaw)!!)
        } catch (e: Exception) { raw }
    }

    // ── Observers ──────────────────────────────────────────────────────

    private fun observeViewModel() {

        // 1. Static user info
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            val isDonor = user.role == "DONOR"

            binding.tvWelcome.text  = "Welcome, ${user.fullName} 👋"
            binding.tvSubtitle.text = if (isDonor)
                "Track your eligibility and find blood donation requests near you."
            else
                "Manage your blood requests and track incoming donor commitments."

            if (isDonor) {

                // ── Row 1: YOUR STATUS | BLOOD TYPE ───────────────────
                binding.labelStat1.text = "YOUR STATUS"
                binding.valueStat1.text = "Checking…"
                binding.subStat1.text   = "Verifying eligibility with server"

                binding.labelStat2.text = "BLOOD TYPE"
                binding.valueStat2.text = formatBloodType(user.bloodType)
                binding.valueStat2.textSize = 32f
                binding.subStat2.text   = "Registered at sign-up"

                // ── Row 2: TOTAL DONATIONS (full width) ───────────────
                val donations = user.totalDonations ?: 0
                binding.labelStat3.text = "TOTAL DONATIONS"
                binding.valueStat3.text = donations.toString()
                binding.subStat3.text   = if (donations == 1)
                    "lifetime donation recorded"
                else
                    "lifetime donations recorded"

                // ── Row 3: LAST DONATION DATE | MEMBER SINCE ──────────
                binding.labelStat4.text = "LAST DONATION"
                binding.valueStat4.text = if (!user.lastDonationDate.isNullOrBlank())
                    formatDate(user.lastDonationDate)
                else
                    "Never"
                binding.subStat4.text   = "last recorded date"

                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "account created"

                // ── Visibility ────────────────────────────────────────
                binding.cardStat3.visibility  = View.VISIBLE
                binding.rowStat45.visibility  = View.VISIBLE

                binding.tvRequestsHeader.text = "Nearby Blood Requests"

            } else {

                // ── Row 1: CURRENT STATUS | TOTAL POSTED ──────────────
                binding.labelStat1.text = "CURRENT STATUS"
                binding.valueStat1.text = "Loading… 📡"
                binding.subStat1.text   = "Checking your active requests"

                binding.labelStat2.text = "TOTAL POSTED"
                binding.valueStat2.text = "—"
                binding.valueStat2.textSize = 32f
                binding.subStat2.text   = "Lifetime emergency requests"

                // ── Row 2: SUCCESSFULLY FULFILLED (full width) ────────
                binding.labelStat3.text = "SUCCESSFULLY FULFILLED"
                binding.valueStat3.text = "—"
                binding.subStat3.text   = "Requests with enough donors"

                // ── Row 3: CONTACT NUMBER | MEMBER SINCE ──────────────
                binding.labelStat4.text = "CONTACT NUMBER"
                binding.valueStat4.text = user.contactNumber ?: "—"
                binding.subStat4.text   = "Primary phone contact for donors"

                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "Thank you for being part of BloodBound"

                // ── Visibility ────────────────────────────────────────
                binding.cardStat3.visibility  = View.VISIBLE
                binding.rowStat45.visibility  = View.VISIBLE

                binding.tvRequestsHeader.text = "Your Active Requests"
            }
        }

        // 2. Eligibility — donors only
        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe

            val user      = viewModel.user.value
            val localCalc = calcEligibility(user?.lastDonationDate)

            if (elig.isEligible || localCalc.eligible) {
                binding.valueStat1.text = "Ready to Donate ✔️"
                binding.subStat1.text   = "You can commit to active requests."
            } else {
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

                    val user = viewModel.user.value

                    if (user?.role == "REQUESTER") {
                        // ── CURRENT STATUS ─────────────────────────────
                        val activeCount = state.requests.count { it.status == "ACTIVE" }
                        binding.valueStat1.text = if (activeCount > 0)
                            "$activeCount Active 📡"
                        else
                            "No Active Requests 💤"
                        binding.subStat1.text = if (activeCount > 0)
                            "Monitoring incoming commitments."
                        else
                            "You currently have no emergency requests."

                        // ── TOTAL POSTED ───────────────────────────────
                        binding.valueStat2.text = state.requests.size.toString()

                        // ── SUCCESSFULLY FULFILLED ─────────────────────
                        val fulfilledCount = state.requests.count { it.status == "FULFILLED" }
                        binding.valueStat3.text = fulfilledCount.toString()
                    }

                    if (state.requests.none { it.status == "ACTIVE" } &&
                        viewModel.user.value?.role == "REQUESTER") {
                        binding.rvRequests.visibility  = View.GONE
                        binding.layoutEmpty.visibility = View.VISIBLE
                    } else if (state.requests.isEmpty()) {
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
        // For requesters, only show ACTIVE requests in the list
        val filtered = if (isDonor) requests
        else requests.filter { it.status == "ACTIVE" }
        binding.rvRequests.adapter = RequestSummaryAdapter(filtered, isDonor)
    }
}