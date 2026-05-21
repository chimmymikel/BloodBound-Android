// FILE: app/src/main/java/com/bloodbound/app/feature/dashboard/ui/RequestSummaryAdapter.kt
package com.bloodbound.app.feature.dashboard.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.R
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.timeAgo
import com.bloodbound.app.databinding.ItemDonorRequestBinding
import com.bloodbound.app.feature.dashboard.data.RequestDto

@SuppressLint("SetTextI18n")
class RequestSummaryAdapter(
    private val items: List<RequestDto>,
    private val isDonor: Boolean,
    // From EligibilityDto.isEligible — true means donor passed the 56-day cooldown
    private val isEligible: Boolean = false,
    // From EligibilityDto.daysUntilEligible — shown in the pill when not eligible
    private val daysUntilEligible: Int = 0,
    private val onCommit: ((RequestDto) -> Unit)? = null
) : RecyclerView.Adapter<RequestSummaryAdapter.VH>() {

    inner class VH(val b: ItemDonorRequestBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDonorRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = items[position]
        val b   = holder.b

        // ── Blood type ────────────────────────────────────────────
        b.tvBloodType.text = formatBloodType(req.bloodType)

        // ── Hospital ──────────────────────────────────────────────
        b.tvHospital.text = req.hospitalName ?: "Hospital Facility"

        // ── Units ─────────────────────────────────────────────────
        val units = req.units ?: 1
        b.tvUnits.text = "$units unit${if (units > 1) "s" else ""} needed"

        // ── Time ago ──────────────────────────────────────────────
        val ago = timeAgo(req.createdAt)
        b.tvTimeAgo.text = if (req.location != null) "Posted $ago · ${req.location}" else "Posted $ago"

        // ── Urgency badge + left accent bar ──────────────────────
        val urgency = (req.urgency ?: "STANDARD").uppercase()
        b.tvUrgency.text = urgency
        val urgencyColor = when (urgency) {
            "CRITICAL" -> Color.parseColor("#DC2626")
            "HIGH"     -> Color.parseColor("#EA580C")
            else       -> Color.parseColor("#16A34A")
        }
        b.tvUrgency.setBackgroundResource(R.drawable.bg_status_badge)
        b.tvUrgency.setTextColor(urgencyColor)
        b.viewUrgencyBar.setBackgroundColor(urgencyColor)

        // ── Contact / anonymous ───────────────────────────────────
        b.tvContactStatus.text  = "🔒 Hidden until committed"
        b.tvAnonymousLabel.text = "👤 Anonymous"

        // ── RIGHT SLOT ────────────────────────────────────────────
        // DONOR eligible   → red "Commit" button (active, tappable)
        // DONOR ineligible → gray "⏳ Xd left" pill
        // REQUESTER        → hide both; eligibility does not apply
        if (isDonor) {
            if (isEligible) {
                b.btnCommit.visibility       = View.VISIBLE
                b.tvRequestExpiry.visibility = View.GONE
                b.btnCommit.setOnClickListener { onCommit?.invoke(req) }
            } else {
                b.btnCommit.visibility       = View.GONE
                b.tvRequestExpiry.visibility = View.VISIBLE
                b.tvRequestExpiry.text       = "⏳ ${daysUntilEligible}d left"
                b.tvRequestExpiry.setTextColor(Color.parseColor("#9CA3AF"))
            }
        } else {
            // REQUESTER — neither commit nor eligibility pill
            b.btnCommit.visibility       = View.GONE
            b.tvRequestExpiry.visibility = View.GONE
        }
    }
}