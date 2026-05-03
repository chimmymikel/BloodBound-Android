// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/LoginFragment.kt
package com.bloodbound.app.feature.auth.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bloodbound.app.MainActivity
import com.bloodbound.app.R
import com.bloodbound.app.databinding.FragmentLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    // Hilt injects the ViewModel — shared with RegisterFragment
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeAuthState()
    }

    private fun setupClickListeners() {
        // Sign In button
        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        // Done on keyboard also triggers login
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }

        // Navigate to Register
        binding.btnCreateAccount.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        // Back to Welcome
        binding.tvBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun attemptLogin() {
        val email    = binding.etEmail.text?.toString()?.trim() ?: ""
        val password = binding.etPassword.text?.toString() ?: ""
        viewModel.login(email, password)
    }

    private fun observeAuthState() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Idle -> {
                    showIdle()
                }

                is AuthState.Loading -> {
                    showLoading()
                }

                is AuthState.Success -> {
                    showIdle()
                    // Tell MainActivity which bottom nav menu to use
                    val isDonor = state.role == "DONOR"
                    (requireActivity() as MainActivity).setupBottomNav(isDonor)

                    // Ensure we only navigate if we are currently inside the Auth Graph
                    if (findNavController().currentDestination?.parent?.id == R.id.auth_graph) {
                        findNavController().navigate(R.id.action_auth_to_main)
                        viewModel.resetState()
                    }
                }

                is AuthState.Error -> {
                    showIdle()
                    showError(state.message)
                }
            }
        }
    }

    private fun showLoading() {
        binding.btnLogin.isEnabled  = false
        binding.btnLogin.text       = "Signing in…"
        binding.progressBar.visibility = View.VISIBLE
        binding.layoutError.visibility  = View.GONE
    }

    private fun showIdle() {
        binding.btnLogin.isEnabled  = true
        binding.btnLogin.text       = "Sign In →"
        binding.progressBar.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.layoutError.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}