package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class InputActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val btnPengeluaran = findViewById<Button>(R.id.btn_pengeluaran)
        val btnPemasukan = findViewById<Button>(R.id.btn_pemasukan)
        val layoutPengeluaran = findViewById<LinearLayout>(R.id.layout_pengeluaran)
        val layoutPemasukan = findViewById<LinearLayout>(R.id.layout_pemasukan)

        // Ganti tampilan berdasarkan tombol yang dipilih
        btnPengeluaran.setOnClickListener {
            layoutPengeluaran.visibility = LinearLayout.VISIBLE
            layoutPemasukan.visibility = LinearLayout.GONE
        }
        btnPemasukan.setOnClickListener {
            layoutPengeluaran.visibility = LinearLayout.GONE
            layoutPemasukan.visibility = LinearLayout.VISIBLE
        }

        // Inisialisasi Spinner Kategori
        val categories = listOf("Kebutuhan Pokok", "Hiburan", "Tabungan", "Sosial")
        val spinner = findViewById<Spinner>(R.id.spinner_kategori)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Setup DatePicker
        val etTanggalPengeluaran = findViewById<EditText>(R.id.et_tanggal_pengeluaran)
        val etTanggalPemasukan = findViewById<EditText>(R.id.et_tanggal_pemasukan)

        etTanggalPengeluaran.setOnClickListener { showDatePicker(etTanggalPengeluaran) }
        etTanggalPemasukan.setOnClickListener { showDatePicker(etTanggalPemasukan) }

        // Tombol Simpan
        val btnSimpanPengeluaran = findViewById<Button>(R.id.btn_simpan_pengeluaran)
        val btnSimpanPemasukan = findViewById<Button>(R.id.btn_simpan_pemasukan)

        btnSimpanPengeluaran.setOnClickListener { saveTransaction("expense") }
        btnSimpanPemasukan.setOnClickListener { saveTransaction("income") }
    }

    private fun showDatePicker(editText: EditText) {
        val datePickerDialog = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            editText.setText(sdf.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        datePickerDialog.show()
    }

    private fun saveTransaction(type: String) {
        val userId = auth.currentUser?.uid ?: return

        val category = if (type == "expense") {
            findViewById<Spinner>(R.id.spinner_kategori).selectedItem.toString()
        } else {
            "Income" // Default kategori untuk pemasukan
        }

        val amount = if (type == "expense") {
            findViewById<EditText>(R.id.et_jumlah_pengeluaran).text.toString().toDoubleOrNull()
        } else {
            findViewById<EditText>(R.id.et_jumlah_pemasukan).text.toString().toDoubleOrNull()
        }

        val date = if (type == "expense") {
            findViewById<EditText>(R.id.et_tanggal_pengeluaran).text.toString()
        } else {
            findViewById<EditText>(R.id.et_tanggal_pemasukan).text.toString()
        }

        val description = if (type == "expense") {
            findViewById<EditText>(R.id.et_keterangan_pengeluaran).text.toString()
        } else {
            findViewById<EditText>(R.id.et_keterangan_pemasukan).text.toString()
        }

        if (amount == null || date.isEmpty()) {
            Toast.makeText(this, "Harap isi semua field", Toast.LENGTH_SHORT).show()
            return
        }

        val transaction = mapOf(
            "userId" to userId,
            "type" to type,
            "category" to category,
            "amount" to amount,
            "description" to description,
            "date" to date
        )

        firestore.collection("transactions").add(transaction)
            .addOnSuccessListener {
                Toast.makeText(this, "Transaksi berhasil disimpan", Toast.LENGTH_SHORT).show()
                if (type == "income") {
                    updateCategoryLimits(userId) // Update limit saat pemasukan disimpan
                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateCategoryLimits(userId: String) {
        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "income")
            .get()
            .addOnSuccessListener { snapshot ->
                val totalIncome = snapshot.documents.sumOf { it.getDouble("amount") ?: 0.0 }

                // Hitung limit kategori
                val limits = mapOf(
                    "kebutuhan" to (totalIncome * 0.45).toInt(),
                    "hiburan" to (totalIncome * 0.25).toInt(),
                    "tabungan" to (totalIncome * 0.20).toInt(),
                    "sosial" to (totalIncome * 0.10).toInt(),
                    "updatedMonth" to SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                )

                // Simpan limit ke koleksi "kategori" berdasarkan userId
                firestore.collection("kategori")
                    .document(userId)
                    .set(limits)
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
}
