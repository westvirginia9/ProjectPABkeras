package com.example.projectpabkeras

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class GoalsActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private lateinit var goalContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_goals)

        goalContainer = findViewById(R.id.goalContainer)

        // Tombol Tambah Goal
        val fabButton = findViewById<ImageButton>(R.id.btn_fabgoals)
        fabButton.setOnClickListener {
            startActivity(Intent(this, AddGoalsActivity::class.java))
        }

        // Navigasi
        setupNavigation()

        // Muat data goals dari Firestore
        loadGoals()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.ic_home).setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }
        findViewById<ImageView>(R.id.icAchievement).setOnClickListener {
            startActivity(Intent(this, AchievementActivity::class.java))
        }
        findViewById<ImageView>(R.id.ic_history).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
        findViewById<ImageView>(R.id.ic_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun loadGoals() {
        goalContainer.removeAllViews()
        if (userId == null) return

        db.collection("goals")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val goalName = document.getString("goalName") ?: ""
                    val goalPeriod = document.getString("goalPeriod") ?: ""
                    val goalDescription = document.getString("goalDescription") ?: ""
                    val goalTargetAmount = document.getLong("goalTargetAmount")?.toInt() ?: 0
                    val currentAmount = document.getLong("currentAmount")?.toInt() ?: 0
                    val goalId = document.getString("goalId") ?: ""

                    addGoalCard(goalName, goalPeriod, goalDescription, goalTargetAmount, currentAmount, goalId)
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data goals", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addGoalCard(
        title: String,
        period: String,
        description: String,
        targetAmount: Int,
        currentAmount: Int,
        goalId: String
    ) {
        val progress = (currentAmount.toFloat() / targetAmount * 100).toInt()

        val cardLayout = LayoutInflater.from(this).inflate(R.layout.item_goal_card, null)
        val tvTitle = cardLayout.findViewById<TextView>(R.id.tvGoalTitle)
        val tvPeriod = cardLayout.findViewById<TextView>(R.id.tvDateRange)
        val progressBar = cardLayout.findViewById<ProgressBar>(R.id.progressBar)
        val tvProgress = cardLayout.findViewById<TextView>(R.id.tvProgress)
        val btnUpdate = cardLayout.findViewById<Button>(R.id.btn_update_goal)

        tvTitle.text = title
        tvPeriod.text = period
        tvProgress.text = "Rp $currentAmount / Rp $targetAmount ($progress%)"
        progressBar.max = 100
        progressBar.progress = progress

        btnUpdate.setOnClickListener {
            showUpdateDialog(goalId, currentAmount, targetAmount)
        }

        goalContainer.addView(cardLayout)
    }

    private fun showUpdateDialog(goalId: String, currentAmount: Int, targetAmount: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update_goal, null)
        val etAmount = dialogView.findViewById<EditText>(R.id.et_amount)

        AlertDialog.Builder(this)
            .setTitle("Update Goal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val inputAmount = etAmount.text.toString().toIntOrNull()
                if (inputAmount != null && inputAmount > 0) {
                    val newAmount = currentAmount + inputAmount
                    if (newAmount <= targetAmount) {
                        updateGoalAmount(goalId, newAmount)
                    } else {
                        Toast.makeText(this, "Jumlah melebihi target!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Input tidak valid", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateGoalAmount(goalId: String, newAmount: Int) {
        db.collection("goals").document(goalId)
            .update("currentAmount", newAmount)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                loadGoals()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui goal", Toast.LENGTH_SHORT).show()
            }
    }
}
