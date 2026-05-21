package com.bloodbound.app.feature.requests.ui.adapter

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

        val alreadyCommitted = committedIds.contains(req.id)
        val blockedByOther   = !alreadyCommitted && hasActiveCommitment
        val disabled         = alreadyCommitted || !isEligible || blockedByOther

        // 1. Core Info
        b.tvBloodType.text = formatBloodType(req.bloodType)
        b.tvHospital.text  = req.hospitalName ?: "Hospital Facility"
        b.tvUnits.text     = "${req.units} unit(s) needed${if (req.notes != null) " · ${req.notes}" else ""}"
        b.tvTimeAgo.text   = "Posted ${timeAgo(req.createdAt)}${if (req.location != null) " · ${req.location}" else ""}"

        // 2. Urgency Badge
        b.tvUrgency.text = req.urgency.uppercase()
        b.tvUrgency.setBackgroundResource(R.drawable.bg_status_badge)
        b.tvUrgency.setTextColor(when (req.urgency.uppercase()) {
            "CRITICAL" -> Color.parseColor("#DC2626")
            "HIGH"     -> Color.parseColor("#EA580C")
            else       -> Color.parseColor("#16A34A")
        })

        // 3. Contact/Name reveal
        if (alreadyCommitted) {
            b.tvContactStatus.text  = "📞 ${req.requesterContactNumber ?: "See My Commitments"}"
            b.tvAnonymousLabel.text = "👤 ${req.requesterName ?: "Requester"}"
        } else {
            b.tvContactStatus.text  = "🔒 Hidden until committed"
            b.tvAnonymousLabel.text = "👤 Anonymous"
        }

        // 4. Donor Eligibility Badge — shows how many days until donor can commit
        //    Visible only when donor is NOT yet eligible (matches Dashboard behavior)
        if (!isEligible && daysLeft > 0) {
            b.tvRequestExpiry.visibility = View.VISIBLE
            b.tvRequestExpiry.text = "⏳ ${daysLeft}d left"

            val pillBg = when {
                daysLeft <= 3  -> R.drawable.bg_pill_red
                daysLeft <= 7  -> R.drawable.bg_pill_yellow
                else           -> R.drawable.bg_pill_blue
            }
            b.tvRequestExpiry.setBackgroundResource(pillBg)
            b.tvRequestExpiry.setTextColor(when (pillBg) {
                R.drawable.bg_pill_red    -> Color.parseColor("#991B1B")
                R.drawable.bg_pill_yellow -> Color.parseColor("#854D0E")
                else                      -> Color.parseColor("#1E40AF")
            })
        } else {
            // Donor is eligible — hide the waiting badge entirely
            b.tvRequestExpiry.visibility = View.GONE
        }

        // 5. Button Logic — same daysLeft source, stays in sync with badge above
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