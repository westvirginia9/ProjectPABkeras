package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private lateinit var auth: FirebaseAuth
private lateinit var firestore: FirebaseFirestore
private lateinit var notificationsContainer: LinearLayout

    class NotificationActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { onBackPressed() }

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        notificationsContainer = findViewById(R.id.notificationsContainer) // Tambahkan ID di XML

        loadNotifications()

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
    }
        private fun loadNotifications() {
            val userId = auth.currentUser?.uid ?: return

            firestore.collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { snapshot ->
                    for (doc in snapshot.documents) {
                        val message = doc.getString("message") ?: "Notifikasi"
                        addNotificationItem(message)
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Gagal memuat notifikasi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        private fun addNotificationItem(message: String) {
            val inflater = layoutInflater
            val notificationItem = inflater.inflate(R.layout.notification_item, null)

            val textViewMessage = notificationItem.findViewById<TextView>(R.id.textNotification)
            textViewMessage.text = message

            notificationsContainer.addView(notificationItem)
        }

}