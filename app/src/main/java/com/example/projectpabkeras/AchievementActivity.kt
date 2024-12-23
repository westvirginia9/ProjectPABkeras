package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AchievementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    private val achievements = mutableListOf<AchievementItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        recyclerView = findViewById(R.id.recyclerViewAchievements)
        recyclerView.layoutManager = LinearLayoutManager(this)
        achievementAdapter = AchievementAdapter(achievements)
        recyclerView.adapter = achievementAdapter

        loadAchievementsFromFirestore()



        findViewById<ImageView>(R.id.btn_back).setOnClickListener { onBackPressed() }


        // Tombol Goals
        val goalsButton: ImageView = findViewById(R.id.goals_bottom)
        goalsButton.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        val homeButton: ImageView = findViewById(R.id.ic_home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        val profileButton: ImageView = findViewById(R.id.ic_profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Tombol Achievement
        val achievementButton: ImageView = findViewById(R.id.icAchievement)
        achievementButton.setOnClickListener {
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
        }


        val icRiwayat: ImageView = findViewById(R.id.ic_history)
        icRiwayat.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateAchievementInFirestore(userId: String, updatedAchievements: List<Map<String, Any>>) {
        val db = FirebaseFirestore.getInstance()
        db.collection("achievements").document(userId)
            .update("achievements", updatedAchievements)
            .addOnSuccessListener {
                Toast.makeText(this, "Pencapaian diperbarui!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memperbarui pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun loadAchievementsFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievementsArray = document.get("achievements") as? List<Map<String, Any>>
                    val achievements = achievementsArray?.map { achievementData ->
                        AchievementItem(
                            title = achievementData["title"] as String,
                            currentLevel = (achievementData["currentLevel"] as Long).toInt(),
                            maxLevel = (achievementData["maxLevel"] as Long).toInt(),
                            exp = (achievementData["exp"] as Long).toInt(),
                            targetAmount = achievementData["targetAmount"] as Long,
                            currentProgress = achievementData["currentProgress"] as Long
                        )
                    } ?: emptyList()

                    val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewAchievements)
                    val achievementAdapter = AchievementAdapter(achievements, true)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.adapter = achievementAdapter
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUserExp(userId: String, expGained: Int) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ambil total EXP yang ada saat ini
                    val currentExp = document.getLong("totalExp")?.toInt() ?: 0
                    val newExp = currentExp + expGained

                    // Perbarui total EXP di Firestore
                    db.collection("users").document(userId)
                        .update("totalExp", newExp)
                        .addOnSuccessListener {
                            Toast.makeText(this, "EXP diperbarui menjadi $newExp", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui EXP: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun updateAchievement(userId: String, achievementType: String, progressToAdd: Long) {
        val db = FirebaseFirestore.getInstance()

        val achievementIndex = achievements.indexOfFirst { it.title.contains(achievementType, ignoreCase = true) }

        if (achievementIndex != -1) {
            val achievement = achievements[achievementIndex]

            // Tambahkan progress
            achievement.currentProgress += progressToAdd

            if (achievement.currentProgress >= achievement.targetAmount) {
                achievement.currentLevel++
                achievement.currentProgress -= achievement.targetAmount

                // Update berdasarkan jenis achievement
                when (achievementType) {
                    "pemasukan" -> {
                        achievement.targetAmount += 2000000
                        achievement.exp += 1000
                        achievement.title = when (achievement.currentLevel) {
                            2 -> "Total keseluruhan pemasukan mencapai Rp 7.000.000"
                            3 -> "Total keseluruhan pemasukan mencapai Rp 9.000.000"
                            else -> "Pencapaian pemasukan luar biasa!"
                        }
                    }
                    "goals" -> {
                        achievement.targetAmount += 2
                        achievement.exp += 200
                        achievement.title = when (achievement.currentLevel) {
                            2 -> "Berhasil menyelesaikan 3 milestone"
                            3 -> "Berhasil menyelesaikan 5 milestone"
                            else -> "Semua milestone selesai!"
                        }
                    }
                }
            }

            db.collection("achievements").document(userId)
                .update("achievements", achievements.map { it.toMap() })
                .addOnSuccessListener {
                    Toast.makeText(this, "Pencapaian diperbarui!", Toast.LENGTH_SHORT).show()
                    achievementAdapter.notifyItemChanged(achievementIndex)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memperbarui pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    fun updateIncomeAchievement(userId: String, incomeAdded: Long) {
        updateAchievement(userId, "pemasukan", incomeAdded)
    }

    private fun updateMilestoneAchievement(userId: String) {
        updateAchievement(userId, "milestone", 1)
    }



}
