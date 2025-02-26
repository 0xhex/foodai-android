package com.codepad.foodai.ui.home.community.adapter

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.Interpolator
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.codepad.foodai.databinding.ItemCommunityPostBinding
import com.codepad.foodai.domain.models.community.CommunityPost
import com.codepad.foodai.extensions.dpToPx
import com.codepad.foodai.extensions.getTimeAgo

class CommunityPostsAdapter(
    private val onPostClick: (CommunityPost) -> Unit
) : ListAdapter<CommunityPost, CommunityPostsAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(
        private val binding: ItemCommunityPostBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                getItem(bindingAdapterPosition)?.let { post ->
                    onPostClick(post)
                }
            }
        }

        fun bind(post: CommunityPost) {
            binding.apply {
                // Meal Type
                txtMealType.apply {
                    text = post.image.mealName?.uppercase()
                    isVisible = !post.image.mealName.isNullOrEmpty()
                    minimumHeight = 0 // Allow text to scale down
                    setAutoSizeTextTypeUniformWithConfiguration(
                        7,
                        10,
                        1,
                        TypedValue.COMPLEX_UNIT_SP
                    )
                }

                // Country Flag
                txtCountryFlag.apply {
                    text = getCountryFlag(post.user.countryCode)
                    elevation = 3f // Add shadow
                }

                // User Info
                txtUsername.text = "@${post.user.name} ${post.user.assignedEmoji}"

                // Description
                txtDescription.apply {
                    text = post.image.description?.take(80)
                    alpha = 0.8f
                }

                // Food Image with shadow
                imgFood.apply {
                    elevation = 6f
                    Glide.with(this)
                        .load(post.image.url)
                        .transform(RoundedCorners(12))
                        .into(this)
                }

                // Nutrition Badges with equal spacing
                badgeCalories.root.layoutParams =
                    (badgeCalories.root.layoutParams as LinearLayout.LayoutParams).apply {
                        weight = 1f
                        marginStart = 4.dpToPx()
                        marginEnd = 4.dpToPx()
                    }
                badgeHealthScore.root.layoutParams =
                    (badgeHealthScore.root.layoutParams as LinearLayout.LayoutParams).apply {
                        weight = 1f
                        marginStart = 4.dpToPx()
                        marginEnd = 4.dpToPx()
                    }
                badgeTime.root.layoutParams =
                    (badgeTime.root.layoutParams as LinearLayout.LayoutParams).apply {
                        weight = 1f
                        marginStart = 4.dpToPx()
                        marginEnd = 4.dpToPx()
                    }
                badgeLikes.root.layoutParams =
                    (badgeLikes.root.layoutParams as LinearLayout.LayoutParams).apply {
                        weight = 1f
                        marginStart = 4.dpToPx()
                        marginEnd = 4.dpToPx()
                    }

                badgeCalories.apply {
                    root.isVisible = post.image.calories != null
                    txtIcon.text = "\uD83D\uDD25"
                    txtValue.text = "${post.image.calories} kcal"
                }

                badgeHealthScore.apply {
                    root.isVisible = post.image.healthScore != null
                    txtIcon.text = "\uD83D\uDC9A"
                    txtValue.text = "${post.image.healthScore}/10"
                }

                badgeTime.apply {
                    txtIcon.text = "\u23F0"
                    txtValue.text = post.createdAt?.getTimeAgo()
                }

                badgeLikes.apply {
                    txtIcon.text = "\u2764\uFE0F"
                    txtValue.text = post.likes?.size.toString()
                }
            }
        }

        private fun getCountryFlag(countryCode: String?): String {
            if (countryCode == null || countryCode.length != 2) return ""

            val firstLetter = Character.codePointAt(countryCode, 0) - 0x41 + 0x1F1E6
            val secondLetter = Character.codePointAt(countryCode, 1) - 0x41 + 0x1F1E6

            return String(Character.toChars(firstLetter)) + String(Character.toChars(secondLetter))
        }
    }

    // Custom Spring Interpolator for animations
    private class SpringInterpolator(private val tension: Float, private val dampingRatio: Float) :
        Interpolator {
        override fun getInterpolation(input: Float): Float {
            return (1f + (-Math.pow(
                Math.E,
                (-tension * input).toDouble()
            ) * Math.cos((dampingRatio * input).toDouble()))).toFloat()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(
            ItemCommunityPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class PostDiffCallback : DiffUtil.ItemCallback<CommunityPost>() {
        override fun areItemsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CommunityPost, newItem: CommunityPost): Boolean {
            return oldItem == newItem
        }
    }
} 