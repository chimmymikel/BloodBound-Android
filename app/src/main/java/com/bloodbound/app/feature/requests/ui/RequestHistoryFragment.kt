package com.bloodbound.app.feature.requests.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.formatDisplayDate
import com.bloodbound.app.databinding.FragmentRequestHistoryBinding
import com.bloodbound.app.databinding.ItemHistoryRequestBinding
import com.bloodbound.app.feature.requests.data.RequestDto
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RequestHistoryFragment : Fragment() {

    private var _binding: FragmentRequestHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RequestsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRequestHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        observeViewModel()
    }

    override fun onResume() { super.onResume(); viewModel.refresh() }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.swipeRefresh.isRefreshing = false
            when (state) {
                is RequestsUiState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvHistory.visibility   = View.GONE
                    binding.tvEmpty.visibility     = View.GONE
                }
                is RequestsUiState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    val fulfilled = state.requests.filter { it.status == "FULFILLED" }
                    if (fulfilled.isEmpty()) {
                        binding.rvHistory.visibility = View.GONE
                        binding.tvEmpty.visibility   = View.VISIBLE
                        binding.tvEmpty.text = "📂 No fulfilled requests yet.\nMark a request as fulfilled and it will appear here."
                    } else {
                        binding.tvEmpty.visibility   = View.GONE
                        binding.rvHistory.visibility = View.VISIBLE
                        binding.rvHistory.adapter    = HistoryAdapter(fulfilled)
                    }
                }
                is RequestsUiState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.tvEmpty.visibility     = View.VISIBLE
                    binding.tvEmpty.text           = "⚠ ${state.message}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }

    inner class HistoryAdapter(private val items: List<RequestDto>) :
        RecyclerView.Adapter<HistoryAdapter.VH>() {

        inner class VH(val b: ItemHistoryRequestBinding) : RecyclerView.ViewHolder(b.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemHistoryRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val req = items[position]
            holder.b.tvBloodType.text  = "${formatBloodType(req.bloodType)} · ${req.units} unit(s)"
            holder.b.tvHospital.text   = "📍 ${req.hospitalName ?: "—"}"
            holder.b.tvDate.text       = "Posted ${formatDisplayDate(req.createdAt)}"
            holder.b.tvDonorCount.text = "✔ ${req.commitmentCount ?: 0} donor(s) helped fulfill this request"
        }
    }
}