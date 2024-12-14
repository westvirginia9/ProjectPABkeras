package com.example.projectpabkeras

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
    val date: String = "", // Format tanggal: "yyyy-MM-dd"
    val type: String = "", // "income" atau "expense"
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
        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener { onBackPressed() }

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
        val adapter = SummaryAdapter(transactions, "harian")
        recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewMingguan() {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val startOfWeek = calendar.time

        val dateFormatter = SimpleDateFormat("dd-MM-yyyy", Locale("id", "ID"))
        tvDate.text = "${dateFormatter.format(startOfWeek)} s/d ${dateFormatter.format(today)}"

        val totalIncome = transactions.filter { isInCurrentWeek(it.date) && it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { isInCurrentWeek(it.date) && it.type == "expense" }.sumOf { it.amount }

        val summaryTransactions = listOf(
            Transaction(
                date = "${dateFormatter.format(startOfWeek)} s/d ${dateFormatter.format(today)}",
                type = "summary",
                amount = totalIncome - totalExpense
            )
        )

        val adapter = SummaryAdapter(summaryTransactions, "mingguan")
        recyclerView.adapter = adapter
    }

    private fun setupRecyclerViewBulanan() {
        val dateFormatter = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
        tvDate.text = dateFormatter.format(currentCalendar.time)

        val totalIncome = transactions.filter { isInCurrentMonth(it.date) && it.type == "income" }.sumOf { it.amount }
        val totalExpense = transactions.filter { isInCurrentMonth(it.date) && it.type == "expense" }.sumOf { it.amount }

        val summaryTransactions = listOf(
            Transaction(
                date = dateFormatter.format(currentCalendar.time),
                type = "summary",
                amount = totalIncome - totalExpense
            )
        )

        val adapter = SummaryAdapter(summaryTransactions, "bulanan")
        recyclerView.adapter = adapter
    }

    private fun setTabSelected(selectedTab: TextView, vararg otherTabs: TextView) {
        selectedTab.setBackgroundResource(R.drawable.tab_selected)
        otherTabs.forEach { it.setBackgroundResource(R.drawable.tab_unselected) }
    }

    private fun isInCurrentWeek(date: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transactionDate = sdf.parse(date) ?: return false

        val calendar = Calendar.getInstance()
        calendar.firstDayOfWeek = Calendar.MONDAY
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val startOfWeek = calendar.time

        calendar.add(Calendar.DAY_OF_WEEK, 6)
        val endOfWeek = calendar.time

        return transactionDate in startOfWeek..endOfWeek
    }

    private fun isInCurrentMonth(date: String): Boolean {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val transactionDate = sdf.parse(date) ?: return false

        val calendar = Calendar.getInstance().apply { time = transactionDate }
        return calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                calendar.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)
    }
}

class SummaryAdapter(private val transactions: List<Transaction>, private val viewType: String) :
    RecyclerView.Adapter<SummaryAdapter.SummaryViewHolder>() {

    class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvTanggal)
        val tvAmount: TextView = itemView.findViewById(R.id.tvPengeluaranHarian)
        val tvCategory: TextView = itemView.findViewById(R.id.tvKategori)
        val tvDescription: TextView = itemView.findViewById(R.id.tvKeterangan)
        val tvDay: TextView = itemView.findViewById(R.id.tvHari)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_riwayat_harian, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.tvDate.text = transaction.date
        holder.tvAmount.text = "Rp ${transaction.amount.toInt()}"

        if (viewType == "harian") {
            holder.tvCategory.visibility = View.VISIBLE
            holder.tvDescription.visibility = View.VISIBLE
            holder.tvDay.visibility = View.VISIBLE
            holder.tvCategory.text = transaction.category
            holder.tvDescription.text = transaction.description
            holder.tvDay.text = getDayName(transaction.date)
        } else {
            holder.tvCategory.visibility = View.GONE
            holder.tvDescription.visibility = View.GONE
            holder.tvDay.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = transactions.size

    private fun getDayName(date: String): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateObj = sdf.parse(date)
        val dayFormat = SimpleDateFormat("EEEE", Locale("id", "ID"))
        return dayFormat.format(dateObj ?: Date())
    }
}
