package com.bloodbound.app.feature.commitments.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.bloodbound.app.databinding.FragmentMyCommitmentsBinding

class MyCommitmentsFragment : Fragment() {
    private var _binding: FragmentMyCommitmentsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMyCommitmentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}