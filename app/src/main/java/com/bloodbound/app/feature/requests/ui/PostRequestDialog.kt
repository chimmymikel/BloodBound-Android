package com.bloodbound.app.feature.requests.ui

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bloodbound.app.databinding.DialogPostRequestBinding
import com.bloodbound.app.feature.auth.data.BLOOD_TYPES
import com.bloodbound.app.feature.requests.data.CreateRequestBody
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PostRequestDialog : DialogFragment() {

    private var _binding: DialogPostRequestBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RequestsViewModel by viewModels({ requireParentFragment() })

    private var selectedUrgency = "STANDARD"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogPostRequestBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBloodTypeDropdown()
        setupUrgencyButtons()
        setupHospitalSpinner()
        setupButtons()
        observeHospitals()
        viewModel.loadHospitals()
    }

    private fun setupBloodTypeDropdown() {
        val labels = BLOOD_TYPES.map { it.second }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBloodType.adapter = adapter
    }

    private fun setupUrgencyButtons() {
        fun updateButtons() {
            binding.btnStandard.alpha = if (selectedUrgency == "STANDARD") 1f else 0.5f
            binding.btnHigh.alpha     = if (selectedUrgency == "HIGH")     1f else 0.5f
            binding.btnCritical.alpha = if (selectedUrgency == "CRITICAL") 1f else 0.5f
        }
        binding.btnStandard.setOnClickListener { selectedUrgency = "STANDARD"; updateButtons() }
        binding.btnHigh.setOnClickListener     { selectedUrgency = "HIGH";     updateButtons() }
        binding.btnCritical.setOnClickListener { selectedUrgency = "CRITICAL"; updateButtons() }
        updateButtons()
    }

    private fun setupHospitalSpinner() {
        val adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_item, mutableListOf("Loading hospitals…"))
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerHospital.adapter = adapter
    }

    private fun observeHospitals() {
        viewModel.hospitals.observe(viewLifecycleOwner) { hospitals ->
            if (hospitals.isEmpty()) return@observe
            val adapter = ArrayAdapter(requireContext(),
                android.R.layout.simple_spinner_item, hospitals.map { it.name })
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerHospital.adapter = adapter
        }
    }

    private fun setupButtons() {
        binding.btnCancelDialog.setOnClickListener { dismiss() }

        binding.btnPostRequest.setOnClickListener {
            val user = viewModel.user.value ?: return@setOnClickListener
            val hospitals = viewModel.hospitals.value ?: emptyList()

            if (hospitals.isEmpty()) {
                return@setOnClickListener
            }

            val unitsInput = binding.etUnits.text.toString().toIntOrNull() ?: 0

            if (unitsInput > 20) {
                binding.etUnits.error = "Maximum of 20 units only!"
                binding.etUnits.requestFocus()
                return@setOnClickListener
            }
            if (unitsInput < 1) {
                binding.etUnits.error = "At least 1 unit is required!"
                binding.etUnits.requestFocus()
                return@setOnClickListener
            }

            val selectedBtLabel = binding.spinnerBloodType.selectedItem.toString()
            val bloodType = BLOOD_TYPES.find { it.second == selectedBtLabel }?.first ?: "O_POSITIVE"
            val hospital  = hospitals.getOrNull(binding.spinnerHospital.selectedItemPosition)
                ?: return@setOnClickListener
            val notes     = binding.etNotes.text.toString().trim().ifBlank { null }

            viewModel.postRequest(CreateRequestBody(
                bloodType   = bloodType,
                units       = unitsInput,
                urgency     = selectedUrgency,
                notes       = notes,
                location    = "Cebu City",
                requesterId = user.id,
                hospitalId  = hospital.id,
                status      = "ACTIVE"
            ))
            dismiss()
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}