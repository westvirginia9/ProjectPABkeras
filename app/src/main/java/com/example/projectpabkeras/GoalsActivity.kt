package com.example.projectpabkeras

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
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

        migrateGoalsCollection()

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
                    val goalId = document.id
                    val isCompleted = document.getBoolean("isCompleted") ?: false

                    // Panggil addGoalCard dengan semua parameter termasuk isCompleted
                    addGoalCard(goalName, goalPeriod, goalDescription, goalTargetAmount, currentAmount, goalId, isCompleted)
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
        goalId: String,
        isCompleted: Boolean
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

        if (isCompleted) {
            btnUpdate.visibility = View.GONE
            tvTitle.text = "$title (Selesai)"
        } else {
            btnUpdate.setOnClickListener {
                showUpdateDialog(goalId, currentAmount, targetAmount)
            }
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
                        updateGoalAmount(goalId, newAmount, targetAmount)
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



    private fun updateGoalAmount(goalId: String, newAmount: Int, targetAmount: Int) {
        db.collection("goals").document(goalId)
            .update("currentAmount", newAmount)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                checkGoalCompletion(goalId, newAmount)
                loadGoals()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memperbarui goal", Toast.LENGTH_SHORT).show()
            }
    }


    private fun fetchActiveGoals(onGoalsFetched: (List<Goal>) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("goals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("isCompleted", false) // Filter hanya goal yang belum selesai
            .get()
            .addOnSuccessListener { snapshot ->
                val goals = snapshot.documents.mapNotNull { it.toObject(Goal::class.java) }
                onGoalsFetched(goals)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat goal: ${e.message}", Toast.LENGTH_SHORT).show()
                onGoalsFetched(emptyList()) // Jika gagal, kirim daftar kosong
            }
    }

    private fun showGoalCompletedDialog(title: String) {
        AlertDialog.Builder(this)
            .setTitle("Goal Selesai!")
            .setMessage("Selamat! Goal \"$title\" telah selesai.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }




    private fun checkGoalCompletion(goalId: String, currentAmount: Int) {
        db.collection("goals").document(goalId)
            .get()
            .addOnSuccessListener { document ->
                val targetAmount = document.getLong("goalTargetAmount")?.toInt() ?: return@addOnSuccessListener
                if (currentAmount >= targetAmount) {
                    showGoalCompletedDialog()
                    markGoalAsCompleted(goalId) // Tandai goal sebagai selesai
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Gagal memuat data goal", Toast.LENGTH_SHORT).show()
            }
    }

    private fun migrateGoalsCollection() {
        val db = FirebaseFirestore.getInstance()

        db.collection("goals").get()
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val currentAmount = document.getLong("currentAmount") ?: 0L
                    val goalTargetAmount = document.getLong("goalTargetAmount") ?: 0L

                    // Tentukan status "isCompleted"
                    val isCompleted = currentAmount >= goalTargetAmount

                    // Perbarui dokumen
                    db.collection("goals").document(document.id)
                        .update("isCompleted", isCompleted)
                        .addOnSuccessListener {
                            println("Dokumen ${document.id} berhasil dimigrasi!")
                        }
                        .addOnFailureListener { e ->
                            println("Gagal memperbarui dokumen ${document.id}: ${e.message}")
                        }
                }
            }
            .addOnFailureListener { e ->
                println("Gagal mengambil koleksi goals: ${e.message}")
            }
    }


    private fun markGoalAsCompleted(goalId: String) {
        db.collection("goals").document(goalId)
            .update("isCompleted", true)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal berhasil ditandai sebagai selesai!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menandai goal sebagai selesai: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun onGoalCompleted() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Update pencapaian milestone
        updateMilestoneAchievement(userId)
    }

    private fun showGoalCompletedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Goal Selesai!")
            .setMessage("Selamat! Anda telah menyelesaikan goal ini.")
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateMilestoneAchievement(userId: String) {
        val db = FirebaseFirestore.getInstance()

        db.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievementsArray = document.get("achievements") as? List<Map<String, Any>>
                    if (achievementsArray != null) {
                        val updatedAchievements = achievementsArray.map { achievementData ->
                            if ((achievementData["title"] as String).contains("milestone", ignoreCase = true)) {
                                val currentLevel = (achievementData["currentLevel"] as Long).toInt()
                                val maxLevel = (achievementData["maxLevel"] as Long).toInt()
                                val exp = (achievementData["exp"] as Long).toInt()
                                var milestonesCompleted = (achievementData["currentProgress"] as Long).toInt()

                                // Tambahkan milestone yang selesai
                                milestonesCompleted += 1

                                // Periksa apakah level naik
                                if (milestonesCompleted >= requiredMilestones(currentLevel + 1) && currentLevel < maxLevel) {
                                    val newLevel = currentLevel + 1
                                    milestonesCompleted = 0 // Reset progress

                                    return@map mapOf(
                                        "title" to newMilestoneTitle(newLevel),
                                        "currentLevel" to newLevel,
                                        "maxLevel" to maxLevel,
                                        "exp" to newExpForLevel(newLevel),
                                        "currentProgress" to milestonesCompleted
                                    )
                                }
                            }
                            return@map achievementData
                        }

                        // Simpan kembali ke Firestore
                        db.collection("achievements").document(userId)
                            .update("achievements", updatedAchievements)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Pencapaian milestone diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal memperbarui milestone: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fungsi untuk mendapatkan judul milestone baru
    private fun newMilestoneTitle(level: Int): String {
        return when (level) {
            2 -> "Berhasil menyelesaikan 3 milestone"
            3 -> "Berhasil menyelesaikan 5 milestone"
            else -> "Semua milestone selesai!"
        }
    }

    // Fungsi untuk menentukan jumlah milestone yang dibutuhkan
    private fun requiredMilestones(level: Int): Int {
        return when (level) {
            2 -> 3
            3 -> 5
            else -> Int.MAX_VALUE
        }
    }

    // Fungsi untuk menentukan EXP baru berdasarkan level
    private fun newExpForLevel(level: Int): Int {
        return when (level) {
            2 -> 600
            3 -> 800
            else -> 1000
        }
    }
    private fun onGoalCompleted(goalId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        updateMilestoneAchievement(userId)
    }

    private fun updateGoalProgress(goalName: String, amount: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        firestore.collection("goals")
            .whereEqualTo("userId", userId)
            .whereEqualTo("goalName", goalName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val goalDocument = snapshot.documents[0]
                    val goalId = goalDocument.id
                    val currentAmount = goalDocument.getLong("currentAmount") ?: 0L

                    firestore.collection("goals").document(goalId)
                        .update("currentAmount", currentAmount + amount)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Progress goal diperbarui!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui goal: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat goal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }






}
