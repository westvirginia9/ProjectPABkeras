package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class AddGoalsActivity : AppCompatActivity() {

    private lateinit var startDate: String
    private lateinit var endDate: String

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
        // Show DatePickerDialog when clicking on periodEditText
        periodEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    startDate = "$dayOfMonth/${month + 1}/$year"
                    // Show end date picker after selecting start date
                    showEndDatePicker(periodEditText)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save Button Logic
        saveButton.setOnClickListener {
            // Get input values
            val goalName = nameEditText.text.toString()
            val description = descriptionEditText.text.toString()
            val targetAmount = targetAmountEditText.text.toString().toIntOrNull()

            // Validate input
            if (goalName.isNotEmpty() && ::startDate.isInitialized && ::endDate.isInitialized && targetAmount != null) {
                val period = "$startDate - $endDate"

                // Pass data to GoalsActivity
                val intent = Intent(this, GoalsActivity::class.java).apply {
                    putExtra("GOAL_NAME", goalName)
                    putExtra("GOAL_PERIOD", period)
                    putExtra("GOAL_DESCRIPTION", description)
                    putExtra("GOAL_TARGET_AMOUNT", targetAmount)
                }
                startActivity(intent)
                finish()
            } else {
                // Show error if validation fails
                Toast.makeText(this, "Isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEndDatePicker(periodEditText: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                endDate = "$dayOfMonth/${month + 1}/$year"
                // Update the EditText with the selected period
                periodEditText.setText("$startDate - $endDate")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
