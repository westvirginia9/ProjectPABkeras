package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast

class GoalsActivity : AppCompatActivity() {

    private lateinit var goalName: String
    private var goalTargetAmount: Int = 0
    private lateinit var goalPeriod: String
    private lateinit var goalDescription: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        // Tombol Update
        val updateButton = findViewById<Button>(R.id.btn_update_goal)
        updateButton.setOnClickListener {
            val intent = Intent(this, UpdateGoalsActivity::class.java).apply {
                putExtra("GOAL_NAME", "Beli Laptop Baru")
                putExtra("GOAL_TARGET_AMOUNT", 15000000)
            }
            startActivity(intent)
        }

        // Tombol FAB (+)
        val fabButton = findViewById<ImageButton>(R.id.btn_fabgoals)
        fabButton.setOnClickListener {
            val intent = Intent(this, AddGoalsActivity::class.java)
            startActivity(intent)
        }

        val editButton = findViewById<Button>(R.id.btn_edit_goal)
        editButton.setOnClickListener {
            val intent = Intent(this, EditGoalsActivity::class.java)
            intent.putExtra("GOAL_NAME", goalName)
            intent.putExtra("GOAL_PERIOD", goalPeriod)
            intent.putExtra("GOAL_DESCRIPTION", goalDescription)
            intent.putExtra("GOAL_TARGET_AMOUNT", goalTargetAmount)
            startActivity(intent)
        }
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

        // Dummy data untuk contoh
        goalName = "Beli Laptop Baru"
        goalPeriod = "21/11/2024 s/d 21/01/2025"
        goalDescription = "Semoga bisa tercapai, aamiin"
        goalTargetAmount = 15000000

        // Tambahkan data ke UI
        val goalContainer = findViewById<LinearLayout>(R.id.goalContainer)
        addGoalCard(goalContainer, goalName, goalPeriod, goalDescription, goalTargetAmount)
    }

    private fun addGoalCard(
        container: LinearLayout,
        title: String,
        period: String,
        description: String,
        targetAmount: Int
    ) {
        // Buat layout kartu tujuan
        val goalCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(16, 16, 16, 16)
            setBackgroundResource(R.drawable.bg_card)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        // Tambahkan komponen
        val titleView = TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(getColor(R.color.black))
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        goalCard.addView(titleView)

        val periodView = TextView(this).apply {
            text = period
            textSize = 12f
            setTextColor(getColor(R.color.red))
        }
        goalCard.addView(periodView)

        val progressTextView = TextView(this).apply {
            text = "Rp 1.500.000 / Rp $targetAmount (10%)"
            textSize = 12f
            setTextColor(getColor(R.color.black))
        }
        goalCard.addView(progressTextView)

        val descriptionView = TextView(this).apply {
            text = description
            textSize = 12f
            setTextColor(getColor(R.color.gray))
        }
        goalCard.addView(descriptionView)

        // Tambahkan ke container
        container.addView(goalCard)
    }
}
