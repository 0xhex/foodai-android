package com.codepad.foodai.ui.home.home.pager.recipe

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemRecipeCardBinding

class RecipeCardAdapter(private val mealTypes: List<String>) :
    RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeCardViewHolder {
        val binding =
            ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeCardViewHolder, position: Int) {
        holder.bind(mealTypes[position])
    }

    override fun getItemCount(): Int = mealTypes.size

    inner class RecipeCardViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(mealType: String) {
            binding.mealType = mealType
            binding.executePendingBindings()
        }
    }
}