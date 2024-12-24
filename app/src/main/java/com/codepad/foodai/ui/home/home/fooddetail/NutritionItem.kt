package com.codepad.foodai.ui.home.home.fooddetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemNutritionBinding

data class NutritionItem(val name: String, val value: String)

class NutritionAdapter(private val items: List<NutritionItem>) :
    RecyclerView.Adapter<NutritionAdapter.NutritionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NutritionViewHolder {
        val binding =
            ItemNutritionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NutritionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NutritionViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class NutritionViewHolder(private val binding: ItemNutritionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NutritionItem) {
            binding.txtNutritionName.text = item.name
            binding.txtNutritionValue.text = item.value
        }
    }
}