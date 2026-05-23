package com.bloodbound.app.feature.dashboard.ui

import android.graphics.Color
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

    // Flag to ensure we only setup the footer once the user data arrives
    private var isFooterSetup = false

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
        // REMOVED: FooterHelper.setup(binding.footer.root) from here to prevent the blank footer bug
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        isFooterSetup = false // Reset flag when view is destroyed
    }

    // ── Setup ──────────────────────────────────────────────────────────

    private fun setupRecyclerView() {
        binding.rvRequests.layoutManager = LinearLayoutManager(requireContext())
    }

    // ── Helpers ────────────────────────────────────────────────────────

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        val cleanRaw = raw.substringBefore("T")
        return try {
            val input  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val output = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            output.format(input.parse(cleanRaw)!!)
        } catch (e: Exception) { raw }
    }

    /**
     * Colors the YOUR STATUS card (id: card_stat1) based on role and status:
     *
     * DONOR_READY → #F0FDF4 / #16A34A (Green)
     * DONOR_WAIT  → #FEF2F2 / #DC2626 (Red)
     * REQ_ACTIVE  → #EFF6FF / #BFDBFE (Blue)
     * NEUTRAL     → #FFFFFF / #E5E7EB (Gray)
     */
    private fun applyStatusCardColor(state: String) {
        val (fill, stroke, iconFill) = when (state) {
            "DONOR_READY" -> Triple("#F0FDF4", "#16A34A", "#DCFCE7") // Green theme
            "DONOR_WAIT"  -> Triple("#FEF2F2", "#DC2626", "#FFD6D6") // Red theme
            "REQ_ACTIVE"  -> Triple("#EFF6FF", "#BFDBFE", "#DBEAFE") // Blue theme
            else          -> Triple("#FFFFFF", "#E5E7EB", "#F3F4F6") // Neutral / loading
        }

        binding.cardStat1.setCardBackgroundColor(Color.parseColor(fill))
        binding.cardStat1.setStrokeColor(Color.parseColor(stroke))
        binding.cardStat1Icon.setCardBackgroundColor(Color.parseColor(iconFill))
    }

    // ── Observers ──────────────────────────────────────────────────────

    private fun observeViewModel() {

        // 1. Static user info
        viewModel.user.observe(viewLifecycleOwner) { user ->
            if (user == null) return@observe

            val isDonor = user.role == "DONOR"

            // ✅ Setup the footer ONLY AFTER we know the user's role
            if (!isFooterSetup) {
                FooterHelper.setup(binding.footer.root)
                isFooterSetup = true
            }

            binding.tvWelcome.text  = "Welcome, ${user.fullName?.toTitleCase()}\u00A0👋"
            binding.tvSubtitle.text = if (isDonor)
                "Track eligibility and find nearby requests."
            else
                "Manage requests and track donor commitments."

            // Reset card to NEUTRAL on every reload
            applyStatusCardColor("NEUTRAL")

            if (isDonor) {
                binding.cardStat1Icon.visibility = View.VISIBLE
                binding.tvStat1Icon.text = "⏳"

                binding.labelStat1.text = "YOUR STATUS"
                binding.valueStat1.text = "Checking…"
                binding.valueStat1.setTextColor(Color.parseColor("#111827")) // default text color
                binding.subStat1.text   = "Verifying eligibility with server"

                val donations = user.totalDonations ?: 0
                binding.labelStat3.text = "TOTAL DONATIONS"
                binding.valueStat3.text = donations.toString()
                binding.subStat3.text   = if (donations == 1)
                    "lifetime donation recorded"
                else
                    "lifetime donations recorded"

                binding.labelStat2.text     = "BLOOD TYPE"
                binding.valueStat2.text     = formatBloodType(user.bloodType)
                binding.valueStat2.textSize = 32f
                binding.subStat2.text       = "Registered at sign-up"

                binding.labelStat4.text = "LAST DONATION"
                binding.valueStat4.text = if (!user.lastDonationDate.isNullOrBlank())
                    formatDate(user.lastDonationDate)
                else
                    "No Record"
                binding.subStat4.text   = "last recorded date"

                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "account created"

                binding.cardStat3Container.visibility = View.VISIBLE
                binding.rowStat45.visibility          = View.VISIBLE
                binding.tvRequestsHeader.text         = "Nearby Blood Requests"

            } else {
                binding.cardStat1Icon.visibility = View.VISIBLE
                binding.tvStat1Icon.text = "⏳"

                binding.labelStat1.text = "CURRENT STATUS"
                binding.valueStat1.text = "Loading…"
                binding.valueStat1.setTextColor(Color.parseColor("#111827")) // default text color
                binding.subStat1.text   = "Checking your active requests"

                binding.labelStat3.text = "TOTAL POSTED"
                binding.valueStat3.text = "—"
                binding.subStat3.text   = "Lifetime emergency requests"

                binding.labelStat2.text     = "SUCCESSFULLY FULFILLED"
                binding.valueStat2.text     = "—"
                binding.valueStat2.textSize = 32f
                binding.subStat2.text       = "Requests with enough donors"

                binding.labelStat4.text = "CONTACT NUMBER"
                binding.valueStat4.text = user.contactNumber ?: "—"
                binding.subStat4.text   = "Primary phone contact for donors"

                binding.labelStat5.text = "MEMBER SINCE"
                binding.valueStat5.text = formatDate(user.createdAt)
                binding.subStat5.text   = "Thank you for being part of BloodBound"

                binding.cardStat3Container.visibility = View.VISIBLE
                binding.rowStat45.visibility          = View.VISIBLE
                binding.tvRequestsHeader.text         = "Your Active Requests"
            }
        }

        // 2. Eligibility — donors only
        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe

            val userLastDonation  = viewModel.user.value?.lastDonationDate
            val eligibilityResult = calcEligibility(userLastDonation)

            if (eligibilityResult.eligible) {
                binding.valueStat1.text = "Ready to Donate"
                binding.subStat1.text   = "You can commit to active requests."
                binding.valueStat1.setTextColor(Color.parseColor("#16A34A")) // Green text
                binding.tvStat1Icon.text = "✅"
                applyStatusCardColor("DONOR_READY")
            } else {
                binding.valueStat1.text = "Eligible in ${eligibilityResult.daysLeft} days"
                binding.subStat1.text   = "56-day waiting period in progress."
                binding.valueStat1.setTextColor(Color.parseColor("#DC2626")) // Red text
                binding.tvStat1Icon.text = "⏳"
                applyStatusCardColor("DONOR_WAIT")
            }

            val currentState = viewModel.requestsState.value
            if (currentState is DashboardUiState.Success) {
                setupAdapter(currentState.requests)
            }
        }

        // 3. Request list
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

                        if (activeCount > 0) {
                            val reqWord = if (activeCount == 1) "Request" else "Requests"
                            binding.valueStat1.text = "$activeCount Active $reqWord"
                            binding.valueStat1.setTextColor(Color.parseColor("#2563EB")) // Standard Blue
                            binding.subStat1.text = "Monitoring incoming donor commitments."
                            binding.tvStat1Icon.text = "📡"
                            applyStatusCardColor("REQ_ACTIVE")
                        } else {
                            binding.valueStat1.text = "No Active Requests"
                            binding.valueStat1.setTextColor(Color.parseColor("#111827")) // Default Dark
                            binding.subStat1.text = "You currently have no emergency requests."
                            binding.tvStat1Icon.text = "💤"
                            applyStatusCardColor("NEUTRAL")
                        }

                        binding.valueStat3.text = state.requests.size.toString()
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

        // 4. Re-render when committed IDs change
        viewModel.committedIds.observe(viewLifecycleOwner) {
            val currentState = viewModel.requestsState.value
            if (currentState is DashboardUiState.Success) {
                setupAdapter(currentState.requests)
            }
        }

        // 5. Re-render when pending commitment status changes
        viewModel.hasPendingCommitment.observe(viewLifecycleOwner) {
            val currentState = viewModel.requestsState.value
            if (currentState is DashboardUiState.Success) {
                setupAdapter(currentState.requests)
            }
        }
    }

    // ── Adapter ────────────────────────────────────────────────────────

    private fun setupAdapter(requests: List<RequestDto>) {
        val isDonor           = viewModel.user.value?.role == "DONOR"
        val userLastDonation  = viewModel.user.value?.lastDonationDate
        val eligibilityResult = calcEligibility(userLastDonation)

        val filtered = if (isDonor) requests
        else requests.filter { it.status == "ACTIVE" }

        binding.rvRequests.adapter = RequestSummaryAdapter(
            items               = filtered,
            isDonor             = isDonor,
            isEligible          = eligibilityResult.eligible,
            daysUntilEligible   = eligibilityResult.daysLeft,
            committedIds        = viewModel.committedIds.value ?: emptySet(),
            hasActiveCommitment = viewModel.hasPendingCommitment.value ?: false,
            onCommit            = { req -> viewModel.commitToDonate(req.id) }
        )
    }
}