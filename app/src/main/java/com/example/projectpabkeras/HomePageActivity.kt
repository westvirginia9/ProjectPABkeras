package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.DatePicker
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.projectpabkeras.models.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

private val categoryLimits = mapOf(
    "Tabungan" to 200000.0,
    "Social" to 200000.0,
    "Hiburan" to 150000.0
)

class HomePageActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var ivLeft: ImageView
    private lateinit var ivRight: ImageView
    private lateinit var if_left: ImageView
    private lateinit var if_right: ImageView
    private lateinit var tvPemasukanNominal: TextView
    private lateinit var tvPengeluaranNominal: TextView
    private lateinit var tvSisaUangNominal: TextView
    private var currentCalendar: Calendar = Calendar.getInstance()
    private lateinit var pieChart: PieChart
    private lateinit var tvProgressKebutuhan: TextView
    private lateinit var tvProgressHiburan: TextView
    private lateinit var tvProgressTabungan: TextView
    private lateinit var tvProgressSosial: TextView

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()

    private var monthlyIncome: Long = 0
    private var limitKebutuhan: Long = 0
    private var limitHiburan: Long = 0
    private var limitTabungan: Long = 0
    private var limitSosial: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Inisialisasi FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Inisialisasi UI
        tvDate = findViewById(R.id.tv_date)
        tvPemasukanNominal = findViewById(R.id.tv_pemasukan_nominal)
        tvPengeluaranNominal = findViewById(R.id.tv_pengeluaran_nominal)
        tvSisaUangNominal = findViewById(R.id.tv_sisa_uang_nominal)
        tvProgressKebutuhan = findViewById(R.id.tv_progress_kebutuhan)
        tvProgressHiburan = findViewById(R.id.tv_progress_hiburan)
        tvProgressTabungan = findViewById(R.id.tv_progress_tabungan)
        tvProgressSosial = findViewById(R.id.tv_progress_sosial)
        pieChart = findViewById(R.id.pieChart)
        ivLeft = findViewById(R.id.iv_left)
        ivRight = findViewById(R.id.iv_right)

        // Setup awal
        setupPieChart()
        updateDateDisplay()

        // Tombol navigasi bulan
        ivLeft.setOnClickListener {
            moveToPreviousMonth()
            refreshData()
        }
        ivRight.setOnClickListener {
            moveToNextMonth()
            refreshData()
        }

        val userId = auth.currentUser?.uid

        // Tampilkan username di TextView
        val welcomeTextView: TextView = findViewById(R.id.welcomeText)
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val username = document.getString("username") ?: "Pengguna"
                        welcomeTextView.text = "Hai $username!"
                    } else {
                        welcomeTextView.text = "Hai Pengguna!"
                    }
                }
                .addOnFailureListener {
                    welcomeTextView.text = "Hai Pengguna!"
                }
        } else {
            welcomeTextView.text = "Hai Pengguna!"
        }

        // Tombol Floating Action Button (FAB)
        val fabButton: ImageButton = findViewById(R.id.btn_fab)
        fabButton.setOnClickListener {
            val intent = Intent(this, InputActivity::class.java)
            startActivity(intent)
        }

        // Tombol Goals
        val goalsButton: ImageView = findViewById(R.id.goals_bottom)
        goalsButton.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
        }

        // Tombol Notification
        val notificationButton: ImageView = findViewById(R.id.icNotification)
        notificationButton.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        val profileButton: ImageView = findViewById(R.id.ic_profile)
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // Tombol Achievement
        val achievementButton: ImageView = findViewById(R.id.icAchievement)
        achievementButton.setOnClickListener {
            val intent = Intent(this, AchievementActivity::class.java)
            startActivity(intent)
        }

        val goalsReward: TextView = findViewById(R.id.seeAllRewards)
        goalsReward.setOnClickListener {
            val intent = Intent(this, RewardActivity::class.java)
            startActivity(intent)
        }
        val detailGrafik: TextView = findViewById(R.id.seeDetailsGrafik)
        detailGrafik.setOnClickListener {
            val intent = Intent(this, DetailGrafikActivity::class.java)
            startActivity(intent)
        }
        val icRiwayat: ImageView = findViewById(R.id.ic_history)
        icRiwayat.setOnClickListener {
            val intent = Intent(this, RiwayatActivity::class.java)
            startActivity(intent)
        }

        // Muat data awal
        refreshData()

        // Cek atau buat data kategori default
        auth.currentUser?.uid?.let { userId ->
            createDefaultCategoryLimitsIfNeeded(userId)
        }
    }

    private fun refreshData() {
        loadMonthlyIncomeForMonth()
        loadTransactionSummary()
        loadCategoryProgress()
        loadPieChartData()
    }

    private fun createDefaultCategoryLimitsIfNeeded(userId: String) {
        val defaultLimits = mapOf(
            "kebutuhan" to 45,
            "hiburan" to 25,
            "tabungan" to 20,
            "sosial" to 10
        )

        val kategoriRef = firestore.collection("kategori").document(userId)
        kategoriRef.get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    kategoriRef.set(defaultLimits)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Kategori limit default dibuat", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal membuat kategori limit: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat kategori limit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun loadMonthlyIncomeForMonth() {
        val userId = auth.currentUser?.uid ?: return
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "income")
            .get()
            .addOnSuccessListener { snapshot ->
                monthlyIncome = snapshot.documents
                    .filter { it.getString("date")?.startsWith(currentMonthYear) == true }
                    .sumOf { it.getLong("amount") ?: 0L }

                calculateCategoryLimits()
                loadCategoryProgress()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pemasukan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun calculateCategoryLimits() {
        limitKebutuhan = (monthlyIncome * 0.45).toLong()
        limitHiburan = (monthlyIncome * 0.25).toLong()
        limitTabungan = (monthlyIncome * 0.20).toLong()
        limitSosial = (monthlyIncome * 0.10).toLong()

        updateLimitsUI()
    }

    private fun updateLimitsUI() {
        tvProgressKebutuhan.text = "Rp 0 / Rp $limitKebutuhan"
        tvProgressHiburan.text = "Rp 0 / Rp $limitHiburan"
        tvProgressTabungan.text = "Rp 0 / Rp $limitTabungan"
        tvProgressSosial.text = "Rp 0 / Rp $limitSosial"
    }

    private fun loadTransactionSummary() {
        val userId = auth.currentUser?.uid ?: return
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                val filteredTransactions = transactions.filter { it.date.startsWith(currentMonthYear) }

                val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
                val totalExpense = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }

                tvPemasukanNominal.text = "Rp ${totalIncome}"
                tvPengeluaranNominal.text = "Rp ${totalExpense}"
                tvSisaUangNominal.text = "Rp ${totalIncome - totalExpense}"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun checkLimitExceeded(category: String, totalSpent: Long, limit: Long) {
        println("Checking limit for $category: totalSpent=$totalSpent, limit=$limit")
        if (totalSpent > limit) {
            val userId = auth.currentUser?.uid ?: return
            val message = "Batas pengeluaran untuk $category telah terlampaui! Total: Rp$totalSpent dari limit Rp$limit"

            // Simpan ke Firestore (koleksi "notifications")
            val notification = mapOf(
                "userId" to userId,
                "category" to category,
                "message" to message,
                "date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            )

            firestore.collection("notifications").add(notification)

            // Tampilkan dialog alert
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Peringatan Pengeluaran")
                .setMessage(message)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }
    }

    private fun loadCategoryProgress() {
        val userId = auth.currentUser?.uid ?: return
        val currentMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "expense")
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter { transaction -> transaction.date.startsWith(currentMonthYear) }

                val totalKebutuhan = transactions.filter { it.category == "Kebutuhan Pokok" }.sumOf { it.amount.toLong() }
                val totalHiburan = transactions.filter { it.category == "Hiburan" }.sumOf { it.amount.toLong() }
                val totalTabungan = transactions.filter { it.category == "Tabungan" }.sumOf { it.amount.toLong() }
                val totalSosial = transactions.filter { it.category == "Sosial" }.sumOf { it.amount.toLong() }

                // Update UI kategori
                tvProgressKebutuhan.text = "Rp ${totalKebutuhan.toInt()} / Rp $limitKebutuhan"
                tvProgressHiburan.text = "Rp ${totalHiburan.toInt()} / Rp $limitHiburan"
                tvProgressTabungan.text = "Rp ${totalTabungan.toInt()} / Rp $limitTabungan"
                tvProgressSosial.text = "Rp ${totalSosial.toInt()} / Rp $limitSosial"

                // Cek apakah melebihi limit
                checkLimitExceeded("Kebutuhan Pokok", totalKebutuhan.toLong(), limitKebutuhan)
                checkLimitExceeded("Hiburan", totalHiburan.toLong(), limitHiburan)
                checkLimitExceeded("Tabungan", totalTabungan.toLong(), limitTabungan)
                checkLimitExceeded("Sosial", totalSosial.toLong(), limitSosial)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat progres kategori: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun setupPieChart() {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setHoleColor(Color.WHITE)

        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.textSize = 12f
        legend.yOffset = 10f
    }

    private fun loadPieChartData() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "expense")
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter {
                        it.date.startsWith(
                            SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)
                        )
                    }

                val categoryTotals = transactions.groupBy { it.category }
                    .mapValues { it.value.sumOf { transaction -> transaction.amount } }

                val entries = categoryTotals.map { PieEntry(it.value.toFloat(), it.key) }

                val dataSet = PieDataSet(entries, "Pengeluaran").apply {
                    colors = ColorTemplate.MATERIAL_COLORS.toList()
                    sliceSpace = 3f
                    selectionShift = 5f
                }

                val data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter())
                    setValueTextSize(12f)
                    setValueTextColor(Color.WHITE)
                }

                pieChart.data = data
                pieChart.invalidate()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data PieChart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun moveToPreviousMonth() {
        currentCalendar.add(Calendar.MONTH, -1)
        updateDateDisplay()
    }

    private fun moveToNextMonth() {
        currentCalendar.add(Calendar.MONTH, 1)
        updateDateDisplay()
    }

    private fun updateDateDisplay() {
        tvDate.text = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(currentCalendar.time)
    }
}