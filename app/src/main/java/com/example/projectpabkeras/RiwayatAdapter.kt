package com.example.projectpabkeras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RiwayatAdapter(private val riwayatList: List<RiwayatItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return riwayatList[position].type
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            RiwayatItem.TYPE_HARIAN -> {
                val view = inflater.inflate(R.layout.item_riwayat_harian, parent, false)
                HarianViewHolder(view)
            }
            RiwayatItem.TYPE_MINGGUAN -> {
                val view = inflater.inflate(R.layout.item_riwayat_mingguan, parent, false)
                MingguanViewHolder(view)
            }
            RiwayatItem.TYPE_BULANAN -> {
                val view = inflater.inflate(R.layout.item_riwayat_bulanan, parent, false)
                BulananViewHolder(view)
            }
            else -> throw IllegalArgumentException("Unknown view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = riwayatList[position]
        when (holder) {
            is HarianViewHolder -> holder.bind(item)
            is MingguanViewHolder -> holder.bind(item)
            is BulananViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount(): Int {
        return riwayatList.size
    }

    // ViewHolder untuk Harian
    inner class HarianViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTanggal: TextView = itemView.findViewById(R.id.tvTanggal)
        private val tvKategori: TextView = itemView.findViewById(R.id.tvKategori)
        private val tvKeterangan: TextView = itemView.findViewById(R.id.tvKeterangan)
        private val tvPengeluaran: TextView = itemView.findViewById(R.id.tvPengeluaranHarian)

        fun bind(item: RiwayatItem) {
            tvTanggal.text = item.tanggal
            tvKategori.text = item.kategori ?: "-"
            tvKeterangan.text = item.keterangan ?: "-"
            tvPengeluaran.text = "- Rp ${item.pengeluaran}"
        }
    }

    // ViewHolder untuk Mingguan
    inner class MingguanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTanggalRentang: TextView = itemView.findViewById(R.id.tvTanggalRentang)
        private val tvPengeluaran: TextView = itemView.findViewById(R.id.tvPengeluaranMingguan)

        fun bind(item: RiwayatItem) {
            tvTanggalRentang.text = item.tanggal
            tvPengeluaran.text = "- Rp ${item.pengeluaran}"
        }
    }

    // ViewHolder untuk Bulanan
    inner class BulananViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvBulan: TextView = itemView.findViewById(R.id.tvBulan)
        private val tvPengeluaran: TextView = itemView.findViewById(R.id.tvPengeluaranBulanan)

        fun bind(item: RiwayatItem) {
            tvBulan.text = item.tanggal
            tvPengeluaran.text = "- Rp ${item.pengeluaran}"
        }
    }
}