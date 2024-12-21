package com.example.projectpabkeras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.Locale

class AchievementAdapter(
    private val items: List<AchievementItem>,
    private val showDetails: Boolean = false
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, showDetails)
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.textAchievementTitle)
        private val level = itemView.findViewById<TextView>(R.id.textAchievementLevel)
        private val exp = itemView.findViewById<TextView>(R.id.textAchievementEXP)

        fun bind(item: AchievementItem, showDetails: Boolean) {
            // Format `targetAmount` sebagai mata uang
            val formattedTargetAmount = NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(item.targetAmount)

            // Ubah judul berdasarkan `currentLevel` atau `targetAmount`
            title.text = when {
                item.targetAmount > 0 -> "Total keseluruhan pemasukan mencapai $formattedTargetAmount"
                item.title.contains("milestone", true) -> when (item.currentLevel) {
                    1 -> "Berhasil menyelesaikan 1 milestone"
                    2 -> "Berhasil menyelesaikan 3 milestone"
                    3 -> "Berhasil menyelesaikan 5 milestone"
                    else -> item.title
                }
                else -> item.title
            }

            // Tampilkan level dan EXP
            level.text = "Level: ${item.currentLevel}/${item.maxLevel}"
            exp.text = "EXP: ${item.exp}"

            // Atur visibilitas level dan EXP
            if (showDetails) {
                level.visibility = View.VISIBLE
                exp.visibility = View.VISIBLE
            } else {
                level.visibility = View.GONE
                exp.visibility = View.GONE
            }
        }
    }
}
