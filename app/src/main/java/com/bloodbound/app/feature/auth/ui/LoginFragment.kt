// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/LoginFragment.kt
package com.bloodbound.app.feature.auth.ui

import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bloodbound.app.R
import com.bloodbound.app.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()
    private var passwordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWordmark()
        setupPasswordToggle()
        setupClicks()
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

    private fun setupPasswordToggle() {
        binding.tvTogglePassword.setOnClickListener {
            passwordVisible = !passwordVisible
            binding.etPassword.apply {
                transformationMethod = if (passwordVisible)
                    HideReturnsTransformationMethod.getInstance()
                else
                    PasswordTransformationMethod.getInstance()
                // Keep cursor at end
                setSelection(text?.length ?: 0)
            }
            binding.tvTogglePassword.text = if (passwordVisible) "🙈" else "👁️"
        }
    }

    private fun setupClicks() {
        binding.btnSignIn.setOnClickListener {
            viewModel.login(
                email    = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString()
            )
        }
        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }
        binding.tvBackHome.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun observeState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    binding.btnSignIn.isEnabled = false
                    binding.btnSignIn.text = "Signing in…"
                    hideError()
                }
                is AuthState.Success -> {
                    // Navigate to dashboard — pass user via nav args or shared ViewModel
                    findNavController().navigate(R.id.action_login_to_dashboard)
                }
                is AuthState.Error -> {
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = "Sign In →"
                    showError(state.message)
                }
                is AuthState.Idle -> {
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = "Sign In →"
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