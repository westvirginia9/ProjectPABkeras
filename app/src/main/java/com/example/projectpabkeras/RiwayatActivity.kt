package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RiwayatActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tabHarian: TextView
    private lateinit var tabMingguan: TextView
    private lateinit var tabBulanan: TextView
    private lateinit var tvDate: TextView
    private lateinit var ivLeft: ImageView
    private lateinit var ivRight: ImageView
    private val currentCalendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
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

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi Tab
        tabHarian = findViewById(R.id.tab_harian)
        tabMingguan = findViewById(R.id.tab_mingguan)
        tabBulanan = findViewById(R.id.tab_bulanan)

        // Inisialisasi komponen untuk bulan dan tahun
        tvDate = findViewById(R.id.tv_date)
        ivLeft = findViewById(R.id.if_left)
        ivRight = findViewById(R.id.if_right)

        // Set listener untuk navigasi bulan
        ivLeft.setOnClickListener { moveToPreviousMonth() }
        ivRight.setOnClickListener { moveToNextMonth() }
        tvDate.setOnClickListener { showDatePickerDialog() }

        // Update tampilan tanggal awal
        updateDateDisplay()

        // Default tampilkan data harian
        tampilkanDataHarian()

        // Set OnClickListeners untuk tab
        tabHarian.setOnClickListener {
            setTabSelected(tabHarian, tabMingguan, tabBulanan)
            tampilkanDataHarian()
        }

        tabMingguan.setOnClickListener {
            setTabSelected(tabMingguan, tabHarian, tabBulanan)
            tampilkanDataMingguan()
        }

        tabBulanan.setOnClickListener {
            setTabSelected(tabBulanan, tabHarian, tabMingguan)
            tampilkanDataBulanan()
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
        val monthFormat = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
        tvDate.text = monthFormat.format(currentCalendar.time)
    }

    private fun showDatePickerDialog() {
        val datePickerDialog = DatePickerDialog(this,
            { _: DatePicker, year: Int, month: Int, _: Int ->
                currentCalendar.set(year, month, 1)
                updateDateDisplay()
            },
            currentCalendar.get(Calendar.YEAR),
            currentCalendar.get(Calendar.MONTH),
            currentCalendar.get(Calendar.DAY_OF_MONTH)
        )

        // Sembunyikan pilihan hari
        datePickerDialog.datePicker.findViewById<View>(
            resources.getIdentifier("day", "id", "android")
        )?.visibility = View.GONE

        datePickerDialog.show()
    }

    private fun tampilkanDataHarian() {
        val riwayatList = listOf(
            RiwayatItem(
                RiwayatItem.TYPE_HARIAN,
                "Rabu, 31-10-2024",
                "Kebutuhan Dasar",
                "Nasi Padang",
                20000
            )
        )
        val adapter = RiwayatAdapter(riwayatList)
        recyclerView.adapter = adapter
    }

    private fun tampilkanDataMingguan() {
        val riwayatList = listOf(
            RiwayatItem(
                RiwayatItem.TYPE_MINGGUAN,
                "31-10-2024 s/d 06-11-2024",
                pengeluaran = 84000
            )
        )
        val adapter = RiwayatAdapter(riwayatList)
        recyclerView.adapter = adapter
    }

    private fun tampilkanDataBulanan() {
        val riwayatList = listOf(
            RiwayatItem(
                RiwayatItem.TYPE_BULANAN,
                "November 2024",
                pengeluaran = 982000
            )
        )
        val adapter = RiwayatAdapter(riwayatList)
        recyclerView.adapter = adapter
    }

    private fun setTabSelected(selectedTab: TextView, vararg otherTabs: TextView) {
        selectedTab.setBackgroundResource(R.drawable.tab_selected)
        otherTabs.forEach { it.setBackgroundResource(R.drawable.tab_unselected) }
    }
}
