package com.example.projectpabkeras

data class AchievementItem(
    var title: String,
    var currentLevel: Int,
    val maxLevel: Int,
    var exp: Int,
    var targetAmount: Long,
    var currentProgress: Long
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "title" to title,
            "currentLevel" to currentLevel,
            "maxLevel" to maxLevel,
            "exp" to exp,
            "targetAmount" to targetAmount,
            "currentProgress" to currentProgress
        )
    }
}
