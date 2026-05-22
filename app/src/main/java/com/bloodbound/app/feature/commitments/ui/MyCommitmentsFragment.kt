package com.bloodbound.app.feature.commitments.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bloodbound.app.R
import com.bloodbound.app.databinding.FragmentMyCommitmentsBinding
import com.bloodbound.app.feature.commitments.data.CommitmentDto
import com.bloodbound.app.feature.commitments.ui.adapter.CommitmentAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyCommitmentsFragment : Fragment() {

    private var _binding: FragmentMyCommitmentsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CommitmentsViewModel by viewModels()

    private var currentTab = "PENDING"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyCommitmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvTickets.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        setupTabs()
        observeViewModel()
    }

    override fun onResume() { super.onResume(); viewModel.refresh() }

    private fun setupTabs() {
        binding.btnTabActive.setOnClickListener {
            currentTab = "PENDING"
            updateTabStyles()
            viewModel.commitments.value?.let { renderList(it) }
        }
        binding.btnTabCompleted.setOnClickListener {
            currentTab = "COMPLETED"
            updateTabStyles()
            viewModel.commitments.value?.let { renderList(it) }
        }
        binding.btnTabCancelled.setOnClickListener {
            currentTab = "CANCELLED"
            updateTabStyles()
            viewModel.commitments.value?.let { renderList(it) }
        }
        updateTabStyles() // apply initial state
    }

    private fun updateTabStyles() {
        val activeColor   = ContextCompat.getColor(requireContext(), R.color.text_primary)
        val inactiveColor = ContextCompat.getColor(requireContext(), R.color.text_muted)
        val whiteBg       = ContextCompat.getColor(requireContext(), R.color.surface_white)
        val clearBg       = android.graphics.Color.TRANSPARENT

        mapOf(
            binding.btnTabActive    to "PENDING",
            binding.btnTabCompleted to "COMPLETED",
            binding.btnTabCancelled to "CANCELLED"
        ).forEach { (btn, tab) ->
            val isActive = currentTab == tab
            btn.setTextColor(if (isActive) activeColor else inactiveColor)
            btn.backgroundTintList = ColorStateList.valueOf(if (isActive) whiteBg else clearBg)
        }
    }

    private fun observeViewModel() {

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.swipeRefresh.isRefreshing = false
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.commitments.observe(viewLifecycleOwner) { all ->
            // Update tab labels with counts
            binding.btnTabActive.text    = "Active (${all.count { it.status == "PENDING" }})"
            binding.btnTabCompleted.text = "Done (${all.count { it.status == "COMPLETED" }})"
            binding.btnTabCancelled.text = "Cancelled (${all.count { it.status == "CANCELLED" }})"

            // Update impact stats
            binding.tvActiveCount.text    = "${all.count { it.status == "PENDING" }}"
            binding.tvCompletedCount.text = "${all.count { it.status == "COMPLETED" }}"
            binding.tvLivesCount.text     = "${all.count { it.status == "COMPLETED" } * 3}"

            renderList(all)
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err ?: return@observe
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text       = "⚠ $err"
            viewModel.clearError()
        }

        viewModel.toast.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
            viewModel.clearToast()
        }
    }

    private fun renderList(all: List<CommitmentDto>) {
        val filtered = all.filter { it.status == currentTab }

        if (filtered.isEmpty()) {
            binding.rvTickets.visibility = View.GONE
            binding.tvEmpty.visibility   = View.VISIBLE
            binding.tvEmpty.text = when (currentTab) {
                "PENDING"   -> "No active tickets.\nCommit to a blood request to get started."
                "COMPLETED" -> "No completed donations yet."
                else        -> "No cancelled commitments."
            }
        } else {
            binding.tvEmpty.visibility   = View.GONE
            binding.rvTickets.visibility = View.VISIBLE
            binding.rvTickets.adapter    = CommitmentAdapter(
                items    = filtered,
                onCancel = { id ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Cancel Commitment?")
                        .setMessage("Are you sure you want to cancel? You can re-commit later if the request is still active.")
                        .setPositiveButton("Yes, Cancel") { _, _ -> viewModel.cancelCommitment(id) }
                        .setNegativeButton("Keep It", null)
                        .show()
                }
            )
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}