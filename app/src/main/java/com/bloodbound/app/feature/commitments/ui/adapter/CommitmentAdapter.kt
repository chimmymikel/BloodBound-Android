package com.bloodbound.app.feature.commitments.ui.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.formatDisplayDate
import com.bloodbound.app.databinding.ItemTicketCardBinding
import com.bloodbound.app.feature.commitments.data.CommitmentDto

class CommitmentAdapter(
    private val items: List<CommitmentDto>,
    private val onCancel: (Long) -> Unit
) : RecyclerView.Adapter<CommitmentAdapter.VH>() {

    inner class VH(val b: ItemTicketCardBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(ItemTicketCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val t = items[position]
        val b = holder.b
        val isPending = t.status == "PENDING"

        b.tvStatus.text    = t.status
        b.tvReference.text = t.referenceNumber ?: "DON-—"
        b.tvHospital.text  = "🏥 ${t.hospitalName ?: "Hospital Facility"}"
        b.tvDate.text      = "Committed · ${formatDisplayDate(t.committedAt)}"
        b.tvBloodType.text = formatBloodType(t.bloodTypeNeeded)
        b.tvUrgency.text   = t.urgency ?: ""

        if (t.status == "CANCELLED") {
            b.tvContact.text   = "Unavailable"
            b.tvRequester.text = ""
        } else {
            b.tvContact.text   = "📞 ${t.requesterContactNumber ?: "—"}"
            b.tvRequester.text = "👤 ${t.requesterName ?: "—"}"
        }

        // Show divider + actions together only for PENDING
        val actionVisibility = if (isPending) View.VISIBLE else View.GONE
        b.divider.visibility       = actionVisibility
        b.layoutActions.visibility = actionVisibility

        b.btnDirections.setOnClickListener {
            val query  = Uri.encode(t.hospitalName ?: "hospital cebu city")
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/maps/search/?api=1&query=$query")
            )
            it.context.startActivity(intent)
        }

        b.btnCancel.setOnClickListener { onCancel(t.id) }
    }
}