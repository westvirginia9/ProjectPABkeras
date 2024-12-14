package com.example.projectpabkeras

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class DetailGrafikActivity2 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_grafik2)

        // Tangkap data dari Intent
        val categoryName = intent.getStringExtra("kategori")
        val categoryPercentage = intent.getFloatExtra("persentase", 0f)
        val expenses = intent.getParcelableArrayListExtra<Expense>("pengeluaran") ?: emptyList()
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


        // Set judul toolbar
        supportActionBar?.title = categoryName

        // Setup PieChart
        val pieChart = findViewById<PieChart>(R.id.pie_chart)
        setupPieChart(pieChart, categoryName, categoryPercentage)

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ExpenseAdapter(expenses)
    }

    private fun setupPieChart(pieChart: PieChart, categoryName: String?, percentage: Float) {
        val entries = listOf(
            PieEntry(percentage, categoryName),
            PieEntry(100 - percentage, "Lain-Lain")
        )

        val dataSet = PieDataSet(entries, "").apply {
            colors = listOf(Color.parseColor("#4CAF50"), Color.LTGRAY)
        }

        val data = PieData(dataSet).apply {
            setValueTextSize(12f)
            setValueTextColor(Color.WHITE)
        }

        pieChart.data = data
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.animateY(1000)
    }
}

