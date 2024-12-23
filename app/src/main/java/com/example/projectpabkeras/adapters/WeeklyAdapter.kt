package com.example.projectpabkeras.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.R
import com.example.projectpabkeras.models.WeeklySummary

class WeeklyAdapter(private val weeklySummaries: List<WeeklySummary>) :
    RecyclerView.Adapter<WeeklyAdapter.WeeklyViewHolder>() {

    class WeeklyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggalRentang: TextView = itemView.findViewById(R.id.tvTanggalRentang)
        val tvTotalPengeluaran: TextView = itemView.findViewById(R.id.tvPengeluaranMingguan)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeeklyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_riwayat_mingguan, parent, false)
        return WeeklyViewHolder(view)
    }

    override fun onBindViewHolder(holder: WeeklyViewHolder, position: Int) {
        val summary = weeklySummaries[position]
        holder.tvTanggalRentang.text = summary.weekRange
        holder.tvTotalPengeluaran.text = "Rp ${summary.totalExpense}"

    }

    override fun getItemCount(): Int = weeklySummaries.size
}

