package com.bloodbound.app.feature.requests.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        val hasEnough = (req.commitmentCount ?: 0) >= (req.units)

        b.tvBloodType.text   = "${formatBloodType(req.bloodType)} · ${req.units} unit(s)"
        b.tvHospital.text    = "📍 ${req.hospitalName ?: "—"}"
        b.tvNotes.text       = req.notes ?: ""
        b.tvTimeAgo.text     = "Posted ${timeAgo(req.createdAt)}"
        b.tvCommitCount.text = "${req.commitmentCount ?: 0} donor(s) committed"

        // Show committed donor contact cards if any
        b.tvDonorCards.text = if (donors.isNotEmpty()) {
            donors.joinToString("\n") { d ->
                "📞 ${d.contactNumber}  👤 ${d.name}  ${formatBloodType(d.bloodType)}"
            }
        } else ""

        // Fulfill button
        b.btnFulfill.text      = if (fulfilled) "✔ Fulfilled" else "✔ Mark Fulfilled"
        b.btnFulfill.isEnabled = !fulfilled && hasEnough
        b.btnFulfill.alpha     = if (!fulfilled && !hasEnough) 0.45f else 1.0f
        b.btnFulfill.setOnClickListener {
            if (!fulfilled && hasEnough) onFulfill(req.id)
        }
    }
}