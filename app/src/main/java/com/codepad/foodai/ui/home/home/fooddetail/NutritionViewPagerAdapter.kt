package com.codepad.foodai.ui.home.home.fooddetail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.codepad.foodai.R
import com.codepad.foodai.domain.models.nutrition.MineralInfo
import com.codepad.foodai.domain.models.nutrition.VitaminInfo

class NutritionViewPagerAdapter(
    private val viewPager: ViewPager2,
    private var vitamins: List<VitaminInfo> = emptyList(),
    private var minerals: List<MineralInfo> = emptyList()
) : RecyclerView.Adapter<NutritionViewPagerAdapter.NutritionPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionPageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.page_nutrition_tab, parent, false)
        
        // Ensure the view fills the entire ViewPager2
        val layoutParams = view.layoutParams
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        view.layoutParams = layoutParams
        
        return NutritionPageViewHolder(view)
    }

    override fun onBindViewHolder(holder: NutritionPageViewHolder, position: Int) {
        when (position) {
            0 -> holder.bindVitamins(vitamins)
            1 -> holder.bindMinerals(minerals)
        }
    }

    override fun getItemCount(): Int = 2

    fun updateData(vitamins: List<VitaminInfo>, minerals: List<MineralInfo>) {
        this.vitamins = vitamins
        this.minerals = minerals
        notifyDataSetChanged()
    }

    inner class NutritionPageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val recyclerView: RecyclerView = itemView.findViewById(R.id.recyclerView)
        private val emptyStateText: TextView = itemView.findViewById(R.id.txtEmptyState)

        fun bindVitamins(vitamins: List<VitaminInfo>) {
            if (vitamins.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateText.visibility = View.VISIBLE
                emptyStateText.text = itemView.context.getString(R.string.no_information_available)
                return
            }

            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
            recyclerView.adapter = VitaminAdapter(vitamins)
        }

        fun bindMinerals(minerals: List<MineralInfo>) {
            if (minerals.isEmpty()) {
                recyclerView.visibility = View.GONE
                emptyStateText.visibility = View.VISIBLE
                emptyStateText.text = itemView.context.getString(R.string.no_information_available)
                return
            }

            recyclerView.visibility = View.VISIBLE
            emptyStateText.visibility = View.GONE
            
            recyclerView.layoutManager = LinearLayoutManager(itemView.context)
            recyclerView.adapter = MineralAdapter(minerals)
        }
    }

    inner class VitaminAdapter(private val vitamins: List<VitaminInfo>) : 
        RecyclerView.Adapter<NutrientViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrientViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_nutrient, parent, false)
            return NutrientViewHolder(view, ContextCompat.getColor(parent.context, R.color.orange))
        }

        override fun onBindViewHolder(holder: NutrientViewHolder, position: Int) {
            val vitamin = vitamins[position]
            holder.bind(
                name = vitamin.name,
                amount = formatAmount(vitamin.amount, vitamin.unit),
                percentage = vitamin.dailyPercentage,
                emoji = vitamin.emoji
            )
        }

        override fun getItemCount(): Int = vitamins.size
    }

    inner class MineralAdapter(private val minerals: List<MineralInfo>) : 
        RecyclerView.Adapter<NutrientViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutrientViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_nutrient, parent, false)
            return NutrientViewHolder(view, ContextCompat.getColor(parent.context, R.color.soft_blue))
        }

        override fun onBindViewHolder(holder: NutrientViewHolder, position: Int) {
            val mineral = minerals[position]
            holder.bind(
                name = mineral.name,
                amount = formatAmount(mineral.amount, mineral.unit),
                percentage = mineral.dailyPercentage,
                emoji = mineral.emoji
            )
        }

        override fun getItemCount(): Int = minerals.size
    }

    class NutrientViewHolder(
        itemView: View,
        private val themeColor: Int
    ) : RecyclerView.ViewHolder(itemView) {
        private val txtName = itemView.findViewById<TextView>(R.id.txtNutrientName)
        private val txtAmount = itemView.findViewById<TextView>(R.id.txtAmount)
        private val txtPercentage = itemView.findViewById<TextView>(R.id.txtPercentage)
        private val txtEmoji = itemView.findViewById<TextView>(R.id.txtEmoji)
        private val emojiBackground = itemView.findViewById<View>(R.id.emojiBackground)
        private val circularProgress = itemView.findViewById<CircularProgressView>(R.id.circularProgress)
        private val txtProgressValue = itemView.findViewById<TextView>(R.id.txtProgressValue)

        fun bind(name: String, amount: String, percentage: Int, emoji: String) {
            txtName.text = name
            txtAmount.text = amount
            
            val context = itemView.context
            txtPercentage.text = context.getString(
                R.string.percentage_daily_value_format, 
                percentage
            )
            txtPercentage.setTextColor(themeColor)
            
            txtEmoji.text = emoji
            
            circularProgress.setProgress(percentage, themeColor)
            txtProgressValue.text = "$percentage%"
            
            // Change background color based on theme
            emojiBackground.backgroundTintList = android.content.res.ColorStateList.valueOf(
                getColorWithOpacity(themeColor, 0.15f)
            )
        }
        
        private fun getColorWithOpacity(color: Int, opacity: Float): Int {
            val alpha = (opacity * 255).toInt()
            return (alpha shl 24) or (color and 0x00FFFFFF)
        }
    }
    
    private fun formatAmount(amount: Double, unit: String): String {
        return when {
            amount >= 10 -> "${amount.toInt()}$unit"
            amount >= 1 -> String.format("%.1f", amount) + unit
            else -> String.format("%.2f", amount) + unit
        }
    }
} 