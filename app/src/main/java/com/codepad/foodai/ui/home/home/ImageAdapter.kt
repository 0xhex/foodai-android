package com.codepad.foodai.ui.home.home

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.codepad.foodai.R

class ImageAdapter(var foodItems: List<ImageItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_STANDARD = 1
        private const val VIEW_TYPE_LOADING = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (foodItems[position]) {
            is ImageItem.Standard -> VIEW_TYPE_STANDARD
            is ImageItem.Loading -> VIEW_TYPE_LOADING
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_STANDARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_standard, parent, false)
                StandardViewHolder(view)
            }

            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StandardViewHolder -> holder.bind(foodItems[position] as ImageItem.Standard)
            is LoadingViewHolder -> holder.bind(foodItems[position] as ImageItem.Loading)
        }
    }

    override fun getItemCount(): Int = foodItems.size

    fun setItems(newItems: List<ImageItem>) {
        foodItems = newItems
        notifyDataSetChanged()
    }

    fun updateItems(newItems: List<ImageItem>) {
        foodItems = newItems
        notifyDataSetChanged()
    }


    fun addLoadingItem(loadingItem: ImageItem.Loading) {
        foodItems = foodItems + loadingItem
        notifyItemInserted(foodItems.size - 1)
    }

    fun removeLoadingItem() {
        val index = foodItems.indexOfFirst { it is ImageItem.Loading }
        if (index != -1) {
            foodItems = foodItems.filterNot { it is ImageItem.Loading }
            notifyItemRemoved(index)
        }
    }

    class StandardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.txt_title)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.txt_calories)
        private val protein: TextView = itemView.findViewById(R.id.txt_protein)
        private val fats: TextView = itemView.findViewById(R.id.txt_fat)
        private val carb: TextView = itemView.findViewById(R.id.txt_carb)
        private val hourTextView: TextView = itemView.findViewById(R.id.txt_hour)

        fun bind(item: ImageItem.Standard) {
            // Bind data to views
            titleTextView.text = item.title
            caloriesTextView.text = item.calories
            protein.text = item.protein
            fats.text = item.fats
            carb.text = item.carb
            hourTextView.text = item.hour

            Glide.with(imageView)
                .load(item.image)
                .centerCrop()
                .into(imageView)
        }
    }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(item: ImageItem.Loading) {
            imageView.setImageBitmap(item.image)
            statusTextView.text = item.statusMessages[item.currentStatusIndex]
        }
    }

    fun updateLoadingStatus(position: Int) {
        val item = foodItems[position] as? ImageItem.Loading ?: return
        if (item.currentStatusIndex < item.statusMessages.size - 1) {
            item.currentStatusIndex++
            notifyItemChanged(position)
        }
    }
}

sealed class ImageItem {
    data class Standard(
        val image: String,
        val title: String,
        val calories: String,
        val protein : String,
        val fats: String,
        val carb: String,
        val hour: String,
    ) : ImageItem()

    data class Loading(
        val image: Bitmap,
        val statusMessages: List<String>,
        var currentStatusIndex: Int = 0,
    ) : ImageItem()
}