// FILE: app/src/main/java/com/bloodbound/app/feature/profile/ui/ProfileFragment.kt
package com.bloodbound.app.feature.profile.ui

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.bloodbound.app.R
import com.bloodbound.app.core.util.calcEligibility
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.formatDisplayDate
import com.bloodbound.app.databinding.FragmentProfileBinding
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ProfileViewModel by viewModels()

    // ── Photo picker launcher ─────────────────────────────────────────
    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri ?: return@registerForActivityResult
            handlePhotoSelected(uri)
        }

    // ── Lifecycle ─────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ── Click listeners ───────────────────────────────────────────────

    private fun setupClickListeners() {
        // Camera FAB — open system photo picker
        binding.fabChangePhoto.setOnClickListener {
            pickPhotoLauncher.launch("image/*")
        }

        // ── LOGOUT BUTTON WITH UI CONFIRMATION ──
        // ── BULLETPROOF LOGOUT BUTTON ──
        binding.btnLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out of your account?")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Log Out") { dialog, _ ->
                    // 1. Wipe the secure data
                    viewModel.performLogout()

                    // 2. The Nuclear Option: Completely restart the app's UI
                    val intent = android.content.Intent(requireContext(), com.bloodbound.app.MainActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)

                    dialog.dismiss()
                }
                .show()
        }

        // Edit contact toggle
        binding.btnEditContact.setOnClickListener {
            val isVisible = binding.layoutEditContact.visibility == View.VISIBLE
            if (isVisible) {
                binding.layoutEditContact.visibility = View.GONE
            } else {
                binding.etContact.setText(binding.tvContact.text)
                binding.layoutEditContact.visibility = View.VISIBLE
                binding.etContact.requestFocus()
            }
        }
        binding.btnSaveContact.setOnClickListener {
            viewModel.updateContact(binding.etContact.text.toString())
        }
        binding.btnCancelContact.setOnClickListener {
            binding.layoutEditContact.visibility = View.GONE
        }

        // Change password toggle
        binding.btnChangePassword.setOnClickListener {
            val isVisible = binding.layoutPassword.visibility == View.VISIBLE
            binding.layoutPassword.visibility = if (isVisible) View.GONE else View.VISIBLE
        }
        binding.btnSavePassword.setOnClickListener {
            viewModel.changePassword(
                old     = binding.etOldPassword.text.toString(),
                new     = binding.etNewPassword.text.toString(),
                confirm = binding.etConfirmPassword.text.toString()
            )
        }
        binding.btnCancelPassword.setOnClickListener {
            binding.layoutPassword.visibility = View.GONE
            clearPasswordFields()
        }
    }

    // ── Photo handling ────────────────────────────────────────────────

    private fun handlePhotoSelected(uri: Uri) {
        try {
            Glide.with(requireContext())
                .load(uri)
                .circleCrop()
                .into(binding.ivProfilePhoto)

            val inputStream = requireContext().contentResolver.openInputStream(uri)
                ?: run {
                    showToast("Cannot read selected photo.")
                    return
                }
            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType  = requireContext().contentResolver.getType(uri) ?: "image/jpeg"
            val extension = if (mimeType.contains("png")) "profile.png" else "profile.jpg"

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", extension, requestBody)

            viewModel.uploadPhoto(part)

        } catch (e: Exception) {
            showToast("Failed to process image: ${e.localizedMessage}")
        }
    }

    private fun loadProfilePhoto(base64String: String?, fullName: String, isDonor: Boolean) {
        val color       = if (isDonor) "DC2626" else "1D4ED8"
        val encodedName = android.net.Uri.encode(fullName.ifBlank { "U" })
        val fallbackUrl = "https://ui-avatars.com/api/?name=$encodedName" +
                "&background=$color&color=fff&size=256&bold=true"

        if (base64String.isNullOrBlank()) {
            Glide.with(requireContext())
                .load(fallbackUrl)
                .circleCrop()
                .into(binding.ivProfilePhoto)
            return
        }

        try {
            val rawBase64 = if (base64String.contains(","))
                base64String.substringAfter(",")
            else
                base64String

            val bytes = Base64.decode(rawBase64, Base64.DEFAULT)

            Glide.with(requireContext())
                .load(bytes)
                .circleCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(fallbackUrl)
                .into(binding.ivProfilePhoto)

        } catch (e: Exception) {
            Glide.with(requireContext())
                .load(fallbackUrl)
                .circleCrop()
                .into(binding.ivProfilePhoto)
        }
    }

    // ── ViewModel observers ───────────────────────────────────────────

    private fun observeViewModel() {
        viewModel.profile.observe(viewLifecycleOwner) { profile ->
            profile ?: return@observe
            val isDonor = profile.role == "DONOR"

            loadProfilePhoto(profile.profilePicture, profile.fullName, isDonor)

            binding.tvFullName.text    = profile.fullName
            binding.tvEmail.text       = profile.email
            binding.tvRole.text        = profile.role
            binding.tvContact.text     = profile.contactNumber ?: "—"
            binding.tvMemberSince.text = formatDisplayDate(profile.createdAt)

            if (isDonor) {
                binding.tvBloodType.visibility    = View.VISIBLE
                binding.tvBloodType.text          = formatBloodType(profile.bloodType)
                binding.tvTotalDonations.text     = "${profile.totalDonations ?: 0}"
                binding.tvLastDonation.text       = formatDisplayDate(profile.lastDonationDate)
                binding.layoutDonorStats.visibility = View.VISIBLE
                binding.cardEligibility.visibility  = View.VISIBLE

                val localElig = calcEligibility(profile.lastDonationDate)
                binding.tvEligibilityStatus.text = if (localElig.eligible)
                    "READY TO DONATE ✔️"
                else
                    "Eligible in ${localElig.daysLeft} days ⏳"
            } else {
                binding.tvBloodType.visibility      = View.GONE
                binding.layoutDonorStats.visibility = View.GONE
                binding.cardEligibility.visibility  = View.GONE
            }
        }

        viewModel.eligibility.observe(viewLifecycleOwner) { elig ->
            elig ?: return@observe
            val profile   = viewModel.profile.value
            val localCalc = calcEligibility(profile?.lastDonationDate)

            binding.tvEligibilityStatus.text = if (elig.isEligible || localCalc.eligible)
                "READY TO DONATE ✔️"
            else
                "Eligible in ${localCalc.daysLeft} days ⏳"
        }

        viewModel.isUploadingPhoto.observe(viewLifecycleOwner) { uploading ->
            binding.layoutPhotoUploading.visibility = if (uploading) View.VISIBLE else View.GONE
            binding.fabChangePhoto.isEnabled = !uploading
        }

        viewModel.toast.observe(viewLifecycleOwner) { msg ->
            msg ?: return@observe
            showToast(msg)
            viewModel.clearToast()
        }

        viewModel.contactSaved.observe(viewLifecycleOwner) { saved ->
            if (!saved) return@observe
            binding.layoutEditContact.visibility = View.GONE
            viewModel.clearContactSaved()
        }

        viewModel.passwordSuccess.observe(viewLifecycleOwner) { success ->
            if (!success) return@observe
            binding.layoutPassword.visibility = View.GONE
            clearPasswordFields()
            viewModel.clearPasswordSuccess()
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────

    private fun clearPasswordFields() {
        binding.etOldPassword.text?.clear()
        binding.etNewPassword.text?.clear()
        binding.etConfirmPassword.text?.clear()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }
}