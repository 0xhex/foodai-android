package com.codepad.foodai.ui.home.home.pager.recipe

import android.content.res.ColorStateList
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codepad.foodai.R
import com.codepad.foodai.databinding.ItemRecipeCardBinding
import com.codepad.foodai.domain.models.recipe.MealType
import com.codepad.foodai.domain.models.recipe.Recipe
import com.codepad.foodai.helpers.RevenueCatManager
import javax.inject.Inject
import kotlin.math.max

class RecipeCardAdapter(
    private val mealTypes: List<MealType>,
    private val onCreateRecipeClick: (MealType) -> Unit,
    private val onViewRecipeClick: (Recipe) -> Unit,
    private val onPremiumRequired: () -> Unit
) : RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder>() {

    @Inject
    lateinit var revenueCatManager: RevenueCatManager

    private var recipes: Map<String, Recipe?> = emptyMap()
    private var loadingStates: MutableMap<String, Boolean> = mutableMapOf()
    private var errorStates: Map<String, String?> = emptyMap()
    private var premiumRequiredStates: Map<String, Boolean> = emptyMap()
    private var loadingMessageIndices: MutableMap<String, Int> = mutableMapOf()
    private val handlers: MutableMap<String, Handler> = mutableMapOf()
    private val loadingStartTimes: MutableMap<String, Long> = mutableMapOf()
    private var recyclerView: RecyclerView? = null

    companion object {
        private const val MIN_LOADING_TIME_MS = 3000L // 3 seconds minimum loading time
        private const val MESSAGE_UPDATE_INTERVAL_MS = 2000L // 2 seconds between messages
    }

    private val loadingMessages = listOf(
        R.string.loading_user_info,
        R.string.analyzing_preferences,
        R.string.finding_perfect_meal,
        R.string.selecting_ingredients,
        R.string.finalizing_recipe
    )

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        handlers.values.forEach { it.removeCallbacksAndMessages(null) }
        handlers.clear()
        this.recyclerView = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipeCardViewHolder {
        val binding =
            ItemRecipeCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecipeCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecipeCardViewHolder, position: Int) {
        val mealType = mealTypes[position]
        holder.bind(mealType)

        // Start or stop loading messages based on the loading state
        if (loadingStates[mealType.codeName] == true && !handlers.containsKey(mealType.codeName)) {
            startLoadingMessages(mealType.codeName, position)
        } else if (loadingStates[mealType.codeName] != true && handlers.containsKey(mealType.codeName)) {
            stopLoadingMessages(mealType.codeName)
        }
    }

    override fun getItemCount(): Int = mealTypes.size

    fun updateRecipe(mealType: String, recipe: Recipe?) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            // Store the recipe immediately
            recipes = recipes.toMutableMap().apply { put(mealType, recipe) }

            // Only update UI if we're not in loading state or if loading time has elapsed
            val startTime = loadingStartTimes[mealType]
            if (startTime != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = max(0, MIN_LOADING_TIME_MS - elapsedTime)

                if (remainingTime > 0) {
                    // Keep the loading state active and schedule the update
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishLoading(mealType, position)
                    }, remainingTime)
                } else {
                    finishLoading(mealType, position)
                }
            } else {
                // If no loading time recorded, update immediately
                notifyItemChanged(position)
            }
        }
    }

    private fun finishLoading(mealType: String, position: Int) {
        loadingStartTimes.remove(mealType)
        loadingStates.remove(mealType)  // Remove instead of setting to false
        stopLoadingMessages(mealType)
        notifyItemChanged(position)
    }

    fun updateLoadingState(mealType: String, isLoading: Boolean) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            if (isLoading) {
                // Start loading
                loadingStartTimes[mealType] = System.currentTimeMillis()
                loadingMessageIndices[mealType] = 0
                loadingStates[mealType] = true
                startLoadingMessages(mealType, position)
            } else {
                // Only stop loading if minimum time has elapsed
                val startTime = loadingStartTimes[mealType] ?: System.currentTimeMillis()
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = max(0, MIN_LOADING_TIME_MS - elapsedTime)

                if (remainingTime > 0) {
                    // Keep loading state active and schedule the stop
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishLoading(mealType, position)
                    }, remainingTime)
                } else {
                    finishLoading(mealType, position)
                }
            }
            notifyItemChanged(position)
        }
    }

    fun updateErrorState(mealType: String, error: String?) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            errorStates = errorStates.toMutableMap().apply { put(mealType, error) }

            // Check if we need to respect minimum loading time
            val startTime = loadingStartTimes[mealType]
            if (startTime != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = max(0, MIN_LOADING_TIME_MS - elapsedTime)

                if (remainingTime > 0) {
                    // Keep loading state active and schedule the stop
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishLoading(mealType, position)
                    }, remainingTime)
                } else {
                    finishLoading(mealType, position)
                }
            } else {
                finishLoading(mealType, position)
            }
        }
    }

    fun updatePremiumRequired(mealType: String, required: Boolean) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            premiumRequiredStates =
                premiumRequiredStates.toMutableMap().apply { put(mealType, required) }

            // Check if we need to respect minimum loading time
            val startTime = loadingStartTimes[mealType]
            if (startTime != null) {
                val elapsedTime = System.currentTimeMillis() - startTime
                val remainingTime = max(0, MIN_LOADING_TIME_MS - elapsedTime)

                if (remainingTime > 0) {
                    // Keep loading state active and schedule the stop
                    Handler(Looper.getMainLooper()).postDelayed({
                        finishLoading(mealType, position)
                    }, remainingTime)
                } else {
                    finishLoading(mealType, position)
                }
            } else {
                finishLoading(mealType, position)
            }
        }
    }

    private fun startLoadingMessages(mealType: String, position: Int) {
        // Clear any existing handlers for this meal type
        handlers[mealType]?.removeCallbacksAndMessages(null)

        loadingMessageIndices[mealType] = 0
        val handler = Handler(Looper.getMainLooper())
        handlers[mealType] = handler

        // Update initial message immediately
        recyclerView?.findViewHolderForAdapterPosition(position)?.let { holder ->
            if ((holder as RecipeCardViewHolder).itemView.tag == mealType) {
                holder.updateLoadingMessage(mealType, 0)
            }
        }

        val updateMessage = object : Runnable {
            override fun run() {
                val currentIndex = loadingMessageIndices[mealType] ?: 0
                if (currentIndex < loadingMessages.size - 1 && loadingStates[mealType] == true) {
                    loadingMessageIndices[mealType] = currentIndex + 1
                    recyclerView?.findViewHolderForAdapterPosition(position)?.let { holder ->
                        if ((holder as RecipeCardViewHolder).itemView.tag == mealType) {
                            holder.updateLoadingMessage(mealType, currentIndex + 1)
                        }
                    }
                    handler.postDelayed(this, MESSAGE_UPDATE_INTERVAL_MS)
                }
            }
        }

        handler.postDelayed(updateMessage, MESSAGE_UPDATE_INTERVAL_MS)
    }

    private fun stopLoadingMessages(mealType: String) {
        handlers[mealType]?.removeCallbacksAndMessages(null)
        handlers.remove(mealType)
        loadingMessageIndices.remove(mealType)
    }

    inner class RecipeCardViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun updateLoadingMessage(mealType: String, messageIndex: Int) {
            val messageResId = loadingMessages[messageIndex]
            binding.loadingMessage.apply {
                visibility = View.VISIBLE
                text = if (messageResId == R.string.finding_perfect_meal) {
                    context.getString(messageResId, mealType)
                } else {
                    context.getString(messageResId)
                }
            }
        }

        fun bind(mealType: MealType) {
            binding.mealType = mealType.displayName
            itemView.tag = mealType.codeName

            val recipe = recipes[mealType.codeName]
            val isLoading = loadingStates[mealType.codeName] ?: false
            val error = errorStates[mealType.codeName]
            val isPremiumRequired = premiumRequiredStates[mealType.codeName] ?: false
            val loadingMessageIndex = loadingMessageIndices[mealType.codeName] ?: 0

            // Update card border based on loading state
            binding.cardRoot.apply {
                strokeWidth = if (isLoading) {
                    resources.getDimensionPixelSize(R.dimen.dimen_2dp)
                } else {
                    0
                }
                strokeColor = if (isLoading) {
                    ContextCompat.getColor(context, R.color.custom_green)
                } else {
                    0
                }
            }

            binding.apply {
                // Reset all states first
                loadingMessage.visibility = View.GONE
                loadingAnimation.visibility = View.GONE
                mealAnimation.visibility = View.GONE
                recipeReadyGroup.visibility = View.GONE
                recipeReadyAnimation.visibility = View.GONE
                initialStateGroup.visibility = View.GONE
                premiumRequiredGroup.visibility = View.GONE
                errorMessage.visibility = View.GONE
                btnCreateRecipe.visibility = View.GONE

                when {
                    // Loading state
                    isLoading -> {
                        loadingMessage.apply {
                            visibility = View.VISIBLE
                            text =
                                if (loadingMessages[loadingMessageIndex] == R.string.finding_perfect_meal) {
                                    context.getString(
                                        loadingMessages[loadingMessageIndex],
                                        mealType.codeName
                                    )
                                } else {
                                    context.getString(loadingMessages[loadingMessageIndex])
                                }
                        }
                        loadingAnimation.apply {
                            visibility = View.VISIBLE
                            setAnimation(R.raw.loading)
                            playAnimation()
                        }

                        // Hide button during loading
                        btnCreateRecipe.visibility = View.GONE
                    }
                    // Recipe ready state
                    recipe?.status == "completed" -> {
                        recipeReadyGroup.visibility = View.VISIBLE
                        recipeReadyAnimation.apply {
                            visibility = View.VISIBLE
                            setAnimation(R.raw.premium)
                            playAnimation()
                        }

                        recipeReadyIcon.apply {
                            visibility = View.VISIBLE
                            setImageResource(R.drawable.ic_check_circle)
                            imageTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.custom_green)
                            )
                        }

                        recipeReadyText.apply {
                            visibility = View.VISIBLE
                            text = root.context.getString(
                                R.string.recipe_ready,
                                mealType.displayName
                            )
                            setTextColor(ContextCompat.getColor(context, R.color.custom_green))
                        }

                        btnCreateRecipe.apply {
                            visibility = View.VISIBLE
                            text = root.context.getString(R.string.view_recipe)
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.custom_green)
                            )
                        }
                    }
                    // Premium required state
                    isPremiumRequired -> {
                        premiumRequiredGroup.visibility = View.VISIBLE
                        btnCreateRecipe.apply {
                            visibility = View.VISIBLE
                            text = root.context.getString(R.string.upgrade_now)
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.blue_button)
                            )
                        }
                    }
                    // Error state
                    error != null -> {
                        errorMessage.apply {
                            visibility = View.VISIBLE
                            text = error
                        }
                        btnCreateRecipe.apply {
                            visibility = View.VISIBLE
                            text = root.context.getString(R.string.create_recipe)
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.custom_green)
                            )
                        }
                    }
                    // Initial state (default)
                    else -> {
                        initialStateGroup.visibility = View.VISIBLE
                        mealAnimation.apply {
                            visibility = View.VISIBLE
                            setAnimation(R.raw.meal)
                            playAnimation()
                        }
                        btnCreateRecipe.apply {
                            visibility = View.VISIBLE
                            text = root.context.getString(R.string.create_recipe)
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.custom_green)
                            )
                        }
                    }
                }

                // Set click listeners based on state
                btnCreateRecipe.setOnClickListener {
                    when {
                        recipe?.status == "completed" -> recipe?.let { onViewRecipeClick(it) }
                        isPremiumRequired -> {
                            onPremiumRequired.invoke()
                        }

                        else -> onCreateRecipeClick(mealType)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }
}