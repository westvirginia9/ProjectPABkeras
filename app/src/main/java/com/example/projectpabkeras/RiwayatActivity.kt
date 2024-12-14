package com.example.projectpabkeras

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    private var transactions: List<Transaction> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_riwayat)

        auth = FirebaseAuth.getInstance()

        tvPemasukan = findViewById(R.id.tv_pemasukan_nominal)
        tvPengeluaran = findViewById(R.id.tv_pengeluaran_nominal)
        tvSisaUang = findViewById(R.id.tv_sisa_uang_nominal)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        fetchTransactions()
    }

    private fun fetchTransactions() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("transactions")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { snapshot ->
                transactions = snapshot.documents.mapNotNull { it.toObject(Transaction::class.java) }
                updateSummary()
                setupRecyclerView()
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

    private fun setupRecyclerView() {
        val adapter = TransactionAdapter(transactions)
        recyclerView.adapter = adapter
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
