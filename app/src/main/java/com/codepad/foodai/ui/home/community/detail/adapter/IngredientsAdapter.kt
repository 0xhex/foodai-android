package com.codepad.foodai.ui.home.community.detail.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ItemIngredientBinding
import com.codepad.foodai.domain.models.image.Ingredient

class IngredientsAdapter : ListAdapter<Ingredient, IngredientsAdapter.IngredientViewHolder>(IngredientDiffCallback()) {

    private var totalCalories: Int = 0

    override fun submitList(list: List<Ingredient>?) {
        // Calculate total calories when setting the list
        totalCalories = list?.sumOf { it.calory ?: 0 } ?: 0
        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IngredientViewHolder {
        val binding = ItemIngredientBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return IngredientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IngredientViewHolder, position: Int) {
        val ingredient = getItem(position)
        holder.bind(ingredient, calculatePercentage(ingredient.calory ?: 0, totalCalories))
    }

    private fun calculatePercentage(calories: Int, totalCalories: Int): Int {
        if (totalCalories <= 0) return 0
        return ((calories.toDouble() / totalCalories.toDouble()) * 100).toInt()
    }

    class IngredientViewHolder(
        private val binding: ItemIngredientBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ingredient: Ingredient, percentage: Int) {
            binding.apply {
                ingredientName.text = ingredient.name
                ingredientCalories.text = "${ingredient.calory} kcal"
                
                // Show percentage with proper color
                ingredientPercentage.text = "($percentage%)"
                
                // Set color based on calories (blue for significant contributions)
                ingredientPercentage.setTextColor(
                    ContextCompat.getColor(
                        itemView.context, 
                        if (ingredient.calory ?: 0 > 100) R.color.blue else R.color.gray
                    )
                )
            }
        }
    }

    private class IngredientDiffCallback : DiffUtil.ItemCallback<Ingredient>() {
        override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
            return oldItem == newItem
        }
    }
} 