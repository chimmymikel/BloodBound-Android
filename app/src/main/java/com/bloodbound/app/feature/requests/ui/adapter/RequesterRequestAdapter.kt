// FILE: app/src/main/java/com/bloodbound/app/feature/requests/ui/adapter/RequesterRequestAdapter.kt
package com.bloodbound.app.feature.requests.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.R
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.timeAgo
import com.bloodbound.app.databinding.ItemRequesterRequestBinding
import com.bloodbound.app.feature.requests.data.RequestDto

class RequesterRequestAdapter(
    private val items: List<RequestDto>,
    private val onFulfill: (Long) -> Unit
) : RecyclerView.Adapter<RequesterRequestAdapter.VH>() {

    inner class VH(val b: ItemRequesterRequestBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemRequesterRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req       = items[position]
        val b         = holder.b
        val fulfilled = req.status == "FULFILLED"
        val donors    = req.committedDonors ?: emptyList()
        val count     = req.commitmentCount ?: 0
        val hasEnough = count >= req.units

        // ── Blood type badge ──────────────────────────────────
        b.tvBloodType.text = formatBloodType(req.bloodType)

        // ── Hospital ──────────────────────────────────────────
        b.tvHospital.text = req.hospitalName ?: "Hospital Facility"

        // ── Urgency pill + accent bar ─────────────────────────
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

        // ── Units ─────────────────────────────────────────────
        val units = req.units
        b.tvUnits.text = "$units unit${if (units > 1) "s" else ""} needed"

        // ── Contact / anonymous ───────────────────────────────
        b.tvContactStatus.text  = "🔒 Hidden until committed"
        b.tvAnonymousLabel.text = "👤 Anonymous"

        // ── Time ago ──────────────────────────────────────────
        val ago = timeAgo(req.createdAt)
        b.tvTimeAgo.text = if (req.location != null) "Posted $ago · ${req.location}" else "Posted $ago"

        // ── Commit count ──────────────────────────────────────
        b.tvCommitCount.text = "$count/${req.units}\ndonors"

        // ── Donor cards ───────────────────────────────────────
        if (donors.isNotEmpty()) {
            b.tvDonorCards.visibility = View.VISIBLE
            b.tvDonorCards.text = donors.joinToString("\n") { d ->
                "📞 ${d.contactNumber}  👤 ${d.name}  ${formatBloodType(d.bloodType)}"
            }
        } else {
            b.tvDonorCards.visibility = View.GONE
        }

        // ── Notes ─────────────────────────────────────────────
        if (!req.notes.isNullOrBlank()) {
            b.tvNotes.visibility = View.VISIBLE
            b.tvNotes.text       = req.notes
        } else {
            b.tvNotes.visibility = View.GONE
        }

        // ── Fulfill button ────────────────────────────────────
        b.btnFulfill.text      = if (fulfilled) "✔ Fulfilled" else "✔ Mark Fulfilled"
        b.btnFulfill.isEnabled = !fulfilled && hasEnough
        b.btnFulfill.alpha     = if (!fulfilled && !hasEnough) 0.45f else 1.0f
        b.btnFulfill.setOnClickListener {
            if (!fulfilled && hasEnough) onFulfill(req.id)
        }
    }
}