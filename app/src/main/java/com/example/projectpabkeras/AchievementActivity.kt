package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle                 // Untuk Bundle (parameter onCreate)
import android.widget.ImageView
import android.widget.Toast              // Untuk menampilkan Toast
import androidx.appcompat.app.AppCompatActivity // Untuk AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager // Untuk LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // Untuk RecyclerView

class AchievementActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var achievementAdapter: AchievementAdapter
    private val achievements = mutableListOf<AchievementItem>() // Data pencapaian


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement)

        // Tombol Navigasi
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { onBackPressed() }

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
        val profileButton: ImageView = findViewById(R.id.ic_profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Inisialisasi RecyclerView dan Adapter
        recyclerView = findViewById(R.id.recyclerViewAchievements)

        achievementAdapter = AchievementAdapter(
            items = achievements,
            showDetails = true, // Tampilkan detail level dan EXP
            onItemClick = { achievement, position -> // Logika klik item
                if (achievement.currentLevel < achievement.maxLevel) {
                    achievement.currentLevel++
                    achievement.exp += 100
                    achievement.title = getUpdatedAchievementTitle(achievement.currentLevel)
                    achievementAdapter.notifyItemChanged(position)
                    Toast.makeText(this, "Level up! ${achievement.title}", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Pencapaian sudah maksimal!", Toast.LENGTH_SHORT).show()
                }
            }
        )

        recyclerView.adapter = achievementAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Muat data awal
        loadAchievements()
    }

    private fun loadAchievements() {
        // Dummy data awal
        achievements.add(AchievementItem("Konsisten Mencatat Pengeluaran Selama 3 hari berturut-turut", 1, 5, 100))
        achievements.add(AchievementItem("Berhasil Menyelesaikan 1 kali Milestone", 1, 5, 100))
        achievements.add(AchievementItem("Berhasil menyisihkan uang sosial >20% pemasukan", 0, 5, 50))
        achievements.add(AchievementItem("Tidak melebihi batas pengeluaran kategori entertainment", 0, 5, 50))
        achievements.add(AchievementItem("Total keseluruhan pemasukan mencapai Rp 5.000.000", 0, 5, 1000))
        achievementAdapter.notifyDataSetChanged()
    }

    private fun getUpdatedAchievementTitle(level: Int): String {
        return when (level) {
            2 -> "Berhasil menyelesaikan 3 milestone"
            3 -> "Berhasil menyelesaikan 5 milestone"
            4 -> "Berhasil menyelesaikan 7 milestone"
            5 -> "Semua milestone selesai!"
            else -> "Pencapaian lainnya"
        }
    }
}

