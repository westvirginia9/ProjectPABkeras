package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RewardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reward)

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
        // Setup RecyclerView
        val rewardList = findViewById<RecyclerView>(R.id.reward_list)
        rewardList.layoutManager = LinearLayoutManager(this)

        // Data Dummy
        val rewards = listOf(
            Reward("Voucher Kuota im3 500 mb", "2000 exp"),
            Reward("Voucher Kuota Indosat 1 gb", "3500 exp"),
            Reward("Voucher Kuota Telkomsel 1 gb", "4000 exp"),
            Reward("Voucher Kuota XL 500 mb", "1800 exp"),
            Reward("Voucher Kuota Indosat 1,5 gb", "5500 exp")
        )

        // Set Adapter
        rewardList.adapter = RewardAdapter(rewards)
    }
}

// Data Model

