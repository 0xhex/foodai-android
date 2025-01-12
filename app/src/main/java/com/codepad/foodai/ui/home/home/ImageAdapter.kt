package com.codepad.foodai.ui.home.home

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.codepad.foodai.R
import com.codepad.foodai.domain.use_cases.user.ExerciseData
import com.codepad.foodai.extensions.toHourString

class ImageAdapter(
    var foodItems: List<ImageItem>,
    private val onItemClick: (item: ImageItem) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_STANDARD = 1
        private const val VIEW_TYPE_LOADING = 2
        private const val VIEW_TYPE_EXERCISE = 3
    }

    override fun getItemViewType(position: Int): Int {
        return when (foodItems[position]) {
            is ImageItem.Standard -> VIEW_TYPE_STANDARD
            is ImageItem.Loading -> VIEW_TYPE_LOADING
            is ImageItem.Exercise -> VIEW_TYPE_EXERCISE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_STANDARD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_standard, parent, false)
                StandardViewHolder(view) { item -> onItemClick(item) }
            }

            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }

            VIEW_TYPE_EXERCISE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_exercise, parent, false)
                ExerciseViewHolder(view) { item -> onItemClick(ImageItem.Exercise(item)) }
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is StandardViewHolder -> holder.bind(foodItems[position] as ImageItem.Standard)
            is LoadingViewHolder -> holder.bind(foodItems[position] as ImageItem.Loading)
            is ExerciseViewHolder -> holder.bind(foodItems[position] as ImageItem.Exercise)
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

    class StandardViewHolder(itemView: View, private val onItemClick: (ImageItem.Standard) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
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
            caloriesTextView.text = item.calories + " kcal"
            protein.text = item.protein
            fats.text = item.fats
            carb.text = item.carb
            hourTextView.text = item.hour

            Glide.with(imageView)
                .load(item.image)
                .centerCrop()
                .into(imageView)

            itemView.setOnClickListener {
                onItemClick(item)
            }
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

    class ExerciseViewHolder(itemView: View, private val onItemClick: (ExerciseData) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val lottieView: LottieAnimationView = itemView.findViewById(R.id.lottie_animation)
        private val lottieRunView: LottieAnimationView = itemView.findViewById(R.id.lottie_animation_run)
        private val titleTextView: TextView = itemView.findViewById(R.id.txt_title)
        private val caloriesTextView: TextView = itemView.findViewById(R.id.txt_calories)
        private val intensityTextView: TextView = itemView.findViewById(R.id.txt_intensity)
        private val durationTextView: TextView = itemView.findViewById(R.id.txt_duration)
        private val hourTextView: TextView = itemView.findViewById(R.id.txt_hour)

        fun bind(item: ImageItem.Exercise) {
            val exercise = item.exerciseData
            titleTextView.text = exercise.exerciseType?.capitalize()
            caloriesTextView.text = "${exercise.caloriesBurned} calories"
            intensityTextView.text = "Intensity: ${exercise.intensity?.capitalize()}"
            durationTextView.text = "⏱ ${exercise.duration} Mins"
            hourTextView.text = exercise.createdAt?.toHourString()

            // Handle animation visibility based on type
            if (exercise.exerciseType == "run") {
                lottieView.visibility = View.GONE
                lottieRunView.visibility = View.VISIBLE
            } else {
                lottieView.visibility = View.VISIBLE
                lottieRunView.visibility = View.GONE
                
                // Set animation for other types
                val animationRes = when (exercise.exerciseType) {
                    "weightlifting" -> R.raw.dumbel
                    else -> R.raw.exercise
                }
                lottieView.setAnimation(animationRes)
                lottieView.playAnimation()
            }

            itemView.setOnClickListener {
                onItemClick(exercise)
            }
        }
    }
}

sealed class ImageItem {
    data class Standard(
        val image: String,
        val title: String,
        val calories: String,
        val protein: String,
        val fats: String,
        val carb: String,
        val hour: String,
    ) : ImageItem()

    data class Loading(
        val image: Bitmap,
        val statusMessages: List<String>,
        var currentStatusIndex: Int = 0,
    ) : ImageItem()

    data class Exercise(
        val exerciseData: ExerciseData
    ) : ImageItem()
}