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

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile) // Pastikan layout sudah benar

        auth = FirebaseAuth.getInstance()

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

        // Tombol Home
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

        val editprofileButton: ImageView = findViewById(R.id.edit_pencil1)
        editprofileButton.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // Referensi UI untuk nama dan email
        val userNameTextView: TextView = findViewById(R.id.user_name)
        val userEmailTextView: TextView = findViewById(R.id.user_email)

        // Ambil userId dari Firebase Authentication
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Ambil data dari Firestore
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // Tampilkan data di UI
                        val username = document.getString("username") ?: "Unknown"
                        val email = document.getString("email") ?: "Unknown"
                        userNameTextView.text = username
                        userEmailTextView.text = email
                    } else {
                        Toast.makeText(this, "Data pengguna tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Gagal memuat data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Pengguna tidak terautentikasi", Toast.LENGTH_SHORT).show()
        }

        // RecyclerView untuk pencapaian
        recyclerView = findViewById(R.id.recyclerViewAchievementsProfile)

        // Inisialisasi adapter dengan showDetails = false untuk menampilkan hanya title
        achievementAdapter = AchievementAdapter(achievements, false)
        recyclerView.adapter = achievementAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Memuat data pencapaian
        loadAchievements()
    }

    private fun loadAchievements() {
        // Contoh data pencapaian
        achievements.add(AchievementItem("Berhasil Menyelesaikan 1 Milestone", 1, 5, 100))
        achievements.add(AchievementItem("Berhasil Menyisihkan Uang Sosial >20% Pemasukan", 1, 5, 100))
        achievements.add(AchievementItem("Berhasil Tidak Melebihi Batas Pengeluaran Hiburan", 0, 5, 50))
        achievements.add(AchievementItem("Total Keseluruhan Pemasukan Mencapai Rp 5.000.000", 0, 5, 1000))

        // Notifikasi adapter untuk memperbarui tampilan
        achievementAdapter.notifyDataSetChanged()
    }
}
