// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/RegisterFragment.kt
package com.bloodbound.app.feature.auth.ui

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bloodbound.app.R
import com.bloodbound.app.databinding.FragmentRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    private var selectedRole: String = ""

    private val bloodTypeLabels = listOf("O+", "O−", "A+", "A−", "B+", "B−", "AB+", "AB−")
    private val bloodTypeValues = listOf(
        "O_POSITIVE", "O_NEGATIVE", "A_POSITIVE", "A_NEGATIVE",
        "B_POSITIVE", "B_NEGATIVE", "AB_POSITIVE", "AB_NEGATIVE"
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWordmark()
        setupBloodTypeSpinner()
        setupStep1Clicks()
        setupStep2Clicks()
        observeState()
    }

    private fun setupWordmark() {
        binding.tvLogoBlood.apply {
            gradientStartColor = 0xFFE63946.toInt()
            gradientEndColor   = 0xFFB91C1C.toInt()
        }
        binding.tvLogoBound.apply {
            gradientStartColor = 0xFF1D4ED8.toInt()
            gradientEndColor   = 0xFF1E40AF.toInt()
        }
    }

    private fun setupBloodTypeSpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            bloodTypeLabels
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerBloodType.adapter = adapter
    }

    private fun setupStep1Clicks() {
        binding.cardDonor.setOnClickListener { selectRole("DONOR") }
        binding.cardRequester.setOnClickListener { selectRole("REQUESTER") }
        binding.btnAlreadyHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
        binding.tvBackHomeS1.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupStep2Clicks() {
        binding.tvStep2Back.setOnClickListener { goToStep1() }
        binding.btnSubmit.setOnClickListener { submitRegistration() }
        binding.btnSignInLink.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_login)
        }
    }

    private fun selectRole(role: String) {
        selectedRole = role
        goToStep2(role)
    }

    private fun goToStep2(role: String) {
        val isDonor = role == "DONOR"

        // Update role badge
        binding.tvRoleBadge.text = if (isDonor) "💉 DONOR" else "🏥 REQUESTER"
        binding.tvRoleBadge.setBackgroundResource(
            if (isDonor) R.drawable.bg_btn_red_gradient else R.drawable.bg_btn_blue_gradient
        )

        // Update form title + subtitle
        binding.tvFormTitle.text = if (isDonor) "Donor Registration" else "Requester Registration"
        binding.tvFormSubtitle.text = if (isDonor)
            "Your blood type helps us match you with urgent requests."
        else
            "Register to post blood requests and find donors fast."

        // Show/hide blood type
        binding.groupBloodType.isVisible = isDonor

        // Update submit button
        binding.btnSubmit.text = if (isDonor) "Register as Donor →" else "Register as Requester →"
        binding.btnSubmit.setBackgroundResource(
            if (isDonor) R.drawable.bg_btn_red_gradient else R.drawable.bg_btn_blue_gradient
        )

        // Switch visibility
        binding.groupStep1.isVisible = false
        binding.groupStep2.isVisible = true

        hideError()
    }

    private fun goToStep1() {
        binding.groupStep1.isVisible = true
        binding.groupStep2.isVisible = false
        selectedRole = ""
        hideError()
        viewModel.resetState()
    }

    private fun submitRegistration() {
        val bloodTypeIndex = binding.spinnerBloodType.selectedItemPosition
        viewModel.register(
            firstName       = binding.etFirstName.text.toString(),
            lastName        = binding.etLastName.text.toString(),
            email           = binding.etEmail.text.toString(),
            password        = binding.etPassword.text.toString(),
            confirmPassword = binding.etConfirmPassword.text.toString(),
            role            = selectedRole,
            contactNumber   = binding.etContact.text.toString(),
            bloodType       = if (selectedRole == "DONOR") bloodTypeValues[bloodTypeIndex] else null
        )
    }

    private fun observeState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnSubmit.isEnabled = false
                    binding.btnSubmit.text = getString(R.string.btn_creating_account)
                    hideError()
                }
                is AuthState.Success -> {
                    // ✅ Fixed: was action_register_to_dashboard (didn't exist)
                    findNavController().navigate(R.id.action_auth_to_main)
                }
                is AuthState.Error -> {
                    binding.btnSubmit.isEnabled = true
                    binding.btnSubmit.text = if (selectedRole == "DONOR")
                        "Register as Donor →" else "Register as Requester →"
                    showError(state.message)
                }
                is AuthState.Idle -> {
                    binding.btnSubmit.isEnabled = true
                    hideError()
                }
            }
        }
    }

    private fun showError(message: String) {
        binding.layoutError.isVisible = true
        binding.tvError.text = message
    }

    private fun hideError() {
        binding.layoutError.isVisible = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetState()
        _binding = null
    }
}