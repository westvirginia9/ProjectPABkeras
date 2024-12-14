package com.example.projectpabkeras.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.R
import com.example.projectpabkeras.models.WeeklySummary

class MonthlyAdapter(private val monthlySummaries: List<WeeklySummary>) :
    RecyclerView.Adapter<MonthlyAdapter.MonthlyViewHolder>() {

    class MonthlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvPengeluaranHarian: TextView = itemView.findViewById(R.id.tvPengeluaranHarian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_harian, parent, false)
        return MonthlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthlyViewHolder, position: Int) {
        val summary = monthlySummaries[position]

        holder.tvTanggal.text = summary.weekRange
        holder.tvPengeluaranHarian.text = "- Rp ${summary.totalExpense.toInt()}"
    }

    override fun getItemCount(): Int = monthlySummaries.size
}
