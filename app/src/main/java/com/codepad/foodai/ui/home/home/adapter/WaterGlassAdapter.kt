package com.codepad.foodai.ui.home.home.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.databinding.ItemWaterGlassBinding

class WaterGlassAdapter(
    private val glassCount: Int,
    private var currentGlasses: Int,
    private val onGlassClick: (Int) -> Unit,
) : RecyclerView.Adapter<WaterGlassAdapter.ViewHolder>() {

    private val isToday: Boolean = true // TODO: Add date comparison logic

    inner class ViewHolder(private val binding: ItemWaterGlassBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.imgGlass.setImageResource(
                if (position < currentGlasses) {
                    com.codepad.foodai.R.drawable.water_filled
                } else {
                    com.codepad.foodai.R.drawable.water_empty
                }
            )

            // Show plus icon only for the next empty glass if it's today
            binding.txtPlus.visibility = if (isToday && position == currentGlasses) {
                View.VISIBLE
            } else {
                View.GONE
            }

            if (isToday) {
                binding.root.setOnClickListener {
                    if (position == currentGlasses && currentGlasses < glassCount) {
                        onGlassClick(position)
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemWaterGlassBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = glassCount

    fun updateCurrentGlasses(count: Int) {
        currentGlasses = count
        notifyDataSetChanged()
    }
} 