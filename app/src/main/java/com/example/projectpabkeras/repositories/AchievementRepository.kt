package com.example.projectpabkeras.repositories

import com.google.firebase.firestore.FirebaseFirestore

data class Achievement(
    val title: String = "",
    val currentLevel: Int = 0,
    val maxLevel: Int = 0,
    val exp: Int = 0,
    val targetAmount: Int = 0,
    val currentProgress: Int = 0
)

class AchievementRepository(private val firestore: FirebaseFirestore) {

    // Membuat dokumen awal
    fun createAchievementsDocument(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        val achievements = listOf(
            Achievement("Total keseluruhan pemasukan mencapai Rp 5.000.000", 0, 5, 100, 5000000, 0),
            Achievement("Berhasil menyelesaikan 1 kali milestone", 0, 5, 50, 1, 0)
        )

        val achievementData = mapOf("achievements" to achievements)

        firestore.collection("achievements").document(userId)
            .set(achievementData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // Mendapatkan achievements selesai
    fun getCompletedAchievements(userId: String, onSuccess: (List<Achievement>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievements = document.toObject(AchievementDocument::class.java)?.achievements.orEmpty()
                    val completedAchievements = achievements.filter { it.currentLevel == it.maxLevel }
                    onSuccess(completedAchievements)
                } else {
                    onSuccess(emptyList()) // Jika dokumen tidak ada
                }
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}

// Membantu memetakan dokumen Firestore ke model data
data class AchievementDocument(
    val achievements: List<Achievement> = emptyList()
)
