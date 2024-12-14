package com.example.projectpabkeras

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class DetailGrafikActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_grafik)

        // Inisialisasi View
        pieChart = findViewById(R.id.pie_chart)
        recyclerView = findViewById(R.id.recycler_view_categories)
        val btnBack = findViewById<ImageView>(R.id.btn_back)

        // Tombol kembali
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

        // Data Kategori (dengan daftar pengeluaran)
        val categories = listOf(
            CategoryAdapter.Category(
                "Kebutuhan Pokok",
                40,
                350000,
                Color.parseColor("#4CAF50"),
                listOf(
                    Expense("1 Nov 2024", "Makan", 20000.0),
                    Expense("2 Nov 2024", "Transportasi", 50000.0)
                )
            ),
            CategoryAdapter.Category(
                "Entertainment",
                20,
                20000,
                Color.parseColor("#FF9800"),
                listOf(
                    Expense("3 Nov 2024", "Bioskop", 60000.0)
                )
            ),
            CategoryAdapter.Category(
                "Tabungan",
                25,
                300000,
                Color.parseColor("#2196F3"),
                listOf(
                    Expense("5 Nov 2024", "Deposito", 150000.0),
                    Expense("6 Nov 2024", "Investasi Saham", 100000.0)
                )
            ),
            CategoryAdapter.Category(
                "Sosial",
                15,
                23000,
                Color.parseColor("#9C27B0"),
                listOf(
                    Expense("7 Nov 2024", "Donasi", 10000.0),
                    Expense("8 Nov 2024", "Kegiatan Sosial", 13000.0)
                )
            )
        )

        // Data untuk PieChart
        val pieEntries = categories.map { PieEntry(it.percentage.toFloat(), it.name) }
        val colors = categories.map { it.color }

        // Setup DataSet
        val dataSet = PieDataSet(pieEntries, "").apply {
            this.colors = colors
            sliceSpace = 3f // Jarak antar potongan
            selectionShift = 5f // Efek pembesaran saat dipilih
        }

        // Setup PieData
        val pieData = PieData(dataSet).apply {
            setValueFormatter(PercentFormatter(pieChart)) // Tampilkan persentase
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        // Konfigurasi PieChart
        pieChart.apply {
            data = pieData
            setUsePercentValues(true)
            description.isEnabled = false // Nonaktifkan deskripsi default
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setDrawHoleEnabled(true)
            holeRadius = 40f // Ukuran lingkaran tengah
            transparentCircleRadius = 45f // Pertebal efek pinggir
            setHoleColor(Color.WHITE) // Warna lingkaran tengah
            setDrawEntryLabels(true) // Tampilkan label data

            // Teks tengah
            centerText = "Pengeluaran"
            setCenterTextSize(12f)

            // Animasi
            animateY(1000)

            // Konfigurasi legenda
            legend.apply {
                verticalAlignment = Legend.LegendVerticalAlignment.TOP
                horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
                orientation = Legend.LegendOrientation.VERTICAL
                textSize = 12f
                yOffset = 10f
            }
        }

        // Setup RecyclerView dengan adapter
        adapter = CategoryAdapter(categories) { selectedCategory ->
            val intent = Intent(this, DetailGrafikActivity2::class.java)
            intent.putExtra("kategori", selectedCategory.name) // Kirim nama kategori
            intent.putExtra("persentase", selectedCategory.percentage.toFloat()) // Kirim persentase kategori
            intent.putParcelableArrayListExtra(
                "pengeluaran",
                ArrayList(selectedCategory.expenses) // Kirim daftar pengeluaran (jika ada)
            )
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }
}
