package com.bloodbound.app.feature.dashboard.ui

import android.annotation.SuppressLint
import android.content.res.ColorStateList
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
    private val isEligible: Boolean = false,
    private val daysUntilEligible: Int = 0,
    private val committedIds: Set<Long> = emptySet(),
    private val hasActiveCommitment: Boolean = false,
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
        b.tvTimeAgo.text = if (req.location != null)
            "Posted $ago · ${req.location}"
        else
            "Posted $ago"

        // ── Urgency badge + left accent bar ───────────────────────
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
        val alreadyCommitted = committedIds.contains(req.id)
        if (alreadyCommitted) {
            b.tvContactStatus.text  = "📞 ${req.requesterContactNumber ?: "See My Commitments"}"
            b.tvAnonymousLabel.text = "👤 ${req.requesterName ?: "Requester"}"
        } else {
            b.tvContactStatus.text  = "🔒 Hidden until committed"
            b.tvAnonymousLabel.text = "👤 Anonymous"
        }

        // ── RIGHT SLOT ────────────────────────────────────────────
        if (isDonor) {
            when {

                // 1. Already committed to THIS request
                //    → Light green outlined pill  (matches web ✓ Committed)
                alreadyCommitted -> {
                    b.btnCommit.visibility       = View.VISIBLE
                    b.tvRequestExpiry.visibility = View.GONE
                    b.btnCommit.text             = "✔ Committed"
                    b.btnCommit.isEnabled        = false
                    b.btnCommit.alpha            = 1.0f
                    b.btnCommit.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#DCFCE7"))   // light green fill
                    b.btnCommit.setTextColor(Color.parseColor("#16A34A"))     // green text
                    b.btnCommit.strokeColor =
                        ColorStateList.valueOf(Color.parseColor("#16A34A"))   // green border
                    b.btnCommit.strokeWidth = 2
                    b.btnCommit.setOnClickListener(null)
                }

                // 2. Pending commitment elsewhere
                //    → Light gray outlined pill  (matches web Unavailable)
                hasActiveCommitment -> {
                    b.btnCommit.visibility       = View.VISIBLE
                    b.tvRequestExpiry.visibility = View.GONE
                    b.btnCommit.text             = "Unavailable"
                    b.btnCommit.isEnabled        = false
                    b.btnCommit.alpha            = 1.0f
                    b.btnCommit.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#F3F4F6"))   // light gray fill
                    b.btnCommit.setTextColor(Color.parseColor("#9CA3AF"))     // gray text
                    b.btnCommit.strokeColor =
                        ColorStateList.valueOf(Color.parseColor("#D1D5DB"))   // gray border
                    b.btnCommit.strokeWidth = 2
                    b.btnCommit.setOnClickListener(null)
                }

                // 3. Still in 56-day cooldown → expiry pill
                !isEligible -> {
                    b.btnCommit.visibility       = View.GONE
                    b.tvRequestExpiry.visibility = View.VISIBLE
                    b.tvRequestExpiry.text       = "⏳ ${daysUntilEligible}d left"
                    b.tvRequestExpiry.setTextColor(Color.parseColor("#9CA3AF"))
                }

                // 4. Eligible and free → active red Commit button
                else -> {
                    b.btnCommit.visibility       = View.VISIBLE
                    b.tvRequestExpiry.visibility = View.GONE
                    b.btnCommit.text             = "Commit"
                    b.btnCommit.isEnabled        = true
                    b.btnCommit.alpha            = 1.0f
                    b.btnCommit.backgroundTintList =
                        ColorStateList.valueOf(Color.parseColor("#DC2626"))   // red fill
                    b.btnCommit.setTextColor(Color.WHITE)
                    b.btnCommit.strokeColor =
                        ColorStateList.valueOf(Color.parseColor("#DC2626"))   // red border
                    b.btnCommit.strokeWidth = 0
                    b.btnCommit.setOnClickListener { onCommit?.invoke(req) }
                }
            }
        } else {
            // REQUESTER — hide both slots
            b.btnCommit.visibility       = View.GONE
            b.tvRequestExpiry.visibility = View.GONE
        }
    }
}