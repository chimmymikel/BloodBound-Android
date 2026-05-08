package com.bloodbound.app.feature.profile.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bloodbound.app.core.util.calcEligibility
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.formatDisplayDate
import com.bloodbound.app.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() { super.onResume(); viewModel.refresh() }

    private fun setupClickListeners() {
        // Edit contact toggle
        binding.btnEditContact.setOnClickListener {
            val isVisible = binding.layoutEditContact.visibility == View.VISIBLE
            binding.layoutEditContact.visibility = if (isVisible) View.GONE else View.VISIBLE
            if (!isVisible) {
                binding.etContact.setText(binding.tvContact.text)
            }
        }
        binding.btnSaveContact.setOnClickListener {
            viewModel.updateContact(binding.etContact.text.toString())
        }
        binding.btnCancelContact.setOnClickListener {
            binding.layoutEditContact.visibility = View.GONE
        }

        // Change password toggle
        binding.btnChangePassword.setOnClickListener {
            val isVisible = binding.layoutPassword.visibility == View.VISIBLE
            binding.layoutPassword.visibility = if (isVisible) View.GONE else View.VISIBLE
        }
        binding.btnSavePassword.setOnClickListener {
            viewModel.changePassword(
                old     = binding.etOldPassword.text.toString(),
                new     = binding.etNewPassword.text.toString(),
                confirm = binding.etConfirmPassword.text.toString()
            )
        }
        binding.btnCancelPassword.setOnClickListener {
            binding.layoutPassword.visibility = View.GONE
            binding.etOldPassword.text?.clear()
            binding.etNewPassword.text?.clear()
            binding.etConfirmPassword.text?.clear()
        }
    }

    private fun observeViewModel() {

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            // Could show a progress indicator here if desired
        }

        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe
            val isDonor = profile.role == "DONOR"

            binding.tvFullName.text    = profile.fullName
            binding.tvEmail.text       = profile.email
            binding.tvRole.text        = profile.role
            binding.tvContact.text     = profile.contactNumber ?: "—"
            binding.tvMemberSince.text = formatDisplayDate(profile.createdAt)

            if (isDonor) {
                binding.tvBloodType.text      = formatBloodType(profile.bloodType)
                binding.tvTotalDonations.text = "${profile.totalDonations ?: 0}"
                binding.tvLastDonation.text   = formatDisplayDate(profile.lastDonationDate)
                binding.layoutDonorStats.visibility = View.VISIBLE

                // Show client-side eligibility while server data loads
                val elig = calcEligibility(profile.lastDonationDate)
                binding.tvEligibilityStatus.text = if (elig.eligible)
                    "READY TO DONATE ✔️"
                else
                    "Eligible in ${elig.daysLeft} days ⏳"
                binding.cardEligibility.visibility = View.VISIBLE
            } else {
                binding.layoutDonorStats.visibility = View.GONE
                binding.cardEligibility.visibility  = View.GONE
            }
        }

        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe
            val profile   = viewModel.profile.value
            val localCalc = calcEligibility(profile?.lastDonationDate)

            binding.tvEligibilityStatus.text = if (elig.isEligible || localCalc.eligible)
                "READY TO DONATE ✔️"
            else
                "Eligible in ${localCalc.daysLeft} days ⏳"
        }

        viewModel.toast.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }

        viewModel.contactSaved.observe(viewLifecycleOwner) { saved ->
            if (!saved) return@observe
            binding.layoutEditContact.visibility = View.GONE
            viewModel.clearContactSaved()
        }

        viewModel.passwordSuccess.observe(viewLifecycleOwner) { success ->
            if (!success) return@observe
            binding.layoutPassword.visibility = View.GONE
            binding.etOldPassword.text?.clear()
            binding.etNewPassword.text?.clear()
            binding.etConfirmPassword.text?.clear()
            viewModel.clearPasswordSuccess()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}