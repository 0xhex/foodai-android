package com.codepad.foodai.ui.home.home.fooddetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R

class RecommendationsAdapter(private val recommendations: List<String>) :
    RecyclerView.Adapter<RecommendationsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(recommendations[position], position)
    }

    override fun getItemCount(): Int = recommendations.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtRecommendation: TextView = itemView.findViewById(R.id.txtRecommendation)

        fun bind(recommendation: String, position: Int) {
            txtRecommendation.text = recommendation
            
            // Reset view properties
            itemView.alpha = 0f
            itemView.translationX = 50f
            
            // Animate with staggered delay based on position
            itemView.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(300)
                .setStartDelay((position * 100).toLong())
                .start()
        }
    }
} 