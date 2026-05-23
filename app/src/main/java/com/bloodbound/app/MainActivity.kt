package com.bloodbound.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() { // ✅ REVERTED TO AppCompatActivity FOR HILT

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // 🩸 THIS MUST BE THE VERY FIRST LINE IN ONCREATE!
        installSplashScreen()

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // ── AUTO-LOGIN LOGIC ──────────────────────────────────────────────
        val navInflater = navController.navInflater
        val graph = navInflater.inflate(R.navigation.nav_graph)

        if (tokenManager.hasToken()) {
            graph.setStartDestination(R.id.main_graph)
            val user = tokenManager.getUser()
            val isDonor = user?.role == "DONOR"
            setupBottomNav(isDonor)
        } else {
            graph.setStartDestination(R.id.auth_graph)
        }

        navController.graph = graph
        // ──────────────────────────────────────────────────────────────────

        // ── LOGOUT BUTTON ─────────────────────────────────────────────────
        binding.btnLogout.setOnClickListener {
            showLogoutDialog()
        }
        // ──────────────────────────────────────────────────────────────────

        // Show Top Bar and Bottom Nav ONLY when inside main_graph
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isMainGraph = destination.parent?.id == R.id.main_graph

            if (isMainGraph) {
                binding.bottomNav.visibility = View.VISIBLE
                binding.appBarLayout.visibility = View.VISIBLE

                // 🩸 THE FIX: If the menu is empty (like right after registration), populate it!
                if (binding.bottomNav.menu.size() == 0) {
                    val user = tokenManager.getUser()
                    setupBottomNav(user?.role == "DONOR")
                }
            } else {
                binding.bottomNav.visibility = View.GONE
                binding.appBarLayout.visibility = View.GONE
            }
        }
    }

    fun setupBottomNav(isDonor: Boolean) {
        val menuRes = if (isDonor) R.menu.bottom_nav_donor else R.menu.bottom_nav_requester
        binding.bottomNav.menu.clear()
        binding.bottomNav.inflateMenu(menuRes)
        binding.bottomNav.setupWithNavController(navController)
    }

    // ── LOGOUT HELPERS ────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Log Out")
            // ✅ Updated the message to match your exact design
            .setMessage("Are you sure you want to log out of your account?")
            .setPositiveButton("Log Out") { d, _ ->
                d.dismiss()
                performLogout()
            }
            .setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            .show()

        // ✅ Made BOTH buttons red to match the attached screenshot
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            ?.setTextColor(android.graphics.Color.parseColor("#DC2626"))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
            ?.setTextColor(android.graphics.Color.parseColor("#DC2626"))
    }

    private fun performLogout() {
        // 1. Clear ALL stored session data (token + user)
        tokenManager.clearAll()

        // 2. Clear the bottom nav menu so it doesn't persist stale state
        binding.bottomNav.menu.clear()

        // 3. Navigate to auth_graph and clear the entire back stack
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.id, true)
            .build()

        navController.navigate(R.id.auth_graph, null, navOptions)
    }
}