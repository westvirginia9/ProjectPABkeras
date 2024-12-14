package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class InputActivity : AppCompatActivity() {

    // Variabel untuk tombol tab
    private lateinit var btnPengeluaran: Button
    private lateinit var btnPemasukan: Button
    private lateinit var btnBack: ImageView

    // Variabel untuk pengeluaran
    private lateinit var spinnerKategori: Spinner
    private lateinit var tanggalPengeluaranEditText: EditText
    private lateinit var keteranganPengeluaranEditText: EditText
    private lateinit var jumlahPengeluaranEditText: EditText

    // Variabel untuk pemasukan
    private lateinit var tanggalPemasukanEditText: EditText
    private lateinit var keteranganPemasukanEditText: EditText
    private lateinit var jumlahPemasukanEditText: EditText

    // Tombol simpan
    private lateinit var btnSimpanPengeluaran: Button
    private lateinit var btnSimpanPemasukan: Button

    private var isPengeluaran = true // Default: Tab Pengeluaran aktif

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        // Inisialisasi komponen
        btnPengeluaran = findViewById(R.id.btn_pengeluaran)
        btnPemasukan = findViewById(R.id.btn_pemasukan)

        // Pengeluaran
        spinnerKategori = findViewById(R.id.spinner_kategori)
        tanggalPengeluaranEditText = findViewById(R.id.et_tanggal_pengeluaran)
        keteranganPengeluaranEditText = findViewById(R.id.et_keterangan_pengeluaran)
        jumlahPengeluaranEditText = findViewById(R.id.et_jumlah_pengeluaran)
        btnSimpanPengeluaran = findViewById(R.id.btn_simpan_pengeluaran)

        // Pemasukan
        tanggalPemasukanEditText = findViewById(R.id.et_tanggal_pemasukan)
        keteranganPemasukanEditText = findViewById(R.id.et_keterangan_pemasukan)
        jumlahPemasukanEditText = findViewById(R.id.et_jumlah_pemasukan)
        btnSimpanPemasukan = findViewById(R.id.btn_simpan_pemasukan)

        btnBack = findViewById(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressed()  // Fungsi untuk kembali ke halaman sebelumnya
        }
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


        // Setup Spinner Kategori untuk pengeluaran
        val kategoriList = listOf("Kebutuhan Pokok", "Entertainment", "Tabungan", "Sosial")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategori.adapter = adapter

        // Setup tombol tab
        btnPengeluaran.setOnClickListener {
            isPengeluaran = true
            updateFormUI()
        }

        btnPemasukan.setOnClickListener {
            isPengeluaran = false
            updateFormUI()
        }

        // Date picker untuk pengeluaran
        tanggalPengeluaranEditText.setOnClickListener { showDatePickerDialog(tanggalPengeluaranEditText) }

        // Date picker untuk pemasukan
        tanggalPemasukanEditText.setOnClickListener { showDatePickerDialog(tanggalPemasukanEditText) }

        // Simpan data pengeluaran
        btnSimpanPengeluaran.setOnClickListener {
            val tanggal = tanggalPengeluaranEditText.text.toString()
            val keterangan = keteranganPengeluaranEditText.text.toString()
            val jumlah = jumlahPengeluaranEditText.text.toString().toDoubleOrNull()
            val kategori = spinnerKategori.selectedItem.toString()

            if (tanggal.isEmpty() || keterangan.isEmpty() || jumlah == null) {
                Toast.makeText(this, "Lengkapi data pengeluaran!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Pengeluaran disimpan: $jumlah - $kategori", Toast.LENGTH_SHORT).show()
        }

        // Simpan data pemasukan
        btnSimpanPemasukan.setOnClickListener {
            val tanggal = tanggalPemasukanEditText.text.toString()
            val keterangan = keteranganPemasukanEditText.text.toString()
            val jumlah = jumlahPemasukanEditText.text.toString().toDoubleOrNull()

            if (tanggal.isEmpty() || keterangan.isEmpty() || jumlah == null) {
                Toast.makeText(this, "Lengkapi data pemasukan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Toast.makeText(this, "Pemasukan disimpan: $jumlah", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFormUI() {
        if (isPengeluaran) {
            btnPengeluaran.setBackgroundColor(getColor(R.color.selected_tab))
            btnPemasukan.setBackgroundColor(getColor(R.color.unselected_tab))

            findViewById<View>(R.id.layout_pengeluaran).visibility = View.VISIBLE
            findViewById<View>(R.id.layout_pemasukan).visibility = View.GONE
        } else {
            btnPengeluaran.setBackgroundColor(getColor(R.color.unselected_tab))
            btnPemasukan.setBackgroundColor(getColor(R.color.selected_tab))

            findViewById<View>(R.id.layout_pengeluaran).visibility = View.GONE
            findViewById<View>(R.id.layout_pemasukan).visibility = View.VISIBLE
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                editText.setText("$dayOfMonth/${month + 1}/$year")
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
}
