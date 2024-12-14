package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

data class Transaction(
    val date: String = "",
    val type: String = "",
    val category: String = "",
    val amount: Double = 0.0,
    val description: String = ""
)

class RiwayatActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvPemasukan: TextView
    private lateinit var tvPengeluaran: TextView
    private lateinit var tvSisaUang: TextView
    private lateinit var tabHarian: TextView
    private lateinit var tabMingguan: TextView
    private lateinit var tabBulanan: TextView
    private lateinit var tvDate: TextView
    private val currentCalendar = Calendar.getInstance()

    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        auth = FirebaseAuth.getInstance()

        // Inisialisasi Views
        tvPemasukan = findViewById(R.id.tv_pemasukan_nominal)
        tvPengeluaran = findViewById(R.id.tv_pengeluaran_nominal)
        tvSisaUang = findViewById(R.id.tv_sisa_uang_nominal)
        recyclerView = findViewById(R.id.recyclerView)
        tabHarian = findViewById(R.id.tab_harian)
        tabMingguan = findViewById(R.id.tab_mingguan)
        tabBulanan = findViewById(R.id.tab_bulanan)
        tvDate = findViewById(R.id.tv_date)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Tombol navigasi
        setupNavigation()

        // Update tampilan tanggal awal
        updateDateDisplay()

        // Fetch data awal (Harian)
        setTabSelected(tabHarian, tabMingguan, tabBulanan)
        fetchTransactions("harian")

        // Navigasi Tab
        tabHarian.setOnClickListener {
            setTabSelected(tabHarian, tabMingguan, tabBulanan)
            fetchTransactions("harian")
        }

        tabMingguan.setOnClickListener {
            setTabSelected(tabMingguan, tabHarian, tabBulanan)
            fetchTransactions("mingguan")
        }

        tabBulanan.setOnClickListener {
            setTabSelected(tabBulanan, tabHarian, tabMingguan)
            fetchTransactions("bulanan")
        }
    }

    private fun setupNavigation() {
        // Tombol Kembali
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { onBackPressed() }

        // Tombol Home
        val homeButton: ImageView = findViewById(R.id.ic_home)
        homeButton.setOnClickListener {
            startActivity(Intent(this, HomePageActivity::class.java))
        }

        // Tombol Goals
        val goalsButton: ImageView = findViewById(R.id.goals_bottom)
        goalsButton.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }

        // Tombol Achievement
        val achievementButton: ImageView = findViewById(R.id.icAchievement)
        achievementButton.setOnClickListener {
            startActivity(Intent(this, AchievementActivity::class.java))
        }

        // Tombol Profil
        val profileButton: ImageView = findViewById(R.id.ic_profile)
        profileButton.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun updateDateDisplay() {
        val monthFormat = SimpleDateFormat("MMM - yyyy", Locale.getDefault())
        tvDate.text = monthFormat.format(currentCalendar.time)
    }

    private fun fetchTransactions(viewType: String) {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }

                when (viewType) {
                    "harian" -> setupRecyclerViewHarian()
                    "mingguan" -> setupRecyclerViewMingguan()
                    "bulanan" -> setupRecyclerViewBulanan()
                }

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
        val adapter = TransactionAdapter(transactions) // Seluruh transaksi
        recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewMingguan() {
        // Filter data mingguan (contoh implementasi)
        val filteredTransactions = transactions.filter {
            val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val calendar = Calendar.getInstance().apply { time = transactionDate }
            val currentWeek = currentCalendar.get(Calendar.WEEK_OF_YEAR)
            calendar.get(Calendar.WEEK_OF_YEAR) == currentWeek
        }
        val adapter = TransactionAdapter(filteredTransactions)
        recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewBulanan() {
        // Filter data bulanan
        val filteredTransactions = transactions.filter {
            val transactionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            val calendar = Calendar.getInstance().apply { time = transactionDate }
            val currentMonth = currentCalendar.get(Calendar.MONTH)
            calendar.get(Calendar.MONTH) == currentMonth
        }
        val adapter = TransactionAdapter(filteredTransactions)
        recyclerView.adapter = adapter
    }

    private fun setTabSelected(selectedTab: TextView, vararg otherTabs: TextView) {
        selectedTab.setBackgroundResource(R.drawable.tab_selected)
        otherTabs.forEach { it.setBackgroundResource(R.drawable.tab_unselected) }
    }
}

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvCategory: TextView = itemView.findViewById(R.id.tvKategori)
        val tvDescription: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvAmount: TextView = itemView.findViewById(R.id.tvPengeluaranHarian)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TransactionViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_harian, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvDate.text = transaction.date
        holder.tvCategory.text = transaction.category
        holder.tvDescription.text = transaction.description
        holder.tvAmount.text = if (transaction.type == "expense") {
            "- Rp ${transaction.amount.toInt()}"
        } else {
            "+ Rp ${transaction.amount.toInt()}"
        }
    }

    override fun getItemCount(): Int = transactions.size
}
