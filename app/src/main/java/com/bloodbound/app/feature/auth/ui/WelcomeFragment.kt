// FILE: app/src/main/java/com/bloodbound/app/feature/auth/ui/WelcomeFragment.kt
package com.bloodbound.app.feature.auth.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bloodbound.app.R
import com.bloodbound.app.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWelcomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWordmark()
        startOrbAnimations()
        startRingAnimations()
        startDropFloatAnimation()
        startFadeUpAnimations()
        setupClicks()
    }

    private fun setupWordmark() {
        binding.tvBlood.apply {
            gradientStartColor = 0xFFE63946.toInt()
            gradientEndColor   = 0xFFB91C1C.toInt()
        }
        binding.tvBound.apply {
            gradientStartColor = 0xFF1D4ED8.toInt()
            gradientEndColor   = 0xFF1E40AF.toInt()
        }
    }

    private fun startOrbAnimations() {
        val easing = AccelerateDecelerateInterpolator()

        // Red orb: translate X + Y, 8s period
        listOf(
            ObjectAnimator.ofFloat(binding.orbRed, "translationX", 0f, 30f, 0f).apply { duration = 8000 },
            ObjectAnimator.ofFloat(binding.orbRed, "translationY", 0f, -20f, 0f).apply { duration = 8000 },
            ObjectAnimator.ofFloat(binding.orbBlue, "translationX", 0f, -25f, 0f).apply { duration = 10000 },
            ObjectAnimator.ofFloat(binding.orbBlue, "translationY", 0f, 20f, 0f).apply { duration = 10000 }
        ).forEach { anim ->
            anim.repeatCount = ValueAnimator.INFINITE
            anim.interpolator = easing
            anim.start()
        }
    }

    private fun startRingAnimations() {
        listOf(binding.ring1, binding.ring2, binding.ring3).forEachIndexed { index, ringView ->
            val delay = index * 1200L
            listOf(
                ObjectAnimator.ofFloat(ringView, "scaleX", 1f, 1.03f, 1f),
                ObjectAnimator.ofFloat(ringView, "scaleY", 1f, 1.03f, 1f),
                ObjectAnimator.ofFloat(ringView, "alpha", 0.3f, 0.55f, 0.3f)
            ).forEach { anim ->
                anim.duration = 5000
                anim.startDelay = delay
                anim.repeatCount = ValueAnimator.INFINITE
                anim.interpolator = AccelerateDecelerateInterpolator()
                anim.start()
            }
        }
    }

    private fun startDropFloatAnimation() {
        ObjectAnimator.ofFloat(binding.tvDrop, "translationY", 0f, -12f, -6f, 0f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    // Staggered fadeUp matching React CSS animation delays
    private fun startFadeUpAnimations() {
        data class AnimTarget(val view: View, val delayMs: Long)

        val targets = listOf(
            AnimTarget(binding.tvStatusBadge, 50L),
            AnimTarget(binding.tvDrop,         150L),
            AnimTarget(binding.llWordmark,      250L),
            AnimTarget(binding.tvSubtitle,      350L),
            AnimTarget(binding.viewDivider,     350L),
            AnimTarget(binding.tvTagline,       450L),
            AnimTarget(binding.llPills,         550L),
            AnimTarget(binding.llButtons,       650L),
            AnimTarget(binding.llTrust,         750L)
        )

        targets.forEach { (view, delay) ->
            view.translationY = 24f
            view.alpha = 0f
            AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply { duration = 600 },
                    ObjectAnimator.ofFloat(view, "translationY", 24f, 0f).apply { duration = 600 }
                )
                startDelay = delay
                interpolator = DecelerateInterpolator()
                start()
            }
        }
    }

    private fun setupClicks() {
        binding.btnLogin.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_login)
        }
        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_welcome_to_register)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}