package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.projectpabkeras.models.Goal
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

data class Goal(
    val goalId: String = "",
    val goalName: String = "",
    val goalPeriod: String = "",
    val goalDescription: String = "",
    val goalTargetAmount: Int = 0,
    val currentAmount: Int = 0
)

class AddGoalsActivity : AppCompatActivity() {

    private lateinit var startDate: String
    private lateinit var endDate: String

    private val firestore = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_goals)

        // Find Views
        val nameEditText = findViewById<EditText>(R.id.et_goal_name)
        val periodEditText = findViewById<EditText>(R.id.et_goal_period)
        val descriptionEditText = findViewById<EditText>(R.id.et_goal_description)
        val targetAmountEditText = findViewById<EditText>(R.id.et_goal_target_amount)
        val saveButton = findViewById<Button>(R.id.btn_save_goal)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

//        val db = FirebaseFirestore.getInstance()
//        val goalId = UUID.randomUUID().toString()

//        data class Goal(
//            val goalId: String = "",
//            val goalName: String = "",
//            val goalPeriod: String = "",
//            val goalDescription: String = "",
//            val goalTargetAmount: Int = 0,
//            val currentAmount: Int = 0
//        )

//        db.collection("goals").document(goalId).set(Goal)
//            .addOnSuccessListener {
//                Toast.makeText(this, "Goal berhasil ditambahkan!", Toast.LENGTH_SHORT).show()
//                startActivity(Intent(this, GoalsActivity::class.java))
//                finish()
//            }
//            .addOnFailureListener {
//                Toast.makeText(this, "Gagal menambahkan goal", Toast.LENGTH_SHORT).show()
//            }

        // Back Button Logic
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
        // Date Picker for Period
        periodEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startDate = "$dayOfMonth/${month + 1}/$year"
                    showEndDatePicker(periodEditText)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save Button Logic
        saveButton.setOnClickListener {
            val goalName = nameEditText.text.toString().trim()
            val goalDescription = descriptionEditText.text.toString().trim()
            val goalTargetAmount = targetAmountEditText.text.toString().toIntOrNull()

            if (goalName.isNotEmpty() && ::startDate.isInitialized && ::endDate.isInitialized && goalTargetAmount != null) {
                val goalPeriod = "$startDate - $endDate"
                saveGoalToFirestore(goalName, goalDescription, goalPeriod, goalTargetAmount)
            } else {
                Toast.makeText(this, "Isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveGoalToFirestore(goalName: String, goalDescription: String, goalPeriod: String, goalTargetAmount: Int) {
        val goalId = firestore.collection("goals").document().id // Auto-generate document ID
        val newGoal = Goal(
            goalId = goalId,
            goalName = goalName,
            goalDescription = goalDescription,
            goalPeriod = goalPeriod,
            goalTargetAmount = goalTargetAmount,
            currentAmount = 0, // Default value
            userId = userId
        )

        // Save the new goal to Firestore
        firestore.collection("goals").document(goalId)
            .set(newGoal)
            .addOnSuccessListener {
                Toast.makeText(this, "Goal berhasil disimpan!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, GoalsActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEndDatePicker(periodEditText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                endDate = "$dayOfMonth/${month + 1}/$year"
                periodEditText.setText("$startDate - $endDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

}
