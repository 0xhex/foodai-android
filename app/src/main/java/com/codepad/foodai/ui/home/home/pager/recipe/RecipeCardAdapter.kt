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

class RecipeCardAdapter(
    private val mealTypes: List<MealType>,
    private val onCreateRecipeClick: (MealType) -> Unit,
    private val onViewRecipeClick: (Recipe) -> Unit,
) : RecyclerView.Adapter<RecipeCardAdapter.RecipeCardViewHolder>() {

    private var recipes: Map<String, Recipe?> = emptyMap()
    private var loadingStates: Map<String, Boolean> = emptyMap()
    private var errorStates: Map<String, String?> = emptyMap()
    private var premiumRequiredStates: Map<String, Boolean> = emptyMap()
    private var loadingMessageIndices: MutableMap<String, Int> = mutableMapOf()
    private val handlers: MutableMap<String, Handler> = mutableMapOf()
    private var recyclerView: RecyclerView? = null

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
            recipes = recipes.toMutableMap().apply { put(mealType, recipe) }
            stopLoadingMessages(mealType)
            notifyItemChanged(position)
        }
    }

    fun updateLoadingState(mealType: String, isLoading: Boolean) {
        loadingStates = loadingStates.toMutableMap().apply {
            // Clear all other loading states first
            clear()
            // Set only the clicked item to loading state
            put(mealType, isLoading)
        }
        loadingMessageIndices[mealType] = 0
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    fun updateErrorState(mealType: String, error: String?) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            errorStates = errorStates.toMutableMap().apply { put(mealType, error) }
            stopLoadingMessages(mealType)
            notifyItemChanged(position)
        }
    }

    fun updatePremiumRequired(mealType: String, required: Boolean) {
        val position = mealTypes.indexOfFirst { it.codeName == mealType.lowercase() }
        if (position != -1) {
            premiumRequiredStates =
                premiumRequiredStates.toMutableMap().apply { put(mealType, required) }
            stopLoadingMessages(mealType)
            notifyItemChanged(position)
        }
    }

    private fun startLoadingMessages(mealType: String, position: Int) {
        // Clear any existing handlers for other meal types
        handlers.forEach { (key, handler) ->
            if (key != mealType) {
                handler.removeCallbacksAndMessages(null)
                handlers.remove(key)
            }
        }

        loadingMessageIndices[mealType] = 0
        val handler = Handler(Looper.getMainLooper())
        handlers[mealType] = handler

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
                    handler.postDelayed(this, 2000) // 2 seconds delay
                }
            }
        }

        handler.post(updateMessage)
    }

    private fun stopLoadingMessages(mealType: String) {
        handlers[mealType]?.removeCallbacksAndMessages(null)
        handlers.remove(mealType)
        loadingMessageIndices.remove(mealType)
    }

    inner class RecipeCardViewHolder(private val binding: ItemRecipeCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun updateLoadingMessage(mealType: String, messageIndex: Int) {
            if (binding.mealType == mealType) {
                val messageResId = loadingMessages[messageIndex]
                binding.loadingMessage.text = if (messageResId == R.string.finding_perfect_meal) {
                    binding.root.context.getString(messageResId, mealType.lowercase())
                } else {
                    binding.root.context.getString(messageResId)
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
                        loadingMessage.visibility = View.VISIBLE
                        loadingAnimation.apply {
                            visibility = View.VISIBLE
                            setAnimation(R.raw.loading)
                            playAnimation()
                        }
                        updateLoadingMessage(mealType.codeName, loadingMessageIndex)
                        
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
                        isPremiumRequired -> { /* TODO: Implement premium upgrade */ }
                        else -> onCreateRecipeClick(mealType)
                    }
                }
            }

            binding.executePendingBindings()
        }
    }
}