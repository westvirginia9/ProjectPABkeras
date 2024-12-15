package com.example.projectpabkeras

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CategoryAdapter(
    private val categories: List<Category>,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    // Data model untuk kategori
    data class Category(
        val name: String,
        val percentage: Int,
        val totalAmount: Double,
        val color: Int,
        val expenses: List<Expense> = emptyList()
    )


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryNamePercentage: TextView = view.findViewById(R.id.category_name_percentage)
        val categoryAmount: TextView = view.findViewById(R.id.category_amount)
        val categoryIndicator: View = view.findViewById(R.id.category_indicator)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        // Set data ke View
        holder.categoryNamePercentage.text = "${category.percentage}% ${category.name}" // Gabungkan nama dan persentase
        holder.categoryAmount.text = "Rp. ${category.totalAmount}" // Gunakan totalAmount

        // Ubah warna indikator sesuai kategori
        holder.categoryIndicator.setBackgroundColor(category.color)

        // Klik item
        holder.itemView.setOnClickListener {
            onItemClick(category)
        }
    }


    override fun getItemCount(): Int = categories.size
}
