package com.example.projectpabkeras

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RewardAdapter(private val rewardList: List<Reward>) :
    RecyclerView.Adapter<RewardAdapter.RewardViewHolder>() {

    // ViewHolder untuk setiap item
    class RewardViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.reward_icon)
        val title: TextView = view.findViewById(R.id.reward_title)
        val points: TextView = view.findViewById(R.id.reward_points)
        val redeemButton: Button = view.findViewById(R.id.reward_redeem_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RewardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reward, parent, false)
        return RewardViewHolder(view)
    }

    override fun onBindViewHolder(holder: RewardViewHolder, position: Int) {
        val reward = rewardList[position]
        holder.title.text = reward.title
        holder.points.text = reward.points
        holder.icon.setImageResource(R.drawable.ic_reward) // Ganti sesuai ikon yang Anda gunakan
        holder.redeemButton.setOnClickListener {
            // Tambahkan aksi saat tombol "Tukar" ditekan
        }
    }

    override fun getItemCount(): Int {
        return rewardList.size
    }
}
