package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.projectpabkeras.adapters.TransactionAdapter
import com.example.projectpabkeras.adapters.WeeklyAdapter
import com.example.projectpabkeras.adapters.MonthlyAdapter
import com.example.projectpabkeras.models.MonthlySummary
import com.example.projectpabkeras.models.Transaction
import com.example.projectpabkeras.models.WeeklySummary
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class RiwayatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tabHarian: TextView
    private lateinit var tabMingguan: TextView
    private lateinit var tabBulanan: TextView
    private lateinit var tvPemasukan: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var tvSisaUang: TextView
    private lateinit var tvDate: TextView
    private val currentCalendar = Calendar.getInstance()
    private var activeTab: String = "Harian" // Default tab saat aplikasi dibuka


    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recyclerView)
        tabHarian = findViewById(R.id.tab_harian)
        tabMingguan = findViewById(R.id.tab_mingguan)
        tabBulanan = findViewById(R.id.tab_bulanan)
        tvPemasukan = findViewById(R.id.tv_pemasukan_nominal)
        tvPengeluaran = findViewById(R.id.tv_pengeluaran_nominal)
        tvSisaUang = findViewById(R.id.tv_sisa_uang_nominal)
        tvDate = findViewById(R.id.tv_date)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Setup navigasi
        setupNavigation()

        // Setup navigasi bulan
        setupMonthNavigation()

        // Fetch all transactions initially
        fetchTransactions()

        // Tab navigation
        tabHarian.setOnClickListener {
            activeTab = "Harian"
            setTabSelected(tabHarian, tabMingguan, tabBulanan)
            setupRecyclerViewHarian()
        }

        tabMingguan.setOnClickListener {
            activeTab = "Mingguan"
            setTabSelected(tabMingguan, tabHarian, tabBulanan)
            setupRecyclerViewMingguan()
        }

        tabBulanan.setOnClickListener {
            activeTab = "Bulanan"
            setTabSelected(tabBulanan, tabHarian, tabMingguan)
            setupRecyclerViewBulanan()
        }


        // Update bulan awal
        updateDateDisplay()
    }

    private fun setupNavigation() {
        val homeButton: ImageView = findViewById(R.id.ic_home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        val goalsButton: ImageView = findViewById(R.id.goals_bottom)
        goalsButton.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        val achievementButton: ImageView = findViewById(R.id.icAchievement)
        achievementButton.setOnClickListener {
            startActivity(Intent(this, AchievementActivity::class.java))
        }

        val profileButton: ImageView = findViewById(R.id.ic_profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun setupMonthNavigation() {
        val ivLeft: ImageView = findViewById(R.id.if_left)
        val ivRight: ImageView = findViewById(R.id.if_right)

        ivLeft.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1) // Mundur 1 bulan
            updateDateDisplay()
            fetchMonthlyTransactions()
            refreshActiveTab()
        }

        ivRight.setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1) // Maju 1 bulan
            updateDateDisplay()
            fetchMonthlyTransactions()
            refreshActiveTab()
        }
    }

    private fun refreshActiveTab() {
        when (activeTab) {
            "Harian" -> setupRecyclerViewHarian()
            "Mingguan" -> setupRecyclerViewMingguan()
            "Bulanan" -> setupRecyclerViewBulanan()
        }
    }


    private fun updateDateDisplay() {
        val monthFormat = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvDate.text = monthFormat.format(currentCalendar.time)
    }

    private fun fetchTransactions() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .map { transaction ->
                        transaction.apply {
                            date = date.takeIf { it.isNotEmpty() }
                                ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        }
                    }
                setupRecyclerViewHarian()
                updateSummary()
            }
            .addOnFailureListener {
                // Handle error
            }
    }


    private fun updateSummary() {
        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
        val remainingMoney = totalIncome - totalExpense

        tvPemasukan.text = "Rp ${totalIncome.toInt()}"
        tvPengeluaran.text = "Rp ${totalExpense.toInt()}"
        tvSisaUang.text = "Rp ${remainingMoney.toInt()}"
    }

    private fun setupRecyclerViewHarian() {
        val selectedMonthYear = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)

        val filteredTransactions = transactions.filter { transaction ->
            transaction.date.startsWith(selectedMonthYear) // Filter berdasarkan bulan
        }.sortedBy { transaction ->
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)?.time
        } // Urutkan berdasarkan tanggal

        val adapter = TransactionAdapter(filteredTransactions)
        recyclerView.adapter = adapter
    }


    private fun setupRecyclerViewMingguan() {
        val weeklySummaries = groupTransactionsByWeekInMonth(transactions)
        val adapter = WeeklyAdapter(weeklySummaries)
        recyclerView.adapter = adapter
    }

    private fun fetchMonthlyTransactions() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val filteredTransactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                    .filter { transaction ->
                        val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                        val transactionCalendar = Calendar.getInstance()
                        transactionCalendar.time = transactionDate ?: Date()

                        // Filter transaksi berdasarkan bulan dan tahun yang dipilih
                        transactionCalendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                transactionCalendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
                    }

                // Setelah data difilter, update tampilan
                updateMonthlySummary(filteredTransactions)
            }
            .addOnFailureListener {
                // Handle error jika pengambilan data gagal
            }
    }

    private fun updateMonthlySummary(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }

        // Perbarui nilai pemasukan, pengeluaran, dan sisa uang di bagian atas
        tvPemasukan.text = "Rp ${totalIncome.toInt()}"
        tvPengeluaran.text = "Rp ${totalExpense.toInt()}"
        tvSisaUang.text = "Rp ${(totalIncome - totalExpense).toInt()}"

        // Buat summary untuk RecyclerView
        val summary = WeeklySummary(
            "${SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(currentCalendar.time)}",
            totalIncome,
            totalExpense
        )

        // Tampilkan summary di RecyclerView
//        val adapter = MonthlyAdapter(listOf(summary))
//        recyclerView.adapter = adapter
    }

    private fun groupTransactionsByWeekInMonth(transactions: List<Transaction>): List<WeeklySummary> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(currentCalendar.time)
        val calendar = Calendar.getInstance()

        val filteredTransactions = transactions.filter {
            it.date.startsWith(currentMonth) // Hanya transaksi dari bulan yang dipilih
        }

        val weeklySummaries = mutableListOf<WeeklySummary>()

        filteredTransactions.groupBy {
            val date = dateFormat.parse(it.date)
            calendar.time = date ?: Date()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) // Mulai dari Senin
            dateFormat.format(calendar.time)
        }.forEach { (startOfWeek, weekTransactions) ->
            val startDate = dateFormat.parse(startOfWeek)
            calendar.time = startDate ?: Date()
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeek = dateFormat.format(calendar.time)

            val totalIncome = weekTransactions.filter { it.type == "income" }
                .sumOf { it.amount.toDouble() } // Konversi ke Double untuk konsistensi
            val totalExpense = weekTransactions.filter { it.type == "expense" }
                .sumOf { it.amount.toDouble() }

            weeklySummaries.add(
                WeeklySummary(
                    weekRange = "$startOfWeek - $endOfWeek",
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )
            )
        }

        return weeklySummaries
    }



    private fun setupRecyclerViewBulanan() {
        val userId = auth.currentUser?.uid ?: return

        // Format bulan dan tahun yang dipilih
        val selectedMonthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(currentCalendar.time)

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                val transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }

                // Filter transaksi berdasarkan bulan dan tahun yang dipilih
                val filteredTransactions = transactions.filter { transaction ->
                    val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
                    val transactionMonthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(transactionDate ?: Date())
                    transactionMonthYear == selectedMonthYear
                }

                // Hitung total pemasukan dan pengeluaran untuk bulan yang dipilih
                val totalIncome = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
                val totalExpense = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }

                // Buat data bulanan
                val monthlySummary = MonthlySummary(
                    monthName = selectedMonthYear,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense
                )

                // Set data ke adapter
                val adapter = MonthlyAdapter(listOf(monthlySummary)) // Hanya data bulan yang dipilih
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal memuat data bulanan: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun groupTransactionsByWeek(transactions: List<Transaction>): List<WeeklySummary> {
        val weeklySummaries = mutableListOf<WeeklySummary>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        transactions.groupBy {
            val date = dateFormat.parse(it.date)
            calendar.time = date ?: Date()
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            dateFormat.format(calendar.time)
        }.forEach { (startOfWeek, weekTransactions) ->
            val startDate = dateFormat.parse(startOfWeek)
            calendar.time = startDate ?: Date()
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeek = dateFormat.format(calendar.time)

            val totalIncome = weekTransactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = weekTransactions.filter { it.type == "expense" }.sumOf { it.amount }

            weeklySummaries.add(WeeklySummary("$startOfWeek s/d $endOfWeek", totalIncome, totalExpense))
        }

        return weeklySummaries
    }



    private fun setTabSelected(selectedTab: TextView, vararg otherTabs: TextView) {
        selectedTab.setBackgroundResource(R.drawable.tab_selected)
        otherTabs.forEach { it.setBackgroundResource(R.drawable.tab_unselected) }
    }
}
