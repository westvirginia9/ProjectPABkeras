package com.example.projectpabkeras.models

data class Transaction(
    val date: String = "", // Format tanggal: "yyyy-MM-dd"
    val type: String = "", // "income" atau "expense"
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = ""
)
