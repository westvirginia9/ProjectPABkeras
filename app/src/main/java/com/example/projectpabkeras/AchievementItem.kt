package com.example.projectpabkeras

data class AchievementItem(
    var title: String,             // Judul pencapaian
    var currentLevel: Int,         // Level saat ini
    val maxLevel: Int,             // Level maksimum
    var exp: Int                   // Jumlah EXP saat ini
)

