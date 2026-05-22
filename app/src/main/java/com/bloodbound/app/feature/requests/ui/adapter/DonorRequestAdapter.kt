package com.bloodbound.app.feature.requests.ui.adapter

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
import com.bloodbound.app.feature.requests.data.RequestDto

@SuppressLint("SetTextI18n")
class DonorRequestAdapter(
    private val items: List<RequestDto>,
    private val committedIds: Set<Long> = emptySet(),
    private val isEligible: Boolean = false,
    private val daysLeft: Int = 0,
    private val hasActiveCommitment: Boolean = false,
    private val onCommit: ((Long) -> Unit)? = null
) : RecyclerView.Adapter<DonorRequestAdapter.VH>() {

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
        // Same priority order as RequestSummaryAdapter (Dashboard):
        // 1. Already committed to THIS request → green "✔ Committed" pill
        // 2. Has a PENDING commitment elsewhere → gray "Unavailable" pill
        // 3. Not eligible (cooldown) → "⏳ Xd left" text
        // 4. Eligible + free → solid red "Commit" button
        when {

            // 1. Already committed to THIS specific request
            alreadyCommitted -> {
                b.btnCommit.visibility       = View.VISIBLE
                b.tvRequestExpiry.visibility = View.GONE
                b.btnCommit.text             = "✔ Committed"
                b.btnCommit.isEnabled        = false
                b.btnCommit.alpha            = 1.0f
                b.btnCommit.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#DCFCE7"))
                b.btnCommit.setTextColor(Color.parseColor("#16A34A"))
                b.btnCommit.strokeColor =
                    ColorStateList.valueOf(Color.parseColor("#16A34A"))
                b.btnCommit.strokeWidth = 2
                b.btnCommit.setOnClickListener(null)
            }

            // 2. Has a PENDING commitment to a different request
            hasActiveCommitment -> {
                b.btnCommit.visibility       = View.VISIBLE
                b.tvRequestExpiry.visibility = View.GONE
                b.btnCommit.text             = "Unavailable"
                b.btnCommit.isEnabled        = false
                b.btnCommit.alpha            = 1.0f
                b.btnCommit.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#F3F4F6"))
                b.btnCommit.setTextColor(Color.parseColor("#9CA3AF"))
                b.btnCommit.strokeColor =
                    ColorStateList.valueOf(Color.parseColor("#D1D5DB"))
                b.btnCommit.strokeWidth = 2
                b.btnCommit.setOnClickListener(null)
            }

            // 3. Still in 56-day cooldown
            !isEligible -> {
                b.btnCommit.visibility       = View.GONE
                b.tvRequestExpiry.visibility = View.VISIBLE
                b.tvRequestExpiry.text       = "⏳ ${daysLeft}d left"
                b.tvRequestExpiry.setTextColor(Color.parseColor("#9CA3AF"))
            }

            // 4. Eligible and free — active red Commit button
            else -> {
                b.btnCommit.visibility       = View.VISIBLE
                b.tvRequestExpiry.visibility = View.GONE
                b.btnCommit.text             = "Commit"
                b.btnCommit.isEnabled        = true
                b.btnCommit.alpha            = 1.0f
                b.btnCommit.backgroundTintList =
                    ColorStateList.valueOf(Color.parseColor("#DC2626"))
                b.btnCommit.setTextColor(Color.WHITE)
                b.btnCommit.strokeColor =
                    ColorStateList.valueOf(Color.parseColor("#DC2626"))
                b.btnCommit.strokeWidth = 0
                b.btnCommit.setOnClickListener { onCommit?.invoke(req.id) }
            }
        }
    }
}