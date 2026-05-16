// FILE: app/src/main/java/com/bloodbound/app/core/util/FooterHelper.kt
package com.bloodbound.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import com.bloodbound.app.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object FooterHelper {

    fun setup(footerView: View) {

        // ── Hotline click-to-call ──────────────────────────────────────
        footerView.findViewById<TextView>(R.id.footer_hotline_redcross)
            ?.setOnClickListener { dial(footerView.context, "143") }

        footerView.findViewById<TextView>(R.id.footer_hotline_ccmc)
            ?.setOnClickListener { dial(footerView.context, "0322531871") }

        footerView.findViewById<TextView>(R.id.footer_hotline_chonghua)
            ?.setOnClickListener { dial(footerView.context, "0322558000") }

        // ── Modal links ────────────────────────────────────────────────
        footerView.findViewById<TextView>(R.id.footer_link_privacy)
            ?.setOnClickListener {
                showModal(footerView.context, "Privacy Policy", getPrivacyText())
            }

        footerView.findViewById<TextView>(R.id.footer_link_terms)
            ?.setOnClickListener {
                showModal(footerView.context, "Terms of Use", getTermsText())
            }

        footerView.findViewById<TextView>(R.id.footer_link_about)
            ?.setOnClickListener {
                showModal(footerView.context, "About BloodBound", getAboutText())
            }
    }

    // ── Private helpers ────────────────────────────────────────────────

    private fun dial(ctx: Context, number: String) {
        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number")))
    }

    private fun showModal(ctx: Context, title: String, message: String) {
        MaterialAlertDialogBuilder(ctx)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    // ── Modal content ──────────────────────────────────────────────────

    private fun getPrivacyText() = """
        BloodBound is committed to protecting your personal information.

        INFORMATION WE COLLECT
        We collect your name, email address, blood type, and donation history solely for matching donors with blood requests in Cebu City.

        HOW WE USE YOUR INFORMATION
        Your data is used to verify donor eligibility, facilitate blood request commitments, and maintain your donation history. We do not sell or share your data with third parties.

        DATA SECURITY
        All passwords are encrypted using BCrypt. Data is transmitted over HTTPS/TLS 1.3. JWT tokens expire after 24 hours.

        YOUR RIGHTS
        You may view and update your account information at any time through your Profile page.
    """.trimIndent()

    private fun getTermsText() = """
        By using BloodBound, you agree to the following terms.

        ELIGIBILITY
        BloodBound is intended for voluntary blood donors and individuals seeking blood donations in Cebu City. Users must provide accurate personal and medical information.

        DONOR RESPONSIBILITIES
        Donors commit to honoring their pledges in good faith. The 56-day eligibility rule must be respected. False commitments may result in account suspension.

        REQUESTER RESPONSIBILITIES
        Requesters must post accurate blood needs corresponding to genuine medical emergencies or planned procedures. Abuse of the platform is strictly prohibited.

        LIMITATIONS
        BloodBound is a coordination platform only. It does not provide medical advice, guarantee donor availability, or act as a blood bank or medical institution.
    """.trimIndent()

    private fun getAboutText() = """
        BloodBound connects blood donors with individuals in urgent medical need across Cebu City and surrounding areas.

        OUR MISSION
        To eliminate the chaos of social-media-based blood donation coordination by providing a fast, verified, and structured platform for the Cebu community.

        HOW IT WORKS
        Requesters post blood needs specifying blood type, hospital, and urgency. Eligible donors see compatible requests and commit to donate.

        ELIGIBILITY SYSTEM
        A built-in 56-day countdown timer ensures donors only commit when medically cleared, following standard donation interval guidelines.

        TECHNOLOGY
        Spring Boot (Java) backend · React + TypeScript web app · Kotlin Android app · PostgreSQL on Supabase · RESTful API

        DEVELOPER
        Developed by Michelle Marie P. Habon as part of IT342 — System Integration and Architecture at CIT-U.
    """.trimIndent()
}