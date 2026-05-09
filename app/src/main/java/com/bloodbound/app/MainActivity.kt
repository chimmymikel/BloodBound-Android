// FILE: app/src/main/java/com/bloodbound/app/MainActivity.kt
package com.bloodbound.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bloodbound.app.core.network.TokenManager
import com.bloodbound.app.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
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

            binding.bottomNav.visibility =
                if (isMainGraph) View.VISIBLE else View.GONE

            binding.appBarLayout.visibility =
                if (isMainGraph) View.VISIBLE else View.GONE
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
        AlertDialog.Builder(this)
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { dialog, _ ->
                dialog.dismiss()
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        // 1. Clear ALL stored session data (token + user)
        tokenManager.clearAll()

        // 2. Clear the bottom nav menu so it doesn't persist stale state
        binding.bottomNav.menu.clear()

        // 3. Navigate to auth_graph and clear the entire back stack
        //    using NavOptions.Builder() to avoid DSL resolution issues
        val navOptions = NavOptions.Builder()
            .setPopUpTo(navController.graph.id, true)
            .build()

        navController.navigate(R.id.auth_graph, null, navOptions)
    }
    // ──────────────────────────────────────────────────────────────────────
}