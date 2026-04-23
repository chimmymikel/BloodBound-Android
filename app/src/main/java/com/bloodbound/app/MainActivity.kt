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

    private val mainScreenIds = setOf(
        R.id.dashboardFragment,
        R.id.activeRequestsFragment,
        R.id.myCommitmentsFragment,
        R.id.requestHistoryFragment,
        R.id.profileFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.visibility =
                if (destination.id in mainScreenIds) View.VISIBLE else View.GONE
        }
    }

    fun setupBottomNav(isDonor: Boolean) {
        val menuRes = if (isDonor) R.menu.bottom_nav_donor else R.menu.bottom_nav_requester
        binding.bottomNav.menu.clear()
        binding.bottomNav.inflateMenu(menuRes)
        binding.bottomNav.setupWithNavController(navController)
    }
}