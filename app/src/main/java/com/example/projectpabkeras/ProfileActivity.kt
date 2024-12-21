package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    private val achievements = mutableListOf<AchievementItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAchievements)

        // Inisialisasi adapter dengan lambda kosong
        achievementAdapter = AchievementAdapter(
            items = achievements,
//            onItemClick = { _, _ -> } // Tidak ada aksi saat item diklik
        )
        recyclerView.adapter = achievementAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Memuat data pencapaian dari Firestore
        loadAchievementsFromFirestore()

        // Tampilkan nama pengguna di profil
        loadUserProfile()

        loadUserExp()

        findViewById<ImageView>(R.id.btn_back).setOnClickListener { onBackPressed() }


        // Tombol Goals
        val goalsButton: ImageView = findViewById(R.id.goals_bottom)
        goalsButton.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        // Tombol Achievement
        val achievementButton: ImageView = findViewById(R.id.icAchievement)
        achievementButton.setOnClickListener {
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
        }

        // Tombol Achievement
        val homeButton: ImageView = findViewById(R.id.ic_home)
        homeButton.setOnClickListener {
            val intent = Intent(this, HomePageActivity::class.java)
            startActivity(intent)
        }
        val icRiwayat: ImageView = findViewById(R.id.ic_history)
        icRiwayat.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            startActivity(intent)
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
                    achievements.clear()

                    achievementsArray?.forEach { achievementData ->
                        val achievement = AchievementItem(
                            title = achievementData["title"] as String,
                            currentLevel = (achievementData["currentLevel"] as Long).toInt(),
                            maxLevel = (achievementData["maxLevel"] as Long).toInt(),
                            exp = (achievementData["exp"] as Long).toInt(),
                            targetAmount = (achievementData["targetAmount"] as Long),
                            currentProgress = (achievementData["currentProgress"] as Long)
                        )
                        achievements.add(achievement)
                    }

                    achievementAdapter.notifyDataSetChanged()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val username = document.getString("username") ?: "Pengguna"
                    val email = document.getString("email") ?: "Tidak diketahui"

                    // Set data ke TextView
                    findViewById<TextView>(R.id.user_name).text = username
                    findViewById<TextView>(R.id.user_email).text = email
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat profil pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserExp() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val totalExp = document.getLong("totalExp")?.toInt() ?: 0
                    findViewById<TextView>(R.id.achievement_exp).text = "Total EXP: $totalExp"
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat EXP: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
