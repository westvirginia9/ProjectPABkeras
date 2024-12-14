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

class EditGoalsActivity : AppCompatActivity() {

    private lateinit var startDate: String
    private lateinit var endDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_goals)

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



        // Get data from Intent (pre-fill the form)
        val goalName = intent.getStringExtra("GOAL_NAME") ?: ""
        val goalPeriod = intent.getStringExtra("GOAL_PERIOD") ?: ""
        val goalDescription = intent.getStringExtra("GOAL_DESCRIPTION") ?: ""
        val goalTargetAmount = intent.getIntExtra("GOAL_TARGET_AMOUNT", 0)

        nameEditText.setText(goalName)
        periodEditText.setText(goalPeriod)
        descriptionEditText.setText(goalDescription)
        targetAmountEditText.setText(goalTargetAmount.toString())

        // Period DatePicker Dialog
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
            val updatedName = nameEditText.text.toString()
            val updatedDescription = descriptionEditText.text.toString()
            val updatedTargetAmount = targetAmountEditText.text.toString().toIntOrNull()

            if (updatedName.isNotEmpty() && ::startDate.isInitialized && ::endDate.isInitialized && updatedTargetAmount != null) {
                val updatedPeriod = "$startDate - $endDate"

                // Pass updated data back to GoalsActivity
                val resultIntent = Intent().apply {
                    putExtra("GOAL_NAME", updatedName)
                    putExtra("GOAL_PERIOD", updatedPeriod)
                    putExtra("GOAL_DESCRIPTION", updatedDescription)
                    putExtra("GOAL_TARGET_AMOUNT", updatedTargetAmount)
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                Toast.makeText(this, "Harap isi semua data dengan benar!", Toast.LENGTH_SHORT).show()
            }
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
