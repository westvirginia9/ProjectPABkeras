package com.example.projectpabkeras.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.R
import com.example.projectpabkeras.models.MonthlySummary

class MonthlyAdapter(private val items: List<MonthlySummary>) :
    RecyclerView.Adapter<MonthlyAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBulan: TextView = itemView.findViewById(R.id.tvBulan)
        val tvPengeluaranBulanan: TextView = itemView.findViewById(R.id.tvPengeluaranBulanan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_bulanan, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvBulan.text = item.monthName
        holder.tvPengeluaranBulanan.text = "- Rp ${item.totalExpense}"
    }

    override fun getItemCount(): Int = items.size
}
