package com.example.projectpabkeras.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.R
import com.example.projectpabkeras.models.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHari: TextView = itemView.findViewById(R.id.tvHari)
        val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvPengeluaranHarian: TextView = itemView.findViewById(R.id.tvPengeluaranHarian)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_harian, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = sdf.parse(transaction.date)
        val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))

        holder.tvHari.text = dayFormat.format(dateObj ?: Date())
        holder.tvTanggal.text = transaction.date
        holder.tvKategori.text = transaction.category
        holder.tvKeterangan.text = transaction.description
        holder.tvPengeluaranHarian.text = if (transaction.type == "expense") {
            "- Rp ${transaction.amount.toInt()}"
        } else {
            "+ Rp ${transaction.amount.toInt()}"
        }
    }

    override fun getItemCount(): Int = transactions.size
}
