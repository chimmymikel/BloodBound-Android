// FILE: app/src/main/java/com/bloodbound/app/MainActivity.kt
package com.bloodbound.app

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
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
            // User is already logged in! Route directly to the main app dashboard
            graph.setStartDestination(R.id.main_graph)

            // Because we skipped the login screen, we need to set up the bottom nav right now!
            val user = tokenManager.getUser()
            val isDonor = user?.role == "DONOR"
            setupBottomNav(isDonor)
        } else {
            // No token found. Route to the Welcome/Auth screen
            graph.setStartDestination(R.id.auth_graph)
        }

        // Attach our dynamically configured graph to the controller
        navController.graph = graph
        // ──────────────────────────────────────────────────────────────────

        // Your existing logic: Show bottom nav ONLY when inside main_graph
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility =
                if (destination.parent?.id == R.id.main_graph) View.VISIBLE else View.GONE
        }
    }

    fun setupBottomNav(isDonor: Boolean) {
        val menuRes = if (isDonor) R.menu.bottom_nav_donor else R.menu.bottom_nav_requester
        binding.bottomNav.menu.clear()
        binding.bottomNav.inflateMenu(menuRes)
        binding.bottomNav.setupWithNavController(navController)
    }
}