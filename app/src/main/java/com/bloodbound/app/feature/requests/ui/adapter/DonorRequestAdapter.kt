package com.bloodbound.app.feature.requests.ui.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.R
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.timeAgo
import com.bloodbound.app.databinding.ItemDonorRequestBinding
import com.bloodbound.app.feature.requests.data.RequestDto

class DonorRequestAdapter(
    private val items: List<RequestDto>,
    private val committedIds: Set<Long>,
    private val isEligible: Boolean,
    private val daysLeft: Int,
    private val hasActiveCommitment: Boolean,
    private val onCommit: (Long) -> Unit
) : RecyclerView.Adapter<DonorRequestAdapter.VH>() {

    inner class VH(val b: ItemDonorRequestBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemDonorRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = items[position]
        val b   = holder.b
        val context = b.root.context

        val alreadyCommitted = committedIds.contains(req.id)
        val blockedByOther   = !alreadyCommitted && hasActiveCommitment
        val disabled         = alreadyCommitted || !isEligible || blockedByOther

        // 1. Core Info (Cleaned up units text)
        b.tvBloodType.text = formatBloodType(req.bloodType)
        b.tvHospital.text  = req.hospitalName ?: "Hospital Facility"
        b.tvUnits.text     = "${req.units} unit needed${if (req.notes != null) " · ${req.notes}" else ""}"
        b.tvTimeAgo.text   = "Posted ${timeAgo(req.createdAt)}${if (req.location != null) " · ${req.location}" else ""}"

        // 2. Dynamic Colors for Left Accent Bar & Urgency Pill
        val urgencyColor = when (req.urgency.uppercase()) {
            "CRITICAL" -> ContextCompat.getColor(context, R.color.status_red)
            "HIGH"     -> ContextCompat.getColor(context, R.color.status_orange)
            else       -> ContextCompat.getColor(context, R.color.status_green)
        }

        // Color the left accent bar
        b.viewUrgencyBar.setBackgroundColor(urgencyColor)

        // Color the Urgency Pill
        b.tvUrgency.text = req.urgency.uppercase()
        b.tvUrgency.setTextColor(urgencyColor)
        b.tvUrgency.setBackgroundResource(R.drawable.bg_pill_outline)

        // Mutate prevents scrolling from breaking other rows' colors
        val urgencyBg = b.tvUrgency.background.mutate() as? GradientDrawable
        val strokePx = (1 * context.resources.displayMetrics.density).toInt()
        urgencyBg?.setStroke(strokePx, urgencyColor)

        // 3. Contact/Name reveal
        if (alreadyCommitted) {
            b.tvContactStatus.text  = "📞 ${req.requesterContactNumber ?: "See My Commitments"}"
            b.tvAnonymousLabel.text = "👤 ${req.requesterName ?: "Requester"}"
        } else {
            b.tvContactStatus.text  = "🔒 Hidden until committed"
            b.tvAnonymousLabel.text = "👤 Anonymous"
        }

        // 4. Donor Eligibility Badge — Muted Gray Design matching web
        if (!isEligible && daysLeft > 0) {
            b.tvRequestExpiry.visibility = View.VISIBLE
            b.tvRequestExpiry.text = "⏳ ${daysLeft}d left"

            // Force the muted gray styling
            b.tvRequestExpiry.setBackgroundResource(R.drawable.bg_pill_outline)
            b.tvRequestExpiry.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))

            val expiryBg = b.tvRequestExpiry.background.mutate() as? GradientDrawable
            val grayBorder = ContextCompat.getColor(context, R.color.border_light)
            expiryBg?.setStroke(strokePx, grayBorder)
        } else {
            b.tvRequestExpiry.visibility = View.GONE
        }

        // 5. Button Logic
        b.btnCommit.text = when {
            alreadyCommitted -> "✔ Committed"
            !isEligible      -> "⏳ ${daysLeft}d left"
            blockedByOther   -> "Unavailable"
            else             -> "Commit"
        }

        b.btnCommit.isEnabled = !disabled
        b.btnCommit.alpha     = if (disabled) 0.5f else 1.0f

        b.btnCommit.setOnClickListener {
            if (!disabled) onCommit(req.id)
        }
    }
}