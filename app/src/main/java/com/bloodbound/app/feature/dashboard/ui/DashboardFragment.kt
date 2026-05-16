// FILE: app/src/main/java/com/bloodbound/app/feature/dashboard/ui/DashboardFragment.kt
package com.bloodbound.app.feature.dashboard.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bloodbound.app.core.util.FooterHelper
import com.bloodbound.app.core.util.calcEligibility
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.toTitleCase
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
        // binding.footer is a FooterComponentBinding — use .root to get the View
        FooterHelper.setup(binding.footer.root)
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

            binding.tvWelcome.text  = "Welcome, ${user.fullName?.toTitleCase()}\u00A0👋"

            binding.tvSubtitle.text = if (isDonor)
                "Track eligibility and find nearby requests."
            else
                "Manage requests and track donor commitments."

            if (isDonor) {

                // ── Row 1: YOUR STATUS (Full width card) ──────────────
                binding.labelStat1.text = "YOUR STATUS"
                binding.valueStat1.text = "Checking…"
                binding.subStat1.text   = "Verifying eligibility with server"

                // ── Row 2 Left: TOTAL DONATIONS ───────────────────────
                val donations = user.totalDonations ?: 0
                binding.labelStat3.text = "TOTAL DONATIONS"
                binding.valueStat3.text = donations.toString()
                binding.subStat3.text   = if (donations == 1)
                    "lifetime donation recorded"
                else
                    "lifetime donations recorded"

                // ── Row 2 Right: BLOOD TYPE ───────────────────────────
                binding.labelStat2.text = "BLOOD TYPE"
                binding.valueStat2.text = formatBloodType(user.bloodType)
                binding.valueStat2.textSize = 32f
                binding.subStat2.text   = "Registered at sign-up"

                // ── Row 3 Left: LAST DONATION DATE ────────────────────
                binding.labelStat4.text = "LAST DONATION"
                binding.valueStat4.text = if (!user.lastDonationDate.isNullOrBlank())
                    formatDate(user.lastDonationDate)
                else
                    "Never"
                binding.subStat4.text   = "last recorded date"

                // ── Row 3 Right: MEMBER SINCE ─────────────────────────
                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "account created"

                // ── Visibility ────────────────────────────────────────
                binding.cardStat3Container.visibility  = View.VISIBLE
                binding.rowStat45.visibility  = View.VISIBLE

                binding.tvRequestsHeader.text = "Nearby Blood Requests"

            } else {

                // ── Row 1: CURRENT STATUS (Full width card) ───────────
                binding.labelStat1.text = "CURRENT STATUS"
                binding.valueStat1.text = "Loading… 📡"
                binding.subStat1.text   = "Checking your active requests"

                // ── Row 2 Left: TOTAL POSTED ──────────────────────────
                binding.labelStat3.text = "TOTAL POSTED"
                binding.valueStat3.text = "—"
                binding.subStat3.text   = "Lifetime emergency requests"

                // ── Row 2 Right: SUCCESSFULLY FULFILLED ───────────────
                binding.labelStat2.text = "SUCCESSFULLY FULFILLED"
                binding.valueStat2.text = "—"
                binding.valueStat2.textSize = 32f
                binding.subStat2.text   = "Requests with enough donors"

                // ── Row 3 Left: CONTACT NUMBER ────────────────────────
                binding.labelStat4.text = "CONTACT NUMBER"
                binding.valueStat4.text = user.contactNumber ?: "—"
                binding.subStat4.text   = "Primary phone contact for donors"

                // ── Row 3 Right: MEMBER SINCE ─────────────────────────
                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "Thank you for being part of BloodBound"

                // ── Visibility ────────────────────────────────────────
                binding.cardStat3Container.visibility  = View.VISIBLE
                binding.rowStat45.visibility  = View.VISIBLE

                binding.tvRequestsHeader.text = "Your Active Requests"
            }
        }

        // 2. Eligibility — donors only
        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe

            val userLastDonation = viewModel.user.value?.lastDonationDate
            val eligibilityResult = calcEligibility(userLastDonation)

            if (eligibilityResult.eligible) {
                binding.valueStat1.text = "Ready to Donate ✔️"
                binding.subStat1.text   = "You can commit to active requests."
            } else {
                binding.valueStat1.text = "Eligible in ${eligibilityResult.daysLeft} days ⏳"
                binding.subStat1.text   = "56-day waiting period in progress."
            }

            val currentState = viewModel.requestsState.value
            if (currentState is DashboardUiState.Success) {
                setupAdapter(currentState.requests)
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
                        val activeCount = state.requests.count { it.status == "ACTIVE" }
                        binding.valueStat1.text = if (activeCount > 0)
                            "$activeCount Active 📡"
                        else
                            "No Active Requests 💤"
                        binding.subStat1.text = if (activeCount > 0)
                            "Monitoring incoming commitments."
                        else
                            "You currently have no emergency requests."

                        // We map total requests to Stat3 now (Left side row 2)
                        binding.valueStat3.text = state.requests.size.toString()

                        // We map fulfilled requests to Stat2 now (Right side row 2)
                        val fulfilledCount = state.requests.count { it.status == "FULFILLED" }
                        binding.valueStat2.text = fulfilledCount.toString()
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

        val userLastDonation = viewModel.user.value?.lastDonationDate
        val eligibilityResult = calcEligibility(userLastDonation)

        val filtered = if (isDonor) requests
        else requests.filter { it.status == "ACTIVE" }

        binding.rvRequests.adapter = RequestSummaryAdapter(
            items             = filtered,
            isDonor           = isDonor,
            isEligible        = eligibilityResult.eligible,
            daysUntilEligible = eligibilityResult.daysLeft
        )
    }
}