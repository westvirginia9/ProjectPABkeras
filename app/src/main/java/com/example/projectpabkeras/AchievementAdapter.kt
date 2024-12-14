package com.example.projectpabkeras

import android.view.LayoutInflater          // Untuk inflating layout item
import android.view.View                    // Untuk View dalam ViewHolder
import android.view.ViewGroup               // Untuk ViewGroup (parent RecyclerView)
import android.widget.TextView              // Untuk TextView dalam item layout
import androidx.recyclerview.widget.RecyclerView

class AchievementAdapter(
    private val items: List<AchievementItem>,
    // Callback optional
    private val showDetails: Boolean = false,
    private val onItemClick: ((AchievementItem, Int) -> Unit)? = null// Boolean untuk mengatur tampilan
) : RecyclerView.Adapter<AchievementAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, showDetails)
        // Set onClickListener jika onItemClick tidak null
        onItemClick?.let { clickListener ->
            holder.itemView.setOnClickListener {
                clickListener(item, position)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.textAchievementTitle)
        private val level = itemView.findViewById<TextView>(R.id.textAchievementLevel)
        private val exp = itemView.findViewById<TextView>(R.id.textAchievementEXP)

        fun bind(item: AchievementItem, showDetails: Boolean) {
            title.text = item.title
            if (showDetails) {
                level.visibility = View.VISIBLE
                exp.visibility = View.VISIBLE
                level.text = "${item.currentLevel}/${item.maxLevel}"
                exp.text = "+${item.exp}exp"
            } else {
                level.visibility = View.GONE
                exp.visibility = View.GONE
            }
        }
    }
}
