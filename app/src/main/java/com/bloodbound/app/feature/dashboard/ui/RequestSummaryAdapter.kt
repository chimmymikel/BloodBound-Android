// FILE: app/src/main/java/com/bloodbound/app/feature/dashboard/ui/RequestSummaryAdapter.kt
package com.bloodbound.app.feature.dashboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bloodbound.app.R
import com.bloodbound.app.core.util.formatBloodType
import com.bloodbound.app.core.util.timeAgo
import com.bloodbound.app.feature.dashboard.data.RequestDto

class RequestSummaryAdapter(
    private val items: List<RequestDto>,
    private val isDonor: Boolean
) : RecyclerView.Adapter<RequestSummaryAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvBloodType:  TextView = view.findViewById(R.id.tv_blood_type)
        val tvHospital:   TextView = view.findViewById(R.id.tv_hospital)
        val tvUnits:      TextView = view.findViewById(R.id.tv_units)
        val tvUrgency:    TextView = view.findViewById(R.id.tv_urgency)
        val tvTimeAgo:    TextView = view.findViewById(R.id.tv_time_ago)
        val tvStatus:     TextView = view.findViewById(R.id.tv_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_request_summary, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val req = items[position]
        holder.tvBloodType.text = formatBloodType(req.bloodType)
        holder.tvHospital.text  = req.hospitalName ?: "Hospital Facility"
        holder.tvUnits.text     = "${req.units ?: 1} unit(s) needed"
        holder.tvUrgency.text   = req.urgency ?: "STANDARD"
        holder.tvTimeAgo.text   = "Posted ${timeAgo(req.createdAt)}"
        holder.tvStatus.text    = req.status ?: ""

        // Color urgency label
        val urgencyColor = when (req.urgency?.uppercase()) {
            "CRITICAL" -> 0xFFDC2626.toInt()
            "HIGH"     -> 0xFFEA580C.toInt()
            else       -> 0xFF16A34A.toInt()
        }
        holder.tvUrgency.setTextColor(urgencyColor)
    }

    override fun getItemCount() = items.size
}