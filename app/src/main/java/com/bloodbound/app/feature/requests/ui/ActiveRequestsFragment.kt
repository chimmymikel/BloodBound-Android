package com.bloodbound.app.feature.requests.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bloodbound.app.databinding.FragmentActiveRequestsBinding

class ActiveRequestsFragment : Fragment() {
    private var _binding: FragmentActiveRequestsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentActiveRequestsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}