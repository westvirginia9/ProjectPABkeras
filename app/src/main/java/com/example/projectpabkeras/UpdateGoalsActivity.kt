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
import kotlin.properties.Delegates

class UpdateGoalsActivity : AppCompatActivity() {

    private var currentAmount by Delegates.notNull<Int>() // Store current total goal amount
    private lateinit var updateDate: String // Store the update date

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_goals)

        // Find Views
        val amountEditText = findViewById<EditText>(R.id.et_update_amount)
        val dateEditText = findViewById<EditText>(R.id.et_update_date)
        val saveButton = findViewById<Button>(R.id.btn_update_goal)
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

        // Get current data from Intent
        val goalName = intent.getStringExtra("GOAL_NAME") ?: ""
        val goalTargetAmount = intent.getIntExtra("GOAL_TARGET_AMOUNT", 0)

        currentAmount = goalTargetAmount

        // Pre-fill amount input if needed (optional)
        // amountEditText.setText(currentAmount.toString())

        // Set DatePickerDialog when clicking on dateEditText
        dateEditText.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    updateDate = "$dayOfMonth/${month + 1}/$year"
                    dateEditText.setText(updateDate) // Display the selected date in the EditText
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save Button Logic
        saveButton.setOnClickListener {
            val updateAmountText = amountEditText.text.toString()
            val updateAmount = updateAmountText.toIntOrNull()

            if (updateAmount != null && updateAmount > 0 && ::updateDate.isInitialized) {
                // Add the update amount to the current total amount
                currentAmount += updateAmount

                // Send updated data back to GoalsActivity
                val resultIntent = Intent().apply {
                    putExtra("GOAL_NAME", goalName)
                    putExtra("GOAL_TARGET_AMOUNT", currentAmount)
                    putExtra("GOAL_UPDATE_DATE", updateDate)
                }
                setResult(RESULT_OK, resultIntent)
                finish() // Close the UpdateGoalsActivity and return to the previous screen
            } else {
                Toast.makeText(this, "Harap isi jumlah yang valid dan pilih tanggal!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
