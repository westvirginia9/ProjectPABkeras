package com.example.projectpabkeras

data class RiwayatItem(
    val type: Int,
    val tanggal: String,
    val kategori: String? = null,
    val keterangan: String? = null,
    val pengeluaran: Int = 0
) {

    companion object {
        const val TYPE_HARIAN = 1
        const val TYPE_MINGGUAN = 2
        const val TYPE_BULANAN = 3
    }
}

