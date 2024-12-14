package com.example.projectpabkeras.models

data class WeeklySummary(
    val weekRange: String, // Contoh: "01-01-2024 s/d 07-01-2024" atau "Januari 2024"
    val totalIncome: Double, // Total pemasukan dalam minggu/bulan
    val totalExpense: Double // Total pengeluaran dalam minggu/bulan
)
