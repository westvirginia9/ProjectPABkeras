package com.example.projectpabkeras

import android.app.DatePickerDialog
import android.content.Intent
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

        // Tombol Navigasi
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
                    onIncomeAdded(amount!!.toLong())
                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                    val incomeAmount = amount?.toLong() ?: 0L
                    AchievementActivity().updateIncomeAchievement(userId, incomeAmount)
                    updateIncomeAchievement(userId, amount.toLong())

                }
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
//        if (type == "income") {
//            val userId = auth.currentUser?.uid ?: return
//
//        }
    }

    private fun updateIncomeAchievement(userId: String, incomeAdded: Long) {
        val db = FirebaseFirestore.getInstance()

        db.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievementsArray = document.get("achievements") as? List<Map<String, Any>>
                    if (achievementsArray != null) {
                        val updatedAchievements = achievementsArray.map { achievementData ->
                            if ((achievementData["title"] as String).contains("pemasukan", ignoreCase = true)) {
                                val currentProgress = (achievementData["currentProgress"] as Long) + incomeAdded
                                val targetAmount = achievementData["targetAmount"] as Long
                                val currentLevel = (achievementData["currentLevel"] as Long).toInt()
                                val maxLevel = (achievementData["maxLevel"] as Long).toInt()
                                val exp = (achievementData["exp"] as Long).toInt()

                                val newLevel = if (currentProgress >= targetAmount && currentLevel < maxLevel) {
                                    currentLevel + 1
                                } else {
                                    currentLevel
                                }

                                // Progress tetap akumulatif
                                val newProgress = currentProgress

                                val newTargetAmount = if (newLevel > currentLevel) {
                                    targetAmount + 2000000
                                } else {
                                    targetAmount
                                }

                                val newExp = if (newLevel > currentLevel) {
                                    exp + 100
                                } else {
                                    exp
                                }

                                return@map mapOf(
                                    "title" to achievementData["title"],
                                    "currentLevel" to newLevel,
                                    "maxLevel" to maxLevel,
                                    "exp" to newExp,
                                    "targetAmount" to newTargetAmount,
                                    "currentProgress" to newProgress
                                )
                            }
                            achievementData
                        }

                        db.collection("achievements").document(userId)
                            .update("achievements", updatedAchievements)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Pencapaian pemasukan diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal memperbarui pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
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

    private fun onIncomeAdded(amount: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("achievements").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val achievementsArray = document.get("achievements") as? List<Map<String, Any>>
                    if (achievementsArray != null) {
                        val updatedAchievements = achievementsArray.map { achievementData ->
                            val currentProgress = (achievementData["currentProgress"] as Long) + amount
                            val targetAmount = achievementData["targetAmount"] as Long
                            val currentLevel = (achievementData["currentLevel"] as Long).toInt()
                            val maxLevel = (achievementData["maxLevel"] as Long).toInt()

                            var newLevel = currentLevel
                            var newTargetAmount = targetAmount

                            // Naikkan level jika currentProgress melebihi targetAmount
                            var remainingProgress = currentProgress
                            while (remainingProgress >= newTargetAmount && newLevel < maxLevel) {
                                remainingProgress -= newTargetAmount
                                newLevel++
                                newTargetAmount += 2000000 // Contoh: target baru +2 juta
                            }

                            // Hitung EXP jika level naik
                            val expGained = if (newLevel > currentLevel) {
                                (newLevel - currentLevel) * 100 // Tambah 100 EXP per level
                            } else {
                                0
                            }

                            // Jika level naik, tambahkan EXP ke user
                            if (expGained > 0) {
                                updateUserExp(userId, expGained)
                            }

                            // Kembalikan data achievement yang diperbarui
                            mapOf(
                                "title" to "Total keseluruhan pemasukan mencapai ${newTargetAmount} IDR",
                                "currentLevel" to newLevel,
                                "maxLevel" to maxLevel,
                                "exp" to (achievementData["exp"] as Long).toInt() + expGained,
                                "targetAmount" to newTargetAmount,
                                "currentProgress" to remainingProgress // Progres tetap dihitung dari sisa
                            )
                        }

                        // Simpan ke Firestore
                        db.collection("achievements").document(userId)
                            .update("achievements", updatedAchievements)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Pencapaian diperbarui!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Gagal memperbarui pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat pencapaian: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }






    private fun updateUserExp(userId: String, expGained: Int) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Ambil total EXP yang ada saat ini
                    val currentExp = document.getLong("totalExp")?.toInt() ?: 0
                    val newExp = currentExp + expGained

                    // Perbarui total EXP di Firestore
                    db.collection("users").document(userId)
                        .update("totalExp", newExp)
                        .addOnSuccessListener {
                            Toast.makeText(this, "EXP diperbarui menjadi $newExp", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Gagal memperbarui EXP: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data pengguna: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }








}
