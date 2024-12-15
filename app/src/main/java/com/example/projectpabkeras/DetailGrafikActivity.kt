package com.example.projectpabkeras

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.models.Transaction
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DetailGrafikActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var tvDate: TextView
    private lateinit var ivLeft: ImageView
    private lateinit var ivRight: ImageView
    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_grafik)

        // Inisialisasi View
        pieChart = findViewById(R.id.pie_chart)
        recyclerView = findViewById(R.id.recycler_view_categories)
        tvDate = findViewById(R.id.grafik_date)
        ivLeft = findViewById(R.id.iv_left)
        ivRight = findViewById(R.id.iv_right)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        // Tombol kembali
        btnBack.setOnClickListener { onBackPressed() }

        // Navigasi bulan
        updateDateDisplay()
        ivLeft.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1) // Mundur bulan
            updateDateDisplay()
            loadChartData()
        }
        ivRight.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1) // Maju bulan
            updateDateDisplay()
            loadChartData()
        }

        // Tombol navigasi lainnya
        setupNavigation()

        // Muat data grafik awal
        loadChartData()
    }

    private fun setupNavigation() {
        findViewById<ImageView>(R.id.goals_bottom).setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }
        findViewById<ImageView>(R.id.icAchievement).setOnClickListener {
            startActivity(Intent(this, AchievementActivity::class.java))
        }
        findViewById<ImageView>(R.id.ic_home).setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }
        findViewById<ImageView>(R.id.ic_history).setOnClickListener {
            startActivity(Intent(this, RiwayatActivity::class.java))
        }
        findViewById<ImageView>(R.id.ic_profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun updateDateDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvDate.text = monthFormat.format(currentCalendar.time)
    }

    private fun loadChartData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        // Ambil awal dan akhir bulan berdasarkan `currentCalendar`
        val startOfMonth = currentCalendar.clone() as Calendar
        startOfMonth.set(Calendar.DAY_OF_MONTH, 1)

        val endOfMonth = currentCalendar.clone() as Calendar
        endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH))

        db.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "expense") // Ambil hanya pengeluaran
            .get()
            .addOnSuccessListener { snapshot ->
                val filteredTransactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter { transaction ->
                        val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                        transactionDate != null && transactionDate.after(startOfMonth.time) && transactionDate.before(endOfMonth.time)
                    }

                updateChartData(filteredTransactions) // Perbarui data grafik
                updateCategoryList(filteredTransactions) // Perbarui daftar kategori
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateChartData(transactions: List<Transaction>) {
        val categoryTotals = transactions.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val pieEntries = categoryTotals.map { (category, total) -> PieEntry(total.toFloat(), category) }
        val colors = listOf(
            Color.parseColor("#4CAF50"), // Hijau
            Color.parseColor("#FF9800"), // Oranye
            Color.parseColor("#2196F3"), // Biru
            Color.parseColor("#9C27B0"), // Ungu
            Color.parseColor("#FF5722"), // Merah
            Color.parseColor("#009688")  // Hijau kebiruan
        )

        val dataSet = PieDataSet(pieEntries, "").apply {
            this.colors = if (pieEntries.size <= colors.size) colors.subList(0, pieEntries.size) else colors
            sliceSpace = 3f
            selectionShift = 5f
        }

        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        pieChart.apply {
            data = pieData
            setUsePercentValues(true)
            description.isEnabled = false
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setDrawHoleEnabled(true)
            holeRadius = 40f
            transparentCircleRadius = 45f
            setHoleColor(Color.WHITE)
            setDrawEntryLabels(true)
            centerText = "Pengeluaran"
            setCenterTextSize(12f)
            animateY(1000)

            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                textSize = 12f
                yOffset = 10f
            }
        }

        pieChart.invalidate() // Refresh chart
    }

    private fun updateCategoryList(transactions: List<Transaction>) {
        val categories = transactions.groupBy { it.category }
            .map { (category, categoryTransactions) ->
                val totalAmount = categoryTransactions.sumOf { it.amount }
                val percentage = (totalAmount / transactions.sumOf { it.amount }) * 100
                val color = when (category) {
                    "Kebutuhan Pokok" -> Color.parseColor("#4CAF50")
                    "Entertainment" -> Color.parseColor("#FF9800")
                    "Tabungan" -> Color.parseColor("#2196F3")
                    "Sosial" -> Color.parseColor("#9C27B0")
                    else -> Color.parseColor("#FF5722")
                }
                CategoryAdapter.Category(category, percentage.toInt(), totalAmount, color, categoryTransactions.map {
                    Expense(it.date, it.description, it.amount)
                })
            }

        adapter = CategoryAdapter(categories) { selectedCategory ->
            val intent = Intent(this, DetailGrafikActivity2::class.java)
            intent.putExtra("kategori", selectedCategory.name)
            intent.putExtra("persentase", selectedCategory.percentage.toFloat())
            intent.putParcelableArrayListExtra("pengeluaran", ArrayList(selectedCategory.expenses))
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
