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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        auth = FirebaseAuth.getInstance()

        // Inisialisasi UI
        tvPemasukanNominal = findViewById(R.id.tv_pemasukan_nominal)
        tvPengeluaranNominal = findViewById(R.id.tv_pengeluaran_nominal)
        tvSisaUangNominal = findViewById(R.id.tv_sisa_uang_nominal)
        pieChart = findViewById(R.id.pieChart)
        tvDate = findViewById(R.id.tv_date)
        // Inisialisasi TextView
        tvProgressKebutuhan = findViewById(R.id.tv_progress_kebutuhan)
        tvProgressHiburan = findViewById(R.id.tv_progress_hiburan)
        tvProgressTabungan = findViewById(R.id.tv_progress_tabungan)
        tvProgressSosial = findViewById(R.id.tv_progress_sosial)

        loadCategoryProgress()

        // Ambil userId dari pengguna yang sedang login
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

        loadTransactionSummary()

        // Inisialisasi PieChart
        setupPieChart(pieChart)
        loadPieChartData(pieChart)

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

        // Inisialisasi komponen untuk bulan dan tahun
        tvDate = findViewById(R.id.grafik_date)
        ivLeft = findViewById(R.id.iv_left)
        ivRight = findViewById(R.id.iv_right)
        if_left = findViewById(R.id.if_left)
        if_right = findViewById(R.id.if_right)

        // Update initial date
        updateDateDisplay()

        if_left.setOnClickListener {
            moveToPreviousMonth()
            loadTransactionSummary()
            loadPieChartData(pieChart)
        }

        if_right.setOnClickListener {
            moveToNextMonth()
            loadTransactionSummary()
            loadPieChartData(pieChart)
        }

        // Set listeners untuk tombol kiri dan kanan
        ivLeft.setOnClickListener {
            moveToPreviousMonth()
            loadTransactionSummary()
            loadPieChartData(pieChart)
        }

        ivRight.setOnClickListener {
            moveToNextMonth()
            loadTransactionSummary()
            loadPieChartData(pieChart)
        }
    }

    private fun loadCategoryProgress() {
        val userId = auth.currentUser?.uid ?: return

        // Ambil data limit kategori
        firestore.collection("kategori").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val limitKebutuhan = document.getLong("kebutuhan") ?: 0
                    val limitHiburan = document.getLong("hiburan") ?: 0
                    val limitTabungan = document.getLong("tabungan") ?: 0
                    val limitSosial = document.getLong("sosial") ?: 0

                    // Hitung pengeluaran berdasarkan kategori
                    firestore.collection("transactions")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("type", "expense")
                        .get()
                        .addOnSuccessListener { snapshot ->
                            val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }

                            val totalKebutuhan = transactions.filter { it.category == "Kebutuhan Pokok" }.sumOf { it.amount }
                            val totalHiburan = transactions.filter { it.category == "Hiburan" }.sumOf { it.amount }
                            val totalTabungan = transactions.filter { it.category == "Tabungan" }.sumOf { it.amount }
                            val totalSosial = transactions.filter { it.category == "Sosial" }.sumOf { it.amount }

                            // Tampilkan progres kategori
                            tvProgressKebutuhan.text = "${totalKebutuhan}/$limitKebutuhan"
                            tvProgressHiburan.text = "${totalHiburan}/$limitHiburan"
                            tvProgressTabungan.text = "${totalTabungan}/$limitTabungan"
                            tvProgressSosial.text = "${totalSosial}/$limitSosial"
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal menghitung pengeluaran: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Data limit belum tersedia", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat limit: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelTextSize(12f)
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.setDrawHoleEnabled(true)
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.setHoleColor(Color.WHITE)
        pieChart.setDrawEntryLabels(true)

        val legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.textSize = 12f
        legend.yOffset = 10f

        pieChart.centerText = "Pengeluaran"
        pieChart.setCenterTextSize(12f)
        pieChart.animateY(1000)
    }

    // Perbarui fungsi loadTransactionSummary untuk menghitung kategori
    private fun loadTransactionSummary() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }

                val filteredTransactions = transactions.filter {
                    val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                    val transactionCalendar = Calendar.getInstance()
                    transactionCalendar.time = transactionDate ?: Date()

                    transactionCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                            transactionCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                }

                val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
                val totalExpense = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
                val remainingMoney = totalIncome - totalExpense

                tvPemasukanNominal.text = "Rp ${totalIncome.toInt()}"
                tvPengeluaranNominal.text = "Rp ${totalExpense.toInt()}"
                tvSisaUangNominal.text = "Rp ${remainingMoney.toInt()}"

                // Tambahkan pengecekan batas kategori
                checkCategoryLimits(filteredTransactions)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkCategoryLimits(transactions: List<Transaction>) {
        val categoryTotals = transactions.filter { it.type == "expense" }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        // Periksa apakah ada kategori yang melebihi batas
        categoryTotals.forEach { (category, total) ->
            val limit = categoryLimits[category]
            if (limit != null && total > limit) {
                showCategoryLimitExceededDialog(category, limit, total)
            }
        }
    }

    private fun showCategoryLimitExceededDialog(category: String, limit: Double, total: Double) {
        val message = """
        Pengeluaran untuk kategori "$category" telah melebihi batas!
        Batas: Rp ${limit.toInt()}
        Total Pengeluaran: Rp ${total.toInt()}
    """.trimIndent()

        // Tampilkan alert dialog
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Batas Kategori Tercapai")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun updateCategoryLimits() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val currentMonthYear = getCurrentMonthYear() // Ambil bulan-tahun sekarang

        // Ambil semua pemasukan bulan ini dari Firestore
        FirebaseFirestore.getInstance().collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "income")
            .get()
            .addOnSuccessListener { snapshot ->
                val totalIncome = snapshot.documents
                    .mapNotNull { it.getLong("amount") } // Ambil jumlah pemasukan
                    .sum()

                // Hitung limit berdasarkan persentase
                val kebutuhanLimit = (45 * totalIncome) / 100
                val entertainmentLimit = (25 * totalIncome) / 100
                val tabunganLimit = (20 * totalIncome) / 100
                val sosialLimit = (10 * totalIncome) / 100

                // Simpan limit ke koleksi 'kategori' di Firestore
                val limitsData = hashMapOf(
                    "kebutuhan" to kebutuhanLimit,
                    "entertainment" to entertainmentLimit,
                    "tabungan" to tabunganLimit,
                    "sosial" to sosialLimit,
                    "updatedMonth" to currentMonthYear // Tanda bulan pembaruan
                )

                // Tambahkan atau perbarui dokumen limits
                FirebaseFirestore.getInstance()
                    .collection("kategori")
                    .document(userId) // Dokumen limit untuk user ini
                    .set(limitsData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Limit kategori berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Gagal memperbarui limit: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menghitung pemasukan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getCurrentMonthYear(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return dateFormat.format(Date())
    }



    private fun loadPieChartData(pieChart: PieChart) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "expense")
            .get()
            .addOnSuccessListener { snapshot ->
                val filteredTransactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter { transaction ->
                        val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                        val transactionCalendar = Calendar.getInstance()
                        transactionCalendar.time = transactionDate ?: Date()

                        transactionCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                transactionCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                    }

                val categoryTotals = filteredTransactions.groupBy { it.category }
                    .mapValues { entry ->
                        entry.value.sumOf { it.amount }
                    }

                val entries = categoryTotals.map { (category, total) ->
                    PieEntry(total.toFloat(), category)
                }

                val categoryColors = listOf(
                    Color.parseColor("#4CAF50"), // Hijau untuk Kebutuhan Pokok
                    Color.parseColor("#FF9800"), // Oranye untuk Entertainment
                    Color.parseColor("#2196F3"), // Biru untuk Tabungan
                    Color.parseColor("#9C27B0"), // Ungu untuk Sosial
                    Color.parseColor("#FF5722"), // Merah untuk lainnya
                    Color.parseColor("#009688")  // Hijau kebiruan untuk lainnya
                )

                val dataSet = PieDataSet(entries, "").apply {
                    colors = if (entries.size <= categoryColors.size) {
                        categoryColors.subList(0, entries.size)
                    } else {
                        categoryColors + ColorTemplate.MATERIAL_COLORS.toList()
                    }
                    sliceSpace = 3f
                    selectionShift = 5f
                }

                val data = PieData(dataSet).apply {
                    setValueFormatter(PercentFormatter(pieChart))
                    setValueTextSize(12f)
                    setValueTextColor(Color.WHITE)
                }

                pieChart.data = data
                pieChart.invalidate() // Refresh Pie Chart
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
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
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvDate.text = monthFormat.format(currentCalendar.time)
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(this,
            { _: DatePicker, year: Int, month: Int, _: Int ->
                currentCalendar.set(year, month, 1)
                updateDateDisplay()
                loadTransactionSummary()
                loadPieChartData(pieChart)
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Hanya memungkinkan pengguna memilih bulan dan tahun
        datePickerDialog.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE

        datePickerDialog.show()
    }
}